package com.rocketfuel.sdbc.postgresql

import com.rocketfuel.sdbc.PostgreSql._
import java.math.{BigDecimal => JBigDecimal}
import org.scalatest._

class SettersSpec
  extends FunSuite {

  test("implicit None conversion works") {
    assertCompiles("val _: ParameterValue = None")
  }

  test("implicit Node conversion works") {
    //assertCompiles doesn't work for this test
    val _: ParameterValue = <a></a>
  }

  test("implicit Seq[Int] conversion works") {
    assertCompiles("val _: ParameterValue = Seq[Int]()")
  }

  test("implicit Seq[String] conversion works") {
    assertCompiles("val _: ParameterValue = Seq[String]()")
  }

  test("implicit Seq[java.math.BigDecimal] conversion works") {
    assertCompiles("val _: ParameterValue = Seq[java.math.BigDecimal]()")
  }

  test("implicit Seq[scala.BigDecimal] conversion works") {
    assertCompiles("val _: ParameterValue = Seq[scala.BigDecimal]()")
  }

  test("implicit Seq[Option[Int]] conversion works") {
    assertCompiles("val _: ParameterValue = Seq[Option[Int]]()")
  }

  test("implicit Seq[Option[java.math.BigDecimal]] conversion works") {
    assertCompiles("val _: ParameterValue = Seq[Option[JBigDecimal]]()")
  }

  test("implicit Seq[Option[scala.BigDecimal]] conversion works") {
    assertCompiles("val _: ParameterValue = Seq[Option[BigDecimal]]()")
  }

  test("implicit Seq[java.lang.Long] conversion works") {
    assertCompiles("val _: ParameterValue = Seq[java.lang.Long]()")
  }

}