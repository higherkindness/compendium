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

import org.http4s.multipart.Multipart
import fs2.text._
import cats.effect.IO
import cats.implicits._
import higherkindness.models.Protocol
import org.apache.avro.Schema
import org.http4s.InvalidMessageBodyFailure

private[http] object Utils {

  private val parser: Schema.Parser = new Schema.Parser()

  def filename(multipart: Multipart[IO]): IO[String] =
    multipart.parts
      .find(_.filename.isDefined)
      .flatMap(_.filename)
      .fold(IO.raiseError[String](new InvalidMessageBodyFailure("Missing filename from upload.")))(
        IO.pure)

  def rawText(multipart: Multipart[IO]): IO[String] = {
    val vector: IO[Vector[String]] =
      multipart.parts.map(_.body.through(utf8Decode).compile.foldMonoid).sequence

    vector.map(_.mkString("\n"))
  }

  def protocol(name: String, text: String): IO[Protocol] =
    IO(parser.parse(text)).map(_ => Protocol(name, text))
}
