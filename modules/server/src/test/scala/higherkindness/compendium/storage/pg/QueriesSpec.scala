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

package higherkindness.compendium.storage.pg

import doobie.specs2._
import higherkindness.compendium.core.refinements.ProtocolId
import higherkindness.compendium.db.MigrationsMode.Data
import higherkindness.compendium.db.PGHelper
import higherkindness.compendium.models.Protocol
import org.specs2.specification.Scope

class QueriesSpec extends PGHelper(Data) with IOChecker {

  "Queries" should {
    "match db model" in new context {
      check(Queries.protocolExists(protocolId))
      check(Queries.storeProtocol(protocolId, protocol))
      check(Queries.recoverProtocol(protocolId))
    }
  }

  trait context extends Scope {
    val protocolId = ProtocolId("my.test.protocol.id")
    val protocol   = Protocol("Raw protocol content")
  }

}