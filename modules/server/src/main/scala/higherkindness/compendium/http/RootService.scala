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

import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.http4s.circe.CirceEntityCodec._
import io.circe.syntax._
import higherkindness.compendium.models._
import org.http4s.dsl.Http4sDsl
import Decoders._
import Encoders._
import higherkindness.compendium.core.CompendiumService
import org.http4s.HttpRoutes
import org.http4s.headers.Location

object RootService {

  def rootRouteService[F[_]: Sync: CompendiumService]: HttpRoutes[F] = {

    object f extends Http4sDsl[F]
    import f._

    HttpRoutes.of[F] {
      case GET -> Root / "ping" => Ok("pong".asJson)

      case req @ POST -> Root / "v0" / "protocol" / id =>
        Sync[F].recoverWith(
          for {
            protocol <- req.as[Protocol]
            _        <- CompendiumService[F].storeProtocol(id, protocol)
            resp     <- Created().map(_.putHeaders(Location(req.uri.withPath(s"${req.uri.path}"))))
          } yield resp
        ) {
          // TODO Handle id already exists error
          case e: org.apache.avro.SchemaParseException => BadRequest(e.getMessage.asJson)
          case t: ProtocolAlreadyExists                => Conflict(t.getMessage.asJson)
          case _                                       => InternalServerError()
        }

      case GET -> Root / "v0" / "protocol" / id =>
        for {
          protocol <- CompendiumService[F].recoverProtocol(id)
          resp     <- protocol.fold(NotFound())(Ok(_))
        } yield resp

    }
  }
}