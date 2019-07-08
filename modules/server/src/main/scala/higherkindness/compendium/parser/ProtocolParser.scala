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
import cats.syntax.functor._
import cats.syntax.either._
import higherkindness.compendium.models.DBModels.MetaProtocol
import higherkindness.compendium.models._
import higherkindness.compendium.models.parserModels._
import higherkindness.skeuomorph.mu
import higherkindness.skeuomorph.avro
import higherkindness.skeuomorph.protobuf.{ParseProto, ProtobufF}
import qq.droste.data.Mu._
import org.apache.avro.{Protocol => AvroProtocol}
import qq.droste.data.Mu

object ProtocolParser {

  def impl[F[_]: Sync]: ProtocolParserService[F] = new ProtocolParserService[F] {

    import utils._

    private def skeuomorphParse(mp: MetaProtocol, target: Target): F[MetaProtocol] =
      (mp.idlName, target) match {
        // (from, to)
        case _ if mp.idlName.entryName == target.entryName => Sync[F].pure(mp)
        case (IdlNames.Avro, Target.Mu) =>
          Sync[F]
            .delay(
              mu.print.proto.print(mu.Protocol.fromAvroProtocol(
                avro.Protocol.fromProto(AvroProtocol.parse(mp.protocol.raw)))))
            .fmap(p => MetaProtocol.apply(IdlNames.withName(target.entryName), Protocol(p)))
        case (IdlNames.Protobuf, Target.Mu) =>
          parseProtobufRaw(mp.protocol.raw) { source =>
            ParseProto
              .parseProto[F, Mu[ProtobufF]]
              .parse(source)
              .map(mu.Protocol.fromProtobufProto(_))
              .map(
                p =>
                  MetaProtocol(
                    IdlNames.withName(target.entryName),
                    Protocol(higherkindness.skeuomorph.mu.print.proto.print(p))))
          }
      }

    override def parse(protocol: Option[MetaProtocol], target: Target): F[ParserResult] =
      protocol.fold(Sync[F].pure(ParserError("No Protocol Found").asLeft[MetaProtocol]))(mp =>
        skeuomorphParse(mp, target).map(_.asRight[ParserError]))
  }

}
