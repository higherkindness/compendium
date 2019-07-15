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

import cats.effect.IO
import cats.implicits._
import higherkindness.compendium.core.refinements.ProtocolId
import higherkindness.compendium.models.IdlName

class PgDBServiceSpec extends PGHelper {

  private lazy val pg = PgDBService.impl[IO](transactor)

  "Postgres Service" should {
    "insert protocol correctly" in {
      val id: ProtocolId   = ProtocolId("pId")
      val idlName: IdlName = IdlName.Avro

      val result: IO[Boolean] =
        pg.upsertProtocol(id, idlName) >> pg.existsProtocol(id)

      result.unsafeRunSync must ===(true)

    }

    "update protocol correctly" in {
      val id: ProtocolId   = ProtocolId("pId2")
      val idlName: IdlName = IdlName.Avro

      val result: IO[Boolean] =
        pg.upsertProtocol(id, idlName) >> pg.upsertProtocol(id, idlName) >> pg.existsProtocol(id)

      result.unsafeRunSync must ===(true)
    }

    "return false when the protocol does not exist" in {
      val id: ProtocolId = ProtocolId("p")

      pg.existsProtocol(id).unsafeRunSync must ===(false)
    }

    "return true when the protocol exists" in {
      val id: ProtocolId   = ProtocolId("pId3")
      val idlName: IdlName = IdlName.Avro

      val result: IO[Boolean] =
        pg.upsertProtocol(id, idlName) >> pg.existsProtocol(id)

      result.unsafeRunSync must ===(true)
    }

  }

}
