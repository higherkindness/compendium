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

package higherkidness.compendium

import cats.effect.IO
import com.typesafe.config.ConfigFactory
import higherkindness.compendium.models.config._
import org.specs2.mutable.Specification
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.catseffect._

class ConfigSpec extends Specification {

  private def configWithStorageBlock(storageBlock: String): String =
    s"""
      |http {
      |  port = 8080
      |  host = "0.0.0.0"
      |}
      |
      |protocols {
      |  $storageBlock
      |}
      |
      |metadata {
      |  storage {
      |    jdbc-url = ""
      |    username = "postgres"
      |    password = "postgres"
      |    driver = "org.postgresql.Driver"
      |  }
      |}
    """.stripMargin

  "Config must load properly with protocols file storage config" >> {

    val storageBlock = """
                         |storage {
                         |  # Choose storage type for protocol data
                         |  # Options: FILE, DATABASE
                         |  storage-type = "FILE"
                         |  # Path to folder where protocol contents will be stored
                         |  path = "/tmp/files"
                         |}
                       """.stripMargin

    val config = configWithStorageBlock(storageBlock)

    ConfigSource
      .fromConfig(ConfigFactory.parseString(config))
      .loadF[IO, CompendiumServerConfig]
      .attempt
      .unsafeRunSync() must beRight[CompendiumServerConfig]
  }

  "Config must load properly with protocols database storage config" >> {

    val storageBlock = """
                         |storage {
                         |  # Choose storage type for protocol data
                         |  # Options: FILE, DATABASE
                         |  storage-type = "DATABASE"
                         |  jdbc-url = ""
                         |  username = "postgres"
                         |  password = "postgres"
                         |  driver = "org.postgresql.Driver"
                         |}
                       """.stripMargin

    val config = configWithStorageBlock(storageBlock)

    ConfigSource
      .fromConfig(ConfigFactory.parseString(config))
      .loadF[IO, CompendiumServerConfig]
      .attempt
      .unsafeRunSync() must beRight[CompendiumServerConfig]
  }

}
