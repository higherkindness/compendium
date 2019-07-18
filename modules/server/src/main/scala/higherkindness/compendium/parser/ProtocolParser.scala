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

    private def skeuomorphParse(fp: FullProtocol, target: IdlName): F[FullProtocol] =
      (fp.metadata.idlName, target) match {
        // (from, to)
        case _ if fp.metadata.idlName.entryName == target.entryName => Sync[F].pure(fp)
        case (IdlName.Avro, IdlName.Mu) =>
          Sync[F]
            .delay(
              mu.print.proto.print(mu.Protocol.fromAvroProtocol(
                avro.Protocol.fromProto(AvroProtocol.parse(fp.protocol.raw)))))
            .fmap(p =>
              FullProtocol
                .apply(fp.metadata.copy(idlName = IdlName.withName(target.entryName)), Protocol(p)))
        case (IdlName.Protobuf, IdlName.Mu) =>
          parseProtobufRaw(fp.protocol.raw) { source =>
            ParseProto
              .parseProto[F, Mu[ProtobufF]]
              .parse(source)
              .fmap(mu.Protocol.fromProtobufProto(_))
              .fmap(
                p =>
                  FullProtocol(
                    fp.metadata.copy(idlName = IdlName.withName(target.entryName)),
                    Protocol(mu.print.proto.print(p))))
          }
      }

    override def parse(protocol: FullProtocol, target: IdlName): F[ParserResult] =
      skeuomorphParse(protocol, target).map(_.asRight[ParserError])
  }

}