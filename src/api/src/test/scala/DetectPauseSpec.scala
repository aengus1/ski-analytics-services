import java.io.InputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date

import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

import scala.collection.JavaConversions._
import scala.ski.crunch.activity.processor.model.ActivityRecord



/**
  * Created by aengusmccullough on 2017-05-06.
  */
class DetectPauseSpec() extends FlatSpec with Matchers with BeforeAndAfter{

  val sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
  var records: List[ActivityRecord] = null
  var activity: Activity = null
  before{

    val parser: FitParser = new FitParser()
    val stream: InputStream = getClass.getResourceAsStream("/interval_test.fit")
    parser.parse(stream)

    val builder: SafBuilder = new SafBuilder()
    this.activity = builder.split(parser.getCombinedActivity, parser.getSessions).get(0)
    this.records = ActivityRecord.parse(activity)
  }


  //  "DetectPauses" should "fail if can't determine the sampling rate with accuracy" in {
  //
  //    val toChange = this.records.size/100*20
  //
  //    //randomize the timestamps of 20% of the records
  //    val nrecords: List[ActivityRecord] = records.reverse.take(toChange).map(x=> new ActivityRecord(sdf.format(new Date(sdf.parse(x.ts).toInstant.plusMillis((Math.random*10000).toLong).toEpochMilli)),
  //    x.hr,x.lat,x.lon,x.altitude,x.velocity,x.grade,x.distance,x.temperature,x.moving,x.cadence,x.event,x.hrv)) ++ records.take(records.size - toChange)
  //      .sortWith((a, b) => ActivityRecord.sdf.parse(a.ts).getTime > ActivityRecord.sdf.parse(b.ts).getTime)
  //
  //    val ap: ActivityProcessor =  new ActivityProcessor(nrecords,null)
  //
  //    val pauses = ap.mergeDuplicates()
  //      .removeCorrupt()
  //      .replaceNulls()
  //      .processGeom()
  //      .presenceOfAttributes()
  //      .calcMoving()
  //      .detectPauses(activity.getEvents.toList,activity.getMeta.getManufacturer.getValue)
  //
  //    assert(pauses.isEmpty)
  //
  //  }
  //
  //  "DetectPauses" should "detect pause at 62 seconds for 14 seconds in interval_test.fit  file" in {
  //
  //    val ap: ActivityProcessor =  new ActivityProcessor(records,null)
  //    val pauses = ap.mergeDuplicates()
  //      .removeCorrupt()
  //      .replaceNulls()
  //      .processGeom()
  //      .presenceOfAttributes()
  //      .calcMoving()
  //      .detectPauses(activity.getEvents.toList,activity.getMeta.getManufacturer.getValue)
  //
  //    assert(pauses.isDefined)
  //    assert(ActivityRecord.tsDiff(records.head.ts,pauses.get.head.ts)==62)
  //    assert(pauses.get.head.secElapsed==14)
  //
  //
  //  }
  //
  //
  //  "DetectPauses" should "detect 1 metre as distance travelled during first pause in interval_test.fit file" in {
  //    val ap: ActivityProcessor =  new ActivityProcessor(records,null)
  //    val pauses: Option[List[Event]] = ap.mergeDuplicates()
  //      .removeCorrupt()
  //      .replaceNulls()
  //      .processGeom()
  //      .presenceOfAttributes()
  //      .calcMoving()
  //      .detectPauses(activity.getEvents.toList,activity.getMeta.getManufacturer.getValue)
  //
  //    assert(pauses.isDefined)
  //    pauses.get.head match{
  //      case h: PauseEvent => assert(h.distTravelled.toInt ==1)
  //      case _ => assert(false)
  //    }
  //
  //
  //
  //  }
  //
  //  "DetectPauses" should " detect 5 pauses  in interval_test.fit test file" in {
  //    val ap: ActivityProcessor =  new ActivityProcessor(records,null)
  //    val pauses = ap.mergeDuplicates()
  //      .removeCorrupt()
  //      .replaceNulls()
  //      .processGeom()
  //      .presenceOfAttributes()
  //      .calcMoving()
  //      .detectPauses(activity.getEvents.toList,activity.getMeta.getManufacturer.getValue)
  //
  //    println(pauses)
  //    assert(pauses.get.size==5)
  //  }
  //
  //  "DetectPauses" should "detect pause at 917 seconds for 65 seconds in interval_test.fit  file" in {
  //
  //    val ap: ActivityProcessor =  new ActivityProcessor(records,null)
  //    val pauses = ap.mergeDuplicates()
  //      .removeCorrupt()
  //      .replaceNulls()
  //      .processGeom()
  //      .presenceOfAttributes()
  //      .calcMoving()
  //      .detectPauses(activity.getEvents.toList,activity.getMeta.getManufacturer.getValue)
  //
  //
  //    assert(pauses.isDefined)
  //    assert(ActivityRecord.tsDiff(records.head.ts,pauses.get(4).ts)==917)
  //    assert(pauses.get(4).secElapsed==65)
  //
  //  }
  //
  //
  //  "DetectPauses" should "not detect any pauses  in test.fit test file" in {
  //
  //    val parser: FitParser = new FitParser()
  //    val stream: InputStream = getClass.getResourceAsStream("/test.fit")
  //    parser.parse(stream)
  //
  //    val builder: SafBuilder = new SafBuilder()
  //    val activity = builder.split(parser.getCombinedActivity, parser.getSessions).get(0)
  //    this.records = ActivityRecord.parse(activity)
  //
  //    val ap: ActivityProcessor =  new ActivityProcessor(records,null)
  //    val pauses = ap.mergeDuplicates()
  //      .removeCorrupt()
  //      .replaceNulls()
  //      .processGeom()
  //      .presenceOfAttributes()
  //      .calcMoving()
  //      .detectPauses(activity.getEvents.toList,activity.getMeta.getManufacturer.getValue)
  //
  //    assert(pauses.get.isEmpty)
  //
  //  }

  "DetectPauses" should "correctly detect two pauses in a garmin.fit file" in {
    val parser: FitParser = new FitParser()
    val stream: InputStream = getClass.getResourceAsStream("/garmin_test.fit")
    parser.parse(stream)

    val builder: SafBuilder = new SafBuilder()
    val activity = builder.split(parser.getCombinedActivity, parser.getSessions).get(0)
    this.records = ActivityRecord.parse(activity)

    val ap: ActivityProcessor =  new ActivityProcessor(records,null)
    val pauses = ap.mergeDuplicates()
      .removeCorrupt()
      .replaceNulls()
      .processGeom()
      .presenceOfAttributes()
      .calcMoving()
      .detectPauses(activity.getEvents.toList,activity.getMeta.getManufacturer.getValue)

    assert(pauses.get.size==2)
    assert(pauses.get(0).secElapsed == 9.0)
    //println("garmin pauses: " + pauses)
  }
