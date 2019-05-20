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
import fs2.Stream
import higherkindness.compendium.core.{CompendiumService, ProtocolUtils}
import higherkindness.compendium.db.{DBService, PgDBService}
import higherkindness.compendium.http.{HealthService, RootService}
import higherkindness.compendium.migrations.Migrations
import higherkindness.compendium.models.CompendiumConfig
import higherkindness.compendium.storage.{FileStorage, Storage}
import org.http4s.server.Router
import pureconfig.generic.auto._

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    CompendiumStreamApp.stream[IO].compile.drain.as(ExitCode.Success)
}

object CompendiumStreamApp {

  def stream[F[_]: ContextShift: ConcurrentEffect: Timer]: Stream[F, ExitCode] = {
    for {
      conf <- Stream.eval(
        Effect[F].delay(pureconfig.loadConfigOrThrow[CompendiumConfig]("compendium")))
      transactor <- Stream.resource(createHikariTransactor[F](conf))
      implicit0(storage: Storage[F])                     = FileStorage.impl[F](conf.storage)
      implicit0(utils: ProtocolUtils[F])                 = ProtocolUtils.impl[F]
      implicit0(db: DBService[F])                        = PgDBService.impl[F](transactor)
      implicit0(compendiumService: CompendiumService[F]) = CompendiumService.impl[F]
      rootService                                        = RootService.rootRouteService
      healthService                                      = HealthService.healthRouteService
      app                                                = Router("/" -> healthService, "/v0" -> rootService)
      _ <- Stream.eval(
        Migrations.makeMigrations(
          conf.postgres.jdbcUrl,
          conf.postgres.username,
          conf.postgres.password
        ))
      code <- CompendiumServerStream.serverStream(conf.http, app)
    } yield code
  }

  private def createHikariTransactor[F[_]: ContextShift: ConcurrentEffect](config: CompendiumConfig) =
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
}
