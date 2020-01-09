/*
 * Copyright 2018-2020 47 Degrees, LLC. <http://www.47deg.com>
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

package higherkindness.compendium.core.doobie

import cats.instances.all._
import doobie.util.{Get, Meta, Put}
import higherkindness.compendium.core.refinements.{ProtocolId, ProtocolVersion}
import higherkindness.compendium.models.{IdlName, Protocol}

object implicits {

  implicit val protocolPut: Put[Protocol] = Put[Array[Byte]].contramap(_.raw.getBytes)
  implicit val protocolGet: Get[Protocol] =
    Get[Array[Byte]].tmap(protoBin => Protocol(new String(protoBin)))

  implicit val protocolIdPut: Put[ProtocolId] = Put[String].contramap(_.value)
  implicit val protocolIdGet: Get[ProtocolId] = Get[String].temap(ProtocolId.from)

  implicit val IdlNamesMeta: Meta[IdlName] = Meta[String].timap(IdlName.withName)(_.entryName)

  implicit val protocolVersionPut: Put[ProtocolVersion] = Put[Int].contramap(_.value)
  implicit val protocolVersionGet: Get[ProtocolVersion] = Get[Int].temap(ProtocolVersion.from)
}
