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

package higherkindness.compendium.core

import cats.effect.Sync
import cats.implicits._
import higherkindness.compendium.db.DBService
import higherkindness.compendium.models.Protocol
import higherkindness.compendium.storage.Storage

trait CompendiumService[F[_]] {

  def storeProtocol(id: String, protocol: Protocol): F[Unit]
  def recoverProtocol(id: String): F[Option[Protocol]]
}

object CompendiumService {

  def impl[F[_]: Sync: Storage: DBService: ProtocolUtils] = new CompendiumService[F] {

    val utils = ProtocolUtils[F]

    override def storeProtocol(id: String, protocol: Protocol): F[Unit] =
      ProtocolUtils[F].validateProtocol(protocol) >>
        DBService[F].addProtocol(id, protocol) >>
        Storage[F].store(id, protocol)

    override def recoverProtocol(protocolId: String): F[Option[Protocol]] =
      Storage[F].recover(protocolId)
  }

  def apply[F[_]](implicit F: CompendiumService[F]): CompendiumService[F] = F
}