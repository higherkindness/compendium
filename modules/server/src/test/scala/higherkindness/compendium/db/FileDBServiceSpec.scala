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
import higherkindness.compendium.models.{Protocol, ProtocolAlreadyExists}
import higherkindness.compendium.storage.StorageStub
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

object FileDBServiceSPec extends Specification with ScalaCheck {

  sequential

  private val dummyProtocol: Protocol = Protocol("")

  "Store protocol" >> {
    "If the protocol doesn't exists we store it" >> prop { id: String =>
      implicit val storage = new StorageStub(Some(dummyProtocol), id)

      FileDBService.impl[IO].addProtocol(id, dummyProtocol).map(_ => success).unsafeRunSync()
    }

    "If the protocol exists we raised an error" >> prop { id: String =>
      implicit val storage = new StorageStub(Some(dummyProtocol), id) {
        override def checkIfExists(id: String): IO[Boolean] = IO(true)
      }

      FileDBService.impl[IO].addProtocol(id, dummyProtocol).unsafeRunSync must
        throwA[ProtocolAlreadyExists]
    }
  }
}
