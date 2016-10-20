package com.rocketfuel.sdbc.sqlserver.implementation

import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import com.rocketfuel.sdbc.base.CISet
import com.rocketfuel.sdbc.base.jdbc._
import java.time.ZoneOffset

/*
Note that in a result set, sql server (or jtds) doesn't do a good job of reporting the types
of values being delivered.

nvarchar could be:
string, date, time, datetime2, datetimeoffset

ntext could be:
string, xml

varbinary could be:
varbinary, hierarchyid
 */
private[sdbc] abstract class SqlServer
  extends DBMS
  with Getters
  with Updaters
  with Setters
  with MultiQuery
  with JdbcConnection {

  val offsetDateTimeFormatter =
    new DateTimeFormatterBuilder().
    parseCaseInsensitive().
    append(DateTimeFormatter.ISO_LOCAL_DATE).
    appendLiteral(' ').
    append(DateTimeFormatter.ISO_LOCAL_TIME).
    optionalStart().
    appendLiteral(' ').
    appendOffset("+HH:MM", "+00:00").
    optionalEnd().
    toFormatter()

  val instantFormatter =
    offsetDateTimeFormatter.
    withZone(ZoneOffset.UTC)

  override def dataSourceClassName ="net.sourceforge.jtds.jdbcx.JtdsDataSource"
  override def jdbcSchemes = CISet("jtds:sqlserver")
  override def productName: String = "Microsoft SQL Server"
  override val supportsIsValid = false

}
