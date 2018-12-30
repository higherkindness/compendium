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

package higherkindness.storage

import java.io.{File, PrintWriter}

import cats.MonadError
import cats.effect.Sync
import cats.syntax.functor._
import cats.syntax.flatMap._
import higherkindness.models.{Protocol, StorageConfig}

object FileStorage {

  def impl[F[_]: Sync](config: StorageConfig)(implicit ME: MonadError[F, Throwable]): Storage[F] =
    new Storage[F] {

      override def store(id: Int, protocol: Protocol): F[Unit] =
        for {
          _ <- ME.catchNonFatal(new File(s"${config.path}${File.separator}$id").mkdirs())
          file <- ME.catchNonFatal(
            new File(s"${config.path}${File.separator}$id${File.separator}${protocol.name}"))
          _ <- ME.catchNonFatal {
            val printWriter = new PrintWriter(file)
            printWriter.write(protocol.raw)
            printWriter.close()
          }
        } yield ()

      override def recover(id: Int): F[Option[Protocol]] =
        for {
          filename <- ME.catchNonFatal {
            Option(new File(s"${config.path}${File.separator}$id").listFiles())
              .fold(Option.empty[String])(_.headOption.map(_.getAbsolutePath))
          }
          source <- ME.catchNonFatal { filename.map(scala.io.Source.fromFile) }
          protocol <- ME.catchNonFatal {
            source.flatMap { s =>
              filename.map { fn =>
                Protocol(fn, s.mkString)
              }
            }
          }
        } yield protocol

      override def numberProtocol(): F[Int] =
        ME.catchNonFatal {
          Option(new File(s"${config.path}${File.separator}").list()).fold(0)(_.length)
        }
    }
}
