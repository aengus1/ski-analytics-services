
  import java.io.InputStream

  import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

  /**
    * Created by aengusmccullough on 2017-05-08.
    */
  class DetectTimerEventSpec extends FlatSpec with Matchers with BeforeAndAfter{
    var ap: ActivityProcessor = null;
    var timerEvents: Option[List[Event]] = null;

    before{
      val parser: FitParser = new FitParser()
      val stream: InputStream = getClass.getResourceAsStream("/interval_test.fit")
      parser.parse(stream)


      val builder: SafBuilder = new SafBuilder()
      val activity = builder.split(parser.getCombinedActivity, parser.getSessions).get(0)
      val records = ActivityRecord.parse(activity)

      this.ap =  new ActivityProcessor(records,null)
        .mergeDuplicates()
        .removeCorrupt()
        .replaceNulls()
        .processGeom()
        .presenceOfAttributes()
        .calcMoving()
      this.timerEvents = ap.detectTimerEvents(activity.getEvents.toList,activity.getMeta.getManufacturer.getValue)
    }


    //Detect Timer Events Suunto

    "DetectTimerEvents for suunto device" should "correctly detect 6 laps in interval_test.fit" in {
      assert(timerEvents.get.size==6)

    }

    "DetectTimerEvents for suunto device" should "correctly determine start time, duration and distance of interval 3 in interval_test.fit" in {
      assert(timerEvents.get(2).secElapsed==240)
      assert(timerEvents.get(2).ts=="07-04-2017 17:02:10")
      //assert(timerEvents.get(3).distance==3100)
    }

    "DetectTimerEvents for garmin device" should "correctly determine elapsed time and timer time  in laptest2.fit" in {

      val parser: FitParser = new FitParser()
      val stream: InputStream = getClass.getResourceAsStream("/garmin_test.fit")
      parser.parse(stream)


      val builder: SafBuilder = new SafBuilder()
      val activity = builder.split(parser.getCombinedActivity, parser.getSessions).get(0)
      val records = ActivityRecord.parse(activity)

      this.ap =  new ActivityProcessor(records,null)
        .mergeDuplicates()
        .removeCorrupt()
        .replaceNulls()
        .processGeom()
        .presenceOfAttributes()
        .calcMoving()
      this.timerEvents = ap.detectTimerEvents(activity.getEvents.toList,activity.getMeta.getManufacturer.getValue)
      println("garmin timer events: " + timerEvents)

    }

    //TODO -> GARMIN EVENTS

  }
