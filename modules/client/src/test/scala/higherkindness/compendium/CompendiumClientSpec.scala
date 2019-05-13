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
import higherkindness.compendium.models.{CompendiumConfig, ErrorResponse, Protocol, Target}
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

  implicit val clientConfig: CompendiumConfig =
    pureconfig.loadConfigOrThrow[CompendiumConfig]("compendium")

  private[this] def asEntityJson[T: io.circe.Encoder](t: T): Entity =
    Entity.StringEntity(t.asJson.toString, ContentType.`application/json`)

  def interp(identifier: String, target: Target)(implicit S: Sync[IO]) = new InterpTrans[IO] {

    val trans: HttpF ~> IO = new (HttpF ~> IO) {
      private def response(entity: Entity): HttpResponse =
        HttpResponse(Status.OK, Map(), entity)

      def apply[A](req: HttpF[A]): IO[A] = req match {
        case Get(HttpRequest(uri, _, _))
            if uri.path.equalsIgnoreCase(s"/v0/protocol/$identifier") =>
          S.catchNonFatal {
            response(asEntityJson(dummyProtocol))
          }

        case Get(HttpRequest(uri, _, _)) if uri.path.equalsIgnoreCase(s"/v0/protocol/error") =>
          S.catchNonFatal {
            response(Entity.EmptyEntity).copy(status = Status.InternalServerError)
          }

        case Get(HttpRequest(uri, _, _))
            if uri.path.equalsIgnoreCase(
              s"/v0/protocol/$identifier/generate/?target=${target.toString}") =>
          S.catchNonFatal {
            response(Entity.StringEntity(uri.path)).copy(status = Status.NotImplemented)
          }

        case Get(_) =>
          S.catchNonFatal {
            response(Entity.EmptyEntity).copy(status = Status.NotFound)
          }

        case Post(HttpRequest(uri, _, _))
            if uri.path.equalsIgnoreCase(s"/v0/protocol/schemaerror") =>
          S.catchNonFatal {
            response(asEntityJson(ErrorResponse("Schema error"))).copy(status = Status.BadRequest)
          }

        case Post(HttpRequest(uri, _, _))
            if uri.path.equalsIgnoreCase(s"/v0/protocol/alreadyexists") =>
          S.catchNonFatal {
            response(Entity.StringEntity(uri.path)).copy(status = Status.OK)
          }

        case Post(HttpRequest(uri, _, _)) if uri.path.equalsIgnoreCase(s"/v0/protocol/internal") =>
          S.catchNonFatal {
            response(Entity.EmptyEntity).copy(status = Status.InternalServerError)
          }

        case Post(HttpRequest(uri, _, _)) =>
          S.catchNonFatal {
            response(Entity.StringEntity(uri.path)).copy(status = Status.Created)
          }

        case _ =>
          S.raiseError(new Exception("Unexpected HTTP Method"))
      }
    }
  }

  "Recover protocol" >> {
    "Given a valid identifier returns a protocol" >> {

      implicit val terp = interp("proto1", Target.Scala)

      CompendiumClient().recoverProtocol("proto1").unsafeRunSync() should beSome(dummyProtocol)
    }

    "Given an invalid identifier returns no protocol" >> {

      implicit val terp = interp("proto1", Target.Scala)

      CompendiumClient().recoverProtocol("proto2").unsafeRunSync() should beNone
    }

    "Given an identifier returns a internal server error" >> {

      implicit val terp = interp("proto1", Target.Scala)

      CompendiumClient()
        .recoverProtocol("error")
        .unsafeRunSync() must throwA[higherkindness.compendium.models.UnknownError]
    }
  }

  "Store protocol" >> {
    "Given a valid identifier and a correct protocol returns no error" >> {

      implicit val terp = interp("proto1", Target.Scala)

      CompendiumClient().storeProtocol("proto1", dummyProtocol).unsafeRunSync() must not(
        throwA[Exception])
    }

    "Given a valid identifier and an incorrect protocol returns a SchemaError" >> {

      implicit val terp = interp("proto1", Target.Scala)

      CompendiumClient()
        .storeProtocol("schemaerror", dummyProtocol)
        .unsafeRunSync() must throwA[higherkindness.compendium.models.SchemaError]
    }

    "Given a valid identifier and a protocol that already exists returns no error" >> {

      implicit val terp = interp("proto1", Target.Scala)

      CompendiumClient()
        .storeProtocol("alreadyexists", dummyProtocol)
        .unsafeRunSync() must not(throwA[Exception])
    }

    "Given a valid identifier and a protocol that returs a InternalServerError returns a UnknownError" >> {

      implicit val terp = interp("proto1", Target.Scala)

      CompendiumClient()
        .storeProtocol("internal", dummyProtocol)
        .unsafeRunSync() must throwA[higherkindness.compendium.models.UnknownError]
    }
  }

  "Generate client" >> {
    "Given a valid identifier and a valid target" >> {
      failure
    }.pendingUntilFixed
  }

}
