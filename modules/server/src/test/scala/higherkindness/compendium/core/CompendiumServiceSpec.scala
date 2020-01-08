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

package higherkindness.compendium.core

import cats.effect.IO
import cats.syntax.all._
import higherkindness.compendium.CompendiumArbitrary._
import higherkindness.compendium.metadata.MetadataStorageStub
import higherkindness.compendium.models.{IdlName, Protocol, ProtocolMetadata}
import higherkindness.compendium.storage.StorageStub
import higherkindness.compendium.transformer.skeuomorph.SkeuomorphProtocolTransformer
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

object CompendiumServiceSpec extends Specification with ScalaCheck {

  sequential

  private val dummyProtocol: Protocol = Protocol("")
  private val dummyIdlName: IdlName   = IdlName.Mu

  "Store protocol" >> {
    "Given a valid protocol it is stored" >> prop { metadata: ProtocolMetadata =>
      implicit val dbService     = new MetadataStorageStub(true)
      implicit val storage       = new StorageStub(dummyProtocol.some, metadata.id, metadata.version)
      implicit val protocolUtils = new ProtocolUtilsStub(dummyProtocol, true)
      implicit val transformer   = SkeuomorphProtocolTransformer[IO]

      CompendiumService
        .impl[IO]
        .storeProtocol(metadata.id, dummyProtocol, dummyIdlName)
        .map(_ => success)
        .unsafeRunSync()
    }

    "Given an invalid protocol an error is raised" >> prop { metadata: ProtocolMetadata =>
      implicit val dbService     = new MetadataStorageStub(true)
      implicit val storage       = new StorageStub(dummyProtocol.some, metadata.id, metadata.version)
      implicit val protocolUtils = new ProtocolUtilsStub(dummyProtocol, false)
      implicit val transformer   = SkeuomorphProtocolTransformer[IO]

      CompendiumService
        .impl[IO]
        .storeProtocol(metadata.id, dummyProtocol, dummyIdlName)
        .unsafeRunSync must throwA[org.apache.avro.SchemaParseException]
    }
  }

  "Retrieve protocol" >> {
    "Given a identifier the protocol is retrieved" >> prop { metadata: ProtocolMetadata =>
      implicit val dbService     = new MetadataStorageStub(true, metadata.some)
      implicit val storage       = new StorageStub(dummyProtocol.some, metadata.id, metadata.version)
      implicit val protocolUtils = new ProtocolUtilsStub(dummyProtocol, true)
      implicit val transformer   = SkeuomorphProtocolTransformer[IO]

      CompendiumService
        .impl[IO]
        .retrieveProtocol(metadata.id, metadata.version.some)
        .unsafeRunSync()
        .map(_.protocol) === dummyProtocol.some
    }
  }

  "Exists protocol" >> {
    "Given a identifier protocol existence is checked" >> prop { metadata: ProtocolMetadata =>
      implicit val dbService     = new MetadataStorageStub(true)
      implicit val storage       = new StorageStub(dummyProtocol.some, metadata.id, metadata.version)
      implicit val protocolUtils = new ProtocolUtilsStub(dummyProtocol, true)
      implicit val transformer   = SkeuomorphProtocolTransformer[IO]

      CompendiumService.impl[IO].existsProtocol(metadata.id).unsafeRunSync() should beTrue
    }
  }

}
