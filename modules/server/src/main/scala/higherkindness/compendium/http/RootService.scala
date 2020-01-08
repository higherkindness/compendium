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

import cats.data.ValidatedNel
import cats.effect.Sync
import cats.implicits._
import higherkindness.compendium.core.CompendiumService
import higherkindness.compendium.core.refinements._
import higherkindness.compendium.http.QueryParams.{IdlNameParam, ProtoVersion, TargetParam}
import higherkindness.compendium.models._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Location
import org.http4s.{HttpRoutes, ParseFailure}

object RootService {

  def rootRouteService[F[_]: Sync: CompendiumService]: HttpRoutes[F] = {

    object f extends Http4sDsl[F]
    import f._

    def idlValidation(idlNameValidated: ValidatedNel[ParseFailure, IdlName]): F[IdlName] =
      Sync[F].fromValidated(idlNameValidated.leftMap(errs => UnknownIdlName(errs.toList.mkString)))

    def versionValidation(
        maybeVersionValidated: Option[ValidatedNel[ParseFailure, ProtocolVersion]]
    ): F[Option[ProtocolVersion]] =
      maybeVersionValidated.traverse { validated =>
        val validation = validated.leftMap(errs => ProtocolVersionError(errs.toList.mkString))
        Sync[F].fromValidated(validation)
      }

    val routes = HttpRoutes.of[F] {
      case req @ POST -> Root / "protocol" / id :? IdlNameParam(idlNameValidated) =>
        for {
          protocolId <- ProtocolId.parseOrRaise(id)
          idlName    <- idlValidation(idlNameValidated)
          protocol   <- req.as[Protocol]
          version    <- CompendiumService[F].storeProtocol(protocolId, protocol, idlName)
          response   <- Created(version.value)
        } yield response.putHeaders(Location(req.uri.withPath(s"${req.uri.path}")))

      case GET -> Root / "protocol" / id :? ProtoVersion(maybeVersionValidated) =>
        for {
          protocolId   <- ProtocolId.parseOrRaise(id)
          maybeVersion <- versionValidation(maybeVersionValidated)
          fullProtocol <- CompendiumService[F].retrieveProtocol(protocolId, maybeVersion)
          response     <- Ok(fullProtocol.protocol)
        } yield response

      case GET -> Root / "protocol" / id / "transformation" :? TargetParam(idlNameValidated) +& ProtoVersion(
            maybeVersionValidated
          ) =>
        for {
          protocolId   <- ProtocolId.parseOrRaise(id)
          maybeVersion <- versionValidation(maybeVersionValidated)
          idlName      <- idlValidation(idlNameValidated)
          protocol     <- CompendiumService[F].retrieveProtocol(protocolId, maybeVersion)
          transform    <- CompendiumService[F].transformProtocol(protocol, idlName)
          response     <- Ok(transform.protocol)
        } yield response
    }

    RoutesMiddleware.middleware(routes)
  }
}
