package com.rocketfuel.sdbc.cassandra

import com.datastax.driver.core
import com.datastax.driver.core._
import com.google.common.reflect.TypeToken
import java.math.{BigDecimal, BigInteger}
import java.net.InetAddress
import java.nio.ByteBuffer
import java.util
import java.util.{Date, UUID}
import shapeless.HList
import shapeless.ops.hlist.{Mapper, ToTraversable}
import shapeless.ops.product.ToHList

case class TupleValue(underlying: core.TupleValue) extends core.GettableByIndexData {

  def apply[A](implicit compositeTupleGetter: CompositeTupleGetter[A]): A = {
    compositeTupleGetter(this, 0)
  }

  override def getUUID(i: Int): UUID = underlying.getUUID(i: Int)

  override def getVarint(i: Int): BigInteger = underlying.getVarint(i: Int)

  override def getInet(i: Int): InetAddress = underlying.getInet(i: Int)

  override def getList[T](i: Int, elementsClass: Class[T]): util.List[T] = underlying.getList[T](i: Int, elementsClass: Class[T])

  override def getDouble(i: Int): Double = underlying.getDouble(i: Int)

  override def getBytesUnsafe(i: Int): ByteBuffer = underlying.getBytesUnsafe(i: Int)

  override def getFloat(i: Int): Float = underlying.getFloat(i: Int)

  override def getLong(i: Int): Long = underlying.getLong(i: Int)

  override def getBool(i: Int): Boolean = underlying.getBool(i: Int)

  override def getMap[K, V](i: Int, keysClass: Class[K], valuesClass: Class[V]): util.Map[K, V] = underlying.getMap[K, V](i: Int, keysClass: Class[K], valuesClass: Class[V])

  override def getDecimal(i: Int): BigDecimal = underlying.getDecimal(i: Int)

  override def isNull(i: Int): Boolean = underlying.isNull(i: Int)

  override def getSet[T](i: Int, elementsClass: Class[T]): util.Set[T] = underlying.getSet[T](i: Int, elementsClass: Class[T])

  override def getDate(i: Int): LocalDate = underlying.getDate(i: Int)

  override def getInt(i: Int): Int = underlying.getInt(i: Int)

  override def getBytes(i: Int): ByteBuffer = underlying.getBytes(i: Int)

  override def getString(i: Int): String = underlying.getString(i: Int)

  override def getTupleValue(i: Int): core.TupleValue = underlying.getTupleValue(i: Int)

  override def getList[T](i: Int, elementsType: TypeToken[T]): util.List[T] = underlying.getList[T](i: Int, elementsType: TypeToken[T])

  override def getUDTValue(i: Int): UDTValue = underlying.getUDTValue(i: Int)

  override def getMap[K, V](i: Int, keysType: TypeToken[K], valuesType: TypeToken[V]): util.Map[K, V] = underlying.getMap[K, V](i: Int, keysType: TypeToken[K], valuesType: TypeToken[V])

  override def getObject(i: Int): AnyRef = underlying.getObject(i: Int)

  override def getSet[T](i: Int, elementsType: TypeToken[T]): util.Set[T] = underlying.getSet[T](i: Int, elementsType: TypeToken[T])

  override def getTimestamp(i: Int): Date = underlying.getTimestamp(i)

  override def get[T](i: Int, targetClass: Class[T]): T = underlying.get(i, targetClass)

  override def get[T](i: Int, targetType: TypeToken[T]): T = underlying.get(i, targetType)

  override def get[T](i: Int, codec: TypeCodec[T]): T = underlying.get(i, codec)

  override def getTime(i: Int): Long = underlying.getTime(i)

  override def getByte(i: Int): Byte = underlying.getByte(i)

  override def getShort(i: Int): Short = underlying.getShort(i)
}

object TupleValue {
  implicit def of(underlying: core.TupleValue): TupleValue = {
    apply(underlying)
  }

  implicit def hlistTupleValue[
    H <: HList,
    ListH <: HList,
    MappedTypesH <: HList,
    MappedValuesH <: HList
  ](h: H
  )(implicit dataTypeMapper: Mapper.Aux[TupleDataType.ToDataType.type, H, MappedTypesH],
    dataTypeList: ToTraversable.Aux[MappedTypesH, Seq, core.DataType],
    dataValueMapper: Mapper.Aux[TupleDataType.ToDataValue.type, H, MappedValuesH],
    dataValueList: ToTraversable.Aux[MappedValuesH, Seq, AnyRef]
  ): TupleValue = {
    val dataTypes = dataTypeList(h.map(TupleDataType.ToDataType))
    val dataValueHList = h.map(TupleDataType.ToDataValue)
    val dataValues = dataValueList(dataValueHList)
    val underlying = core.TupleType.of(ProtocolVersion.NEWEST_SUPPORTED, CodecRegistry.DEFAULT_INSTANCE, dataTypes: _*)
                     .newValue(dataValues: _*)
    TupleValue(underlying)
  }

  implicit def productTupleValue[
    P,
    H <: HList,
    ListH <: HList,
    MappedTypesH <: HList,
    MappedValuesH <: HList
  ](p: P
  )(implicit toHList: ToHList.Aux[P, H],
    dataTypeMapper: Mapper.Aux[TupleDataType.ToDataType.type, H, MappedTypesH],
    dataTypeList: ToTraversable.Aux[MappedTypesH, Seq, core.DataType],
    dataValueMapper: Mapper.Aux[TupleDataType.ToDataValue.type, H, MappedValuesH],
    dataValueList: ToTraversable.Aux[MappedValuesH, Seq, AnyRef]
  ): TupleValue = {
    val asH = toHList(p)
    val tv = hlistTupleValue(asH)
    tv
  }
}
