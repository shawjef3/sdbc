package com.rocketfuel.sdbc.postgresql

import java.time.OffsetDateTime
import org.scalatest._
import fs2.Strategy
import org.apache.commons.lang3.RandomStringUtils
import scala.reflect.ClassTag

abstract class PostgreSqlSuite
  extends fixture.FunSuite
  with HasPostgreSqlPool
  with BeforeAndAfterAll {
  self: PostgreSql =>

  def testSelect[T](query: String, expectedValue: Option[T])(implicit converter: RowConverter[Option[T]]): Unit = {
    test(query) { implicit connection =>
      val result = Select[Option[T]](query).one()
      (expectedValue, result) match {
        case (Some(expectedOffset: OffsetDateTime), Some(resultOffset: OffsetDateTime)) =>
          assertResult(expectedOffset.toInstant)(resultOffset.toInstant)
        case (expected, actual) =>
          assertResult(expected)(actual)
      }
    }
  }

  type FixtureParam = Connection

  override protected def withFixture(test: OneArgTest): Outcome = {
    withPg[Outcome] { connection =>
      withFixture(test.toNoArgTest(connection))
    }
  }

  override protected def beforeAll(): Unit = {
    pgStart()
    createLTree()
    createHstore()
  }

  override protected def afterAll(): Unit = {
    pgStop()
  }

  implicit val strategy =
    Strategy.sequential

  def testUpdate[T](
    typeName: String
  )(before: T
  )(after: T
  )(implicit ctag: ClassTag[T],
    updater: Updater[T],
    setter: T => ParameterValue,
    converter: RowConverter[Option[T]]
  ): Unit = {
    test(s"Update ${ctag.runtimeClass.getName}") {implicit connection =>
      val tableName = RandomStringUtils.randomAlphabetic(10)

      Ignore.ignore(s"CREATE TABLE $tableName (id serial PRIMARY KEY, v $typeName)")

      Ignore.ignore(s"INSERT INTO $tableName (v) VALUES (@before :: $typeName)", Map("before" -> before))

      def updateRow(row: UpdatableRow): Unit = {
        row("v") = after
        row.updateRow()
      }

      val summary =  SelectForUpdate.update(s"SELECT * FROM $tableName", rowUpdater = updateRow)

      assertResult(UpdatableRow.Summary(updatedRows = 1))(summary)

      val maybeValue = Select.one[Option[T]](s"SELECT v FROM $tableName")

      assert(maybeValue.nonEmpty)

      (after, maybeValue.get) match {
        case (a: Array[_], b: Array[_]) =>
          assert(a.sameElements(b))
        case (expectedAfter, actualAfter) =>
          assertResult(expectedAfter)(actualAfter)
      }
    }
  }

}

object PostgreSqlSuite {

  abstract class Base
    extends PostgreSqlSuite
    with PostgreSql {
    override def initializeJson(connection: Connection): Unit = ()
  }

  abstract class Argonaut
    extends PostgreSqlSuite
    with PostgreSql
    with ArgonautSupport

  abstract class Json4s
    extends PostgreSqlSuite
      with PostgreSql
      with Json4sSupport

}
