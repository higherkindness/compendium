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

package higherkindness.compendium.db

import cats.effect.IO
import cats.effect.concurrent.Ref
import higherkindness.compendium.models.{IdlNames, MetaProtocolDB}
import higherkindness.compendium.core.refinements.ProtocolId

class DBServiceStub(val exists: Boolean, refProt: Option[Ref[IO, MetaProtocolDB]] = None)
    extends DBService[IO] {
  override def upsertProtocol(id: ProtocolId, idlNames: IdlNames): IO[Unit] = IO.unit
  override def existsProtocol(id: ProtocolId): IO[Boolean]                  = IO.pure(exists)
  override def ping(): IO[Boolean]                                          = IO.pure(exists)

  override def selectProtocolBytId(id: ProtocolId): IO[MetaProtocolDB] =
    refProt.fold[IO[MetaProtocolDB]](IO.raiseError(new Throwable("Protocol not found")))(
      _.get.flatMap(mp =>
        if (mp.id == id.value) IO(mp) else IO.raiseError(new Throwable("Protocol not found"))))
}
