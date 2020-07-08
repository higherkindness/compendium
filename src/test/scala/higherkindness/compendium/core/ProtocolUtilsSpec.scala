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

package higherkindness.compendium.core

import cats.effect.IO
import higherkindness.compendium.CompendiumArbitrary._
import higherkindness.compendium.models.{IdlName, Protocol}
import higherkindness.compendium.models.transformer.types.SchemaParseException
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import java.io.InputStream

object ProtocolUtilsSpec extends Specification with ScalaCheck {

  sequential

  private val validator = ProtocolUtils.impl[IO]

  "Protocol validator" >> {
    "[Avro] Given a raw protocol text returns the same protocol if it validates correctly" >> {
      val stream: InputStream = getClass.getResourceAsStream("/correct.avro")
      val text: String        = scala.io.Source.fromInputStream(stream).getLines.mkString
      val protocol: Protocol  = Protocol(text)
      stream.close()

      validator.validateProtocol(protocol, IdlName.Avro).unsafeRunSync === protocol
    }

    "[Avro] Given a raw protocol text raises an error if the protocol is incorrect" >> prop {
      protocol: Protocol =>
        validator
          .validateProtocol(protocol, IdlName.Avro)
          .unsafeRunSync must throwA[SchemaParseException]
    }

    "[Avro] Given multiple protocols validates them sequentially" >> {
      val stream: InputStream = getClass.getResourceAsStream("/correct.avro")
      val text: String        = scala.io.Source.fromInputStream(stream).getLines.mkString
      val protocol: Protocol  = Protocol(text)
      stream.close()

      val validation = validator.validateProtocol(protocol, IdlName.Avro) *> validator
        .validateProtocol(protocol, IdlName.Avro)

      validation.unsafeRunSync should not(throwA[SchemaParseException])
    }

    "[Proto] Given a raw protocol text returns the same protocol if it validates correctly" >> {
      val stream: InputStream = getClass.getResourceAsStream("/correct.proto")
      val text: String        = scala.io.Source.fromInputStream(stream).getLines.mkString
      val protocol: Protocol  = Protocol(text)
      stream.close()

      validator.validateProtocol(protocol, IdlName.Protobuf).unsafeRunSync === protocol
    }

    "[Proto] Given a raw protocol text raises an error if idl specified is incorrect" >> {
      val stream: InputStream = getClass.getResourceAsStream("/correct.avro")
      val text: String        = scala.io.Source.fromInputStream(stream).getLines.mkString
      val protocol: Protocol  = Protocol(text)
      stream.close()

      validator
        .validateProtocol(protocol, IdlName.Protobuf)
        .unsafeRunSync must throwA[SchemaParseException]
    }

    "[Proto] Given a raw protocol text raises an error if the protocol is incorrect" >> {
      val stream: InputStream = getClass.getResourceAsStream("/wrong.proto")
      val text: String        = scala.io.Source.fromInputStream(stream).getLines.mkString
      val protocol: Protocol  = Protocol(text)
      stream.close()

      validator
        .validateProtocol(protocol, IdlName.Protobuf)
        .unsafeRunSync must throwA[SchemaParseException]
    }

    "[OpenAPI - Yaml] Given a raw protocol text returns the same protocol if it validates correctly" >> {
      val stream: InputStream = getClass.getResourceAsStream("/correctYamlOpenAPI.yaml")
      val text: String        = scala.io.Source.fromInputStream(stream).getLines.mkString("\n")
      val protocol: Protocol  = Protocol(text)
      stream.close()

      validator
        .validateProtocol(protocol, IdlName.OpenAPIYaml)
        .unsafeRunSync === protocol
    }

    "[OpenAPI - Yaml] Given a raw protocol text raises an error if the protocol is incorrect" >> {
      val stream: InputStream = getClass.getResourceAsStream("/wrongYamlOpenAPI.yaml")
      val text: String        = scala.io.Source.fromInputStream(stream).getLines.mkString("\n")
      val protocol: Protocol  = Protocol(text)
      stream.close()

      validator
        .validateProtocol(protocol, IdlName.OpenAPIYaml)
        .unsafeRunSync must throwA[SchemaParseException]
    }

    "[OpenAPI - Yaml] Given a raw protocol text raises an error if idl specified is incorrect" >> {
      val stream: InputStream = getClass.getResourceAsStream("/correctYamlOpenAPI.yaml")
      val text: String        = scala.io.Source.fromInputStream(stream).getLines.mkString("\n")
      val protocol: Protocol  = Protocol(text)
      stream.close()

      validator
        .validateProtocol(protocol, IdlName.Protobuf)
        .unsafeRunSync must throwA[SchemaParseException]
    }

    "[OpenAPI - JSON] Given a raw protocol text returns the same protocol if it validates correctly" >> {
      val stream: InputStream = getClass.getResourceAsStream("/correctJSONOpenAPI.json")
      val text: String        = scala.io.Source.fromInputStream(stream).getLines.mkString("\n")
      val protocol: Protocol  = Protocol(text)
      stream.close()

      validator
        .validateProtocol(protocol, IdlName.OpenAPIJson)
        .unsafeRunSync === protocol
    }

    "[OpenAPI - JSON] Given a raw protocol text raises an error if the protocol is incorrect" >> {
      val stream: InputStream = getClass.getResourceAsStream("/wrongJSONOpenAPI.json")
      val text: String        = scala.io.Source.fromInputStream(stream).getLines.mkString("\n")
      val protocol: Protocol  = Protocol(text)
      stream.close()

      validator
        .validateProtocol(protocol, IdlName.OpenAPIJson)
        .unsafeRunSync must throwA[SchemaParseException]
    }

    "[OpenAPI - JSON] Given a raw protocol text raises an error if idl specified is incorrect" >> {
      val stream: InputStream = getClass.getResourceAsStream("/correctYamlOpenAPI.yaml")
      val text: String        = scala.io.Source.fromInputStream(stream).getLines.mkString("\n")
      val protocol: Protocol  = Protocol(text)
      stream.close()

      validator
        .validateProtocol(protocol, IdlName.Avro)
        .unsafeRunSync must throwA[SchemaParseException]
    }
  }
}
