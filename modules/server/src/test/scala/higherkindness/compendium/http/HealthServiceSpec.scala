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

import buildinfo.BuildInfo
import cats.effect.IO
import higherkindness.compendium.metadata.MetadataStorageStub
import higherkindness.compendium.models.HealthResponse
import org.http4s.circe.CirceEntityCodec._
import org.http4s.implicits._
import org.http4s.{Method, Request, Response, Status, Uri}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

object HealthServiceSpec extends Specification with ScalaCheck {

  sequential

  private val healthyResponse: HealthResponse =
    HealthResponse("pass", BuildInfo.name, BuildInfo.version)

  "GET /health" >> {
    "If successs returns a health response with status 200" >> {
      implicit val dbService = new MetadataStorageStub(true)

      val request: Request[IO] =
        Request[IO](method = Method.GET, uri = Uri(path = s"/health"))

      val response: IO[Response[IO]] =
        HealthService.healthRouteService[IO].orNotFound(request)

      response.map(_.status).unsafeRunSync === Status.Ok
      response.flatMap(_.as[HealthResponse]).unsafeRunSync === healthyResponse
    }

    "If protocol not found returns not found" >> {
      implicit val dbService = new MetadataStorageStub(false)

      val request: Request[IO] =
        Request[IO](method = Method.GET, uri = Uri(path = s"/health"))

      val response: IO[Response[IO]] =
        HealthService.healthRouteService[IO].orNotFound(request)

      response.map(_.status).unsafeRunSync === Status.InternalServerError
    }
  }

}
