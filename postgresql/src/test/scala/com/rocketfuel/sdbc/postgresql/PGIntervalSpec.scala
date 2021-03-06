package com.rocketfuel.sdbc.postgresql

import com.rocketfuel.sdbc.postgresql.IntervalConstants._
import java.time.Duration
import org.postgresql.util.PGInterval
import org.scalatest.FunSuite

class PGIntervalSpec extends FunSuite with IntervalImplicits {

  val str = "9 years 11 mons 29 days 06:41:38.636266"

  val asPg = new PGInterval(str)

  val asPgParts = new PGInterval(9, 11, 29, 6, 41, 38.636266)

  val asDuration: Duration = asPg

  test("PGInterval created from string equals PGInterval created from parts") {
    assertResult(asPgParts.getValue)(asPg.getValue)
  }

  test("Duration converted from PGInterval equals Duration") {
    val nanos = 636266000L
    var seconds = 0L
    seconds += 38
    seconds += 41 * secondsPerMinute
    seconds += 6 * secondsPerHour
    seconds += 29 * secondsPerDay
    seconds += 11 * secondsPerMonth
    seconds += 9 * secondsPerYear
    val duration = Duration.ofSeconds(seconds, nanos)

    val difference = asDuration.minus(duration)
    val differenceNanos = difference.getSeconds * nanosecondsPerSecond + difference.getNano

    assert(differenceNanos.abs < 100, "The difference must be less than 100 nanoseconds.")
  }

  test("PGInterval <-> Duration conversion is commutative.") {
    val asPg2: PGInterval = asDuration

    assertResult(asPg.getYears)(asPg2.getYears)
    assertResult(asPg.getMonths)(asPg2.getMonths)
    assertResult(asPg.getDays)(asPg2.getDays)
    assertResult(asPg.getHours)(asPg2.getHours)
    assertResult(asPg.getMinutes)(asPg2.getMinutes)
    assert((asPg.getSeconds - asPg2.getSeconds).abs < 0.00001,
      "The difference between the seconds should be non-zero to allow for rounding."
    )
  }

}
