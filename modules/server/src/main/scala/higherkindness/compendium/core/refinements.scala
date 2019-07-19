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
import cats.syntax.all._
import eu.timepit.refined._
import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.boolean.{And, AnyOf}
import eu.timepit.refined.char.LetterOrDigit
import eu.timepit.refined.collection.{Forall, MaxSize}
import eu.timepit.refined.generic.Equal
import eu.timepit.refined.numeric.Positive
import higherkindness.compendium.models.{ProtocolIdError, ProtocolVersionError}
import shapeless.{::, HNil}

object refinements {

  // Protocol identifier size (based on filename size limits)
  type MaxProtocolIdSize = MaxSize[W.`255`.T]
  type ValidProtocolIdChars =
    Forall[AnyOf[LetterOrDigit :: Equal[W.`'-'`.T] :: Equal[W.`'.'`.T] :: HNil]]
  type ProtocolIdConstraints = And[MaxProtocolIdSize, ValidProtocolIdChars]

  type ProtocolId = String Refined ProtocolIdConstraints

  object ProtocolId extends RefinedTypeOps[ProtocolId, String] {
    def parseOrRaise[F[_]: Sync](id: String): F[ProtocolId] =
      Sync[F].fromEither(ProtocolId.from(id).leftMap(ProtocolIdError))
  }

  type ProtocolVersion = Int Refined Positive

  object ProtocolVersion extends RefinedTypeOps[ProtocolVersion, Int] {
    def parseOrRaise[F[_]: Sync](version: String): F[ProtocolVersion] =
      for {
        number <- Sync[F].delay(version.toInt)
        protoVersion <- Sync[F].fromEither(
          ProtocolVersion.from(number).leftMap(ProtocolVersionError))
      } yield protoVersion
  }
}
