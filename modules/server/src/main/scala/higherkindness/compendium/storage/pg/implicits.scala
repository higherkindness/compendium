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

package higherkindness.compendium.storage.pg

import cats.instances.string._
import doobie.util.{Get, Put}
import higherkindness.compendium.core.refinements.ProtocolId
import higherkindness.compendium.models.Protocol

object implicits {

  implicit val protocolPut: Put[Protocol] = Put[Array[Byte]].contramap(_.raw.getBytes)
  implicit val protocolGet: Get[Protocol] =
    Get[Array[Byte]].tmap(protoBin => Protocol(new String(protoBin)))

  implicit val protocolIdPut: Put[ProtocolId] = Put[String].contramap(_.value)
  implicit val protocolIdGet: Get[ProtocolId] = Get[String].temap(ProtocolId.from)
}
