package scala.ski.crunch.activity.processor

import java.text.SimpleDateFormat
import java.util.Date

import com.vividsolutions.jts.geom.{Coordinate, GeometryFactory, PrecisionModel}
import javax.annotation.processing.Processor

import scala.annotation.tailrec
import scala.collection.immutable.HashMap
import scala.collection.mutable
import scala.ski.crunch.activity.processor.model.{ActivityRecord, TimerEvent}
import scala.collection.JavaConverters._



/**
  * Created by aengusmccullough on 2017-02-25.
  * merge duplicate records
  * remove corrupt records
  * replace null values in sequences with -999
  * derive distance and gradient
  *
  * TODO
  *  - deal with events.  stops / pauses get tricky here (e.g. moving while paused etc)
  *  - summarize
  *
  */
class RecordProcessor(records: List[ActivityRecord]) {



  val sdf: SimpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
  type Records = List[ActivityRecord]


  /**
    *
    * if tsDiff(a, a+1) ==0 then avg other values if they are not null
    *
    * @return
    */
  def mergeDuplicates(): RecordProcessor = {
    val source = sort()

    @tailrec
    def loop(source: Records, sink: Records): Records = {
      source match {
        case x :: xs => {
          if (!sink.isEmpty && x.ts == sink.head.ts) {
            loop(xs, x.avg(sink.head) :: sink.tail)
          }
          else

            loop(xs, x :: sink)
        }
        case Nil => sink
      }
    }

    new RecordProcessor(loop(source, Nil))
  }

  def removeCorrupt(): RecordProcessor = {

    val corruptDistance = ActivityRecord.detectCorruptRecords(
      records, (a, b) => a.distDiff(b) > ActivityRecord.MAX_DISTANCE_DELTA)



    val corruptAltitude = ActivityRecord.detectCorruptRecords(
      records,
      (a, b) => a.altDiff(b) > ActivityRecord.MAX_ALTITUDE_DELTA
    )

    val corruptSpeed = ActivityRecord.detectCorruptRecords(
      records,
      (a, b) => a.speedDiff(b) > ActivityRecord.MAX_SPEED_DELTA
    )

    val corruptRecords = corruptDistance ++ corruptAltitude ++ corruptSpeed

    new RecordProcessor(records.zipWithIndex.filter(x => !corruptRecords.contains(x._2)).map(_._1))


  }

  /**
    * create a lookup map between timestamp and index.  Useful for calculating start/end index of events
    *
    * @return
    */
  def buildTsIndex(): HashMap[String, Int] = {

    @tailrec
    def loop(rem: List[ActivityRecord], result: HashMap[String, Int], idx: Int): HashMap[String, Int] = {
      rem match {
        case x :: xs => loop(xs, result + ((x.ts, idx)), idx + 1)
        case Nil => result
      }
    }

    loop(records, new HashMap[String, Int], 0)
  }

  def replaceNulls(): RecordProcessor = {
    val hrReplaced = ActivityRecord.nullReplace(records.map((x) => x.hr), -999)
    val altReplaced = ActivityRecord.nullReplace(records.map((x) => x.altitude), -999.0)
    val velocityReplaced = ActivityRecord.nullReplace(records.map((x) => x.velocity), -999.0)
    val latReplaced = ActivityRecord.nullReplace(records.map((x) => x.lat), -999.0)
    val lonReplaced = ActivityRecord.nullReplace(records.map((x) => x.lon), -999.0)
    val cadenceReplaced = ActivityRecord.nullReplace(records.map((x) => x.cadence), -999.0)
    val tempReplaced = ActivityRecord.nullReplace(records.map((x) => x.temperature), -999.0)
    val vertSpeedReplaced = ActivityRecord.nullReplace(records.map((x) => x.verticalSpeed), -999.0)
    val distanceNullReplaced = ActivityRecord.nullReplace(records.map((x) => x.distance), -999.0)
    val distanceZeroValReplaced = ActivityRecord.nullReplace(distanceNullReplaced, 0)
    val gradeReplaced = ActivityRecord.nullReplace(records.map((x) => x.grade), -999.0)
    val hrvReplaced = ActivityRecord.nullReplace(records.map((x) => x.hrv), -999.0)


    val tuple = hrReplaced zip altReplaced zip velocityReplaced zip latReplaced zip lonReplaced zip cadenceReplaced zip tempReplaced zip vertSpeedReplaced zip distanceZeroValReplaced zip gradeReplaced zip hrvReplaced zip records map {
      case (((((((((((a, b), c), d), e), f), g), h), i), j), k), l) => (a, b, c, d, e, f, g, h, i, j, k, l)
    }
    new RecordProcessor(tuple.map(x =>
      new ActivityRecord(x._12.ts, x._1, x._4, x._5, x._2, x._3, x._10, x._9, x._7,
        x._12.moving, x._6, x._8, x._11)))
  }

