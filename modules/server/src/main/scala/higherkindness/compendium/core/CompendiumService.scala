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

package higherkindness.compendium.core

import cats.effect.Sync
import cats.implicits._
import higherkindness.compendium.core.refinements.{ProtocolId, ProtocolVersion}
import higherkindness.compendium.db.DBService
import higherkindness.compendium.models._
import higherkindness.compendium.models.parserModels.{ParserError, ParserResult}
import higherkindness.compendium.parser.ProtocolParserService
import higherkindness.compendium.storage.Storage

trait CompendiumService[F[_]] {

  def storeProtocol(id: ProtocolId, protocol: Protocol, idlName: IdlName): F[ProtocolVersion]
  def recoverProtocol(protocolId: ProtocolId): F[Option[FullProtocol]]
  def recoverProtocolVersion(id: ProtocolId, version: ProtocolVersion): F[Option[FullProtocol]]
  def existsProtocol(protocolId: ProtocolId): F[Boolean]
  def parseProtocol(protocolName: ProtocolId, target: IdlName): F[ParserResult]
}

object CompendiumService {

  implicit def impl[F[_]: Sync: Storage: DBService: ProtocolUtils: ProtocolParserService]: CompendiumService[
    F] =
    new CompendiumService[F] {

      private def getProtocol(
          id: ProtocolId,
          maybeVersion: Option[ProtocolVersion] = None): F[Option[FullProtocol]] =
        for {
          maybeMetadata <- DBService[F].selectProtocolMetadataById(id)
          maybeProto <- maybeMetadata.flatTraverse(metadata =>
            Storage[F].recover(maybeVersion.fold(metadata)(version =>
              metadata.copy(version = version))))
        } yield maybeProto

      override def storeProtocol(
          id: ProtocolId,
          protocol: Protocol,
          idlName: IdlName): F[ProtocolVersion] =
        for {
          _       <- ProtocolUtils[F].validateProtocol(protocol)
          version <- DBService[F].upsertProtocol(id, idlName)
          _       <- Storage[F].store(id, protocol)
        } yield version

      override def recoverProtocol(protocolId: ProtocolId): F[Option[FullProtocol]] =
        getProtocol(protocolId)

      override def recoverProtocolVersion(
          id: ProtocolId,
          version: ProtocolVersion): F[Option[FullProtocol]] = getProtocol(id, Option(version))

      override def existsProtocol(protocolId: ProtocolId): F[Boolean] =
        DBService[F].existsProtocol(protocolId)

      override def parseProtocol(protocolId: ProtocolId, target: IdlName): F[ParserResult] =
        recoverProtocol(protocolId).flatMap {
          case Some(protocol) => ProtocolParserService[F].parse(protocol, target)
          case _ =>
            Sync[F].pure(
              ParserError(s"No Protocol Found with id: $protocolId").asLeft[FullProtocol])
        }
    }

  def apply[F[_]](implicit F: CompendiumService[F]): CompendiumService[F] = F
}
