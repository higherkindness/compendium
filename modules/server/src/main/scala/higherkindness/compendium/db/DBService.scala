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

package higherkindness.compendium.db

import higherkindness.compendium.models.{IdlNames, MetaProtocolDB}
import higherkindness.compendium.core.refinements.ProtocolId

trait DBService[F[_]] {
  def upsertProtocol(id: ProtocolId, idlName: IdlNames): F[Unit]
  def existsProtocol(id: ProtocolId): F[Boolean]
  def selectProtocolById(id: ProtocolId): F[MetaProtocolDB]
  def ping(): F[Boolean]
}

object DBService {
  def apply[F[_]](implicit F: DBService[F]): DBService[F] = F
}
