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

package higherkindness.compendium.models

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

object Decoders {
  implicit val healthResponseDecoder: Decoder[HealthResponse] = deriveDecoder[HealthResponse]
}

object Encoders {
  implicit val healthResponseEncoder: Encoder[HealthResponse] = deriveEncoder[HealthResponse]
}

final case class HealthResponse(status: String, version: String, serviceID: String)

final case class StorageConfig(path: String)

final case class PostgresConfig(
    jdbcUrl: String,
    username: String,
    password: String,
    driver: String
)

final case class CompendiumConfig(
    http: HttpConfig,
    storage: StorageConfig,
    postgres: PostgresConfig
)
