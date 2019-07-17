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

package higherkindness.compendium.models.config

import java.nio.file.Path

import cats.effect.Sync
import com.zaxxer.hikari.HikariConfig
import higherkindness.compendium.storage.Storage
import pureconfig.generic.FieldCoproductHint

import scala.concurrent.duration.FiniteDuration

sealed abstract class StorageConfig extends Product with Serializable

object StorageConfig {
  def retrieveStorage[F[_]: Sync](storageConfig: StorageConfig): Storage[F] = ???

  implicit val fieldHinter = new FieldCoproductHint[StorageConfig]("storage-type") {
    override def fieldValue(name: String): String =
      name.dropRight("StorageConfig".length).toUpperCase
  }
}

final case class FileStorageConfig(path: Path) extends StorageConfig

final case class DatabaseStorageConfig(
    jdbcUrl: String,
    username: String,
    password: String,
    driver: String,
    connectionTimeout: Option[FiniteDuration] = None,
    idleTimeout: Option[FiniteDuration] = None,
    maxLifetime: Option[FiniteDuration] = None,
    minimumIdle: Option[Int] = None,
    maximumPoolSize: Option[Int] = None
) extends StorageConfig

object DatabaseStorageConfig {

  def getHikariConfig(c: DatabaseStorageConfig): HikariConfig = {

    val hikariConfig: HikariConfig = new HikariConfig()

    hikariConfig.setJdbcUrl(c.jdbcUrl)
    hikariConfig.setUsername(c.username)
    hikariConfig.setPassword(c.password)
    hikariConfig.setDriverClassName(c.driver)

    c.connectionTimeout.foreach(v => hikariConfig.setConnectionTimeout(v.toMillis))
    c.idleTimeout.foreach(v => hikariConfig.setIdleTimeout(v.toMillis))

    hikariConfig
  }
}
