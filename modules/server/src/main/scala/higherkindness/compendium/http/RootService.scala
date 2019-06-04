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
import higherkindness.compendium.core.CompendiumService
import higherkindness.compendium.http.QueryParams.TargetQueryParam
import higherkindness.compendium.models._
import mouse.all._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Location

object RootService {

  def rootRouteService[F[_]: Sync: CompendiumService]: HttpRoutes[F] = {

    object f extends Http4sDsl[F]
    import f._

    HttpRoutes.of[F] {
      case req @ POST -> Root / "protocol" / id =>
        Sync[F].recoverWith(
          for {
            protocol <- req.as[Protocol]
            exists   <- CompendiumService[F].existsProtocol(id)
            _        <- CompendiumService[F].storeProtocol(id, protocol)
            resp     <- exists.fold(Ok(), Created())
          } yield resp.putHeaders(Location(req.uri.withPath(s"${req.uri.path}")))
        ) {
          case e: org.apache.avro.SchemaParseException => BadRequest(ErrorResponse(e.getMessage))
          case e: org.http4s.InvalidMessageBodyFailure => BadRequest(ErrorResponse(e.getMessage))
          case _ => InternalServerError()
        }

      case GET -> Root / "protocol" / id =>
        for {
          protocol <- CompendiumService[F].recoverProtocol(id)
          resp     <- protocol.fold(NotFound())(Ok(_))
        } yield resp

      case GET -> Root / "protocol" / _ / "generate" :? TargetQueryParam(_) =>
        NotImplemented()

    }
  }
}
