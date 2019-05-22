package scala.ski.crunch.activity.processor.model

import java.text.SimpleDateFormat

import com.vividsolutions.jts.geom._
import org.geotools.geometry.jts.JTS
import org.geotools.referencing.CRS

import scala.annotation.tailrec
import scala.ski.crunch.activity.ActivityUtils

class ActivityRecord(val ts: String, val hr: Int, val lat: Double, val lon: Double, val altitude: Double,
                     val velocity: Double, val grade: Double, val distance: Double,
                     val temperature: Double, val moving: Boolean, val cadence: Int, val verticalSpeed: Double,
                     var hrv: Double) {



  //def hrv_= (value:Double): Unit = hrv = value;
  val sourceDateFormat: SimpleDateFormat = new SimpleDateFormat("dd-mm-yyyy hh:mm:ss")
  val targetDateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-mm-dd'T'hh:mm:ss")

  // comparison methods

  /**
    * Timestamp difference (in seconds) from another scala.ski.crunch.activity.processor.model.ActivityRecord
    *
    * @param another scala.ski.crunch.activity.processor.model.ActivityRecord
    * @return seconds Int
    */
  def tsDiff(another: ActivityRecord): Int = {
    (sourceDateFormat.parse(another.ts).getTime - sourceDateFormat.parse(this.ts).getTime).asInstanceOf[Int] / 1000
  }

  /**
    * Distance difference (in metres) from another scala.ski.crunch.activity.processor.model.ActivityRecord.  Assumes another has a greater distance than this,
    * otherwise will return 0 rather than a negative
    *
    * @param another scala.ski.crunch.activity.processor.model.ActivityRecord
    * @return distance in metres Double
    */
  def distDiff(another: ActivityRecord): Double = {
    if ((another.distance - this.distance) > 0)
      another.distance - this.distance
    else
      0
  }

  /**
    * Returns altitude difference (in metres) from another scala.ski.crunch.activity.processor.model.ActivityRecord.  If either is -999, returns 0
    *
    * @param another scala.ski.crunch.activity.processor.model.ActivityRecord
    * @return distance (metres)
    */
  def altDiff(another: ActivityRecord): Double = {
    if (another.altitude == (-999.0) || this.altitude == (-999.0))
      0
    else
      another.altitude - altitude
  }

  /**
    * Returns speed difference (in km/h) from another scala.ski.crunch.activity.processor.model.ActivityRecord.  If either is -999, returns 0
    *
    * @param another scala.ski.crunch.activity.processor.model.ActivityRecord
    * @return speed differential (km/h)
    */
  def speedDiff(another: ActivityRecord): Double = {
    if (another.velocity == (-999.0) || this.velocity == (-999.0))
      0
    else
      another.velocity - velocity
  }


  /**
    * Returns average of each field over this and another ActivityRecord
    *
    * @param another ActivityRecord
    * @return ActivityRecord
    */
  def avg(another: ActivityRecord): ActivityRecord = {

    //val timestamp = (sourceDateFormat.parse(ts).getTime + sourceDateFormat.parse(another.ts).getTime) / 2
    new ActivityRecord(
      ts,
      ActivityUtils.avgNum(hr, another.hr).toInt,
      ActivityUtils.avgNum(lat, another.lat),
      ActivityUtils.avgNum(lon, another.lon),
      ActivityUtils.avgNum(altitude, another.altitude),
      ActivityUtils.avgNum(velocity, another.velocity),
      ActivityUtils.avgNum(grade, another.grade),
      ActivityUtils.avgNum(distance, another.distance),
      ActivityUtils.avgNum(temperature, another.temperature),
      if (moving != null && another.moving != null) (moving || another.moving) else if (another.moving != null) another.moving else moving,
      ActivityUtils.avgNum(cadence, another.cadence).toInt,
      ActivityUtils.avgNum(verticalSpeed, another.verticalSpeed),
      ActivityUtils.avgNum(hrv, another.hrv)
    )
  }

}

  object ActivityRecord {


    val MAX_DISTANCE_DELTA: Int = 10000;
    val MAX_SPEED_DELTA: Int = 250;
    val MAX_ALTITUDE_DELTA: Int = 100;

    val sdf: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");



    def NullConstructorWithTsAndHrv(ts: String, hrv: Double): ActivityRecord = {
      new ActivityRecord(ts, -999, -999, -999, -999, -999, -999, -999, -999, true, -999, -999, hrv);
    }

    private val geomFactory: GeometryFactory = new GeometryFactory(new PrecisionModel,4326)

    def tsDiff(tsA: String, tsB: String): Int = {
      (sdf.parse(tsB).getTime() - sdf.parse(tsA).getTime()).asInstanceOf[Int] / 1000
    }



    /*
    * if tsDiff(a, a+1) ==0 then avg other values if they are not null
     */
    def mergeDuplicates(source: List[ActivityRecord]): List[ActivityRecord] = {

      @tailrec
      def loop(source: List[ActivityRecord], sink: List[ActivityRecord]): List[ActivityRecord] = {
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

      loop(source,Nil)
    }

    /**
      * iterates through the list of records, comparing element to adjacent element. If predicate
      * returns true then add index of element to the list
      * @param source
      * @param predicate
      * @return
      */
    def detectCorruptRecords(source: List[ActivityRecord],predicate: (ActivityRecord,ActivityRecord)=>Boolean):List[Int] = {

      @tailrec
      def loop(source: List[ActivityRecord],elem: ActivityRecord,res: List[Int],idx:Int): List[Int] =  {
        source match {
          case x::xs => {
            //println(idx + " " + x.distance + " " + elem.distance + " " + x.distDiff(elem))
            //println(x.ts + " " + x.distance)
            if(predicate(x,elem))
              loop(xs,x,idx::res,idx+1)
            else
              loop(xs,x,res,idx+1)
          }
          case Nil =>
            res
        }
      }

      source match {
        case x::xs => loop(xs,x,Nil,0)
        case Nil => Nil
      }

    }


    /**
      * replaces all null values in a list with the value of previous item
      * e.g.  List(0,3,null,6,7)  =>  List(0,3,3,6,7)
      *
      * Note that leading null values will not be replaced
      * e.g. List(null,3,null,6,7) => List(null,3,3,6,7)
      * @param source  List
      * @param nullValue the null value to replace
      * @tparam X the type of list
      * @tparam T the type of null value
      * @return
      */
    def nullReplace[X,T](source: List[X],nullValue: T): List[X] = {

      def f(b:X,a:X):X = {
        if(a == (nullValue))
          b
        else
          a
      }
      source.foldLeft(List[X]())((a,b) => f(if(!a.isEmpty) a.head else b, b)::a).reverse
    }

    def altDiff(altitude: List[Double]):List[Double] = {
      val res = for (List(left,right) <- altitude.sliding(2) ) yield (right - left);
      res.toList
    }



        def dist(geometry: List[Geometry]):List[Double] = {
          //val res = for (List(left,right) <- geometry.sliding(2) ) yield (left.distance(right));
          val res = for (List(left,right) <- geometry.sliding(2)) yield
            if(left.getCoordinate.getOrdinate(0)!=(-999) && right.getCoordinate.getOrdinate(0)!=(-999)) {
              (JTS.orthodromicDistance(left.getCoordinate, right.getCoordinate, CRS.decode("EPSG:4326")))
            }else {
              0
            }

          res.toList
        }

    def gradient(distance: List[Double], altdiff: List[Double]):List[Double] = {
      distance.zip(altdiff).map{
        case (dist:Double,ht:Double) => if(dist!=0){ ht/dist }else if(ht>0){ 1} else if(ht==0){0}else{-1}}
    }


//      def toGradient(altitude: List[Integer],geometry : List[Geometry]):List[Double] = {
//
//      }
//      def calcDistance[Geometry,T](source: List[Geometry],nullValue: T): List[Geometry] = {
//
//        def f(b:Geometry,a:Geometry):Double = {
//
//          if(a == (nullValue))
//            0
//          else
//            a.asInstanceOf[Point].distance(b.asInstanceOf[Point])
//        }
//        source.foldLeft(List[Geometry]())((a,b) => f(if(!a.isEmpty) a.head else b, b)::a).reverse
//      }

        def toGeometry(source: List[ActivityRecord]):List[Geometry] = {
          source.map(x => x.lat) zip source.map(x => x.lon) map (pair => geomFactory.createPoint(new Coordinate(pair._1,pair._2)))
        }


  }

//
//    val MAX_DISTANCE_DELTA: Int = 10000;
//    val MAX_SPEED_DELTA: Int = 250;
//    val MAX_ALTITUDE_DELTA: Int = 100;
//
//    val sdf: SimpleDateFormat = new SimpleDateFormat("dd-mm-yyyy hh:mm:ss");
//
//
//    //private val geomFactory: GeometryFactory = new GeometryFactory(new PrecisionModel,4326)
//
//    def tsDiff(tsA: String, tsB: String): Int = {
//      (sdf.parse(tsB).getTime() - sdf.parse(tsA).getTime()).asInstanceOf[Int] / 1000
//    }
//
////    /**
////      * Parses a single activity into a list of activity records
////      *
////      * @param activity
////      * @return
////      */
////    def parse(activity: Activity): List[ActivityRecord] = {
////
////      @tailrec
////      def loop(records: List[ActivityRecord], idx: Integer): List[ActivityRecord] = {
////        if (activity.getTs().size() > idx) {
////          loop(
////            //todo deal with events
////            new ActivityRecord(
////              activity.getTs().get(idx),
////              activity.getHr().get(idx),
////              activity.getLat().get(idx),
////              activity.getLon().get(idx),
////              activity.getAltitude().get(idx),
////              activity.getVelocity().get(idx),
////              activity.getGrade().get(idx),
////              activity.getDistance().get(idx),
////              activity.getTemperature().get(idx),
////              activity.getMoving().get(idx),
////              activity.getCadence().get(idx),
////              null,null
////              //              Option(activity.getEvents.get(idx)),
////              //            Option(activity.getHrvs.get(idx))
////            ) :: records, idx + 1)
////        } else {
////          records.reverse
////        }
////      }
////
////      loop(Nil, 0)
////    }
//
//    /*
//    * if tsDiff(a, a+1) ==0 then avg other values if they are not null
//     */
//    def mergeDuplicates(source: List[ActivityRecord]): List[ActivityRecord] = {
//
//      @tailrec
//      def loop(source: List[ActivityRecord], sink: List[ActivityRecord]): List[ActivityRecord] = {
//        source match {
//          case x :: xs => {
//            if (!sink.isEmpty && x.ts == sink.head.ts) {
//              loop(xs, x.avg(sink.head) :: sink.tail)
//            }
//            else
//
//              loop(xs, x :: sink)
//          }
//          case Nil => sink
//        }
//      }
//
//      loop(source,Nil)
//    }
//
//    /**
//      * iterates through the list of records, comparing element to adjacent element. If predicate
//      * returns true then add index of element to the list
//      * @param source
//      * @param predicate
//      * @return
//      */
//    def detectCorruptRecords(source: List[ActivityRecord],predicate: (ActivityRecord,ActivityRecord)=>Boolean):List[Int] = {
//
//      @tailrec
//      def loop(source: List[ActivityRecord],elem: ActivityRecord,res: List[Int],idx:Int): List[Int] =  {
//        source match {
//          case x::xs => {
//            //println(idx + " " + x.distance + " " + elem.distance + " " + x.distDiff(elem))
//            //println(x.ts + " " + x.distance)
//            if(predicate(x,elem))
//              loop(xs,x,idx::res,idx+1)
//            else
//              loop(xs,x,res,idx+1)
//          }
//          case Nil =>
//            res
//        }
//      }
//
//      source match {
//        case x::xs => loop(xs,x,Nil,0)
//        case Nil => Nil
//      }
//
//    }
//
//
//    /**
//      * replaces all null values in a list with the value of previous item
//      * e.g.  List(0,3,null,6,7)  =>  List(0,3,3,6,7)
//      *
//      * Note that leading null values will not be replaced
//      * e.g. List(null,3,null,6,7) => List(null,3,3,6,7)
//      * @param source  List
//      * @param nullValue the null value to replace
//      * @tparam X the type of list
//      * @tparam T the type of null value
//      * @return
//      */
//    def nullReplace[X,T](source: List[X],nullValue: T): List[X] = {
//
//      def f(b:X,a:X):X = {
//        if(a == (nullValue))
//          b
//        else
//          a
//      }
//      source.foldLeft(List[X]())((a,b) => f(if(!a.isEmpty) a.head else b, b)::a).reverse
//    }
//
//    def altDiff(altitude: List[Double]):List[Double] = {
//      val res = for (List(left,right) <- altitude.sliding(2) ) yield (right - left);
//      res.toList
//    }
//
////    def dist(geometry: List[Geometry]):List[Double] = {
////      //val res = for (List(left,right) <- geometry.sliding(2) ) yield (left.distance(right));
////      val res = for (List(left,right) <- geometry.sliding(2)) yield
////        if(left.getCoordinate.getOrdinate(0)!=(-999) && right.getCoordinate.getOrdinate(0)!=(-999)) {
////          (JTS.orthodromicDistance(left.getCoordinate, right.getCoordinate, CRS.decode("EPSG:4326")))
////        }else {
////          0
////        }
////
////      res.toList
////    }
//
//    def gradient(distance: List[Double], altdiff: List[Double]):List[Double] = {
//      distance.zip(altdiff).map{case (dist:Double,ht:Double) => if(dist!=0){ ht/dist }else if(ht>0){ 1} else if(ht==0){0}else{-1}}
//    }
//
//
//    //  def toGradient(altitude: List[Integer],geometry : List[Geometry]):List[Double] = {
//    //
//    //  }
//    //  def calcDistance[Geometry,T](source: List[Geometry],nullValue: T): List[Geometry] = {
//    //
//    //    def f(b:Geometry,a:Geometry):Double = {
//    //
//    //      if(a == (nullValue))
//    //        0
//    //      else
//    //        a.asInstanceOf[Point].distance(b.asInstanceOf[Point])
//    //    }
//    //    source.foldLeft(List[Geometry]())((a,b) => f(if(!a.isEmpty) a.head else b, b)::a).reverse
//    //  }
//
////    def toGeometry(source: List[ActivityRecord]):List[Geometry] = {
////      source.map(x => x.lat) zip source.map(x => x.lon) map (pair => geomFactory.createPoint(new Coordinate(pair._1,pair._2)))
////    }







