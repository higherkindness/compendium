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

import cats.effect.{Resource, Sync}
import cats.implicits._
import higherkindness.compendium.models.{IdlName, Protocol}
import higherkindness.compendium.models.transformer.types.SchemaParseException
import higherkindness.droste.data.Mu
import higherkindness.skeuomorph.protobuf.{ProtobufF, Protocol => ProtobufProtocol}
import higherkindness.skeuomorph.protobuf.ParseProto._
import org.apache.avro.Schema

trait ProtocolUtils[F[_]] {
  def validateProtocol(protocol: Protocol, schema: IdlName): F[Protocol]
}

object ProtocolUtils {

  final case class FilePrintWriter(file: File, pw: PrintWriter)

  private def parserAvro: Schema.Parser = new Schema.Parser()

  private def parserProtobuf[F[_]: Sync](raw: String): F[ProtobufProtocol[Mu[ProtobufF]]] = {
    for {
      tmpFile <- writeTempFile(raw)
      p <- parseProto[F, Mu[ProtobufF]].parse(
        ProtoSource(
          tmpFile.file.getName,
          tmpFile.file.getAbsolutePath.replaceAll(tmpFile.file.getName, "")
        )
      )
    } yield p
  }

  private def writeTempFile[F[_]: Sync](msg: String): F[FilePrintWriter] =
    Resource
      .make(F.delay {
        val file = File.createTempFile("protoTempFile", ".proto")
        file.deleteOnExit()
        FilePrintWriter(file, new PrintWriter(file))
      }) { fpw: FilePrintWriter => F.delay(fpw.pw.close()) }
      .use((fpw: FilePrintWriter) => F.delay(fpw.pw.write(msg)).as(fpw))

  def impl[F[_]: Sync]: ProtocolUtils[F] = new ProtocolUtils[F] {

    override def validateProtocol(protocol: Protocol, schema: IdlName): F[Protocol] =
      if (protocol.raw.trim.isEmpty)
        F.raiseError(SchemaParseException("Protocol is empty"))
      else
        schema match {
          case IdlName.Avro =>
            F.delay(parserAvro.parse(protocol.raw))
              .map(_ => protocol)
              .handleErrorWith(e =>
                F.raiseError(
                  SchemaParseException("Avro schema provided not valid. " + e.getMessage)
                )
              )
          case IdlName.Protobuf =>
            parserProtobuf(protocol.raw)
              .map(_ => protocol)
              .handleErrorWith(e =>
                F.raiseError(
                  SchemaParseException("Protobuf schema provided not valid. " + e.getMessage)
                )
              )
          case _ => F.raiseError(SchemaParseException(s"$schema type not implemented yet"))
        }
  }

  def apply[F[_]: ProtocolUtils]: ProtocolUtils[F] = F
}
