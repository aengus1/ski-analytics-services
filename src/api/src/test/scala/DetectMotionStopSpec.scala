import java.io.InputStream
import java.text.SimpleDateFormat

import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

/**
  * Created by aengusmccullough on 2017-05-07.
  */
class DetectMotionStopSpec extends FlatSpec with Matchers with BeforeAndAfter{

  val sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")

  var ap: ActivityProcessor = null;

  before{

    val parser: FitParser = new FitParser()
    val stream: InputStream = getClass.getResourceAsStream("/garmin_test.fit")
    parser.parse(stream)

    val builder: SafBuilder = new SafBuilder()
    val activity = builder.split(parser.getCombinedActivity, parser.getSessions).get(0)
    val records  = ActivityRecord.parse(activity)

    this.ap =  new ActivityProcessor(records,null)
      .mergeDuplicates()
      .removeCorrupt()
      .replaceNulls()
      .processGeom()
      .presenceOfAttributes()
      .calcMoving()

  }


  "DetectMotionStops" should "detect three stops totalling 8 seconds in motion test.fit" in {


    val motionStops =ap.detectMotionStops()

    println(motionStops)
    assert(motionStops.isDefined)
    assert(motionStops.get.size==3)
    assert(motionStops.get.map(x=>x.secElapsed).foldLeft(0.0)(_+_).toInt ==8)

  }


  "DetectMotionStops" should "correctly detect a motion stop at start of activity" in {

    val data =  ap.get

    //replace first four readings with stopped
    val moving: List[Boolean] = false::false::false::false::data.map(x => x.moving).drop(4)

    val modifiedData: List[ActivityRecord] = data.zip(moving).map(x => new ActivityRecord(x._1.ts,x._1.hr,x._1.lat,x._1.lon,x._1.altitude,x._1.velocity,x._1.grade
      ,x._1.distance,x._1.temperature,x._2,x._1.cadence,x._1.event,x._1.hrv))

      val stops: Option[List[Event]] = new ActivityProcessor(modifiedData,null)
      .detectMotionStops()

    assert(stops.isDefined && stops.get.size==4)
    assert(stops.get(0).secElapsed==4.0)
  }


  "DetectMotionStops" should "correctly detect a motion stop at end of activity" in {

    val data = ap.get

    //replace final four readings with stopped
    val moving: List[Boolean] = (false::false::false::false::data.map(x => x.moving).reverse.drop(4)).reverse

    val modifiedData: List[ActivityRecord] = data.zip(moving).map(x => new ActivityRecord(x._1.ts,x._1.hr,x._1.lat,x._1.lon,x._1.altitude,x._1.velocity,x._1.grade
      ,x._1.distance,x._1.temperature,x._2,x._1.cadence,x._1.event,x._1.hrv))

    val stops: Option[List[Event]] = new ActivityProcessor(modifiedData,null)
      .detectMotionStops()


    assert(stops.isDefined && stops.get.size==4)
    assert(stops.get(3).secElapsed==3.0)

  }

}
