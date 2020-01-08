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

package higherkindness.compendium.metadata.pg

import cats.effect.IO
import cats.implicits._
import higherkindness.compendium.core.refinements.{ProtocolId, ProtocolVersion}
import higherkindness.compendium.metadata.MigrationsMode.Metadata
import higherkindness.compendium.metadata.PGHelper
import higherkindness.compendium.models.{IdlName, ProtocolMetadata}

class PgMetadataStorageSpec extends PGHelper(Metadata) {

  private lazy val pg = PgMetadataStorage[IO](transactor)

  "Postgres Service" should {
    "insert protocol correctly" in {
      val id: ProtocolId   = ProtocolId("pId")
      val idlName: IdlName = IdlName.Avro

      val result: IO[ProtocolMetadata] =
        pg.store(id, idlName) >> pg.retrieve(id)

      val expected = ProtocolMetadata(id, idlName, ProtocolVersion(1))

      result.unsafeRunSync must_=== expected

    }

    "update protocol correctly" in {
      val id: ProtocolId   = ProtocolId("pId2")
      val idlName: IdlName = IdlName.Avro

      val result: IO[ProtocolMetadata] =
        pg.store(id, idlName) >> pg.store(id, idlName) >> pg
          .retrieve(id)

      val expected = ProtocolMetadata(id, idlName, ProtocolVersion(2))

      result.unsafeRunSync must_=== expected
    }

    "return false when the protocol does not exist" in {
      val id: ProtocolId = ProtocolId("p")

      pg.exists(id).unsafeRunSync must ===(false)
    }

    "return true when the protocol exists" in {
      val id: ProtocolId   = ProtocolId("pId3")
      val idlName: IdlName = IdlName.Avro

      val result: IO[Boolean] =
        pg.store(id, idlName) >> pg.exists(id)

      result.unsafeRunSync must_=== true
    }

  }

}
