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

import cats.effect.Async
import cats.implicits._
import doobie.util.transactor.Transactor
import doobie.implicits._
import higherkindness.compendium.core.refinements.ProtocolId
import higherkindness.compendium.db.queries.Queries
import higherkindness.compendium.models.{IdlNames, MetaProtocolDB}

object PgDBService {

  def impl[F[_]: Async](xa: Transactor[F]): DBService[F] =
    new DBService[F] {

      override def upsertProtocol(id: ProtocolId, idlName: IdlNames): F[Unit] =
        Queries.upsertProtocolIdQ(id.value, idlName.entryName).run.void.transact(xa)

      override def existsProtocol(id: ProtocolId): F[Boolean] =
        Queries.checkIfExistsQ(id.value).unique.transact(xa)

      override def selectProtocolById(id: ProtocolId): F[MetaProtocolDB] =
        Queries.selectProtocolById(id.value).unique.transact(xa)

      override def ping(): F[Boolean] = Queries.checkConnection().unique.transact(xa)
    }
}
