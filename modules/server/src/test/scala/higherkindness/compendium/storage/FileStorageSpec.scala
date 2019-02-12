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
import higherkindness.compendium.models.{Protocol, StorageConfig}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import org.specs2.mutable.After
import org.specs2.specification.{AfterAll, BeforeAll}

import scala.reflect.io.Directory

object FileStorageSpec
    extends Specification
    with ScalaCheck
    with BeforeAll
    with AfterAll
    with After {

  sequential

  private[this] lazy val basePath: String             = "/tmp/filespec"
  private[this] lazy val storageConfig: StorageConfig = StorageConfig(s"$basePath/files")
  private[this] lazy val baseDirectory                = new Directory(new File(basePath))
  private[this] lazy val storageDirectory             = new Directory(new File(storageConfig.path))
  private[this] def storageProtocol(id: String) =
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

  "Store a file" >> {
    "Successfully stores a file" >> prop { protocol: Protocol =>
      val storageProtocolFile = storageProtocol("id")
      val io = for {
        _ <- fileStorage.store("id", protocol)
      } yield storageDirectory.exists && storageProtocolFile.exists

      io.unsafeRunSync() should beTrue
    }

    "Successfully stores and recovers a file" >> prop { protocol: Protocol =>
      val file = for {
        _ <- fileStorage.store("id", protocol)
        f <- fileStorage.recover("id")
      } yield f

      file.unsafeRunSync() should beSome(protocol)
    }
  }
}
