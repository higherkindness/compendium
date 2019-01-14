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

import java.io.InputStream

import cats.effect.IO
import cats.syntax.apply._
import fs2.Stream
import fs2.text.utf8Encode
import higherkindness.compendium.CompendiumArbitrary._
import higherkindness.compendium.db.DBService
import higherkindness.compendium.models.Protocol
import higherkindness.compendium.storage.Storage
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import org.http4s.{EntityEncoder, Headers, Method, Request, Response, Status, Uri}
import org.http4s.dsl.io._
import org.http4s.headers.`Content-Disposition`
import org.http4s.multipart.{Multipart, Part}

object RootServiceSpec extends Specification with ScalaCheck {

  sequential

  def dbServiceIO(protocol: Protocol, identifier: Int): DBService[IO] = new DBService[IO] {
    override def addProtocol(protocol: Protocol): IO[Int] = IO(identifier)

    override def lastProtocol(): IO[Option[Protocol]] = IO(Some(protocol))
  }

  def storageIO(proto: Protocol, identifier: Int): Storage[IO] = new Storage[IO] {
    override def store(id: Int, protocol: Protocol): IO[Unit] =
      IO {
        proto === protocol
        id === identifier
      } *> IO.unit

    override def recover(id: Int): IO[Option[Protocol]] =
      if (id == identifier) IO(Some(proto)) else IO(None)

    override def numberProtocol(): IO[Int] = IO(identifier)
  }

  def stringToMultipart(filename: String, str: String): Multipart[IO] = {

    val part: Part[IO] = Part(
      Headers(
        `Content-Disposition`("form-data", Map("name" -> "text", "filename" -> filename)) :: Nil),
      Stream.emit(str).through(utf8Encode))

    Multipart[IO](Vector(part))
  }

  "GET /v0/protocol/id" >> {
    "If successs returns a valid protocol and status code" >> prop { protocol: Protocol =>
      implicit val dbService = dbServiceIO(protocol, 1)
      implicit val storage   = storageIO(protocol, 1)

      val request = Request[IO](
        uri = Uri(
          path = s"/v0/protocol/1"
        )
      )

      val response: IO[Response[IO]] =
        RootService.rootRouteService[IO].orNotFound(request)

      response.flatMap(_.as[String]).unsafeRunSync === protocol.raw
      response.map(_.status).unsafeRunSync === Status.Ok
    }

    "If protocol not found returns not found" >> prop { protocol: Protocol =>
      implicit val dbService = dbServiceIO(protocol, 1)
      implicit val storage   = storageIO(protocol, 1)

      val request = Request[IO](
        uri = Uri(
          path = s"/v0/protocol/12"
        )
      )

      val response: IO[Response[IO]] =
        RootService.rootRouteService[IO].orNotFound(request)

      response.map(_.status).unsafeRunSync === Status.NotFound
    }
  }

  "POST /v0/protocol/" >> {
    "If protocol is invalid returns bad request" >> prop { protocol: Protocol =>
      implicit val dbService = dbServiceIO(protocol, 1)
      implicit val storage   = storageIO(protocol, 1)

      val multipart = stringToMultipart(protocol.name, protocol.raw)

      val response: IO[Response[IO]] = for {
        entity <- EntityEncoder[IO, Multipart[IO]].toEntity(multipart)
        request <- IO(
          Request[IO](
            uri = Uri(
              path = s"/v0/protocol"
            ),
            method = Method.POST,
            body = entity.body,
            headers = multipart.headers
          ))
        resp <- RootService.rootRouteService[IO].orNotFound(request)
      } yield resp

      response.map(_.status).unsafeRunSync === Status.BadRequest
    }

    "If protocol is valid returns OK and the location in the headers" >> prop { identifier: Int =>
      val stream: InputStream = getClass.getResourceAsStream("/correct.avro")
      val text                = scala.io.Source.fromInputStream(stream).getLines.mkString
      val protocol            = Protocol("correct.avro", text)
      val id                  = Math.abs(identifier)

      implicit val dbService = dbServiceIO(protocol, id)
      implicit val storage   = storageIO(protocol, id)

      val multipart = stringToMultipart(protocol.name, protocol.raw)

      val response: IO[Response[IO]] = for {
        entity <- EntityEncoder[IO, Multipart[IO]].toEntity(multipart)
        request <- IO(
          Request[IO](
            uri = Uri(
              path = s"/v0/protocol"
            ),
            method = Method.POST,
            body = entity.body,
            headers = multipart.headers
          ))
        resp <- RootService.rootRouteService[IO].orNotFound(request)
      } yield resp

      response.map(_.status).unsafeRunSync === Status.Ok
      response
        .map(_.headers.find(_.name == "Location".ci))
        .unsafeRunSync
        .map(_.value) === Some(s"/v0/protocol/$id")
    }
  }
}
