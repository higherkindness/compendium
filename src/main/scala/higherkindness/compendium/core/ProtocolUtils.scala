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

package higherkindness.compendium.core

import java.io.{File, PrintWriter}

import cats.effect.Sync
import cats.implicits._
import higherkindness.compendium.models.{IdlName, Protocol}
import higherkindness.compendium.models.transformer.types.SchemaParseException
import higherkindness.droste.data.Mu
import higherkindness.skeuomorph.protobuf.ParseProto.ProtoSource
import higherkindness.skeuomorph.protobuf.ProtobufF
import higherkindness.skeuomorph.protobuf.ParseProto._
import org.apache.avro.Schema

import higherkindness.skeuomorph.protobuf

trait ProtocolUtils[F[_]] {
  def validateProtocol(protocol: Protocol, schema: IdlName): F[Protocol]
}

object ProtocolUtils {

  private def parserAvro: Schema.Parser = new Schema.Parser()

  private def parserProtobuf[F[_]: Sync](raw: String): F[protobuf.Protocol[Mu[ProtobufF]]] = {
    for {
      tmpFile <- writeTempFile(raw)
      p <- parseProto[F, Mu[ProtobufF]].parse(
        ProtoSource(tmpFile.getName, tmpFile.getAbsolutePath.replaceAll(tmpFile.getName, ""))
      )
    } yield p
  }

  private def writeTempFile[F[_]: Sync](msg: String): F[File] = {
    F.delay {
      val file = File.createTempFile("protoTempFile", ".proto")
      file.deleteOnExit
      val pw = new PrintWriter(file)
      pw.write(msg)
      pw.close()
      file
    }
  }

  def impl[F[_]: Sync]: ProtocolUtils[F] = new ProtocolUtils[F] {
    override def validateProtocol(protocol: Protocol, schema: IdlName): F[Protocol] =
      if (protocol.raw.trim.isEmpty)
        F.raiseError(SchemaParseException("Protocol is empty"))
      else
        schema match {
          case IdlName.Avro =>
            F.catchNonFatal(parserAvro.parse(protocol.raw))
              .map(_ => protocol)
              .handleErrorWith(e => F.raiseError(SchemaParseException(e.getMessage)))
          case IdlName.Protobuf =>
            parserProtobuf(protocol.raw)
              .map(_ => protocol)
              .handleErrorWith(e => F.raiseError(SchemaParseException(e.getMessage)))

          case _ => F.raiseError(SchemaParseException("Schema type not implemented yet"))
        }
  }

  def apply[F[_]: ProtocolUtils]: ProtocolUtils[F] = F
}
