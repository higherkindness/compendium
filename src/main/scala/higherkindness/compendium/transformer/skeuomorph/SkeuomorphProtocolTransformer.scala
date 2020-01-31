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

package higherkindness.compendium.transformer.skeuomorph

import cats.effect.Sync
import cats.implicits._
import higherkindness.compendium.models.{FullProtocol, IdlName, Protocol, ProtocolMetadata}
import higherkindness.compendium.models.transformer.types._
import higherkindness.compendium.transformer.ProtocolTransformer
import higherkindness.skeuomorph.protobuf.ParseProto.ProtoSource
import higherkindness.skeuomorph.{avro, mu, protobuf}
import org.apache.avro.{Protocol => AvroProtocol}
import higherkindness.droste.data.Mu
import higherkindness.droste.data.Mu._
import scala.meta._

object SkeuomorphProtocolTransformer {

  def apply[F[_]: Sync]: ProtocolTransformer[F] = new ProtocolTransformer[F] {

    import higherkindness.compendium.transformer.protobuf.parsing._

    private def protobufToMu(
        source: ProtoSource,
        oldMetadata: ProtocolMetadata,
        streamCtor: (Type, Type) => Type.Apply
    ): F[FullProtocol] =
      protobuf.ParseProto
        .parseProto[F, Mu[protobuf.ProtobufF]]
        .parse(source)
        .map(protobuf => mu.Protocol.fromProtobufProto(mu.CompressionType.Identity, true)(protobuf))
        .flatMap(p =>
          Sync[F]
            .fromEither(mu.codegen.protocol(p, streamCtor).leftMap(TransformError).map(_.syntax))
        )
        .map(p => FullProtocol(oldMetadata.copy(idlName = IdlName.Mu), Protocol(p)))

    private def skeuomorphTransformation(fp: FullProtocol, target: IdlName): F[FullProtocol] = {
      val streamCtor: (Type, Type) => Type.Apply = {
        case (f, a) => t"_root_.fs2.Stream[$f, $a]"
      }

      (fp.metadata.idlName, target) match {
        // (from, to)
        case _ if fp.metadata.idlName.entryName == target.entryName => Sync[F].pure(fp)
        case (IdlName.Avro, IdlName.Mu) =>
          for {
            avroProto      <- F.delay(AvroProtocol.parse(fp.protocol.raw))
            skeuoAvroProto <- F.delay(avro.Protocol.fromProto(avroProto))
            muProto <- F.delay(
              mu.Protocol.fromAvroProtocol(mu.CompressionType.Identity, true)(skeuoAvroProto)
            )
            proto <- F.fromEither(
              mu.codegen.protocol(muProto, streamCtor).leftMap(TransformError).map(_.syntax)
            )
          } yield {
            val targetMetadata = fp.metadata.copy(idlName = IdlName.Mu)
            val targetProto    = Protocol(proto)
            FullProtocol(targetMetadata, targetProto)
          }
        case (IdlName.Protobuf, IdlName.Mu) =>
          transformProtobuf(fp.protocol.raw)(protobufToMu(_, fp.metadata, streamCtor))
      }

    }

    override def transform(protocol: FullProtocol, target: IdlName): F[FullProtocol] =
      skeuomorphTransformation(protocol, target)
  }

}
