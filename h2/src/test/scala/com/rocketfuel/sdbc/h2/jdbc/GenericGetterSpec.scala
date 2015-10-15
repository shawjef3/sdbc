package com.rocketfuel.sdbc.h2.jdbc

class GenericGetterSpec extends H2Suite {

  test("(Int, Int, Int)") {implicit connection =>
    val query = Select[(Int, Int, Int)]("VALUES (1, 2, 3)")
    val result = query.option()
    val expected = Some((1, 2, 3))
    assertResult(expected)(result)
  }

  test("(Int, String)") {implicit connection =>
    val query = Select[(Int, String)]("VALUES (1, 'hi')")
    val result = query.option()
    val expected = Some((1, "hi"))
    assertResult(expected)(result)
  }

  test("case class TestClass(id: Int, value: String)") {implicit connection =>
    case class TestClass(id: Int, value: String)

    val query = Select[TestClass]("VALUES (1, 'hi')")
    val result = query.option()
    val expected = Some(TestClass(1, "hi"))
    assertResult(expected)(result)
  }

}