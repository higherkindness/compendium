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

package higherkindness.compendium.db

import cats.effect._
import com.dimafeng.testcontainers.PostgreSQLContainer
import doobie.util.transactor.Transactor
import higherkindness.compendium.migrations.Migrations
import higherkindness.compendium.models.config.PostgresConfig
import io.chrisdavenport.testcontainersspecs2.ForAllTestContainer
import org.specs2.mutable.Specification

import scala.concurrent.ExecutionContext.global

sealed abstract class MigrationsMode extends Product with Serializable

object MigrationsMode {
  case object Metadata extends MigrationsMode
  case object Data     extends MigrationsMode
}

// PGHelper spins up a fresh postgres container and runs the specified migrations afterwards
abstract class PGHelper(mode: MigrationsMode) extends Specification with ForAllTestContainer {

  import higherkindness.compendium.db.MigrationsMode.{Data, Metadata}

  private val location = mode match {
    case Metadata => Migrations.metadataLocation[IO]
    case Data     => Migrations.dataLocation[IO]
  }

  override lazy val container: PostgreSQLContainer =
    PostgreSQLContainer("postgres:11-alpine")

  private lazy val conf: PostgresConfig = PostgresConfig(
    container.jdbcUrl,
    container.username,
    container.password,
    container.driverClassName
  )

  implicit lazy val CS: ContextShift[IO] = IO.contextShift(global)

  implicit lazy val transactor: Transactor[IO] =
    Transactor.fromDriverManager[IO](
      container.driverClassName,
      container.jdbcUrl,
      container.username,
      container.password
    )

  override def afterStart(): Unit = {
    (for {
      migrations <- location
      _          <- Migrations.makeMigrations[IO](conf, List(migrations))
    } yield ()).unsafeRunSync()
  }
}