  def fillEmpty(): RecordProcessor = {
    return null;
  }

  /**
    * Relatively crude method of calculating gradient.
    * Calc geo distance and altitude difference between each pair wise record
    * Gradient (degrees) = arctan(rise / run)
    * Using a 5 pt moving window average due to poor elevation resolution
    * @return
    */
  def calcGrade(): RecordProcessor = {
    val geoms = ActivityRecord.toGeometry(records)
    val geomFactory = new GeometryFactory(new PrecisionModel, 4326);
    val pt = geomFactory.createPoint(new Coordinate(-999, -999));
    val altDiff = ActivityRecord.altDiff(records map (x => x.altitude))

    try {
      // calc pairwise distance and gradients
      val dist = ActivityRecord.dist(ActivityRecord.nullReplace(geoms, pt))
      val grad = ActivityRecord.gradient(dist, altDiff)

      //create sliding distance window of 5 and get average of each window
      val distWindow = dist.iterator.sliding(5, 1).withPartial(false).toList
      val avgDists =  distWindow.map(x => if(x.length !=0 && x.sum != 0){ x.sum / x.length} else if(x.length ==0){0} else {1})

      //create sliding gradient window of 5 and get average of each window
      val gradWindow = grad.iterator.sliding(5,1).withPartial(false).toList
      val avgGrads = gradWindow.map(x => if(x.length !=0 && x.sum != 0){ x.sum / x.length} else if(x.length ==0){0} else {1})

      //zip averaged distance and averaged gradient windows together
      val combinedGradDist =  avgGrads zip avgDists map{ case (a,b) => (a,b)}
      // create smoothed gradient list
      var smoothGradient = combinedGradDist.map(x => if(x._1 !=0 && x._2 !=0){ Math.atan(x._1 / x._2)} else if( x._1==0){Math.atan(1)} else { Math.atan(0)})
      // add nulls at beginning and end of list
      smoothGradient = ((-999.0)::(-999.0)::smoothGradient).reverse
      smoothGradient = ((-999.0)::(-999.0)::smoothGradient).reverse

      //debugging traces
//      println(smoothGradient.max + " " + smoothGradient.min)
//      println(avgDists.length + " " + dist.length + " " + avgGrads.length + "  " + grad.length +" " +  smoothGradient.length)
////      println("sum dist = " + dist.drop(1).foldLeft[Double](0)((a, b) => Math.abs(a) + Math.abs(b)))
//      println("gradient = " + grad)
//      println("smooth gradient = " + smoothGradient)


      val tuple = dist zip smoothGradient zip records map {
        case (((a, b), c) ) => (a, b, c)
      }

      //stick with device distance
      new RecordProcessor(tuple.map(x =>
        new ActivityRecord(x._3.ts, x._3.hr, x._3.lat, x._3.lon, x._3.altitude, x._3.velocity, x._2, x._3.distance, x._3.temperature,
          x._3.moving, x._3.cadence, x._3.verticalSpeed, x._3.hrv)))

    } catch {
      case e: Throwable => {
        println("Got some other kind of exception")
        e.printStackTrace
        new RecordProcessor(records)
      }
    }

  }


  def detectAndPatchSpikes(maxSpeed: Int): RecordProcessor = {

    // distance
    // altitude
    // speed
    // vert speed
    // cadence
    // lat lon

    //group the gaps between reading by their frequency. return a sorted map (gap(seconds), frequency))
    val gaps = records.sliding(2).map(x => ActivityRecord.tsDiff(x.head.ts, x.tail.head.ts)).toList.groupBy(identity).map(x => (x._1, x._2.size)).toList.sortBy(_._2).reverse
    this
  }


  def calcMoving(): RecordProcessor = {
    val moving = (records.map(x => x.velocity)).map(x => if(x>0) true else false)
    val pairs = records.zip(moving)
    new RecordProcessor(pairs.map(x => new ActivityRecord(x._1.ts, x._1.hr, x._1.lat, x._1.lon, x._1.altitude, x._1.velocity, x._1.grade, x._1.distance, x._1.temperature,
      x._2, x._1.cadence, x._1.verticalSpeed, x._1.hrv)))
  }

  private def sort(): Records = {
    records.sortWith((a, b) => ActivityRecord.sdf.parse(a.ts).getTime > ActivityRecord.sdf.parse(b.ts).getTime)
  }

   def getRecords: java.util.ArrayList[ActivityRecord] = {
     new java.util.ArrayList[ActivityRecord](records.asJava)
  }
}



