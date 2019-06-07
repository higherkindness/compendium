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

package higherkindness.compendium.parser

import cats.effect.Sync
import cats.syntax.either._
import higherkindness.compendium.models.{IdlNames, MetaProtocol, Protocol, Target}
import higherkindness.compendium.models.parserModels._
import higherkindness.skeuomorph.avro
import higherkindness.skeuomorph.openapi


object ProtocolParser {

  def impl[F[_]: Sync]: ProtocolParserService[F] = new ProtocolParserService[F] {

    private def skeuomorphParse(mp: MetaProtocol, target: Target): Protocol = (mp.idlName, target) match {
      case _ if mp.idlName.entryName == target.entryName => mp.protocol
      case (IdlNames.Protobuf, Target.Avro) => avro.Protocol.fromProto()
    }

    override def parse(protocol: Option[MetaProtocol], target: Target): F[ParserResult] =
      protocol.fold(
        Sync[F].pure(ParserError("No Protocol Found").asLeft[Protocol]))(
        mp => Sync[F].delay(skeuomorphParse(mp, target).asRight[ParserError]))
  }

}
