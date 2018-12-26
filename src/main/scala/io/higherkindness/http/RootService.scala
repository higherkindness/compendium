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

package io.higherkindness.http

import cats.effect.IO
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.multipart.Multipart
import cats.effect.IO._
import org.http4s.headers._
import io.higherkindness.db.DBService
import io.higherkindness.domain.DomainService

object RootService {

  def rootRouteService(
      domainService: DomainService[IO],
      dbService: DBService[IO]): HttpService[IO] =
    HttpService[IO] {
      case GET -> Root / "ping" => Ok("pong")
      case req @ POST -> Root / "v0" / "domain" =>
        req.decode[Multipart[IO]] { m =>
          val act = for {
            tempFile <- Utils.storeMultipart(m)
            id       <- dbService.lastDomain().map(_.fold(1)(_.id + 1))
            _        <- domainService.store(id, tempFile._1, tempFile._2)
            _        <- IO(tempFile._2.delete())
          } yield id
          act.flatMap(id => Ok(s"$id").map(_.putHeaders(Location(req.uri.withPath(s"$id")))))
        }

      case GET -> Root / "v0" / "domain" / domainId => NotImplemented(domainId)
    }

}
