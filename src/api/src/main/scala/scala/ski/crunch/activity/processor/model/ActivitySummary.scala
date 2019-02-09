package scala.ski.crunch.activity.processor.model

/**
  * Created by aengusmccullough on 2017-02-26.
  */
class ActivitySummary(val segmentType: String, val startTs: String, val endTs: String,
                      val totalElapsed: Double, val totalTimer: Double, val totalMoving: Double,
                      val totalStopped: Double, val totalPaused: Double, val totalAscent: Double,
                      val totalDescent: Double, val totalDistance :Double,
                      val avgHr: Int, val maxHr: Int, val avgCadence: Int, val maxCadence:Int,
                      val avgTemp: Double, val maxTemp: Double, val avgSpeed: Double, val maxSpeed: Double,
                      val avPositiveGrade: Double, val maxPositiveGrade: Double, val avNegativeGrade: Double,
                      val maxNegativeGrade: Double, val avPositiveVerticalSpeed: Double, val maxPositiveVerticalSpeed: Double,
                      val avNegativeVerticalSpeed: Double, val maxNegativeVerticalSpeed: Double)




