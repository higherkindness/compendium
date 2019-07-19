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

package higherkindness.compendium

import cats.syntax.apply._
import higherkindness.compendium.core.refinements.{ProtocolId, ProtocolVersion}
import higherkindness.compendium.models.{IdlName, Protocol, ProtocolMetadata}
import org.scalacheck._
import org.scalacheck.cats.implicits._
import eu.timepit.refined.scalacheck.numeric._

trait CompendiumArbitrary {

  implicit val protocolArbitrary: Arbitrary[Protocol] = Arbitrary {
    Gen.alphaNumStr.map(Protocol.apply)
  }

  implicit val protocolIdArbitrary: Arbitrary[ProtocolId] = Arbitrary {
    Gen
      .nonEmptyListOf(
        Gen.oneOf(Gen.alphaNumChar, Gen.const('-'), Gen.const('.'))
      )
      .suchThat(_.length > 10)
      .map(id => ProtocolId.unsafeFrom(id.mkString))
  }

  implicit val idlNamesArbitrary: Arbitrary[IdlName] = Arbitrary {
    Gen.oneOf(IdlName.values)
  }

  implicit val metaProtocolArbitrary: Arbitrary[ProtocolMetadata] = Arbitrary {
    (
      protocolIdArbitrary.arbitrary,
      idlNamesArbitrary.arbitrary,
      Arbitrary.arbitrary[ProtocolVersion])
      .mapN(ProtocolMetadata.apply)
  }

  implicit val differentIdentifiersArb: Arbitrary[DifferentIdentifiers] = Arbitrary {
    for {
      id1 <- Gen.alphaStr
      id2 <- Gen.alphaStr.suchThat(id => !id.equalsIgnoreCase(id1))
    } yield DifferentIdentifiers(id1, id2)
  }

}

object CompendiumArbitrary extends CompendiumArbitrary
