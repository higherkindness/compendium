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

package higherkindness.compendium.metadata.pg

import doobie.implicits._
import doobie.{Query0, Update0}
import higherkindness.compendium.core.doobie.implicits._
import higherkindness.compendium.core.refinements.ProtocolId
import higherkindness.compendium.models.ProtocolMetadata

object Queries {

  def exists(id: ProtocolId): Query0[Boolean] =
    sql"""
          SELECT exists (SELECT true FROM metaprotocols WHERE id=$id)
       """.query[Boolean]

  def store(id: ProtocolId, idl_name: String): Update0 =
    sql"""
          INSERT INTO metaprotocols (id, idl_name, version)
          VALUES ($id, $idl_name::idl, 1)
          ON CONFLICT (id) DO UPDATE SET version = metaprotocols.version + 1
          RETURNING version
       """.update

  def retrieve(id: ProtocolId): Query0[ProtocolMetadata] =
    sql"""
         SELECT * from metaprotocols WHERE id=$id
       """.query[ProtocolMetadata]

  def checkConn: Query0[Boolean] =
    sql"SELECT exists (SELECT 1)".query[Boolean]
}
