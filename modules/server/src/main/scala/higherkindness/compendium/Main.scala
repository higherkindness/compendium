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
import fs2.Stream
import higherkindness.compendium.core.{CompendiumService, ProtocolUtils}
import higherkindness.compendium.db.{DBService, FileDBService}
import higherkindness.compendium.http.RootService
import higherkindness.compendium.models.CompendiumConfig
import higherkindness.compendium.parser.{ProtocolParser, ProtocolParserService}
import higherkindness.compendium.storage.{FileStorage, Storage}
import pureconfig.generic.auto._

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    CompendiumStreamApp.stream[IO].compile.drain.as(ExitCode.Success)
}

object CompendiumStreamApp {

  def stream[F[_]: ConcurrentEffect]: Stream[F, ExitCode] =
    for {
      conf <- Stream.eval(
        Effect[F].delay(pureconfig.loadConfigOrThrow[CompendiumConfig]("compendium")))
      implicit0(storage: Storage[F])                      = FileStorage.impl[F](conf.storage)
      implicit0(dbService: DBService[F])                  = FileDBService.impl[F]
      implicit0(utils: ProtocolUtils[F])                  = ProtocolUtils.impl[F]
      implicit0(protocolParser: ProtocolParserService[F]) = ProtocolParser.impl[F]
      implicit0(compendiumService: CompendiumService[F])  = CompendiumService.impl[F]
      service                                             = RootService.rootRouteService
      code <- CompendiumServerStream.serverStream(conf.http, service)
    } yield code
}
