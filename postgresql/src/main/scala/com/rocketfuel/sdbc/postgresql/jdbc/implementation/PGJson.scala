package com.rocketfuel.sdbc.postgresql.jdbc.implementation

import java.sql.SQLException

import org.json4s.jackson.JsonMethods
import org.json4s.JValue
import org.postgresql.util.PGobject

class PGJson() extends PGobject() {

  var jValue: Option[JValue] = None

  override def getValue: String = {
    jValue.map(j => JsonMethods.compact(JsonMethods.render(j))).orNull
  }

  override def setValue(value: String): Unit = {
    this.jValue = for {
      reallyValue <- Option(value)
    } yield {
        //PostgreSQL uses numeric (i.e. BigDecimal) for json numbers
        //http://www.postgresql.org/docs/9.4/static/datatype-json.html
        JsonMethods.parse(reallyValue, useBigDecimalForDouble = true)
      }
  }

}

object PGJson {
  def apply(j: JValue): PGJson = {
    val p = new PGJson()
    p.jValue = Some(j)

    p
  }
}

trait PGJsonImplicits {

  implicit def JValueToPGJson(j: JValue): PGJson = {
    PGJson(j)
  }

  implicit def PGobjectToJValue(x: PGobject): JValue = {
    x match {
      case p: PGJson =>
        p.jValue.get
      case _ =>
        throw new SQLException("column does not contain a json")
    }
  }

}