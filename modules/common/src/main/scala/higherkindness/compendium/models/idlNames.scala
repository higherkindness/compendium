package higherkindness.compendium.models

import enumeratum.{EnumEntry, _}

sealed trait IdlNames extends EnumEntry

object IdlNames extends Enum[IdlNames] with CirceEnum[IdlNames] {
  val values = findValues

  case object Avro extends IdlNames
  case object Protobuf extends IdlNames
  case object Mu extends IdlNames
  case object OpenApi extends IdlNames
}
