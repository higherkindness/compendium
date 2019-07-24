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

import doobie.syntax.string._
import doobie.{Query0, Update0}
import higherkindness.compendium.core.refinements.{ProtocolId, ProtocolVersion}
import higherkindness.compendium.core.doobie.implicits._
import higherkindness.compendium.models.Protocol

object Queries {

  def store(id: ProtocolId, version: ProtocolVersion, protocol: Protocol): Update0 =
    sql"""
        INSERT INTO protocols 
        VALUES ($id, $version, $protocol)
        ON CONFLICT (id, version) DO NOTHING
       """.update

  def retrieve(id: ProtocolId, version: ProtocolVersion): Query0[Protocol] =
    sql"""
        SELECT protocol
        FROM protocols
        WHERE id=$id AND version=$version
      """.query[Protocol]

  def exists(id: ProtocolId): Query0[Boolean] =
    sql"""SELECT exists (SELECT true FROM protocols WHERE id=$id)""".query[Boolean]

}
