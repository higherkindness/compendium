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

import cats.effect.{IO, Sync}
import cats.~>
import cats.effect._
import hammock.{HttpF, HttpRequest, InterpTrans, Post}
import higherkindness.compendium.models.{ClientConfig, Protocol}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import pureconfig.generic.auto._
import hammock._
import higherkindness.compendium.http._
import Encoders._
import io.circe.syntax._

object CompendiumClientSpec extends Specification with ScalaCheck {

  sequential

  private[this] val dummyProtocol: Protocol = Protocol("rawProtocol")

  implicit val clientConfig: ClientConfig = pureconfig.loadConfigOrThrow[ClientConfig]

  private[this] def asEntityJson[T: io.circe.Encoder](t: T): Entity =
    Entity.StringEntity(t.asJson.toString, ContentType.`application/json`)

  def interp(version: String) = new InterpTrans[IO] {

    def trans(implicit S: Sync[IO]): HttpF ~> IO = new (HttpF ~> IO) {
      private def response(entity: Entity): HttpResponse =
        HttpResponse(Status.OK, Map(), entity)

      def apply[A](req: HttpF[A]): IO[A] = req match {
        case Get(HttpRequest(uri, _, _)) if uri.path.equalsIgnoreCase(s"/v0/protocol/$version") =>
          S.catchNonFatal {
            response(asEntityJson(dummyProtocol))
          }

        case Get(_) =>
          S.catchNonFatal {
            response(Entity.StringEntity("")).copy(status = Status.NotFound)
          }

        case Post(HttpRequest(uri, _, _)) =>
          S.catchNonFatal {
            response(Entity.StringEntity(uri.path))
          }

        case _ =>
          S.raiseError(new Exception("Unexpected HTTP Method"))
      }
    }
  }

  "Recover protocol" >> {
    "Given a valid identifier returns a protocol" >> {

      implicit val terp = interp("proto1")

      val protocol = CompendiumClient[IO].recoverProtocol("proto1").unsafeRunSync()

      protocol should beSome(dummyProtocol)
    }

    "Given a valid identifier returns a protocol" >> {

      implicit val terp = interp("proto1")

      val protocol = CompendiumClient[IO].recoverProtocol("proto2").unsafeRunSync()

      protocol should beNone
    }
  }
}
