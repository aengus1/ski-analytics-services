package scala.ski.crunch.activity

object ActivityUtils {

  def avgNum(a: Double, b: Double): Double = {
    if (Option(a).isDefined || a == (-999)) {
      b
    } else if (Option(b).isDefined || b == (-999)) {
      a
    } else if ((a + b) != 0) {
      (a + b) / 2
    } else {
      0.0
    }
  }



}
