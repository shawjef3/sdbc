package com.wda.sdbc.sqlserver

import com.wda.sdbc.SqlServer._
import com.wda.sdbc.config.{SqlTestingConfig, TestingConfig}
import org.scalatest.{BeforeAndAfterAll, FunSuite}

class HasSqlServerPoolSpec
  extends FunSuite
  with HasSqlServerPool
  with TestingConfig
  with SqlTestingConfig
  with BeforeAndAfterAll {

  def testDatabaseExists(): Boolean = {
    withSqlMaster[Boolean] { implicit connection =>
      Select[Int]("SELECT CASE WHEN db_id($databaseName) IS NULL THEN 0 ELSE 1 END").on("databaseName" -> sqlTestCatalogName).single() == 1
    }
  }

  test("creates and destroys test database") {

    sqlBeforeAll()

    assert(testDatabaseExists())

    sqlDropTestCatalogs()

    assert(! testDatabaseExists())

  }

  override protected def afterAll(): Unit = {
    sqlMasterPool.shutdown()
  }
}
