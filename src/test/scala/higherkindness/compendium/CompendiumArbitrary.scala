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

package higherkindness.compendium

import cats.syntax.apply._
import higherkindness.compendium.core.refinements.ProtocolId
import higherkindness.compendium.models._
import org.scalacheck._
import org.scalacheck.cats.implicits._

trait CompendiumArbitrary {

  implicit val protocolArbitrary: Arbitrary[Protocol] = Arbitrary {
    Gen.alphaNumStr.map(Protocol.apply)
  }

  implicit val protocolIdArbitrary: Arbitrary[ProtocolId] = Arbitrary {
    Gen
      .nonEmptyListOf(
        Gen.frequency(95 -> Gen.alphaNumChar, 4 -> Gen.const('-'), 1 -> Gen.const('.'))
      )
      .map(id => ProtocolId.unsafeFrom(id.mkString.take(200)))
  }

  implicit val idlNamesArbitrary: Arbitrary[IdlName] = Arbitrary {
    Gen.oneOf(IdlName.values)
  }

  implicit val metaProtocolArbitrary: Arbitrary[ProtocolMetadata] = Arbitrary {
    (
      protocolIdArbitrary.arbitrary,
      idlNamesArbitrary.arbitrary,
      protocolVersionArb.arbitrary
    ).mapN(ProtocolMetadata.apply)
  }

  implicit val differentIdentifiersArb: Arbitrary[DifferentIdentifiers] = Arbitrary {
    for {
      id1 <- Gen.alphaStr
      id2 <- Gen.alphaStr.suchThat(id => !id.equalsIgnoreCase(id1))
    } yield DifferentIdentifiers(id1, id2)
  }

  private def genVersion[A](f: Int => A) = Gen.posNum[Int].map(f(_))

  implicit val additionVersionArb: Arbitrary[AdditionVersion] = Arbitrary(
    genVersion(AdditionVersion(_))
  )

  implicit val revisionVersionArb: Arbitrary[RevisionVersion] = Arbitrary(
    genVersion(RevisionVersion(_))
  )

  implicit val modelVersionArb: Arbitrary[ModelVersion] = Arbitrary(
    genVersion(ModelVersion(_))
  )

  implicit val protocolVersionArb: Arbitrary[ProtocolVersion] = Arbitrary(
    (modelVersionArb.arbitrary, revisionVersionArb.arbitrary, additionVersionArb.arbitrary)
      .mapN(ProtocolVersion.apply)
  )

  implicit val rawProtocolVersionArb: Arbitrary[String] = Arbitrary(
    (Gen.posNum[Int], Gen.posNum[Int], Gen.posNum[Int]).mapN { case (f, m, t) => s"$f.$m.$t" }
  )

  implicit val invalidProtocolVersion: Arbitrary[String] = Arbitrary(
    (Gen.alphaChar, Gen.alphaChar, Gen.alphaChar).mapN { case (f, m, t) => s"$f.$m.$t" }
  )

}

object CompendiumArbitrary extends CompendiumArbitrary
