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

package higherkindness.compendium.http

import buildinfo.BuildInfo
import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.circe.syntax._
import org.http4s.circe.CirceEntityCodec._
import mouse.all._
import higherkindness.compendium.db.DBService
import higherkindness.compendium.models.HealthResponse
import higherkindness.compendium.models.Encoders._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object HealthService {

  def healthRouteService[F[_]: Sync: DBService]: HttpRoutes[F] = {

    object f extends Http4sDsl[F]
    import f._

    HttpRoutes.of[F] {
      case GET -> Root / "health" =>
        for {
          exists <- DBService[F].ping()
          resp <- exists.fold(
            Ok(HealthResponse("pass", BuildInfo.version, BuildInfo.name).asJson),
            InternalServerError()
          )
        } yield resp
    }
  }
}
