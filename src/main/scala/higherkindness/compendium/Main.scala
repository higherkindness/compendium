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

package higherkindness.compendium

import cats.effect._
import cats.implicits._
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import fs2.Stream
import higherkindness.compendium.core._
import higherkindness.compendium.http._
import higherkindness.compendium.metadata._
import higherkindness.compendium.metadata.pg.PgMetadataStorage
import higherkindness.compendium.migrations.Migrations
import higherkindness.compendium.models.config._
import higherkindness.compendium.storage._
import higherkindness.compendium.storage.files.FileStorage
import higherkindness.compendium.storage.pg.PgStorage
import higherkindness.compendium.transformer.ProtocolTransformer
import higherkindness.compendium.transformer.skeuomorph.SkeuomorphProtocolTransformer
import org.http4s.server.Router
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    CompendiumStreamApp.stream[IO].compile.drain.as(ExitCode.Success)
}

object CompendiumStreamApp {

  def stream[F[_]: ConcurrentEffect: Timer: ContextShift]: Stream[F, ExitCode] =
    for {
      blocker <- Stream.resource(Blocker[F])
      conf <- Stream.eval(
        ConfigSource.default.at("compendium").loadF[F, CompendiumServerConfig](blocker)
      )

      implicit0(storage: Storage[F])             <- initProtocolStorage[F](conf.protocols)
      implicit0(metaStorage: MetadataStorage[F]) <- initMetadataStorage[F](conf.metadata)

      implicit0(utils: ProtocolUtils[F])             = ProtocolUtils.impl[F]
      implicit0(transformer: ProtocolTransformer[F]) = SkeuomorphProtocolTransformer[F]
      implicit0(compendium: CompendiumService[F])    = CompendiumService.impl[F]
      rootService                                    = RootService.rootRouteService
      healthService                                  = HealthService.healthRouteService
      app                                            = Router("/" -> healthService, "/v0" -> rootService)
      code <- CompendiumServerStream.serverStream(conf.http, app)
    } yield code

  private def initProtocolStorage[F[_]: Async: ContextShift](
      protocolConf: CompendiumProtocolConfig
  ): Stream[F, Storage[F]] =
    protocolConf.storage match {
      case fsc: FileStorageConfig =>
        Stream.emit(FileStorage[F](fsc))
      case dbsc: DatabaseStorageConfig =>
        Stream.eval(
          Migrations.dataLocation.flatMap(l => Migrations.makeMigrations(dbsc, l :: Nil))
        ) >>
          Stream.resource(createTransactor(dbsc).map(PgStorage[F](_)))
    }

  private def initMetadataStorage[F[_]: Async: ContextShift](
      metadataConf: CompendiumMetadataConfig
  ): Stream[F, MetadataStorage[F]] =
    Stream.eval(
      Migrations.metadataLocation.flatMap(l => Migrations.makeMigrations(metadataConf.storage, l :: Nil))
    ) >>
      Stream.resource(createTransactor(metadataConf.storage).map(PgMetadataStorage[F](_)))

  private def createTransactor[F[_]: Async: ContextShift](
      conf: DatabaseStorageConfig
  ): Resource[F, Transactor[F]] =
    for {
      ce      <- ExecutionContexts.fixedThreadPool[F](10)
      blocker <- Blocker[F]
      xa <-
        HikariTransactor
          .fromHikariConfig[F](DatabaseStorageConfig.getHikariConfig(conf), ce, blocker)
    } yield xa
}
