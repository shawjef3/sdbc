package com.rocketfuel.sdbc.cassandra

import com.rocketfuel.sdbc.Cassandra._
import org.scalacheck.Gen
import org.scalatest.prop.GeneratorDrivenPropertyChecks

import scala.reflect._

class RichResultSetSpec
  extends CassandraSuite
  with GeneratorDrivenPropertyChecks {

  override implicit val generatorDrivenConfig: PropertyCheckConfig = PropertyCheckConfig(maxSize = 10)

  test("iterator() works on several results") {implicit connection =>
    Query(s"CREATE TABLE $keyspace.tbl (id int PRIMARY KEY, x int)").execute()

    forAll { (randoms: Seq[Int]) =>
      val insert = Query(s"INSERT INTO $keyspace.tbl (id, x) VALUES (@id, @x)")

      for ((random, ix) <- randoms.zipWithIndex) {
        insert.on("id" -> ix, "x" -> random).execute()
      }

      val results = Query[Int](s"SELECT x FROM $keyspace.tbl").iterator().toSeq

      assertResult(randoms.sorted)(results.sorted)

      truncate()
    }
  }

  test("iterator() works on several nullable results") {implicit connection =>
    Query(s"CREATE TABLE $keyspace.tbl (x int PRIMARY KEY, y int)").execute()

    forAll { (randoms: Seq[Option[Int]]) =>
      val insert = Query(s"INSERT INTO $keyspace.tbl (x, y) VALUES (@x, @y)")

      for ((random, ix) <- randoms.zipWithIndex) {
        insert.on("x" -> ix, "y" -> random).execute()
      }

      val results = Query[Option[Int]](s"SELECT y FROM $keyspace.tbl").iterator().toSeq

      assertResult(randoms.sorted)(results.sorted)

      truncate()
    }
  }

  test("Insert and select works for tuples.") { implicit connection =>
    Query(s"CREATE TABLE $keyspace.tbl (id int PRIMARY KEY, x tuple<int, int>)").execute()

    forAll { (tuples: Seq[(Int, Int)]) =>
      //Note: Peng verified that values in tuples are nullable, so we need
      //to support that.

      val insert = Query(s"INSERT INTO $keyspace.tbl (id, x) VALUES (@id, @x)")

      for ((tuple, ix) <- tuples.zipWithIndex) {
        insert.on("id" -> ix, "x" -> tuple).execute()
      }

      val results = Query[TupleValue](s"SELECT x FROM $keyspace.tbl").iterator().map(_[(Int, Int)]).toSeq

      assertResult(tuples.toSet)(results.toSet)

      assertResult(tuples.size)(results.size)

      truncate()
    }
  }

  test("Insert and select works for tuples having some null elements.") {implicit connection =>
    Query(s"CREATE TABLE $keyspace.tbl (id int PRIMARY KEY, x tuple<int, int>)").execute()

    forAll { (tuples: Seq[(Option[Int], Option[Int])]) =>
      val insert = Query(s"INSERT INTO $keyspace.tbl (id, x) VALUES (@id, @x)")

      for ((tuple, ix) <- tuples.zipWithIndex) {
        val tupleParam: ParameterValue = productParameterValue(tuple)
        insert.on("id" -> ix, "x" -> tupleParam).execute()
      }

      val results = Query[TupleValue](s"SELECT x FROM $keyspace.tbl").iterator().map(_[(Option[Int], Option[Int])]).toSeq

      assertResult(tuples.toSet)(results.toSet)

      assertResult(tuples.size)(results.size)

      truncate()
    }
  }

  test("Insert and select works for sets.") {implicit connection =>
    Query(s"CREATE TABLE $keyspace.tbl (id int PRIMARY KEY, x set<text>)").execute()

    forAll(Gen.nonEmptyListOf(Gen.nonEmptyContainerOf[Set, String](Gen.alphaStr))) { sets =>
      val insert = Query(s"INSERT INTO $keyspace.tbl (id, x) VALUES (@id, @x)")

      for ((set, id) <- sets.zipWithIndex) {
        insert.on("id" -> id, "x" -> set).execute()
      }

      val results = Query[Set[String]](s"SELECT x FROM $keyspace.tbl").iterator().toSeq

      assertResult(sets.toSet)(results.toSet)

      assertResult(sets.size)(results.size)

      truncate()
    }
  }

  val genStringTuple = for {
    t0 <- Gen.alphaStr
    t1 <- Gen.alphaStr
  } yield (t0, t1)

  test("Insert and select works for maps.") {implicit connection =>
    Query(s"CREATE TABLE $keyspace.tbl (id int PRIMARY KEY, x map<text, text>)").execute()

    forAll(Gen.nonEmptyListOf[Map[String, String]](Gen.nonEmptyMap[String, String](genStringTuple))) { maps =>
      val insert = Query(s"INSERT INTO $keyspace.tbl (id, x) VALUES (@id, @x)")

      for ((map, id) <- maps.zipWithIndex) {
        insert.on("id" -> id, "x" -> map).execute()
      }

      val results = Query[Map[String, String]](s"SELECT x FROM $keyspace.tbl").iterator().toSeq

      assertResult(maps.toSet)(results.toSet)

      assertResult(maps.size)(results.size)

      truncate()
    }
  }

}