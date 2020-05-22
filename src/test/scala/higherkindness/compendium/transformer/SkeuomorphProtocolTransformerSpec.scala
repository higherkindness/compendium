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

package higherkindness.compendium.transformer

import cats.effect.IO
import higherkindness.compendium.core.refinements.ProtocolId
import higherkindness.compendium.models._
import higherkindness.compendium.transformer.skeuomorph.SkeuomorphProtocolTransformer
import org.specs2.mutable.Specification

class SkeuomorphProtocolTransformerSpec extends Specification {

  import protocols._

  val transformer     = SkeuomorphProtocolTransformer[IO]
  val protocolVersion = ProtocolVersion.initial

  "Skeuomorph based protocol transformer" should {
    "Transform a simple Avro Schema to Mu" >> {
      val protocolMetadata = ProtocolMetadata(ProtocolId("id"), IdlName.Avro, protocolVersion)
      val fullProtocol     = FullProtocol(protocolMetadata, Protocol(simpleAvroExample))

      val transformResult = transformer.transform(fullProtocol, IdlName.Mu)

      transformResult.attempt.unsafeRunSync() should beRight
    }

    "Transform a simple Protobuf Schema to Mu" >> {
      val protocolMetadata =
        ProtocolMetadata(ProtocolId("id"), IdlName.Protobuf, protocolVersion)
      val fullProtocol = FullProtocol(protocolMetadata, Protocol(simpleProtobufExample))

      val transformResult = transformer.transform(fullProtocol, IdlName.Mu)

      transformResult.attempt.unsafeRunSync() should beRight
    }
  }

}
