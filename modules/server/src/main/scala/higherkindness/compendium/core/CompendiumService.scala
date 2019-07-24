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
import higherkindness.compendium.core.refinements._
import higherkindness.compendium.db.DBService
import higherkindness.compendium.models._
import higherkindness.compendium.models.transformers.types.{TransformError, TransformResult}
import higherkindness.compendium.parser.ProtocolTransformer
import higherkindness.compendium.storage.Storage

trait CompendiumService[F[_]] {

  def storeProtocol(id: ProtocolId, protocol: Protocol, idlName: IdlName): F[ProtocolVersion]
  def recoverProtocol(id: ProtocolId, version: Option[ProtocolVersion]): F[Option[FullProtocol]]
  def existsProtocol(id: ProtocolId): F[Boolean]
  def transformProtocol(
      id: ProtocolId,
      target: IdlName,
      version: Option[ProtocolVersion]): F[TransformResult]
}

object CompendiumService {

  implicit def impl[F[_]: Sync: Storage: DBService: ProtocolUtils: ProtocolTransformer]: CompendiumService[
    F] =
    new CompendiumService[F] {

      override def storeProtocol(
          id: ProtocolId,
          protocol: Protocol,
          idlName: IdlName): F[ProtocolVersion] =
        for {
          _       <- ProtocolUtils[F].validateProtocol(protocol)
          version <- DBService[F].upsertProtocol(id, idlName)
          _       <- Storage[F].store(id, version, protocol)
        } yield version

      override def recoverProtocol(
          id: ProtocolId,
          version: Option[ProtocolVersion]): F[Option[FullProtocol]] =
        for {
          maybeMetadata <- DBService[F].selectProtocolMetadataById(id)
          maybeProto <- maybeMetadata.flatTraverse(metadata =>
            Storage[F].recover(version.fold(metadata)(version => metadata.copy(version = version))))
        } yield maybeProto

      override def existsProtocol(id: ProtocolId): F[Boolean] =
        DBService[F].existsProtocol(id)

      override def transformProtocol(
          id: ProtocolId,
          target: IdlName,
          version: Option[ProtocolVersion]): F[TransformResult] =
        for {
          maybeProto  <- recoverProtocol(id, version)
          maybeResult <- maybeProto.traverse(ProtocolTransformer[F].transform(_, target))
        } yield
          maybeResult.getOrElse(
            TransformError(s"No Protocol Found with id: $id").asLeft[FullProtocol])
    }

  def apply[F[_]](implicit F: CompendiumService[F]): CompendiumService[F] = F
}
