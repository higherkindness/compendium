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

package higherkindness.compendium.models

import cats.implicits._
import cats.kernel.Monoid
import higherkindness.compendium.CompendiumArbitrary._
import higherkindness.compendium.models._
import higherkindness.compendium.models.ProtocolVersion._
import org.specs2.mutable.Specification
import org.specs2.ScalaCheck
import org.scalacheck.Prop

object ProtocolVersionSpec extends Specification with ScalaCheck {

  "Set" >> {
    "setAddition should set the correct addition value" >> prop { (pv: ProtocolVersion, addVer: AdditionVersion) =>
      pv.setAddition(addVer).addition === addVer
    }

    "setRevision should set the correct revision value and reset addition" >> prop {
      (pv: ProtocolVersion, revVer: RevisionVersion) =>
        val version = pv.setRevision(revVer)
        version.revision === revVer && version.addition === Monoid[AdditionVersion].empty
    }

    "setModel should set the correct model value and reset revision and addition" >> prop {
      (pv: ProtocolVersion, modelVer: ModelVersion) =>
        val version = pv.setModel(modelVer)
        version.model === modelVer && version.revision === Monoid[
          RevisionVersion
        ].empty && version.addition === Monoid[AdditionVersion].empty
    }
  }

  "Increment" >> {
    "incAddition should increment the addition" >> prop { protocolVersion: ProtocolVersion =>
      protocolVersion.incAddition === protocolVersion.setAddition(
        protocolVersion.addition |+| AdditionVersion(1)
      )
    }

    "incRevision should increment the revision" >> prop { protocolVersion: ProtocolVersion =>
      protocolVersion.incRevision === protocolVersion.setRevision(
        protocolVersion.revision |+| RevisionVersion(1)
      )
    }

    "incModel should increment the model" >> prop { protocolVersion: ProtocolVersion =>
      protocolVersion.incModel === protocolVersion.setModel(
        protocolVersion.model |+| ModelVersion(1)
      )
    }
  }

  "Show" >> {
    "Show should create the correct string" >> prop { protocolVersion: ProtocolVersion =>
      protocolVersion.show === show"${protocolVersion.model}.${protocolVersion.revision}.${protocolVersion.addition}"
    }
  }

  "Eq" >> {
    "Eq should work as expected" >> prop { protocolVersion: ProtocolVersion =>
      protocolVersion === protocolVersion && protocolVersion.incAddition =!= protocolVersion
    }
  }

  "Parsing" >> {
    val shortVersion: String => String = _.dropWhile(_ != '.').tail

    "fromString should parse a correct version of type xx.yy.zz" >> Prop.forAllNoShrink(
      rawProtocolVersionArb.arbitrary
    ) { rawVersion =>
      ProtocolVersion.fromString(rawVersion).map(_.show) must_== Right(rawVersion)
    }

    "fromString should parse a correct version of type xx.yy" >> Prop.forAllNoShrink(
      rawProtocolVersionArb.arbitrary
    ) { rawVersion =>
      val fixedVersion = shortVersion(rawVersion)
      ProtocolVersion.fromString(fixedVersion).map(_.show) must_== Right(fixedVersion |+| ".0")
    }

    "fromString should parse a correct version of type xx" >> Prop.forAllNoShrink(
      rawProtocolVersionArb.arbitrary
    ) { rawVersion =>
      val fixedVersion = shortVersion(shortVersion(rawVersion))
      ProtocolVersion.fromString(fixedVersion).map(_.show) must_== Right(fixedVersion |+| ".0.0")
    }

    "fromString should return a Left(ProtocolVersionError) on error" >> Prop.forAllNoShrink(
      invalidProtocolVersion.arbitrary
    ) { rawVersion =>
      ProtocolVersion.fromString(rawVersion) must beLeft[ProtocolVersionError]
    }
  }

}
