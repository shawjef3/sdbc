package com.rocketfuel.sdbc.base.jdbc

import com.rocketfuel.sdbc.base
import com.rocketfuel.sdbc.base.{ParameterValueImplicits, jdbc}
import com.zaxxer.hikari.HikariDataSource

abstract class DBMS
  extends IndexImplicits
  with HikariImplicits
  with ParameterValueImplicits
  with GetterImplicits
  with UpdaterImplicits
  with base.BatchableMethods[Connection, Batch]
  with base.UpdatableMethods[Connection, Update]
  with base.SelectableMethods[Connection, Select]
  with base.ExecutableMethods[Connection, Execute]
  with StringContextMethods {

  /**
   *
   */
  implicit val parameterSetter: ParameterSetter

  type Index = jdbc.Index

  type Getter[+T] = jdbc.Getter[T]

  type ParameterValue = jdbc.ParameterValue
  val ParameterValue = jdbc.ParameterValue

  type ParameterList = jdbc.ParameterList

  type Batchable[Key] = base.Batchable[Key, Connection, Batch]

  type Executable[Key] = jdbc.Executable[Key]

  type Selectable[Key, Value] = jdbc.Selectable[Key, Value]

  type Updatable[Key] = jdbc.Updatable[Key]

  type Select[T] = jdbc.Select[T]

  val Select = jdbc.Select

  type SelectForUpdate = jdbc.SelectForUpdate

  val SelectForUpdate = jdbc.SelectForUpdate

  type Update = jdbc.Update

  val Update = jdbc.Update

  type Batch = jdbc.Batch

  val Batch = jdbc.Batch

  type Execute = jdbc.Execute

  val Execute = jdbc.Execute

  type Pool = jdbc.Pool

  val Pool = jdbc.Pool

  type Connection = jdbc.Connection

  type Row = jdbc.Row

  type MutableRow = jdbc.MutableRow

  type UpdatableRow = jdbc.UpdatableRow

  type ImmutableRow = jdbc.ImmutableRow

  implicit def PoolToHikariPool(pool: Pool): HikariDataSource = {
    pool.underlying
  }

  implicit class ConnectionMethods(connection: Connection) {
    def iterator[T](
      queryText: String,
      parameters: (String, Option[ParameterValue])*
    )(implicit converter: MutableRow => T
    ): Iterator[T] = {
      Select[T](queryText).on(parameters: _*).iterator()(connection)
    }

    def iteratorForUpdate(
      queryText: String,
      parameters: (String, Option[ParameterValue])*
    ): Iterator[UpdatableRow] = {
      SelectForUpdate(queryText).on(parameters: _*).iterator()(connection)
    }

    def update(
      queryText: String,
      parameterValues: (String, Option[ParameterValue])*
    ): Long = {
      Update(queryText).on(parameterValues: _*).update()(connection)
    }

    def execute(
      queryText: String,
      parameterValues: (String, Option[ParameterValue])*
    ): Unit = {
      Execute(queryText).on(parameterValues: _*).execute()(connection)
    }
  }

  /**
   * Class name for the DataSource class.
   */
  def dataSourceClassName: String

  /**
   * Class name for the JDBC driver class.
   */
  def driverClassName: String

  def jdbcSchemes: Set[String]

  /**
   * The result of getMetaData.getDatabaseProductName
   */
  def productName: String

  /**
   * If the JDBC driver supports the .isValid() method.
   */
  def supportsIsValid: Boolean

  /**
   * Perform any connection initialization that should be done when a connection
   * is created. EG add a type mapping.
   *
   * By default this method does nothing.
   * @param connection
   */
  def initializeConnection(connection: java.sql.Connection): Unit = {

  }

  register(this)

}
