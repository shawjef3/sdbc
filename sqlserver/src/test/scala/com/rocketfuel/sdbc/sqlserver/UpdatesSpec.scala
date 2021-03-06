package com.rocketfuel.sdbc.sqlserver

import java.sql.{Date, Time, Timestamp}
import java.time.{Instant, LocalDate, LocalTime}
import java.util.UUID
import scodec.bits.ByteVector
import com.rocketfuel.sdbc.SqlServer._

import scala.reflect.ClassTag

class UpdatesSpec extends SqlServerSuite {

  def testUpdate[T](
    typeName: String
  )(before: T
  )(after: T
  )(implicit ctag: ClassTag[T],
    updater: Updater[T],
    setter: T => ParameterValue,
    converter: RowConverter[T]
  ): Unit = {
    test(s"Update ${ctag.runtimeClass.getName}") {implicit connection =>
      Ignore.ignore(s"CREATE TABLE tbl (id int identity PRIMARY KEY, v $typeName)")

      Ignore("INSERT INTO tbl (v) VALUES (@before)").on("before" -> before).ignore()

      def updateRow(row: UpdatableRow): Unit = {
        row("v") = after
        row.updateRow()
      }

      val summary = selectForUpdate"SELECT * FROM tbl".copy(rowUpdater = updateRow).update()

      assertResult(UpdatableRow.Summary(updatedRows = 1))(summary)

      val maybeValue = Select[T]("SELECT v FROM tbl").option()

      assert(maybeValue.nonEmpty)

      assertResult(Some(after))(maybeValue)
    }
  }

  testUpdate[Long]("bigint")(1L)(2L)

  testUpdate[Int]("int")(1)(2)

  testUpdate[Short]("smallint")(1.toShort)(2.toShort)

  testUpdate[Byte]("tinyint")(1.toByte)(2.toByte)

  testUpdate[Double]("float")(1.0)(2.0)

  testUpdate[Float]("real")(1.0F)(2.0F)

  testUpdate[java.lang.Long]("bigint")(1L)(2L)

  testUpdate[java.lang.Integer]("int")(1)(2)

  testUpdate[java.lang.Short]("smallint")(1.toShort)(2.toShort)

  testUpdate[java.lang.Byte]("tinyint")(1.toByte)(2.toByte)

  testUpdate[java.lang.Double]("float")(1.0)(2.0)

  testUpdate[java.lang.Float]("real")(1.0F)(2.0F)

  testUpdate[ByteVector]("varbinary(max)")(ByteVector(1, 2, 3))(ByteVector(4, 5, 6))

  testUpdate[BigDecimal]("decimal")(BigDecimal(3))(BigDecimal("500"))

  testUpdate[Date]("date")(new Date(0))(Date.valueOf(LocalDate.now()))

  testUpdate[Time]("time")(new Time(0))(Time.valueOf(LocalTime.now()))

  testUpdate[LocalDate]("date")(LocalDate.ofEpochDay(0))(LocalDate.now())

  testUpdate[LocalTime]("time")(LocalTime.of(0, 0, 0))(LocalTime.now())

  testUpdate[Boolean]("bit")(false)(true)

  testUpdate[String]("varchar(max)")("hi")("bye")

  testUpdate[UUID]("uniqueidentifier")(UUID.randomUUID())(UUID.randomUUID())

  /**
    * JTDS returns a value with a precision of about 4 ms,
    * so we can't use straight equality.
    *
    * http://sourceforge.net/p/jtds/feature-requests/73/
    */
  test("Update java.sql.Timestamp") {implicit connection =>
    Ignore.ignore(s"CREATE TABLE tbl (id int identity PRIMARY KEY, v datetime2)")

    update"INSERT INTO tbl (v) VALUES (${new Timestamp(0)})".update()

    val after = Timestamp.from(Instant.now())

    def updateRow(row: UpdatableRow): Unit = {
      row("v") = after
      row.updateRow()
    }

    val summary = selectForUpdate"SELECT v FROM tbl".copy(rowUpdater = updateRow).update()

    assertResult(UpdatableRow.Summary(updatedRows = 1))(summary)

    val values = Select.vector[Timestamp]("SELECT v FROM tbl")

    assert(values.nonEmpty)

    assert(Math.abs(values.head.getTime - after.getTime) < 5)
  }

  test("Update java.time.Instant") {implicit connection =>
    Ignore.ignore(s"CREATE TABLE tbl (id int identity PRIMARY KEY, v datetime2)")

    update"INSERT INTO tbl (v) VALUES (${Instant.ofEpochMilli(0)})".update()

    val after = Instant.now()

    def updateRow(row: UpdatableRow): Unit = {
      row("v") = after
      row.updateRow()
    }

    val summary = selectForUpdate"SELECT v FROM tbl".copy(rowUpdater = updateRow).update()

    assertResult(UpdatableRow.Summary(updatedRows = 1))(summary)

    val maybeValue = Select[Instant]("SELECT v FROM tbl").option()

    assert(maybeValue.nonEmpty)

    /*
     * JTDS returns a value with a precision of about 4 ms,
     * so we can't use equality.
     *
     * http://sourceforge.net/p/jtds/feature-requests/73/
     */
    assert(Math.abs(maybeValue.get.toEpochMilli - after.toEpochMilli) < 5)
  }

  test(s"Update HierarchyId") {implicit connection =>
    val before = HierarchyId()
    val after = HierarchyId(1, 2)

    Ignore.ignore(s"CREATE TABLE tbl (id int identity PRIMARY KEY, v hierarchyid)")

    update"INSERT INTO tbl (v) VALUES ($before)".update()

    def updateRow(row: UpdatableRow): Unit = {
      row("v") = after
      row.updateRow()
    }

    val summary = selectForUpdate"SELECT id, v FROM tbl".copy(rowUpdater = updateRow).update()

    assertResult(UpdatableRow.Summary(updatedRows = 1))(summary)

    val maybeValue = Select[HierarchyId]("SELECT v.ToString() FROM tbl").option()

    assert(maybeValue.nonEmpty)

    assertResult(Some(after))(maybeValue)
  }

  test(s"Update None") {implicit connection =>
    val before = Some(1)
    val after = None

    Ignore.ignore(s"CREATE TABLE tbl (id int identity PRIMARY KEY, v int)")

    update"INSERT INTO tbl (v) VALUES ($before)".update()

    def updateRow(row: UpdatableRow): Unit = {
      row("v") = after
      row.updateRow()
    }

    val summary = selectForUpdate"SELECT v FROM tbl".copy(rowUpdater = updateRow).update()

    assertResult(UpdatableRow.Summary(updatedRows = 1))(summary)

    val maybeRow = Select[Option[Int]]("SELECT v FROM tbl").option()

    assert(maybeRow.nonEmpty, "There was a row")

    val maybeValue = maybeRow.get

    assert(maybeValue.isEmpty, "There was an unexpected value.")
  }

}
