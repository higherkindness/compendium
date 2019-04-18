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
import higherkindness.compendium.CompendiumArbitrary._
import higherkindness.compendium.DifferentIdentifiers
import higherkindness.compendium.models.Protocol
import higherkindness.compendium.storage.{Storage, StorageStub}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

object FileDBServiceSpec extends Specification with ScalaCheck {

  sequential

  private val dummyProtocol: Protocol = Protocol("")

  "File DBService upsert" >> {
    "Returns always unit" >> prop { id: String =>
      implicit val storage: Storage[IO] = new StorageStub(Some(dummyProtocol), id)
      val dbService: DBService[IO]      = FileDBService.impl[IO]

      dbService.upsertProtocol(id).unsafeRunSync() must not(throwA[Exception])
    }
  }

  "File DBService exists" >> {
    "If the protocol exists returns true" >> prop { id: String =>
      implicit val storage: Storage[IO] = new StorageStub(Some(dummyProtocol), id)
      val dbService: DBService[IO]      = FileDBService.impl[IO]

      dbService.existsProtocol(id).unsafeRunSync() should beTrue
    }

    "If the protocol doesn't exist returns false" >> prop { ids: DifferentIdentifiers =>
      implicit val storage: Storage[IO] = new StorageStub(Some(dummyProtocol), ids.identifier1)
      val dbService: DBService[IO]      = FileDBService.impl[IO]

      dbService.existsProtocol(ids.identifier2).unsafeRunSync() should beFalse
    }
  }
}
