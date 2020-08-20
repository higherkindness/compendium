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

import cats.MonadError
import cats.data.{Kleisli, OptionT}
import cats.syntax.option._
import cats.syntax.applicativeError._
import higherkindness.compendium.models._
import higherkindness.compendium.models.transformer.types.SchemaParseException
import org.http4s._

object RoutesMiddleware {

  def middleware[F[_]: MonadError[*[_], Throwable]](httpRoutes: HttpRoutes[F]): HttpRoutes[F] = {

    def genResponse(s: Status, msg: String): Option[Response[F]] =
      Response[F](s).withEntity(msg).some

    def badRequest(msg: String) = genResponse(Status.BadRequest, msg)
    def notFound(msg: String)   = genResponse(Status.NotFound, msg)

    Kleisli { req =>
      OptionT {
        httpRoutes.run(req).value.handleError {
          case e: SchemaParseException      => badRequest(e.getMessage)
          case e: InvalidMessageBodyFailure => badRequest(e.getMessage)
          case e: ProtocolIdError           => badRequest(e.getMessage)
          case e: ProtocolNotFound          => notFound(e.getMessage)
          case e: UnknownIdlName            => badRequest(e.getMessage)
          case e: ProtocolVersionError      => badRequest(e.getMessage)
          case e: OptValidationError        => badRequest(e.getMessage)
          case e                            => genResponse(Status.InternalServerError, e.getMessage)
        }
      }
    }

  }

}
