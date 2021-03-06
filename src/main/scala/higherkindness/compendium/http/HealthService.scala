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

package higherkindness.compendium.http

import buildinfo.BuildInfo
import cats.effect.Sync
import cats.syntax.flatMap._
import higherkindness.compendium.metadata.MetadataStorage
import higherkindness.compendium.models.HealthResponse
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl

object HealthService {

  def healthRouteService[F[_]: Sync: MetadataStorage]: HttpRoutes[F] = {

    object f extends Http4sDsl[F]
    import f._

    HttpRoutes.of[F] {
      case GET -> Root / "health" =>
        F.ping.ifM(
          Ok(HealthResponse("pass", BuildInfo.name, BuildInfo.version)),
          InternalServerError()
        )
    }
  }
}
