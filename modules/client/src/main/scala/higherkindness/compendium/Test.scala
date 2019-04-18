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

import cats.effect.IO
import hammock.asynchttpclient.AsyncHttpClientInterpreter._
import higherkindness.compendium.models.{CompendiumConfig, Protocol}
import pureconfig.generic.auto._

object Test extends App {

  implicit val clientConfig: CompendiumConfig =
    pureconfig.loadConfigOrThrow[CompendiumConfig]("compendium")

  val client = CompendiumClient[IO]

  val raw      = """{"namespace": "correct.avro",
     "type": "record",
     "name": "User",
     "fields": [
         {"name": "name", "type": "string"},
         {"name": "age",  "type": "int"},
         {"name": "address", "type": ["string", "null"]}
     ]
    }"""
  val protocol = Protocol(raw)

  val identifier = client.storeProtocol("identifier", protocol)

  println(identifier.unsafeRunSync())
}
