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

package higherkindness.compendium.metadata

import cats.effect.IO
import higherkindness.compendium.models.{IdlName, ProtocolMetadata, ProtocolNotFound}
import higherkindness.compendium.core.refinements.{ProtocolId, ProtocolVersion}

class MetadataStorageStub(val exists: Boolean, metadata: Option[ProtocolMetadata] = None)
    extends MetadataStorage[IO] {
  override def store(id: ProtocolId, idlNames: IdlName): IO[ProtocolVersion] =
    IO.pure(metadata.map(_.version).getOrElse(ProtocolVersion(1)))
  override def exists(id: ProtocolId): IO[Boolean] = IO.pure(exists)
  override def ping: IO[Boolean]                   = IO.pure(exists)

  override def retrieve(id: ProtocolId): IO[ProtocolMetadata] =
    metadata.fold(IO.raiseError[ProtocolMetadata](ProtocolNotFound("Protocol not found")))(mp =>
      if (mp.id == id) IO(mp)
      else IO.raiseError[ProtocolMetadata](ProtocolNotFound("Protocol not found"))
    )
}
