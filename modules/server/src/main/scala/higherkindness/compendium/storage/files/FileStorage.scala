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

package higherkindness.compendium.storage.files

import java.io.{File, FilenameFilter, PrintWriter}

import cats.effect.Sync
import cats.implicits._
import higherkindness.compendium.core.refinements.{ProtocolId, ProtocolVersion}
import higherkindness.compendium.models.config.FileStorageConfig
import higherkindness.compendium.models._
import higherkindness.compendium.storage.Storage

object FileStorage {

  /**
   * File pattern should match `{protocol identifier}_{version}.{extension}` where:
   *
   * - protocol identifier should comply with
   * [[higherkindness.compendium.core.refinements.ProtocolId]] predicates
   * - version should be a positive zero-leftpadded five digits version number like 00001
   * - extension is `protocol`
   */
  private[storage] def buildFilename(id: ProtocolId, version: ProtocolVersion): String =
    s"${id.value}_${f"${version.value}%05d"}.protocol"

  def apply[F[_]: Sync](config: FileStorageConfig): Storage[F] =
    new Storage[F] {

      override def store(id: ProtocolId, version: ProtocolVersion, protocol: Protocol): F[Unit] = {
        val filename = buildFilename(id, version)
        val path     = s"${config.path}${File.separator}$filename"

        for {
          _    <- Sync[F].catchNonFatal(new File(config.path.toUri).mkdirs)
          file <- Sync[F].catchNonFatal(new File(path))
          _ <- Sync[F].catchNonFatal {
            val printWriter = new PrintWriter(file)
            printWriter.write(protocol.raw)
            printWriter.close()
          }
        } yield ()
      }

      override def retrieve(protocolMetadata: ProtocolMetadata): F[FullProtocol] = {
        val filename = buildFilename(protocolMetadata.id, protocolMetadata.version)
        val file     = new File(s"${config.path}${File.separator}$filename")
        def checkFile: F[File] =
          Sync[F]
            .delay(file.exists)
            .ifM(Sync[F].delay(file), Sync[F].raiseError(FileNotFound(filename)))

        for {
          file   <- checkFile
          source <- Sync[F].delay(scala.io.Source.fromFile(file))
          proto  <- Sync[F].delay(Protocol(source.mkString))
        } yield FullProtocol(protocolMetadata, proto)
      }

      override def exists(id: ProtocolId): F[Boolean] = {
        val filter = new FilenameFilter {
          override def accept(dir: File, name: String): Boolean = name.startsWith(s"${id.value}_")
        }

        Sync[F].catchNonFatal(new File(s"${config.path}").listFiles(filter)).map(_.nonEmpty)
      }
    }

}
