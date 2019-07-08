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

package higherkindness.compendium.parser

import cats.effect.IO
import cats.syntax.option._
import higherkindness.compendium.models.DBModels.MetaProtocol
import higherkindness.compendium.models._
import org.specs2.mutable.Specification

class ProtocolParserSpec extends Specification {

  import protocols._

  val parser = ProtocolParser.impl[IO]

  "ProtocolParser should parse a simple Avro Schema to Mu" >> {
    val metaProtocol = MetaProtocol(IdlNames.Avro, Protocol(simpleAvroExample)).some

    parser
      .parse(metaProtocol, Target.Mu)
      .map(_.isRight)
      .unsafeRunSync()
  }

  "ProtocolParser should parse a simple Protobuf Schema to Mu" >> {
    val metaProtocol = MetaProtocol(IdlNames.Protobuf, Protocol(simpleProtobufExample)).some
    parser
      .parse(metaProtocol, Target.Mu)
      .map(_.isRight)
      .unsafeRunSync()
  }

}
