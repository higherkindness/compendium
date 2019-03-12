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
import higherkindness.compendium.db.DBServiceStub
import higherkindness.compendium.models.Protocol
import higherkindness.compendium.storage.StorageStub
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

object CompendiumServiceSpec extends Specification with ScalaCheck {

  sequential

  private val dummyProtocol: Protocol = Protocol("")

  "Store protocol" >> {
    "If it's a valid protocol we store it" >> prop { id: String =>
      implicit val dbService     = new DBServiceStub(true)
      implicit val storage       = new StorageStub(Some(dummyProtocol), id)
      implicit val protocolUtils = new ProtocolUtilsStub(dummyProtocol, true)

      CompendiumService.impl[IO].storeProtocol(id, dummyProtocol).map(_ => success).unsafeRunSync()
    }

    "If it's an invalid protocol we raise an error" >> prop { id: String =>
      implicit val dbService     = new DBServiceStub(true)
      implicit val storage       = new StorageStub(Some(dummyProtocol), id)
      implicit val protocolUtils = new ProtocolUtilsStub(dummyProtocol, false)

      CompendiumService
        .impl[IO]
        .storeProtocol(id, dummyProtocol)
        .unsafeRunSync must throwA[org.apache.avro.SchemaParseException]
    }
  }

  "Recover protocol" >> {
    "Given a identifier we recover the protocol" >> prop { id: String =>
      implicit val dbService     = new DBServiceStub(true)
      implicit val storage       = new StorageStub(Some(dummyProtocol), id)
      implicit val protocolUtils = new ProtocolUtilsStub(dummyProtocol, true)

      CompendiumService.impl[IO].recoverProtocol(id).unsafeRunSync() === Some(dummyProtocol)
    }
  }

  "Exists protocol" >> {
    "Given a identifier we check if a protocol exists" >> prop { id: String =>
      implicit val dbService     = new DBServiceStub(true)
      implicit val storage       = new StorageStub(Some(dummyProtocol), id)
      implicit val protocolUtils = new ProtocolUtilsStub(dummyProtocol, true)

      CompendiumService.impl[IO].existsProtocol(id).unsafeRunSync() should beTrue
    }
  }

}
