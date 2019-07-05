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

import doobie.util.Meta
import higherkindness.compendium.core.refinements.ProtocolId
import higherkindness.compendium.models.Protocol

object implicits {

  implicit val protocolMeta: Meta[Protocol] =
    Meta[Array[Byte]].timap[Protocol](binProto => Protocol(new String(binProto)))(_.raw.getBytes)

  // TODO Blows up when retrieving an invalid protocol id
  implicit val protocolIdMeta: Meta[ProtocolId] =
    Meta[String].timap[ProtocolId](ProtocolId.from(_).right.get)(_.value)

}
