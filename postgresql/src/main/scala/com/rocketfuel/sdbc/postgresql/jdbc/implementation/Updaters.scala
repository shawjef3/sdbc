package com.rocketfuel.sdbc.postgresql.jdbc.implementation

import java.net.InetAddress
import java.time.{Duration => JavaDuration, _}
import com.rocketfuel.sdbc.base.jdbc._
import org.json4s._
import org.postgresql.util.PGobject
import scala.concurrent.duration.{Duration => ScalaDuration}
import scala.xml.Node

//PostgreSQL doesn't support Byte, so we don't use the default updaters.
trait Updaters
  extends AnyRefUpdater
  with LongUpdater
  with IntUpdater
  with ShortUpdater
  with BytesUpdater
  with DoubleUpdater
  with FloatUpdater
  with JavaBigDecimalUpdater
  with ScalaBigDecimalUpdater
  with TimestampUpdater
  with DateUpdater
  with TimeUpdater
  with BooleanUpdater
  with StringUpdater
  with UUIDUpdater
  with InputStreamUpdater
  with UpdateReader
  with LocalDateTimeUpdater
  with InstantUpdater
  with LocalDateUpdater
  with LocalTimeUpdater {
  self: PGTimestampTzImplicits
    with PGTimeTzImplicits
    with IntervalImplicits
    with PGInetAddressImplicits
    with PGJsonImplicits =>

  private def IsPGobjectUpdater[T](implicit converter: T => PGobject): Updater[T] = {
    new Updater[T] {
      override def update(row: UpdatableRow, columnIndex: Int, x: T): Unit = {
        PGobjectUpdater.update(row, columnIndex, converter(x))
      }
    }
  }

  implicit val OffsetTimeUpdater = IsPGobjectUpdater[OffsetTime]

  implicit val OffsetDateTimeUpdater = IsPGobjectUpdater[OffsetDateTime]

  implicit val ScalaDurationUpdater = IsPGobjectUpdater[ScalaDuration]

  implicit val JavaDurationUpdater = IsPGobjectUpdater[JavaDuration]

  implicit val JValueUpdater = IsPGobjectUpdater[JValue]

  implicit val InetAddressUpdater = IsPGobjectUpdater[InetAddress]

  implicit val PGobjectUpdater = new Updater[PGobject] {
    override def update(
      row: UpdatableRow,
      columnIndex: Int,
      x: PGobject
    ): Unit = {
      row.updateObject(columnIndex, x)
    }
  }

  implicit val XmlUpdater = new Updater[Node] {
    override def update(
      row: UpdatableRow,
      columnIndex: Int,
      x: Node
    ): Unit = {
      row.updateString(columnIndex, x.toString)
    }
  }

  implicit val MapUpdater = new Updater[Map[String, String]] {
    override def update(row: UpdatableRow, columnIndex: Int, x: Map[String, String]): Unit = {
      import scala.collection.convert.decorateAsJava._
      row.updateObject(columnIndex, x.asJava)
    }
  }

}
