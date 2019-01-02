package scala.ski.crunch.activity.processor.model

/**
  * Created by aengusmccullough on 2017-02-26.
  */
class ActivitySummary(val startTs: String, val endTs: String,
                      val totalElapsed: Double, val totalTimer: Double, val totalMoving: Double,
                      val totalStopped: Double, val totalPaused: Double, val totalAscent: Double,
                      val totalDescent: Double, val totalDistance :Double, val totalCalories: Double,
                      val avgHr: Int, val maxHr: Int, val avgCadence: Int, val maxCadence:Int,
                      val avgTemp: Int, val maxTemp: Int, val avgSpeed: Double, val maxSpeed: Double, nLaps:Int) {


}
