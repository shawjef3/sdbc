package com.rocketfuel.sdbc.base.jdbc

import com.typesafe.config.Config
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}

trait Pool {
  self: DBMS with Connection =>

  class Pool(configuration: HikariConfig) {

    //Set the test query if the driver doesn't support .isValid().
    if (!self.supportsIsValid) {
      configuration.setConnectionTestQuery("SELECT 1")
    }

    val underlying = new HikariDataSource(configuration)

    def getConnection(): Connection = {
      new Connection(underlying.getConnection())
    }

    def withConnection[T](f: Connection => T): T = {
      val connection = getConnection()
      try {
        f(connection)
      } finally {
        connection.close()
      }
    }

    def withTransaction[T](f: Connection => T): T = {
      withConnection[T] { connection =>
        connection.setAutoCommit(false)
        val result = f(connection)
        connection.commit()
        result
      }
    }

  }

  object Pool {
    def apply(config: Config): Pool = {
      new Pool(config.toHikariConfig)
    }
  }

  implicit def poolToHikariDataSource(pool: Pool): HikariDataSource = {
    pool.underlying
  }

}
