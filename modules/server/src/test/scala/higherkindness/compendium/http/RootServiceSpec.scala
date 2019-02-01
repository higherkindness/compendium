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

import cats.effect.IO
import higherkindness.compendium.models.Protocol
import org.specs2.mutable.Specification
import org.http4s.{Method, Request, Response, Status, Uri}
import org.http4s.dsl.io._
import org.http4s.circe.CirceEntityCodec._
import Encoders._
import Decoders._
import higherkindness.compendium.core.CompendiumService
import org.specs2.ScalaCheck
import org.scalacheck.Gen

object RootServiceSpec extends Specification with ScalaCheck {

  sequential

  private val dummyProtocol: Protocol = Protocol("")

  def compendiumServiceIO(protocolOpt: Option[Protocol]) =
    new CompendiumService[IO] {
      override def storeProtocol(id: String, protocol: Protocol): IO[Unit] = IO.unit

      override def recoverProtocol(id: String): IO[Option[Protocol]] = IO(protocolOpt)
    }

  "GET /v0/protocol/id" >> {
    "If successs returns a valid protocol and status code" >> {
      implicit val compendiumService = compendiumServiceIO(Some(dummyProtocol))

      val request = Request[IO](
        uri = Uri(
          path = s"/v0/protocol/my.proto"
        )
      )

      val response: IO[Response[IO]] =
        RootService.rootRouteService[IO].orNotFound(request)

      response.flatMap(_.as[Protocol]).unsafeRunSync === dummyProtocol
      response.map(_.status).unsafeRunSync === Status.Ok
    }

    "If protocol not found returns not found" >> {
      implicit val compendiumService = compendiumServiceIO(None)

      val request = Request[IO](
        uri = Uri(
          path = s"/v0/protocol/my.proto"
        )
      )

      val response: IO[Response[IO]] =
        RootService.rootRouteService[IO].orNotFound(request)

      response.map(_.status).unsafeRunSync === Status.NotFound
    }
  }

  "POST /v0/protocol/" >> {
    "If protocol returns an invalid avro schema returns bad request" >> {
      implicit val compendiumService = new CompendiumService[IO] {
        override def storeProtocol(id: String, protocol: Protocol): IO[Unit] =
          IO.raiseError[Unit](new org.apache.avro.SchemaParseException(""))
        override def recoverProtocol(protocolId: String): IO[Option[Protocol]] = IO(None)
      }

      val request: Request[IO] = Request[IO](
        uri = Uri(
          path = s"/v0/protocol/test"
        ),
        method = Method.POST
      ).withEntity(dummyProtocol)

      val response: IO[Response[IO]] =
        RootService.rootRouteService[IO].orNotFound(request)

      response.map(_.status).unsafeRunSync === Status.BadRequest
    }

    "If protocol is valid returns OK and the location in the headers" >> prop { id: String =>
      implicit val compendiumService = compendiumServiceIO(None)

      val request: Request[IO] = Request[IO](
        uri = Uri(
          path = s"/v0/protocol/${id}"
        ),
        method = Method.POST
      ).withEntity(dummyProtocol)

      val response: IO[Response[IO]] =
        RootService.rootRouteService[IO].orNotFound(request)

      response.map(_.status).unsafeRunSync === Status.Created
      response
        .map(_.headers.find(_.name == "Location".ci))
        .unsafeRunSync
        .map(_.value) === Some(s"/v0/protocol/${id}")
    }.setGen(Gen.alphaNumStr suchThat (!_.isEmpty))
  }
}