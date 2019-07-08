/*
 * Copyright 2018-2019 47 Degrees, LLC. <http://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package higherkindness.compendium.models

import enumeratum.EnumEntry.Lowercase
import enumeratum._

sealed trait IdlNames extends EnumEntry

object IdlNames extends Enum[IdlNames] with CirceEnum[IdlNames] {
  val values = findValues

  case object Avro     extends IdlNames with Lowercase
  case object Protobuf extends IdlNames with Lowercase
  case object Mu       extends IdlNames with Lowercase
  case object OpenApi  extends IdlNames with Lowercase
}
