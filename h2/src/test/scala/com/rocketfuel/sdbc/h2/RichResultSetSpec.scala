package com.rocketfuel.sdbc.h2

import org.scalatest.BeforeAndAfterEach
import com.rocketfuel.sdbc.H2._
import scala.collection.immutable.Seq

class RichResultSetSpec
  extends H2Suite
  with BeforeAndAfterEach {

  test("iterator() works on a single result") {implicit connection =>
    val results = Query[Int]("SELECT 1").iterator().toSeq
    assertResult(Seq(1))(results)
  }

  test("iterator() works on several results") {implicit connection =>
    val randoms = Seq.fill(10)(util.Random.nextInt())
    Execute("CREATE TABLE tbl (id identity PRIMARY KEY, x int)").execute()

    val batch = randoms.foldLeft(Batch("INSERT INTO tbl (x) VALUES (@x)")) {
      case (batch, r) =>
        batch.addBatch("x" -> r)
    }

    val insertions = batch.iterator

    assertResult(randoms.size)(insertions.sum)

    val results = Query[Int]("SELECT x FROM tbl ORDER BY id ASC").iterator().toSeq

    assertResult(randoms)(results)
  }

  test("using SelectForUpdate to update a value works") {implicit connection =>
    val randoms = Seq.fill(10)(util.Random.nextInt()).sorted

    Execute("CREATE TABLE tbl (id identity PRIMARY KEY, x int)").execute()

    val batch = randoms.foldLeft(Batch("INSERT INTO tbl (x) VALUES (@x)")) {
      case (batch, r) =>
        batch.addBatch("x" -> r)
    }

    batch.iterator()

    for(row <- connection.iteratorForUpdate("SELECT * FROM tbl")) {
      row("x") = row[Int]("x") + 1
      row.updateRow()
    }

    val afterUpdate = connection.iterator[Int]("SELECT x FROM tbl ORDER BY x ASC").toSeq

    for ((afterUpdate, original) <- afterUpdate.zip(randoms)) {
      assertResult(original + 1)(afterUpdate)
    }
  }

  test("to[Vector] works") {implicit connection =>
    val randoms = Seq.fill(10)(util.Random.nextInt()).sorted

    Execute("CREATE TABLE tbl (id identity PRIMARY KEY, x int)").execute()

    val batch = randoms.foldLeft(Batch("INSERT INTO tbl (x) VALUES (@x)")) {
      case (batch, r) =>
        batch.addBatch("x" -> r)
    }

    batch.iterator()

    val result = Query[Int]("SELECT x FROM tbl ORDER BY id ASC").iterator().toVector

    assertResult(randoms)(result)
  }

  override protected def afterEach(): Unit = {
    withMemConnection()(_.execute("DROP TABLE IF EXISTS tbl"))
  }

}
