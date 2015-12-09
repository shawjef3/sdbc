package com.rocketfuel.sdbc.base.jdbc

import org.scalatest.FunSuite
import shapeless._
import shapeless.syntax.singleton._
import scalaz.Scalaz._

class SettersSpec
  extends FunSuite {

  val q = TestDbms.Update("@hi @bye")

  test("set a pair") {
    assertCompiles("""q.on("hi" -> 3)""")
  }

  test("set a pair, and its values are correct") {
    val withParams = q.on("hi" -> 3)

    assertResult(Map("hi" -> 3.some))(withParams.parameterValues)
  }

  test("set a record") {
    val params = ('hi ->> 3) :: ('bye ->> 4) :: HNil

    assertCompiles("""q.onRecord(params)""")
  }

  test("set a record, and its values are correct") {
    val params = ('hi ->> 3) :: ('bye ->> 4) :: HNil

    val withParams = q.onRecord(params)

    assertResult(Map("hi" -> params.head.some, "bye" -> params(1).some))(withParams.parameterValues)
  }

  test("set a product") {
    case class Args(hi: Int, bye: Int)

    val param = Args(3, 4)

    assertCompiles("""q.onProduct(param)""")
  }

  test("set a product, and its values are correct") {
    case class Args(hi: Int, bye: Int)

    val param = Args(3, 4)

    val withParams = q.onProduct(param)

    assertResult(Map("hi" -> param.hi.some, "bye" -> param.bye.some))(withParams.parameterValues)
  }

}
