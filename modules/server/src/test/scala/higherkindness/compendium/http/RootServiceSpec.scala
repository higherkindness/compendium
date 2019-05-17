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
import higherkindness.compendium.models._
import org.specs2.mutable.Specification
import org.http4s.{Method, Request, Response, Status, Uri}
import org.http4s.circe.CirceEntityCodec._
import Encoders._
import Decoders._
import higherkindness.compendium.core.CompendiumServiceStub
import org.http4s.syntax.string._
import org.http4s.syntax.kleisli._
import org.specs2.ScalaCheck
import org.scalacheck.Gen

object RootServiceSpec extends Specification with ScalaCheck {

  sequential

  private val dummyProtocol: Protocol = Protocol("")

  "GET /protocol/id" >> {
    "If successs returns a valid protocol and status code" >> {
      implicit val compendiumService = new CompendiumServiceStub(Some(dummyProtocol), true)

      val request: Request[IO] =
        Request[IO](method = Method.GET, uri = Uri(path = s"/protocol/my.proto"))

      val response: IO[Response[IO]] =
        RootService.rootRouteService[IO].orNotFound(request)

      response.map(_.status).unsafeRunSync === Status.Ok
      response.flatMap(_.as[Protocol]).unsafeRunSync === dummyProtocol
    }

    "If protocol not found returns not found" >> {
      implicit val compendiumService = new CompendiumServiceStub(None, true)

      val request: Request[IO] =
        Request[IO](method = Method.GET, uri = Uri(path = s"/protocol/my.proto"))

      val response: IO[Response[IO]] =
        RootService.rootRouteService[IO].orNotFound(request)

      response.map(_.status).unsafeRunSync === Status.NotFound
    }
  }

  "POST /protocol/" >> {
    "If protocol returns an invalid avro schema returns BadRequest" >> {
      implicit val compendiumService = new CompendiumServiceStub(None, false) {
        override def storeProtocol(id: String, protocol: Protocol): IO[Unit] =
          IO.raiseError[Unit](new org.apache.avro.SchemaParseException(""))
      }

      val request: Request[IO] =
        Request[IO](method = Method.POST, uri = Uri(path = s"/protocol/test"))
          .withEntity(dummyProtocol)

      val response: IO[Response[IO]] =
        RootService.rootRouteService[IO].orNotFound(request)

      response.map(_.status).unsafeRunSync === Status.BadRequest
    }

    "If protocol is valid returns Created and the location in the headers" >> prop { id: String =>
      implicit val compendiumService = new CompendiumServiceStub(None, false)

      val request: Request[IO] =
        Request[IO](method = Method.POST, uri = Uri(path = s"/protocol/$id"))
          .withEntity(dummyProtocol)

      val response: IO[Response[IO]] =
        RootService.rootRouteService[IO].orNotFound(request)

      response.map(_.status).unsafeRunSync === Status.Created
      response
        .map(_.headers.find(_.name == "Location".ci))
        .unsafeRunSync
        .map(_.value) === Some(s"/protocol/$id")
    }.setGen(Gen.alphaNumStr suchThat (!_.isEmpty))

    "If protocol is valid and it was already in compendium returns Ok and the location in the headers" >> prop {
      id: String =>
        implicit val compendiumService = new CompendiumServiceStub(None, true)

        val request: Request[IO] =
          Request[IO](method = Method.POST, uri = Uri(path = s"/protocol/$id"))
            .withEntity(dummyProtocol)

        val response: IO[Response[IO]] =
          RootService.rootRouteService[IO].orNotFound(request)

        response.map(_.status).unsafeRunSync === Status.Ok
        response
          .map(_.headers.find(_.name == "Location".ci))
          .unsafeRunSync
          .map(_.value) === Some(s"/protocol/$id")
    }.setGen(Gen.alphaNumStr suchThat (!_.isEmpty))
  }

  "GET /protocol/id/generate?target={target}" >> {
    "If identifier and target is valid returns Ok and the client" >> {
      failure
    }.pendingUntilFixed

    "If identifier is valid but target is invalid returns NotFound" >> {
      failure
    }.pendingUntilFixed

    "If target is valid but identifier is invalid returns NotFound" >> {
      failure
    }.pendingUntilFixed
  }
}
