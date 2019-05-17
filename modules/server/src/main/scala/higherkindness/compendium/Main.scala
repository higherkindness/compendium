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

package higherkindness.compendium

import cats.effect._
import cats.syntax.functor._
import cats.syntax.flatMap._
import doobie._
import doobie.hikari.HikariTransactor
import higherkindness.compendium.core.{CompendiumService, ProtocolUtils}
import higherkindness.compendium.db.PgDBService
import higherkindness.compendium.http.{HealthService, RootService}
import higherkindness.compendium.migrations.Migrations
import higherkindness.compendium.models.CompendiumConfig
import higherkindness.compendium.storage.FileStorage
import org.http4s.server.Router
import org.http4s.syntax.kleisli._
import pureconfig.generic.auto._

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    CompendiumStreamApp.program[IO]
}

object CompendiumStreamApp {

  def program[F[_]: ContextShift: ConcurrentEffect: Timer]: F[ExitCode] = {

    Effect[F].delay(pureconfig.loadConfigOrThrow[CompendiumConfig]("compendium")).flatMap {
      config =>
        {
          val transactor: Resource[F, HikariTransactor[F]] =
            for {
              ce <- ExecutionContexts.fixedThreadPool[F](32)
              te <- ExecutionContexts.cachedThreadPool[F]
              xa <- HikariTransactor.newHikariTransactor[F](
                config.postgres.driver,
                config.postgres.jdbcUrl,
                config.postgres.username,
                config.postgres.password,
                ce,
                te
              )
            } yield xa

          transactor.use(xa => {
            implicit val db                = PgDBService.impl[F](xa)
            implicit val storage           = FileStorage.impl[F](config.storage)
            implicit val utils             = ProtocolUtils.impl[F]
            implicit val compendiumService = CompendiumService.impl[F]
            val rootService                = RootService.rootRouteService
            val healthService              = HealthService.healthRouteService
            val app                        = Router("/" -> healthService, "/v0" -> rootService).orNotFound

            for {
              _ <- Migrations.makeMigrations(
                config.postgres.jdbcUrl,
                config.postgres.username,
                config.postgres.password
              )
              code <- CompendiumServerStream
                .serverStream(config.http, app)
                .compile
                .drain
                .as(ExitCode.Success)
            } yield code
          })
        }
    }
  }

}
