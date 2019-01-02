package scala.ski.crunch.activity.processor.model

/**
  * Created by aengusmccullough on 2017-04-10.
  */
sealed abstract class Event(val startIndex: Int, val endIndex: Int, val ts: String, val secElapsed: Double) {
  override def toString:String = {
     "Event(" + ts +": " + secElapsed+")"
  }
}

/**
  *
  * @param startIndex
  * @param endIndex
  * @param ts
  * @param secElapsed
  * @param distTravelled
  * @param altDiff
  * @param initMoving  was the last reading before pause moving?  needed so we don't skew moving time with paused data
  */
case class PauseEvent(override val startIndex: Int, override val endIndex: Int, override val ts: String,  override val secElapsed: Double, val distTravelled: Double,  altDiff: Double,  initMoving: Boolean) extends Event(startIndex, endIndex, ts,  secElapsed)
case class LapEvent(override val startIndex: Int, override val endIndex: Int, override val ts: String,   secTimer: Double, override val secElapsed: Double, val distTravelled: Double) extends Event(startIndex, endIndex, ts,  secElapsed)
case class TimerEvent(override val startIndex: Int, override val endIndex: Int, override val ts: String,  secTimer: Double, override val secElapsed: Double, val distTravelled: Double) extends Event(startIndex, endIndex, ts,  secElapsed)
case class MotionStopEvent(override val startIndex: Int, override val endIndex: Int, override val ts: String, override val secElapsed: Double) extends Event(startIndex, endIndex, ts,  secElapsed)





