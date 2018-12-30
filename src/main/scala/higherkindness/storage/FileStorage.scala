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

import cats.effect.IO
import higherkindness.models.{Protocol, StorageConfig}

object FileStorage {

  def impl[S](config: StorageConfig): Storage[IO] = new Storage[IO] {

    override def store(id: Int, protocol: Protocol): IO[Unit] =
      for {
        _ <- IO { new File(s"${config.path}${File.separator}$id").mkdirs() }
        file <- IO {
          new File(s"${config.path}${File.separator}$id${File.separator}${protocol.name}")
        }
        _ <- IO {
          val printWriter = new PrintWriter(file)
          printWriter.write(protocol.raw)
          printWriter.close()
        }
      } yield ()

    override def recover(id: Int): IO[Option[Protocol]] =
      for {
        filename <- IO {
          Option(new File(s"${config.path}${File.separator}$id").listFiles())
            .fold(Option.empty[String])(_.headOption.map(_.getAbsolutePath))
        }
        source <- IO { filename.map(scala.io.Source.fromFile) }
        protocol <- IO {
          source.flatMap { s =>
            filename.map { fn =>
              Protocol(fn, s.mkString)
            }
          }
        }
      } yield protocol

    override def numberProtocol(): IO[Int] =
      IO { Option(new File(s"${config.path}${File.separator}").list()).fold(0)(_.length) }
  }
}
