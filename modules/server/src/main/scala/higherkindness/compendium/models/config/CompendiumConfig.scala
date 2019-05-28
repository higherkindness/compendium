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

import com.zaxxer.hikari.HikariConfig

import scala.concurrent.duration.FiniteDuration

final case class CompendiumConfig(
    http: HttpConfig,
    storage: StorageConfig,
    postgres: PostgresConfig
)

final case class StorageConfig(path: String)

final case class PostgresConfig(
    jdbcUrl: String,
    username: String,
    password: String,
    driver: String,
    connectionTimeout: Option[FiniteDuration] = None,
    idleTimeout: Option[FiniteDuration] = None,
    maxLifetime: Option[FiniteDuration] = None,
    minimumIdle: Option[Int] = None,
    maximumPoolSize: Option[Int] = None
)

object PostgresConfig {

  def getHikariConfig(c: PostgresConfig): HikariConfig = {

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
