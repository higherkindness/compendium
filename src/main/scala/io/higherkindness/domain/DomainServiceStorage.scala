/*
 * Copyright 2018 47 Degrees, LLC. <http://www.47deg.com>
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

package io.higherkindness.domain

import java.io.File
import java.nio.file.Files.copy
import java.nio.file.Paths.get

import cats.effect.IO
import io.higherkindness.models.Storage

object DomainServiceStorage {

  def impl(storage: Storage): DomainService[IO] =
    new DomainService[IO] {
      val path = storage.path
      override def store(id: Int, filename: String, tmp: File): IO[Unit] = {
        val destPath = s"$path${File.separator}$id"
        println(s"$destPath${File.separator}$filename")
        for {
          _ <- IO { new File(destPath).mkdir() }
          _ <- IO {
            copy(
              get(tmp.getAbsolutePath),
              get(s"$destPath${File.separator}$filename"),
              java.nio.file.StandardCopyOption.REPLACE_EXISTING)
          }
        } yield ()
      }

      override def recover(id: Int): IO[Option[File]] =
        IO {
          Option(new File(s"$path${File.separator}$id").listFiles())
            .fold(Option.empty[File])(_.headOption)
        }
    }
}
