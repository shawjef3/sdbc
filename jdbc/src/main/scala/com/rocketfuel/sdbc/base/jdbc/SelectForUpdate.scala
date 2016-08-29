package com.rocketfuel.sdbc.base.jdbc

import com.rocketfuel.sdbc.base.Logging

trait SelectForUpdate {
  self: DBMS =>

  case class SelectForUpdate(
    override val statement: CompiledStatement,
    override val parameterValues: Map[String, ParameterValue] = Map.empty
  ) extends ParameterizedQuery[SelectForUpdate]
    with Executes {

    override def subclassConstructor(parameterValues: Map[String, ParameterValue]): SelectForUpdate = {
      copy(parameterValues = parameterValues)
    }

    def iterator()(implicit connection: Connection): CloseableIterator[UpdatableRow] = {
      SelectForUpdate.iterator(statement, parameterValues)
    }

    def execute()(implicit connection: Connection): Unit = {
      Execute.execute(statement, parameterValues)
    }

  }

  object SelectForUpdate
    extends Logging {

    def apply(
      queryText: String
    ): SelectForUpdate = {
      SelectForUpdate(
        statement = CompiledStatement(queryText),
        parameterValues = Map.empty[String, ParameterValue]
      )
    }

    /**
      * Construct the query without finding named parameters. No escaping will
      * need to be performed for a literal '@' to appear in the query. You will
      * not be able to use parameters when running this query.
      *
      * @param queryText
      * @tparam A
      * @return
      */
    def literal[A](
      queryText: String
    ): SelectForUpdate = {
      SelectForUpdate(
        statement = CompiledStatement.literal(queryText),
        parameterValues = Map.empty[String, ParameterValue]
      )
    }

    def iterator(
      queryText: String,
      parameterValues: Map[String, ParameterValue]
    )(implicit connection: Connection
    ): CloseableIterator[UpdatableRow] = {
      val statement = CompiledStatement(queryText)
      logRun(statement, parameterValues)
      iterator(statement, parameterValues)
    }

    def iterator[A](
      statement: CompiledStatement,
      parameterValues: Map[String, ParameterValue]
    )(implicit connection: Connection
    ): CloseableIterator[UpdatableRow] = {
      val executed = QueryMethods.executeForUpdate(statement, parameterValues)
      StatementConverter.updatableResults(executed)
    }

    private def logRun(
      compiledStatement: CompiledStatement,
      parameters: Map[String, ParameterValue]
    ): Unit = {
      logger.debug(s"""Selecting for update "${compiledStatement.originalQueryText}" with parameters $parameters.""")
    }

  }

}
