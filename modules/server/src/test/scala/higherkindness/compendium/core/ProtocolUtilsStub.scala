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

import cats.effect.IO
import higherkindness.compendium.models.Protocol
import mouse.all._

class ProtocolUtilsStub(val pro: Protocol, val valid: Boolean) extends ProtocolUtils[IO] {
  def validateProtocol(protocol: Protocol): IO[Protocol] =
     valid.fold(IO(pro), IO.raiseError(new org.apache.avro.SchemaParseException("Error")))
}
