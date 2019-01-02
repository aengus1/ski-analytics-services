import java.io.InputStream

import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}


/**
  * Created by aengusmccullough on 2017-05-08.
  *
  * FILE 1 schematic (suunto_1.fit, garmin_1.fit)
  *
  * start_time: 12/5/17 12:17:06
  * hr -> no
  * gps -> yes
  *
  *
  * start       pause       motion-stop     lap       interval      interval        end
  *
  * |       |-------|       |--------|       |         |---|---------------|         |
  * 0       30      60      90      120     150       180  240            480       510
  *
  *
  * *Suunto -> triple pressed lap button at 150s
  * * Garmin -> No interval timer
  *
  *
  *
  */
class SuuntoDetectLapEventSpec extends FlatSpec with Matchers with BeforeAndAfter {
  var ap: ActivityProcessor = null;
  var lapEvents: Option[List[Event]] = null;

  before {
    val parser: FitParser = new FitParser()
    val stream: InputStream = getClass.getResourceAsStream("/suunto_1.fit")
    parser.parse(stream)


    val builder: SafBuilder = new SafBuilder()
    val activity = builder.split(parser.getCombinedActivity, parser.getSessions).get(0)
    val records = ActivityRecord.parse(activity)


    this.ap = new ActivityProcessor(records, null)
      .mergeDuplicates()
      .removeCorrupt()
      .replaceNulls()
      .smoothGeom()
      .processGeom()
      .presenceOfAttributes()
      .calcMoving()

    var origEvts = ap.setFitEvtIndexes(activity.getEvents.toList)

    this.lapEvents = ap.detectLapEvents(origEvts, activity.getMeta.getManufacturer.getValue)
  }

  //Detect Lap  Events Suunto

  "DetectLapEvents for suunto device" should "correctly detect 4 laps in suunto_1.fit" in {

    //println("lap events" + lapEvents)
    assert(lapEvents.isDefined && lapEvents.get.size == 4)

  }

  "DetectLapEvents for suunto device" should "correctly determine start time and duration  of lap 1  in suunto_1.fit" in {


    assert(lapEvents.get(0).ts == "12-05-2017 12:17:06")
    assert(lapEvents.get(0).secElapsed == 156)


    lapEvents.get(0) match {
      case l: LapEvent => {
        assert(l.secTimer.toInt == 120)
      }
      case _ => assert(false, "Expected a lapevent")
    }


    lapEvents.get(0) match {
      case l: LapEvent => {
        println(l.distTravelled)
        assert(l.distTravelled.toInt == 158)
      }
      case _ => assert(false, "Expected a lapevent")
    }


  }


  /**
    * Created by aengusmccullough on 2017-05-08.
    *
    * FILE 2 schematic (suunto_2.fit, garmin_2.fit)
    *
    * start_time: 12/5/17 12:27:06
    * hr -> no
    * gps -> yes
    *
    *
    * start m-stop      lap         pause         lap        m-stop  end
    *
    * |--------|       |       |--------|       |         |--------|
    * 0       30      60      90      120     150       180       210
    *
    *
    * *Suunto -> double pressed lap button at 150s
    * * Garmin -> No interval timer
    *
    */

  "DetectLapEvents for suunto device" should "correctly determine lap 2 elapsed time as 90 seconds and timer time as 60 seconds in suunto_2.fit" in {
    val parser: FitParser = new FitParser()
    val stream: InputStream = getClass.getResourceAsStream("/suunto_2.fit")
    parser.parse(stream)


    val builder: SafBuilder = new SafBuilder()
    val activity = builder.split(parser.getCombinedActivity, parser.getSessions).get(0)
    val records = ActivityRecord.parse(activity)


    this.ap = new ActivityProcessor(records, null)
      .mergeDuplicates()
      .removeCorrupt()
      .replaceNulls()
      .smoothGeom()
      .processGeom()
      .presenceOfAttributes()
      .calcMoving()

    var origEvts = ap.setFitEvtIndexes(activity.getEvents.toList)

    this.lapEvents = ap.detectLapEvents(origEvts, activity.getMeta.getManufacturer.getValue)
    println(lapEvents)

    assert(lapEvents.isDefined && lapEvents.get(1).secElapsed.toInt == 89 )


    lapEvents.get(1) match {
      case l: LapEvent => {
        assert(l.secTimer.toInt == 60)
      }
      case _ => assert(false, "Expected a lapevent")
    }
  }
}
