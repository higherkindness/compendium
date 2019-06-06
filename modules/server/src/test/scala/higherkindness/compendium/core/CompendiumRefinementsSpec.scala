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
import higherkindness.compendium.core.refinements._
import higherkindness.compendium.models.ProtocolIdentifierError
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

object CompendiumRefinementsSpec extends Specification with ScalaCheck {

  sequential

  "Given a runtime string value to be refined into protocol id" >> {
    "Returns a valid protocol id if refining was successful" >> {
      val rawProtocolId = "super-domain.proto"

      val refine = validateProtocolId[IO](rawProtocolId)(new Exception(_))

      refine.unsafeRunSync() === ProtocolId("super-domain.proto")
    }

    "Returns a given exception if refining was unsuccessful" >> {
      val rawProtocolId = "invalid_dom@in"

      val refine = validateProtocolId[IO](rawProtocolId)(ProtocolIdentifierError)

      refine.unsafeRunSync() must throwA[ProtocolIdentifierError]
    }
  }

}
