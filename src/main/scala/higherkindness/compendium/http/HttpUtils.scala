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

package higherkindness.compendium.http

import org.http4s.multipart.Multipart
import fs2.text._
import cats.effect.Sync
import higherkindness.compendium.models.Protocol
import org.apache.avro.Schema
import org.http4s.InvalidMessageBodyFailure

final class HttpUtils[F[_]: Sync] {

  private val parser: Schema.Parser = new Schema.Parser()

  def filename(multipart: Multipart[F]): F[String] =
    multipart.parts
      .find(_.filename.isDefined)
      .flatMap(_.filename)
      .fold(Sync[F].raiseError[String](
        new InvalidMessageBodyFailure("Missing filename from upload.")))(Sync[F].pure)

  import cats.implicits._

  def rawText(multipart: Multipart[F]): F[String] =
    multipart.parts
      .map(_.body.through(utf8Decode).compile.foldMonoid)
      .toList
      .sequence
      .map(_.mkString("\n"))

  def protocol(name: String, text: String): F[Protocol] =
    if (text.trim.isEmpty)
      Sync[F].raiseError(new org.apache.avro.SchemaParseException("Protocol is empty"))
    else
      Sync[F].catchNonFatal(parser.parse(text)).map(_ => Protocol(name, text))
}

object HttpUtils {
  def apply[F[_]: Sync]: HttpUtils[F] = new HttpUtils
}
