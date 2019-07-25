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

package higherkindness.compendium.core

import java.io.InputStream

import cats.effect.IO
import cats.syntax.apply._
import higherkindness.compendium.CompendiumArbitrary._
import higherkindness.compendium.models.Protocol
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

object ProtocolUtilsSpec extends Specification with ScalaCheck {

  sequential

  private val validator = ProtocolUtils.impl[IO]

  "Protocol validator" >> {
    "Given a raw protocol text returns the same protocol if it validates correctly" >> {
      val stream: InputStream = getClass.getResourceAsStream("/correct.avro")
      val text: String        = scala.io.Source.fromInputStream(stream).getLines.mkString
      val protocol: Protocol  = Protocol(text)

      validator.validateProtocol(protocol).unsafeRunSync === protocol
    }

    "Given a raw protocol text raises an error if the protocol is incorrect" >> prop {
      protocol: Protocol =>
        validator
          .validateProtocol(protocol)
          .unsafeRunSync must throwA[org.apache.avro.SchemaParseException]
    }

    "Given multiple protocols validates them sequentially" >> {
      val stream: InputStream = getClass.getResourceAsStream("/correct.avro")
      val text: String        = scala.io.Source.fromInputStream(stream).getLines.mkString
      val protocol: Protocol  = Protocol(text)

      val validation = validator.validateProtocol(protocol) *> validator.validateProtocol(protocol)

      validation.unsafeRunSync should not(throwA[org.apache.avro.SchemaParseException])
    }
  }

}
