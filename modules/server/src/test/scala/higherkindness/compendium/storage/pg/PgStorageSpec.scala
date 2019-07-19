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

import cats.effect.IO
import cats.implicits._
import higherkindness.compendium.core.refinements.ProtocolId
import higherkindness.compendium.db.MigrationsMode.Data
import higherkindness.compendium.db.PGHelper
import higherkindness.compendium.models.{FullProtocol, IdlName, Protocol, ProtocolMetadata}

class PgStorageSpec extends PGHelper(Data) {

  private lazy val pgStorage = PgStorage[IO](transactor)

  "Postgres Storage" should {

    "insert protocol correctly" in {
      val id        = ProtocolId("p1")
      val metadata  = ProtocolMetadata(id, IdlName.Avro)
      val proto     = Protocol("the new protocol content")
      val fullProto = FullProtocol(metadata, proto)

      val result: IO[Option[FullProtocol]] = pgStorage.store(id, proto) >> pgStorage.recover(
        metadata)

      result.unsafeRunSync must ===(fullProto.some)

    }

    "update protocol correctly" in {
      val id        = ProtocolId("proto1")
      val metadata  = ProtocolMetadata(id, IdlName.Mu)
      val proto1    = Protocol("The protocol one content")
      val proto2    = Protocol("The protocol two content")
      val fullProto = FullProtocol(metadata, proto2)

      val result: IO[Option[FullProtocol]] = pgStorage.store(id, proto1) >> pgStorage.store(
        id,
        proto2) >> pgStorage
        .recover(metadata)

      result.unsafeRunSync must ===(fullProto.some)
    }

    "return false when the protocol does not exist" in {
      val id: ProtocolId = ProtocolId("p")

      pgStorage.exists(id).unsafeRunSync must ===(false)
    }

    "return true when the protocol exists" in {
      val id    = ProtocolId("pId3")
      val proto = Protocol("Another protocol")

      val result: IO[Boolean] = pgStorage.store(id, proto) >> pgStorage.exists(id)

      result.unsafeRunSync must ===(true)
    }

  }

}
