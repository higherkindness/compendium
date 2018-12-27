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

package higherkindness.http

import java.io.{File, FileWriter, PrintWriter}

import cats.effect.IO
import org.http4s.InvalidMessageBodyFailure
import org.http4s.multipart.Multipart
import cats.implicits._

import scala.io.Source

private[http] object Utils {

  type FileLocation = (String, File)
  def storeMultipart(multipart: Multipart[IO]): IO[FileLocation] =
    for {
      tempFile <- IO { File.createTempFile("domain", ".tmp") }
      fName    <- filename(multipart)
      file     <- toFile(multipart, tempFile)
    } yield (fName, file)

  def filename(multipart: Multipart[IO]): IO[String] =
    multipart.parts
      .find(_.filename.isDefined)
      .flatMap(_.filename)
      .fold(IO.raiseError[String](new InvalidMessageBodyFailure("Missing filename from upload.")))(
        IO.pure)

  def toTemp(file: File, data: Vector[Byte]): IO[File] = IO {
    val fileWriter  = new FileWriter(file)
    val printWriter = new PrintWriter(fileWriter)
    val text        = Source.fromBytes(data.toArray).getLines().mkString("\n")
    printWriter.write(text)
    printWriter.close()
    fileWriter.close()
    file
  }

  def toFile(multipart: Multipart[IO], file: File): IO[File] =
    multipart.parts.map(_.body.compile.toVector.flatMap(toTemp(file, _))).reduce { (x, y) =>
      x.flatTap(_ => y)
    }
}
