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
import cats.syntax.apply._
import higherkindness.compendium.db.DBService
import higherkindness.compendium.models.Protocol
import higherkindness.compendium.storage.Storage
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

object CompendiumServiceSpec extends Specification with ScalaCheck {

  sequential

  def dbServiceIO(protocol: Protocol, identifier: Int): DBService[IO] = new DBService[IO] {
    override def addProtocol(protocol: Protocol): IO[Int] = IO(identifier)

    override def lastProtocol(): IO[Option[Protocol]] = IO(Some(protocol))
  }

  def storageIO(proto: Option[Protocol], identifier: Int): Storage[IO] = new Storage[IO] {
    override def store(id: Int, protocol: Protocol): IO[Unit] =
      IO {
        proto === Some(protocol)
        id === identifier
      } *> IO.unit

    override def recover(id: Int): IO[Option[Protocol]] =
      if (id == identifier) IO(proto) else IO(None)

    override def numberProtocol(): IO[Int] = IO(identifier)
  }

  def protocolUtilsIO(pro: Protocol, valid: Boolean): ProtocolUtils[IO] =
    new ProtocolUtils[IO] {
      override def validateProtocol(protocol: Protocol): IO[Protocol] =
        if (valid) IO(pro)
        else IO.raiseError(new org.apache.avro.SchemaParseException("Error"))
    }

  private val dummyProtocol: Protocol = Protocol("", "")

  "Store protocol" >> {
    "If it's a valid protocol we store it" >> prop { identifier: Int =>
      val id                     = Math.abs(identifier)
      implicit val dbService     = dbServiceIO(dummyProtocol, id)
      implicit val storage       = storageIO(Some(dummyProtocol), id)
      implicit val protocolUtils = protocolUtilsIO(dummyProtocol, true)

      CompendiumService.impl[IO].storeProtocol(dummyProtocol).unsafeRunSync() === id
    }

    "If it's an invalid protocol we raise an error" >> prop { identifier: Int =>
      val id                     = Math.abs(identifier)
      implicit val dbService     = dbServiceIO(dummyProtocol, id)
      implicit val storage       = storageIO(Some(dummyProtocol), id)
      implicit val protocolUtils = protocolUtilsIO(dummyProtocol, false)

      CompendiumService
        .impl[IO]
        .storeProtocol(dummyProtocol)
        .unsafeRunSync must throwA[org.apache.avro.SchemaParseException]
    }
  }

  "Recover protocol" >> {
    "Given a identifier we recover the protocol" >> prop { identifier: Int =>
      val id                     = Math.abs(identifier)
      implicit val dbService     = dbServiceIO(dummyProtocol, id)
      implicit val storage       = storageIO(Some(dummyProtocol), id)
      implicit val protocolUtils = protocolUtilsIO(dummyProtocol, true)

      CompendiumService.impl[IO].recoverProtocol(id).unsafeRunSync() === Some(dummyProtocol)
    }
  }

}
