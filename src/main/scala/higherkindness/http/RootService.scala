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

import cats.effect.IO
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.multipart.Multipart
import cats.effect.IO._
import org.http4s.headers._
import higherkindness.db.DBService
import cats.implicits._
import higherkindness.storage.StorageService

object RootService {

  def rootRouteService(
      storageService: StorageService[IO],
      dbService: DBService[IO]): HttpService[IO] = {

    HttpService[IO] {
      case GET -> Root / "ping" => Ok("pong")

      case req @ POST -> Root / "v0" / "protocol" =>
        req.decode[Multipart[IO]] { m =>
          val act = for {
            name     <- Utils.filename(m)
            text     <- Utils.rawText(m)
            protocol <- Utils.protocol(name, text)
            id       <- dbService.addProtocol(protocol)
            _        <- storageService.store(id, protocol)
          } yield id

          act
            .flatMap(id => Ok(s"$id").map(_.putHeaders(Location(req.uri.withPath(s"$id")))))
            .recoverWith {
              case e: org.apache.avro.SchemaParseException => BadRequest(e.getMessage)
              case _                                       => InternalServerError()
            }
        }

      case GET -> Root / "v0" / "protocol" / IntVar(protocolId) =>
        storageService
          .recover(protocolId)
          .flatMap(_.fold(NotFound())(p => Ok(p.raw)))

    }
  }
}
