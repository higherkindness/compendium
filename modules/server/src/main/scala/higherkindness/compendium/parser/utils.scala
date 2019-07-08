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

import java.io.{File, PrintWriter}

import cats.effect.Sync
import cats.syntax.flatMap._
import higherkindness.compendium.models.DBModels.MetaProtocol
import higherkindness.skeuomorph.protobuf.ParseProto
import higherkindness.skeuomorph.protobuf.ParseProto.ProtoSource

import scala.util.Random

object utils {

  def parseProtobufRaw[F[_]: Sync](raw: String)(
      f: ProtoSource => F[MetaProtocol]): F[MetaProtocol] = {

    val fileName = s"${Random.alphanumeric}.proto"
    val path     = "/tmp"
    val filePath = s"$path/$fileName"

    Sync[F]
      .delay {
        new PrintWriter(filePath) {
          write(raw); close()
        }
        ParseProto.ProtoSource(fileName, path)
      }
      .flatMap(f)
      .flatTap(
        _ =>
          Sync[F].delay(
            new File(filePath).delete()
        ))
  }

}
