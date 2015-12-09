package com.rocketfuel.sdbc.cassandra

import com.datastax.driver.core
import com.google.common.util.concurrent.{Futures, FutureCallback, ListenableFuture}
import com.rocketfuel.sdbc.base

import scala.concurrent.{Promise, Future, ExecutionContext}

package object implementation
  extends Index {

  type ParameterValue = base.ParameterValue
  val ParameterValue = base.ParameterValue

  type ParameterList = Seq[(String, ParameterValue)]

  type ParameterizedQuery[Self <: ParameterizedQuery[Self, Setters], Setters] = base.ParameterizedQuery[Self, Setters]

  type Session = core.Session

  type Cluster = core.Cluster

  type Executable[Key] = base.Executable[Key, Session, Execute]

  type Selectable[Key, Value] = base.Selectable[Key, Value, Session, Select[Value]]

  private [sdbc] def prepare(
    query: com.rocketfuel.sdbc.cassandra.implementation.ParameterizedQuery[_],
    queryOptions: QueryOptions
  )(implicit session: Session
  ): core.BoundStatement = {
    val prepared = session.prepare(query.queryText)

    bind(
      query,
      queryOptions,
      prepared
    )
  }

  private [sdbc] def bind(
    query: com.rocketfuel.sdbc.cassandra.implementation.ParameterizedQuery[_],
    queryOptions: QueryOptions,
    statement: core.PreparedStatement
  ): core.BoundStatement = {
    val forBinding = statement.bind()

    for ((key, maybeValue) <- query.parameterValues) {
      val parameterIndices = query.parameterPositions(key)

      maybeValue match {
        case None =>
          for (parameterIndex <- parameterIndices) {
            ParameterSetter.setNone(forBinding, parameterIndex)
          }
        case Some(value) =>
          for (parameterIndex <- parameterIndices) {
            ParameterSetter.setAny(forBinding, parameterIndex, value)
          }
      }
    }

    forBinding.setConsistencyLevel(queryOptions.consistencyLevel)
    forBinding.setSerialConsistencyLevel(queryOptions.serialConsistencyLevel)
    queryOptions.defaultTimestamp.map(forBinding.setDefaultTimestamp)
    forBinding.setFetchSize(queryOptions.fetchSize)
    forBinding.setIdempotent(queryOptions.idempotent)
    forBinding.setRetryPolicy(queryOptions.retryPolicy)

    if (queryOptions.tracing) {
      forBinding.enableTracing()
    } else {
      forBinding.disableTracing()
    }

    forBinding
  }

  private [sdbc] def toScalaFuture[T](f: ListenableFuture[T])(implicit ec: ExecutionContext): Future[T] = {
    //Thanks http://stackoverflow.com/questions/18026601/listenablefuture-to-scala-future
    val p = Promise[T]()

    val pCallback = new FutureCallback[T] {
      override def onFailure(t: Throwable): Unit = {
        p.failure(t)
      }

      override def onSuccess(result: T): Unit = {
        p.success(result)
      }
    }

    Futures.addCallback(f, pCallback)

    p.future
  }
}
