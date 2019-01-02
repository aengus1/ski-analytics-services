import java.io.InputStream

import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

import scala.ski.crunch.activity.processor.RecordProcessor


/**
  * Created by aengusmccullough on 2017-05-08.
  *
  * FILE 1 schematic (suunto_1.fit, garmin_1.fit)
  *
  * start       pause       motion-stop     lap       interval      interval        end
  *
  *   |       |-------|       |--------|       |         |--------|--------|         |
  *   0       30      60      90      120     150       180       210     240       270
  *
  *
  *   *Suunto -> double pressed lap button at 150s
  *   * Garmin -> No interval timer
  *
  *
  *
  */
class DetectLapEventSpec extends FlatSpec with Matchers with BeforeAndAfter{
  var ap: RecordProcessor = null;
  var lapEvents: Option[List[Event]] = null;

    before{
    val parser: FitParser = new FitParser()
    val stream: InputStream = getClass.getResourceAsStream("/laptest.fit")
    //val stream: InputStream = getClass.getResourceAsStream("/garmin_test.fit")
    parser.parse(stream)


    val builder: SafBuilder = new SafBuilder()
    val activity = builder.split(parser.getCombinedActivity, parser.getSessions).get(0)
    val records = ActivityRecord.parse(activity)

    this.ap =  new RecordProcessor(records)
      .mergeDuplicates()
      .removeCorrupt()
      .replaceNulls()
      .processGeom()
      .presenceOfAttributes()
      .calcMoving()
    this.lapEvents = ap.detectLapEvents(activity.getEvents.toList,activity.getMeta.getManufacturer.getValue)
 }

  //Detect Lap  Events Suunto

  "DetectLapEvents for suunto device" should "correctly detect 11 laps in laptest.fit" in {

    assert(lapEvents.isDefined && lapEvents.get.size==11)

  }

  "DetectLapEvents for suunto device" should "correctly determine start time, duration and distance of lap 4  in laptest2.fit" in {


    assert(lapEvents.get(3).ts =="26-09-2016 18:11:18")
    assert(lapEvents.get(3).secElapsed ==90.373)
    //println(lapEvents.get(3) match { case  l: LapEvent => l.distTravelled })
//    assert(lapEvents.get(3).distTravelled=="?")

  }

  "DetectLapEvents for garmin device" should "correctly detect 11 laps in garmin_test.fit" in {

    val parser: FitParser = new FitParser()
    val stream: InputStream = getClass.getResourceAsStream("/garmin_test.fit")
    //val stream: InputStream = getClass.getResourceAsStream("/garmin_test.fit")
    parser.parse(stream)


    val builder: SafBuilder = new SafBuilder()
    val activity = builder.split(parser.getCombinedActivity, parser.getSessions).get(0)
    val records = ActivityRecord.parse(activity)

    this.ap =  new RecordProcessor(records,null)
      .mergeDuplicates()
      .removeCorrupt()
      .replaceNulls()
      .processGeom()
      .presenceOfAttributes()
      .calcMoving()
    this.lapEvents = ap.detectLapEvents(activity.getEvents.toList,activity.getMeta.getManufacturer.getValue)

    println("garmin lap events" + lapEvents)
    //assert(lapEvents.isDefined && lapEvents.get.size==11)

  }


  //TODO -> GARMIN EVENTS

}
