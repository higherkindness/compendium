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

import cats.effect.IO
import cats.implicits._
import org.specs2.mutable.Specification
import org.specs2.matcher.IOMatchers
import org.specs2.ScalaCheck
import org.scalacheck.Prop
import higherkindness.compendium.CompendiumArbitrary._
import higherkindness.compendium.core.refinements.ProtocolVersionRefined
import higherkindness.compendium.models.ProtocolVersionError

object refinementsSpec extends Specification with ScalaCheck with IOMatchers {

  private val shortVersion: String => String = _.dropWhile(_ != '.').tail

  "ProtocolVersionRefined" >> {
    "parseOrRaise should parse a correct version of type xx.yy.zz" >> Prop.forAll(
      rawProtocolVersionArb.arbitrary
    ) { rawVersion =>
      ProtocolVersionRefined.parseOrRaise[IO](rawVersion).map(_.show) must returnValue(rawVersion)
    }

    "parseOrRaise should parse a correct version of type xx.yy" >> Prop.forAll(
      rawProtocolVersionArb.arbitrary
    ) { rawVersion =>
      val fixedVersion = shortVersion(rawVersion)
      ProtocolVersionRefined.parseOrRaise[IO](fixedVersion).map(_.show) must returnValue(
        fixedVersion |+| ".0"
      )
    }

    "parseOrRaise should parse a correct version of type xx" >> Prop.forAll(
      rawProtocolVersionArb.arbitrary
    ) { rawVersion =>
      val fixedVersion = shortVersion(shortVersion(rawVersion))
      ProtocolVersionRefined.parseOrRaise[IO](fixedVersion).map(_.show) must returnValue(
        fixedVersion |+| ".0.0"
      )
    }

    "parseOrRaise shoudl raise a ProtocolVersionError on error" >> Prop.forAll(
      invalidProtocolVersion.arbitrary
    ) { rawVersion =>
      ProtocolVersionRefined
        .parseOrRaise[IO](rawVersion)
        .unsafeRunSync() must throwA[ProtocolVersionError]
    }
  }
}
