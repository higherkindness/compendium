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

package http

import cats.effect.IO
import fs2.Stream
import fs2.text.utf8Encode
import higherkindness.compendium.http.HttpUtils
import higherkindness.compendium.models.Protocol
import org.http4s.Headers
import org.http4s.headers.`Content-Disposition`
import org.http4s.multipart.{Multipart, Part}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

import scala.io.Source

object HtttpUtilsSpec extends Specification with ScalaCheck {

  sequential

  val utils = HttpUtils[IO]

  "Given a multipart" >> {
    "Returns the filename" >> prop { filename: String =>
      val part: Part[IO] = Part(
        Headers(
          `Content-Disposition`("form-data", Map("name" -> "text", "filename" -> filename)) :: Nil),
        Stream.emit("").through(utf8Encode))

      val multipart = Multipart[IO](Vector(part))

      utils.filename(multipart).unsafeRunSync === filename
    }

    "Returns the text" >> prop { text: String =>
      val part: Part[IO] = Part(
        Headers(
          `Content-Disposition`("form-data", Map("name" -> text, "filename" -> "filename")) :: Nil),
        Stream.emit("").through(utf8Encode))

      val multipart = Multipart[IO](Vector(part))

      utils.rawText(multipart).unsafeRunSync === text
    }.pendingUntilFixed("Encoding errors")
  }

  "Given a avro text" >> {
    "Returns a protocol if the avro text it is correct" >> {
      val text = Source.fromResource("correct.avro").getLines.mkString

      utils.protocol("name", text).unsafeRunSync === Protocol("name", text)
    }

    "Raise an error if the protocol is incorrect" >> {
      val text = Source.fromResource("correct.avro").getLines.mkString

      utils.protocol("name", text).unsafeRunSync must throwA[org.apache.avro.SchemaParseException]
    }
  }

}
