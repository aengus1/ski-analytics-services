package scala.ski.crunch.activity.processor

import java.util

import com.garmin.fit.Manufacturer
import ski.crunch.activity.model.processor.{ActivityEvent, ActivityHolder, EventType}

import scala.annotation.tailrec
import scala.collection.JavaConversions._
import scala.ski.crunch.activity.processor.model.ActivityRecord
import scala.collection.JavaConverters._


class EventProcessor(holder: ActivityHolder) {

  /**
    * For Suunto devices, pause detection relies on a regular sampling interval between records.  This threshold
    * indicates the percentage of records that adhere to the sampling rate.
    * e.g. threshold = 0.8 => if less than 80% of records have the same time gap between them (e.g. 1 sec, 10 sec etc)
    * then we will give up on pause detection
    * Garmin devices are not affected by this because they use explicit event messages for pause detection
    *
    * Other device manufacturers are not currently supported
    *
    */
  private val PAUSE_DETECTION_THRESHOLD = 0.8

  /**
    *
    * @return
    */
  def detectPauses(): EventProcessor = {
    var records = holder.getRecords.toList
    val recordProcessor = new RecordProcessor(records)
    val index = recordProcessor.buildTsIndex()

    holder.getManufacturer match {
      //suunto doesn't record pauses explicitly.  We detect them by looking at variation in sampling rate.
      //if sampling rate is relatively consistent and we detect a gap then we assume this is a pause
      case "SUUNTO" => {
        //group the gaps between reading by their frequency. return a sorted map (gap(seconds), frequency))
        val gaps = records.sliding(2).map(x => ActivityRecord.tsDiff(x.head.ts, x.tail.head.ts)).toList.groupBy(identity).map(x => (x._1, x._2.size)).toList.sortBy(_._2).reverse
        var evts: List[ActivityEvent] = List[ActivityEvent]()
        //if can't determine the sampling rate with accuracy then don't try and detect pauses
        if (gaps.head._2.toDouble / records.size < PAUSE_DETECTION_THRESHOLD) {
          //println(gaps.head + " " + records.size)
          this
        } else {
          //println("sampling rate = " + gaps.head._1)
          records.sliding(2).foreach(x => {
            val diff = ActivityRecord.tsDiff(x.head.ts, x.tail.head.ts)
            if (diff != gaps.head._1 && diff > 2) {
              val pauseStart = new ActivityEvent()
              pauseStart.setEventType(EventType.PAUSE_START)
              pauseStart.setIndex(index.get(x.head.ts).get)
              pauseStart.setTs(x.head.ts)
              pauseStart.setTrigger("manual detection")
              val pauseEnd = new ActivityEvent()
              pauseEnd.setEventType(EventType.PAUSE_STOP)
              pauseEnd.setIndex(index.get(x.tail.head.ts).get)
              pauseEnd.setTs(x.tail.head.ts)
              pauseEnd.setTrigger("manual detection")

              evts = pauseStart :: pauseEnd :: evts
            }
          })
          val res: java.util.ArrayList[ActivityEvent] = new java.util.ArrayList[ActivityEvent](evts.reverse.asJava)
          holder.setEvents(res)
          val newHolder = new ActivityHolder(holder)
          newHolder.setEvents(res)
          new EventProcessor(newHolder)
        }
      }
      //garmin records pauses explicitly. Pauses are the gaps between timer_stop and timer_start events
      case "GARMIN" => {
        val index = recordProcessor.buildTsIndex()
        var events = holder.getEvents().toList

        val timerst = events.filter(x => (x.getEventType == EventType.TIMER_START))
        val timeren = events.filter(x => (x.getEventType == EventType.TIMER_STOP))
        // if only one timer start and equals activity start then no pauses are present
        if (timerst.length == 1 && timerst.get(0).getTs.equals(events.filter(x => x.getEventType == EventType.ACTIVITY_START))) {
          return this
        }
        //        //zip together 1st stop with 2nd start, 2nd stop with 3rd start.. etc
        val pausePeriods = timeren.zip(timerst.tail)
        pausePeriods.foreach(x => {
          val st = new ActivityEvent(index.get(x._1.getTs).get, EventType.PAUSE_START, x._1.getTs)
          val en = new ActivityEvent(index.get(x._2.getTs).get, EventType.PAUSE_STOP, x._2.getTs)
          events = st :: en :: events
        })

        val res: java.util.ArrayList[ActivityEvent] = new java.util.ArrayList[ActivityEvent](events.reverse.asJava)
        holder.setEvents(res)
        val newHolder = new ActivityHolder(holder)
        newHolder.setEvents(res)
        new EventProcessor(newHolder)
      }
      case everythingElse => {
        println(" manufacturer currently not supported")
        return this
      }

    }
  }

  def getEvents(): java.util.ArrayList[ActivityEvent] = {
    new java.util.ArrayList[ActivityEvent](holder.getEvents)
  }

