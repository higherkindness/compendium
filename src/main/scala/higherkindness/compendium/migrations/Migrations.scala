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

package higherkindness.compendium.migrations

import cats.effect.Sync
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import higherkindness.compendium.models.config.DatabaseStorageConfig
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.Location

object Migrations {

  def metadataLocation[F[_]: Sync]: F[Location] =
    Sync[F].delay(new Location("db/migration/metadata"))
  def dataLocation[F[_]: Sync]: F[Location] = Sync[F].delay(new Location("db/migration/data"))

  def makeMigrations[F[_]: Sync](conf: DatabaseStorageConfig, migrations: List[Location]): F[Int] =
    F.delay {
        Flyway
          .configure()
          .dataSource(conf.jdbcUrl, conf.username, conf.password)
          .locations(migrations: _*)
          .load()
          .migrate()
      }
      .attempt
      .flatMap {
        case Right(count) => F.delay(count)
        case Left(error)  => F.raiseError(error)
      }
}
