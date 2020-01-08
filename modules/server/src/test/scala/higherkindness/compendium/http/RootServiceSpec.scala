/*
 * Copyright 2018-2020 47 Degrees, LLC. <http://www.47deg.com>
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
import cats.syntax.all._
import higherkindness.compendium.CompendiumArbitrary._
import higherkindness.compendium.core.refinements.{ProtocolId, ProtocolVersion}
import higherkindness.compendium.core.CompendiumServiceStub
import higherkindness.compendium.models._
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import org.http4s.circe.CirceEntityCodec._
import org.http4s.implicits._
import org.http4s._
import org.http4s.{Method, Request, Response, Status, Uri}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

object RootServiceSpec extends Specification with ScalaCheck {

  import TestEncoders._

  sequential

  private val dummyProtocol: ProtocolId => FullProtocol = (pid: ProtocolId) =>
    FullProtocol(ProtocolMetadata(pid, IdlName.Avro, ProtocolVersion(1)), Protocol(""))

  "GET /protocol/id?version={version}" >> {
    "If successs returns a valid protocol" >> prop { id: ProtocolId =>
      val dummyProto = dummyProtocol(id)

      implicit val compendiumService = CompendiumServiceStub(dummyProto.some, true)

      val request = Request[IO](method = Method.GET, uri = Uri(path = s"/protocol/${id.value}"))

      val response = RootService.rootRouteService[IO].orNotFound(request).unsafeRunSync

      response.status === Status.Ok
      response.as[Protocol].unsafeRunSync === dummyProto.protocol
    }

    "If successs with specific version returns a valid protocol" >> {
      implicit val compendiumService =
        CompendiumServiceStub(Some(dummyProtocol(ProtocolId("id"))), true)

      val request: Request[IO] =
        Request[IO](
          method = Method.GET,
          uri = Uri(path = "/protocol/my.proto", query = Query("version" -> Option("1"))))

      val response: IO[Response[IO]] =
        RootService.rootRouteService[IO].orNotFound(request)

      response.map(_.status).unsafeRunSync === Status.Ok
      response.flatMap(_.as[Protocol]).unsafeRunSync === dummyProtocol(ProtocolId("id")).protocol
    }

    "If protocol not found returns not found" >> {
      implicit val compendiumService = CompendiumServiceStub(None, true)

      val request: Request[IO] =
        Request[IO](method = Method.GET, uri = Uri(path = "/protocol/my.proto"))

      val response: IO[Response[IO]] =
        RootService.rootRouteService[IO].orNotFound(request)

      response.map(_.status).unsafeRunSync === Status.NotFound
    }

    "If protocol identifier is malformed returns bad request" >> {
      implicit val compendiumService =
        CompendiumServiceStub(Some(dummyProtocol(ProtocolId("id"))), true)

      val request: Request[IO] =
        Request[IO](method = Method.GET, uri = Uri(path = "/protocol/not_valid@"))

      val response: IO[Response[IO]] =
        RootService.rootRouteService[IO].orNotFound(request)

      response.map(_.status).unsafeRunSync === Status.BadRequest
    }

    "If protocol version is string returns bad request" >> {
      implicit val compendiumService =
        CompendiumServiceStub(Some(dummyProtocol(ProtocolId("id"))), true)

      val request: Request[IO] =
        Request[IO](
          method = Method.GET,
          uri = Uri(path = "/protocol/id", query = Query("version" -> Option("fake"))))

      val response: IO[Response[IO]] =
        RootService.rootRouteService[IO].orNotFound(request)

      response.map(_.status).unsafeRunSync === Status.BadRequest
    }

    "If protocol version is non positive returns bad request" >> {
      implicit val compendiumService =
        CompendiumServiceStub(Some(dummyProtocol(ProtocolId("id"))), true)

      val request: Request[IO] =
        Request[IO](
          method = Method.GET,
          uri = Uri(path = "/protocol/id", query = Query("version" -> Option("0"))))

      val response: IO[Response[IO]] =
        RootService.rootRouteService[IO].orNotFound(request)

      response.map(_.status).unsafeRunSync === Status.BadRequest
    }
  }

  "POST /protocol/id?idlName={idlName}" >> {
    "If protocol returns an invalid avro schema returns BadRequest" >> {
      implicit val compendiumService = new CompendiumServiceStub(None, false) {
        override def storeProtocol(
            id: ProtocolId,
            protocol: Protocol,
            idlNames: IdlName): IO[ProtocolVersion] =
          IO.raiseError[ProtocolVersion](new org.apache.avro.SchemaParseException(""))
      }

      val request: Request[IO] =
        Request[IO](
          method = Method.POST,
          uri = Uri(path = s"/protocol/test", query = Query.fromString("idlName=avro")))
          .withEntity(dummyProtocol(ProtocolId("id")).protocol)

      val response: IO[Response[IO]] =
        RootService.rootRouteService[IO].orNotFound(request)

      response.map(_.status).unsafeRunSync === Status.BadRequest
    }

    "If protocol is valid returns Created with id in the body and location in the headers" >> prop {
      id: ProtocolId =>
        implicit val compendiumService = CompendiumServiceStub(None, false)

        val request: Request[IO] =
          Request[IO](
            method = Method.POST,
            uri = Uri(path = s"/protocol/${id.value}", query = Query("idlName" -> Option("avro"))))
            .withEntity(dummyProtocol(id).protocol)

        val response = RootService.rootRouteService[IO].orNotFound(request).unsafeRunSync

        response.status === Status.Created
        response.headers.find(_.name == "Location".ci).map(_.value) === Some(
          s"/protocol/$id?idlName=avro")
        response.as[Int].unsafeRunSync === 1 // Default version number when POSTing new protocols
    }

    "If protocol identifier is malformed returns bad request" >> {
      implicit val compendiumService = CompendiumServiceStub(None, false)

      val request: Request[IO] =
        Request[IO](
          method = Method.POST,
          uri = Uri(path = "/protocol/not_valid@", query = Query("idlName" -> Option("avro"))))
          .withEntity(dummyProtocol(ProtocolId("id")).protocol)

      val response: IO[Response[IO]] =
        RootService.rootRouteService[IO].orNotFound(request)

      response.map(_.status).unsafeRunSync === Status.BadRequest
    }

    "If protocol IDL Name is not valid returns bad request" >> {
      implicit val compendiumService =
        CompendiumServiceStub(Some(dummyProtocol(ProtocolId("id"))), true)

      val request: Request[IO] =
        Request[IO](
          method = Method.POST,
          uri = Uri(path = s"/protocol/id", query = Query("idlName" -> Option("asdf"))))

      val response = RootService.rootRouteService[IO].orNotFound(request).unsafeRunSync

      response.status === Status.BadRequest
    }

    "If json is invalid returns bad Request" >> prop { id: ProtocolId =>
      implicit val compendiumService = CompendiumServiceStub(None, false)

      case class Malformed(malformed: String)

      implicit val encoder: Encoder[Malformed] = deriveEncoder[Malformed]

      val request: Request[IO] =
        Request[IO](
          method = Method.POST,
          uri = Uri(path = s"/protocol/${id.value}", query = Query.fromString("idlName=avro")))
          .withEntity(Malformed("test"))

      val response: IO[Response[IO]] =
        RootService.rootRouteService[IO].orNotFound(request)

      response.map(_.status).unsafeRunSync === Status.BadRequest
    }

    "If protocol is valid and it was already in compendium returns Ok and the location in the headers" >> prop {
      id: ProtocolId =>
        implicit val compendiumService = CompendiumServiceStub(None, true)

        val request: Request[IO] =
          Request[IO](
            method = Method.POST,
            uri = Uri(path = s"/protocol/${id.value}", query = Query.fromString("idlName=avro")))
            .withEntity(dummyProtocol(id).protocol)

        val response: IO[Response[IO]] =
          RootService.rootRouteService[IO].orNotFound(request)

        response.map(_.status).unsafeRunSync === Status.Ok
        response
          .map(_.headers.find(_.name == "Location".ci))
          .unsafeRunSync
          .map(_.value) === Some(s"/protocol/$id?idlName=avro")
    }
  }

  "GET /protocol/id/transformation?target={target}[&version={version}]" >> {
    "If identifier, version and target are valid returns the protocol transformed" >> prop {
      metadata: ProtocolMetadata =>
        val dummyProto = dummyProtocol(metadata.id)

        implicit val compendiumService = CompendiumServiceStub(dummyProto.some, true)
        val target                     = IdlName.Mu

        val request = Request[IO](
          method = Method.GET,
          uri = Uri(
            path = s"/protocol/${metadata.id.value}/transformation",
            query = Query.fromString(s"target=${target.entryName}&version=1")))

        val response = RootService.rootRouteService[IO].orNotFound(request).unsafeRunSync

        response.status === Status.Ok
        response.as[Protocol].unsafeRunSync === dummyProto.protocol
    }

    "If source protocol does not exist returns not found" >> prop { metadata: ProtocolMetadata =>
      implicit val compendiumService = CompendiumServiceStub(None, true)
      val target                     = IdlName.Mu

      val request = Request[IO](
        method = Method.GET,
        uri = Uri(
          path = s"/protocol/${metadata.id.value}/transformation",
          query = Query.fromString(s"target=${target.entryName}&version=1"))
      )

      val response = RootService.rootRouteService[IO].orNotFound(request).unsafeRunSync

      response.status === Status.NotFound
    }

    "If version is invalid returns bad request" >> prop { metadata: ProtocolMetadata =>
      val dummyProto = dummyProtocol(metadata.id)

      implicit val compendiumService = CompendiumServiceStub(dummyProto.some, true)
      val target                     = IdlName.Mu

      val request = Request[IO](
        method = Method.GET,
        uri = Uri(
          path = s"/protocol/${metadata.id.value}/transformation",
          query = Query.fromString(s"target=${target.entryName}&version=fake"))
      )

      val response = RootService.rootRouteService[IO].orNotFound(request).unsafeRunSync

      response.status === Status.BadRequest
    }

    "If identifier is valid but target is invalid returns bad request" >> prop {
      metadata: ProtocolMetadata =>
        val dummyProto = dummyProtocol(metadata.id)

        implicit val compendiumService = CompendiumServiceStub(dummyProto.some, true)

        val request = Request[IO](
          method = Method.GET,
          uri = Uri(
            path = s"/protocol/${metadata.id.value}/transformation",
            query = Query.fromString("target=wrong")))

        val response = RootService.rootRouteService[IO].orNotFound(request).unsafeRunSync

        response.status === Status.BadRequest
    }

    "If target is valid but identifier is invalid returns bad request" >> {
      val fakeId                     = "fake@id"
      implicit val compendiumService = CompendiumServiceStub(None, true)
      val target                     = IdlName.Mu

      val request = Request[IO](
        method = Method.GET,
        uri = Uri(
          path = s"/protocol/$fakeId/transformation",
          query = Query.fromString(s"target=${target.entryName}")))

      val response = RootService.rootRouteService[IO].orNotFound(request).unsafeRunSync

      response.status === Status.BadRequest
    }
  }
}