  def detectLapEvents(): EventProcessor = {
    //suunto lap button press is coded as lap, manual.  interval timer as lap, distance.
    // for garmin auto-lap is coded as lap, distance, manual is coded as lap, session_end


    val records = holder.getRecords.toList
    val events = holder.getEvents.toList
    val nonLapEvents: List[ActivityEvent] = events.filter(x => !x.getEventType.equals(EventType.LAP_START) && !x.getEventType.equals(EventType.LAP_STOP))
    val recordProcessor = new RecordProcessor(records)
    val index = recordProcessor.buildTsIndex()

    holder.getManufacturer match {

      //SUUNTO lap button press is coded as lap, manual (interval timer is coded as lap, distance)
      case "SUUNTO" => {
        val startLapEvents = events.filter(x => x.getEventType.equals(EventType.LAP_START) && x.getTrigger == "MANUAL")
          .map(x => new ActivityEvent(index.get(x.getTs).getOrElse(-999), EventType.LAP_START, x.getTs))

        val stopLapEvents = events.filter(x => x.getEventType.equals(EventType.LAP_STOP) && x.getTrigger == "MANUAL")
          .map(x => new ActivityEvent(index.get(x.getTs).getOrElse(-999), EventType.LAP_STOP, x.getTs))
        val eventResults: List[ActivityEvent] = startLapEvents ++ stopLapEvents ++ nonLapEvents
        val res: java.util.ArrayList[ActivityEvent] = new java.util.ArrayList[ActivityEvent](eventResults.reverse.asJava)
        holder.setEvents(res)
        new EventProcessor(holder)
      }
      case "GARMIN" => {
        // lap button press on garmin is coded as lap - session_end.  autolap is coded as lap - distance
        val startLapEvents = events.filter(x => x.getEventType.equals(EventType.LAP_START) && x.getTrigger == "SESSION_END")
          .map(x => new ActivityEvent(index.get(x.getTs).getOrElse(-999), EventType.LAP_START, x.getTs))

        val startAutoLapEvents = events.filter(x => x.getEventType.equals(EventType.LAP_START) && x.getTrigger == "DISTANCE")
          .map(x => new ActivityEvent(index.get(x.getTs).getOrElse(-999), EventType.LAP_START, x.getTs, "autolap"))

        val stopLapEvents = events.filter(x => x.getEventType.equals(EventType.LAP_STOP) && x.getTrigger == "SESSION_END")
          .map(x => new ActivityEvent(index.get(x.getTs).getOrElse(-999), EventType.LAP_STOP, x.getTs))

        val stopAutoLapEvents = events.filter(x => x.getEventType.equals(EventType.LAP_STOP) && x.getTrigger == "SESSION_END")
          .map(x => new ActivityEvent(index.get(x.getTs).getOrElse(-999), EventType.LAP_STOP, x.getTs, "autolap"))

        val eventResults: List[ActivityEvent] = startLapEvents ++ startAutoLapEvents ++ stopLapEvents ++ stopAutoLapEvents ++nonLapEvents
        val res: java.util.ArrayList[ActivityEvent] = new java.util.ArrayList[ActivityEvent](eventResults.reverse.asJava)
        holder.setEvents(res)
        new EventProcessor(holder)

      }
      case _ => None
    }

    return new EventProcessor(holder)
  }

  def detectMotionStops(): EventProcessor = {
    val records = holder.getRecords.toList
    val events = holder.getEvents.toList
    val recordProcessor = new RecordProcessor(records)
    val index = recordProcessor.buildTsIndex()

      val st = records.sliding(2).filter(x => x.head.moving==true && x.tail.head.moving ==false).map(x => x.tail.head).toList
      val en = records.sliding(2).filter(x => x.head.moving == false && x.tail.head.moving==true).map(x => x.tail.head).toList

      def doMap(tuple: List[(ActivityRecord, ActivityRecord)]): List[ActivityEvent] = {
        tuple.map(r => new ActivityEvent(index.get(r._1.ts).get, if( index.get(r._1.ts).get  > index.get(r._2.ts).get ) EventType.MOTION_STOP else EventType.MOTION_START, r._1.ts, "") ::
          new ActivityEvent(index.get(r._2.ts).get, if( index.get(r._1.ts).get  > index.get(r._2.ts).get ) EventType.MOTION_START else EventType.MOTION_STOP,r._2.ts,""):: Nil).flatten
      }

    // +=
    //         new ActivityEvent(index.get(r._2.ts).get, EventType.MOTION_START,r._2.ts,"")

      // handle the cases where the activity starts or finishes with a motion stop
     val evts =  (st.size - en.size) match {
       case 0 => doMap(st zip en)
       case -1 => doMap(records.head :: st zip en)
       case 1 => doMap(st zip en :+ records.reverse.head)
       case _ => {
         println("Unexpected state.  Number of motion start/stop events differ by more than one")
         events
       }
     }
          val res: java.util.ArrayList[ActivityEvent] = new java.util.ArrayList[ActivityEvent](events ++ evts.reverse.asJava)
          holder.setEvents(res)
          new EventProcessor(holder)
    }

}
