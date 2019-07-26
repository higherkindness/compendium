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
import cats.implicits._
import higherkindness.compendium.core.CompendiumService
import higherkindness.compendium.core.refinements._
import higherkindness.compendium.http.QueryParams.{IdlNameParam, ProtoVersion, TargetParam}
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
      case req @ POST -> Root / "protocol" / id :? IdlNameParam(validated) =>
        val idlValidation =
          Sync[F].fromValidated(validated.leftMap(errs => UnknownTargetError(errs.toList.mkString)))

        (for {
          protocol   <- req.as[Protocol]
          protocolId <- ProtocolId.parseOrRaise(id)
          exists     <- CompendiumService[F].existsProtocol(protocolId)
          idlName    <- idlValidation
          version    <- CompendiumService[F].storeProtocol(protocolId, protocol, idlName)
          response   <- exists.fold(Ok(), Created(version.value))
        } yield response.putHeaders(Location(req.uri.withPath(s"${req.uri.path}")))).recoverWith {
          case e: org.apache.avro.SchemaParseException => BadRequest(ErrorResponse(e.getMessage))
          case e: org.http4s.InvalidMessageBodyFailure => BadRequest(ErrorResponse(e.getMessage))
          case e: ProtocolIdError                      => BadRequest(ErrorResponse(e.msg))
          case e: UnknownTargetError                   => BadRequest(ErrorResponse(e.msg))
          case _                                       => InternalServerError()
        }

      case GET -> Root / "protocol" / id :? ProtoVersion(versionParam) =>
        val idlValidation = versionParam.traverse { validated =>
          val validation = validated.leftMap(errs => ProtocolVersionError(errs.toList.mkString))
          Sync[F].fromValidated(validation)
        }

        (for {
          protocolId   <- ProtocolId.parseOrRaise(id)
          maybeIdlName <- idlValidation
          protocol     <- CompendiumService[F].retrieveProtocol(protocolId, maybeIdlName)
          response     <- protocol.fold(NotFound())(mp => Ok(mp.protocol))
        } yield response).recoverWith {
          case e: ProtocolIdError      => BadRequest(ErrorResponse(e.msg))
          case e: ProtocolVersionError => BadRequest(ErrorResponse(e.msg))
          case _                       => InternalServerError()
        }

      case GET -> Root / "protocol" / id / "transform" :? TargetParam(validated) =>
        val idlValidation =
          Sync[F].fromValidated(validated.leftMap(errs => UnknownTargetError(errs.toList.mkString)))

        (for {
          protocolId <- ProtocolId.parseOrRaise(id)
          idlName    <- idlValidation
          transform  <- CompendiumService[F].transformProtocol(protocolId, idlName, None)
          response   <- transform.fold(pe => InternalServerError(pe.msg), mp => Ok(mp.protocol.raw))
        } yield response).recoverWith {
          case e: ProtocolIdError    => BadRequest(ErrorResponse(e.msg))
          case e: UnknownTargetError => BadRequest(ErrorResponse(e.msg))
          case _                     => InternalServerError()
        }
    }
  }
}
