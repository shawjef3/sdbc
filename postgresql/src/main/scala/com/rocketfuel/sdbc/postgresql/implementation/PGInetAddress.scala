package com.rocketfuel.sdbc.postgresql.implementation

import com.rocketfuel.sdbc.base.jdbc.statement.ParameterValue
import java.net.InetAddress
import org.postgresql.util.PGobject

private[sdbc] class PGInetAddress(
  var inetAddress: Option[InetAddress]
) extends PGobject() {

  def this() {
    this(None)
  }

  setType("inet")

  override def getValue: String = {
    inetAddress.map(_.getHostAddress).
      getOrElse(throw new IllegalStateException("setValue must be called first"))
  }

  override def setValue(value: String): Unit = {
    inetAddress = Option(value).map(InetAddress.getByName)
  }

  override def equals(obj: scala.Any): Boolean = {
    obj match {
      case pg: PGInetAddress =>
        pg.inetAddress == this.inetAddress
      case _ => false
    }
  }

  override def hashCode(): Int = {
    inetAddress.hashCode()
  }
}

private[sdbc] object PGInetAddress {
  implicit def apply(address: InetAddress): PGInetAddress = {
    new PGInetAddress(inetAddress = Some(address))
  }
}

private[sdbc] trait InetAddressParameter {
  self: ParameterValue =>

  implicit object InetAddressParameter extends Parameter[InetAddress] {
    override val set: (InetAddress) => (PreparedStatement, Int) => PreparedStatement = {
      address => (statement, ix) =>
        val pgAddress = PGInetAddress(address)
        statement.setObject(ix + 1, pgAddress)
        statement
    }
  }

}