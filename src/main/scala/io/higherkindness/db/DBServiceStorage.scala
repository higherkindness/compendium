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

package io.higherkindness.db

import java.io.File

import cats.effect.IO
import io.higherkindness.models.{Domain, Storage}

object DBServiceStorage {

  def impl(storage: Storage): DBService[IO] =
    new DBService[IO] {
      val path = storage.path
      override def addDomain(filename: String): IO[Domain] =
        for {
          lastDomain <- lastDomain()
          created <- IO {
            new File(s"$path${File.separator}${lastDomain.fold(1)(_.id) + 1}").mkdir()
          }
          domain <- if (created) IO.pure(Domain(lastDomain.fold(1)(_.id) + 1, filename))
          else IO.raiseError(new Exception("Error creating folder"))
        } yield domain

      override def lastDomain(): IO[Option[Domain]] =
        for {
          identifier <- IO {
            Option(new File(path).list)
              .fold(List.empty[String])(_.toList)
              .map(_.toInt)
              .sorted
              .lastOption
          }
          filename <- IO {
            identifier.flatMap(
              id =>
                Option(new File(s"$path${File.separator}$id").listFiles)
                  .fold(Option.empty[File])(_.headOption))
          }
        } yield identifier.flatMap(id => filename.map(fn => Domain(id, fn.getName)))
    }
}
