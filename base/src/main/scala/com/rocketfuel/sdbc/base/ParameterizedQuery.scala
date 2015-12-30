package com.rocketfuel.sdbc.base

import shapeless._
import shapeless.record._
import shapeless.ops.record._
import shapeless.ops.hlist._

trait ParameterizedQuery {
  self: ParameterValue =>

  object ToParameterValue extends Poly {
    implicit def fromValue[A](implicit parameter: Parameter[A]) = {
      use {
        (value: A) =>
          ParameterValue.of[A](value)
      }
    }

    implicit def fromOptionalValue[A](implicit parameter: Parameter[A]) = {
      use {
        (value: Option[A]) =>
          ParameterValue.ofOption[A](value)
      }
    }

    implicit def fromSomeValue[A](implicit parameter: Parameter[A]) = {
      use {
        (value: Some[A]) =>
          ParameterValue.of[A](value.get)
      }
    }

    implicit def fromNone(implicit parameter: Parameter[None.type]) = {
      use {
        (value: None.type) =>
          ParameterValue.ofNone(value)
      }
    }
  }

  /**
    * Given a query with named parameters beginning with '@',
    * construct the query for use with JDBC, so that names
    * are replaced by '?', and each parameter
    * has a map to its positions in the query.
    *
    * Parameter names must start with a unicode letter or underscore, and then
    * any character after the first one can be a unicode letter, unicode number,
    * or underscore. A parameter that does not follow
    * this scheme must be quoted by backticks. Parameter names
    * are case sensitive.
    *
    * Examples of identifiers:
    *
    * {{{"@hello"}}}
    *
    * {{{"@`hello there`"}}}
    *
    * {{{"@_i_am_busy"}}}
    */
  trait ParameterizedQuery[Self <: ParameterizedQuery[Self]] {

    def statement: CompiledStatement

    def parameterValues: Map[String, ParameterValue]

    /**
      * The query text with name parameters replaced with positional parameters.
      * @return
      */
    def queryText: String = statement.queryText

    def originalQueryText: String = statement.originalQueryText

    def parameterPositions: Map[String, Set[Int]] = statement.parameterPositions

    private def setParameter(
      parameterValues: Map[String, ParameterValue],
      nameValuePair: (String, ParameterValue)
    ): Map[String, ParameterValue] = {
      if (parameterPositions.contains(nameValuePair._1)) {
        parameterValues + nameValuePair
      } else {
        throw new IllegalArgumentException(s"${nameValuePair._1} is not a parameter in the query.")
      }
    }

    protected def subclassConstructor(
      statement: CompiledStatement,
      parameterValues: Map[String, ParameterValue]
    ): Self

    def on(parameterValues: (String, ParameterValue)*): Self = {
      val newValues = setParameters(parameterValues: _*)
      subclassConstructor(statement, newValues)
    }

    protected def productParameters[
      A,
      Repr <: HList,
      ReprKeys <: HList,
      MappedRepr <: HList
    ](t: A
    )(implicit genericA: LabelledGeneric.Aux[A, Repr],
      keys: Keys.Aux[Repr, ReprKeys],
      valuesMapper: MapValues.Aux[ToParameterValue.type, Repr, MappedRepr],
      ktl: ToList[ReprKeys, Symbol],
      vtl: ToList[MappedRepr, ParameterValue]
    ): ParameterList = {
      val asGeneric = genericA.to(t)
      recordParameters(asGeneric)
    }

    def onProduct[
      A,
      Repr <: HList,
      MappedRepr <: HList,
      ReprKeys <: HList
    ](t: A
    )(implicit genericA: LabelledGeneric.Aux[A, Repr],
      keys: Keys.Aux[Repr, ReprKeys],
      valuesMapper: MapValues.Aux[ToParameterValue.type, Repr, MappedRepr],
      ktl: ToList[ReprKeys, Symbol],
      vtl: ToList[MappedRepr, ParameterValue]
    ): Self = {
      val newValues = setParameters(productParameters(t): _*)
      subclassConstructor(statement, newValues)
    }

    protected def recordParameters[
      Repr <: HList,
      ReprKeys <: HList,
      MappedRepr <: HList
    ](t: Repr
    )(implicit keys: Keys.Aux[Repr, ReprKeys],
      valuesMapper: MapValues.Aux[ToParameterValue.type, Repr, MappedRepr],
      ktl: ToList[ReprKeys, Symbol],
      vtl: ToList[MappedRepr, ParameterValue]
    ): ParameterList = {
      val mapped = t.mapValues(ToParameterValue)
      t.keys.toList.map(_.name) zip mapped.toList
    }

    def onRecord[
      Repr <: HList,
      ReprKeys <: HList,
      MappedRepr <: HList
    ](t: Repr
    )(implicit keys: Keys.Aux[Repr, ReprKeys],
      valuesMapper: MapValues.Aux[ToParameterValue.type, Repr, MappedRepr],
      ktl: ToList[ReprKeys, Symbol],
      vtl: ToList[MappedRepr, ParameterValue]
    ): Self = {
      val newValues = setParameters(recordParameters(t): _*)
      subclassConstructor(statement, newValues)
    }

    protected def setParameters(nameValuePairs: (String, ParameterValue)*): Map[String, ParameterValue] = {
      nameValuePairs.foldLeft(parameterValues)(setParameter)
    }

  }

}
