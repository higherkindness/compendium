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

package higherkindness.compendium.transformer.protobuf

import java.io.PrintWriter
import java.nio.file.{Files, Path}

import cats.effect.{Bracket, Resource, Sync}
import higherkindness.compendium.models.FullProtocol
import higherkindness.skeuomorph.protobuf.ParseProto.ProtoSource

object parsing {

  def transformProtobuf[F[_]: Sync: Bracket[*[_], Throwable]](raw: String)(
      transformToTarget: ProtoSource => F[FullProtocol]): F[FullProtocol] = {

    val tmpFileCreation =
      F.delay(Files.createTempFile("compendium", "protobuf"))
    def printWriterCreation(tmpFile: Path): F[PrintWriter] =
      F.delay(new PrintWriter(tmpFile.toFile))

    val protoSource = for {
      tmpFile   <- Resource.liftF(tmpFileCreation)
      tmpWriter <- Resource.fromAutoCloseable(printWriterCreation(tmpFile))
      _         <- Resource.liftF(F.delay(tmpWriter.write(raw)))
      _         <- Resource.liftF(F.delay(tmpWriter.close()))
    } yield ProtoSource(tmpFile.getFileName.toString, tmpFile.getParent.toString)

    protoSource.use(transformToTarget)
  }

}
