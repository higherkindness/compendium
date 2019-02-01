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

import java.io.{File, PrintWriter}

import cats.effect.Sync
import cats.syntax.functor._
import cats.syntax.flatMap._
import higherkindness.compendium.models.{Protocol, StorageConfig}

object FileStorage {

  def impl[F[_]: Sync](config: StorageConfig): Storage[F] =
    new Storage[F] {

      override def store(id: String, protocol: Protocol): F[Unit] =
        for {
          _    <- Sync[F].catchNonFatal(new File(s"${config.path}${File.separator}$id").mkdirs())
          file <- Sync[F].catchNonFatal(new File(s"${config.path}${File.separator}${id}"))
          _ <- Sync[F].catchNonFatal {
            val printWriter = new PrintWriter(file)
            printWriter.write(protocol.raw)
            printWriter.close()
          }
        } yield ()

      override def recover(id: String): F[Option[Protocol]] =
        for {
          filename <- Sync[F].catchNonFatal {
            Option(new File(s"${config.path}${File.separator}${id}").listFiles())
              .fold(Option.empty[String])(_.headOption.map(_.getAbsolutePath))
          }
          source <- Sync[F].catchNonFatal { filename.map(scala.io.Source.fromFile) }
          protocol <- Sync[F].catchNonFatal {
            source.flatMap { s =>
              filename.map { _ =>
                Protocol(s.mkString)
              }
            }
          }
        } yield protocol

      override def checkIfExists(id: String): F[Boolean] =
        Sync[F].catchNonFatal(new File(s"${config.path}${File.separator}${id}")).map(_.exists)
    }
}
