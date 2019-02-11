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

import cats.effect.Sync
import cats.implicits._
import higherkindness.compendium.models.Protocol
import org.apache.avro.Schema

class ProtocolUtils[F[_]: Sync] {

  private def parser: Schema.Parser = new Schema.Parser()

  def validateProtocol(protocol: Protocol): F[Protocol] =
    if (protocol.raw.trim.isEmpty)
      Sync[F].raiseError(new org.apache.avro.SchemaParseException("Protocol is empty"))
    else
      Sync[F].catchNonFatal(parser.parse(protocol.raw)).map(_ => protocol)
}

object ProtocolUtils {

  def impl[F[_]: Sync](): ProtocolUtils[F] = new ProtocolUtils

  def apply[F[_]](implicit F: ProtocolUtils[F]): ProtocolUtils[F] = F
}
