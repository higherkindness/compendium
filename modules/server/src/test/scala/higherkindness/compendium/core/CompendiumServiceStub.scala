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

package higherkindness.compendium.core

import cats.effect.IO
import higherkindness.compendium.core.refinements.ProtocolId
import higherkindness.compendium.models.DBModels.MetaProtocol
import higherkindness.compendium.models.parserModels.ParserResult
import higherkindness.compendium.models._

class CompendiumServiceStub(val protocolOpt: Option[MetaProtocol], exists: Boolean)
    extends CompendiumService[IO] {
  override def storeProtocol(id: ProtocolId, protocol: Protocol, idlName: IdlNames): IO[Unit] =
    IO.unit
  override def recoverProtocol(id: ProtocolId): IO[Option[MetaProtocol]] = IO(protocolOpt)
  override def existsProtocol(protocolId: ProtocolId): IO[Boolean]       = IO(exists)

  override def parseProtocol(protocolId: ProtocolId, target: Target): IO[ParserResult] = ???
}
