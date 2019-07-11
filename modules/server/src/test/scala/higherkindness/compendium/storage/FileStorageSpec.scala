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

package higherkindness.compendium.storage

import java.io.File

import cats.effect.IO
import higherkindness.compendium.CompendiumArbitrary._
import higherkindness.compendium.core.refinements.ProtocolId
import higherkindness.compendium.models._
import higherkindness.compendium.models.config.StorageConfig
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import org.specs2.specification.{AfterEach, BeforeAfterAll}

import scala.reflect.io.Directory

object FileStorageSpec extends Specification with ScalaCheck with BeforeAfterAll with AfterEach {

  private[this] lazy val basePath: String             = "/tmp/filespec"
  private[this] lazy val storageConfig: StorageConfig = StorageConfig(s"$basePath/files")
  private[this] lazy val baseDirectory                = new Directory(new File(basePath))
  private[this] lazy val storageDirectory             = new Directory(new File(storageConfig.path))
  private[this] def storageProtocol(id: ProtocolId) =
    new Directory(new File(s"${storageConfig.path}/$id/protocol"))

  private[this] lazy val fileStorage: Storage[IO] = FileStorage.impl[IO](storageConfig)

  override def beforeAll(): Unit = {
    val _ = baseDirectory.createDirectory()
  }

  baseDirectory.createDirectory()
  override def afterAll(): Unit = {
    val _ = baseDirectory.deleteRecursively()
  }

  override def after = {
    baseDirectory.deleteRecursively()
    val _ = baseDirectory.createDirectory()
  }

  sequential

  "Store a file" >> {
    "Successfully stores a file" >> prop {
      (protocolMetadata: ProtocolMetadata, protocol: Protocol) =>
        val storageProtocolFile = storageProtocol(protocolMetadata.protocolId)

        val io = fileStorage
          .store(protocolMetadata.protocolId, protocol)
          .map(_ => storageDirectory.exists && storageProtocolFile.exists)

        io.unsafeRunSync() should beTrue
    }

    "Successfully stores and recovers a file" >> prop {
      (protocolMetadata: ProtocolMetadata, protocol: Protocol) =>
        val file = for {
          _ <- fileStorage.store(protocolMetadata.protocolId, protocol)
          f <- fileStorage.recover(protocolMetadata)
        } yield f

        file.unsafeRunSync() should beSome(FullProtocol(protocolMetadata, protocol))
    }

    "Returns true if there is a file" >> prop {
      (protocolMetadata: ProtocolMetadata, protocol: Protocol) =>
        val file = for {
          _      <- fileStorage.store(protocolMetadata.protocolId, protocol)
          exists <- fileStorage.exists(protocolMetadata.protocolId)
        } yield exists

        file.unsafeRunSync() should beTrue
    }

    "Returns false if there is no file" >> prop { protocolId: ProtocolId =>
      val out = for {
        exists <- fileStorage.exists(protocolId)
      } yield exists

      out.unsafeRunSync() should beFalse
    }

  }

}
