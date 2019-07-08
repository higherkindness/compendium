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

import cats.effect.IO
import cats.syntax.option._
import higherkindness.compendium.CompendiumArbitrary._
import higherkindness.compendium.core.refinements.ProtocolId
import higherkindness.compendium.db.DBServiceStub
import higherkindness.compendium.models.{IdlNames, MetaProtocolDB, Protocol}
import higherkindness.compendium.parser.ProtocolParser
import higherkindness.compendium.storage.StorageStub
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

object CompendiumServiceSpec extends Specification with ScalaCheck {

  sequential

  private val dummyProtocol: Protocol = Protocol("")
  private val dummyIdlName: IdlNames  = IdlNames.Mu

  "Store protocol" >> {
    "If it's a valid protocol we store it" >> prop { id: ProtocolId =>
      implicit val dbService          = new DBServiceStub(true)
      implicit val storage            = new StorageStub(Some(dummyProtocol), id)
      implicit val protocolUtils      = new ProtocolUtilsStub(dummyProtocol, true)
      implicit val protoParserService = ProtocolParser.impl[IO]

      CompendiumService
        .impl[IO]
        .storeProtocol(id, dummyProtocol, dummyIdlName)
        .map(_ => success)
        .unsafeRunSync()
    }

    "If it's an invalid protocol we raise an error" >> prop { id: ProtocolId =>
      implicit val dbService          = new DBServiceStub(true)
      implicit val storage            = new StorageStub(Some(dummyProtocol), id)
      implicit val protocolUtils      = new ProtocolUtilsStub(dummyProtocol, false)
      implicit val protoParserService = ProtocolParser.impl[IO]

      CompendiumService
        .impl[IO]
        .storeProtocol(id, dummyProtocol, dummyIdlName)
        .unsafeRunSync must throwA[org.apache.avro.SchemaParseException]
    }
  }

  "Recover protocol" >> {
    "Given a identifier we recover the protocol" >> prop { id: ProtocolId =>
      val MetaProtocol                = MetaProtocolDB(IdlNames.Avro, id.value)
      implicit val dbService          = new DBServiceStub(true, MetaProtocol.some)
      implicit val storage            = new StorageStub(Some(dummyProtocol), id)
      implicit val protocolUtils      = new ProtocolUtilsStub(dummyProtocol, true)
      implicit val protoParserService = ProtocolParser.impl[IO]

      CompendiumService.impl[IO].recoverProtocol(id).unsafeRunSync().map(_.protocol) === Some(
        dummyProtocol)
    }
  }

  "Exists protocol" >> {
    "Given a identifier we check if a protocol exists" >> prop { id: ProtocolId =>
      implicit val dbService          = new DBServiceStub(true)
      implicit val storage            = new StorageStub(Some(dummyProtocol), id)
      implicit val protocolUtils      = new ProtocolUtilsStub(dummyProtocol, true)
      implicit val protoParserService = ProtocolParser.impl[IO]

      CompendiumService.impl[IO].existsProtocol(id).unsafeRunSync() should beTrue
    }
  }

}
