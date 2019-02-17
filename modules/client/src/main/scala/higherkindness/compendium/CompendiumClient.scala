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

import cats.effect.Sync
import hammock._
import hammock.asynchttpclient.AsyncHttpClientInterpreter
import hammock.circe.implicits._
import higherkindness.compendium.http.Encoders._
import higherkindness.compendium.http.Decoders._
import higherkindness.compendium.models.{ClientConfig, Protocol}

trait CompendiumClient[F[_]] {

  /** Stores a protocol
   *
   * @param protocol a protocol
   * @return the identifier of the protocol
   */
  def storeProtocol(protocol: Protocol): F[String]

  /** Retrieve a Protocol by its id
   *
   * @param identifier the protocol identifier
   * @return a protocol
   */
  def recoverProtocol(identifier: String): F[Option[Protocol]]
}

object CompendiumClient {

  def impl[F[_]: Sync: AsyncHttpClientInterpreter](
      clientConfig: ClientConfig): CompendiumClient[F] = {

    val baseUrl: String = s"https://${clientConfig.http.host}:${clientConfig.http.port}"

    new CompendiumClient[F] {

      override def storeProtocol(protocol: Protocol): F[String] =
        Hammock
          .request(Method.POST, uri"$baseUrl/protocol/", Map(), Some(protocol))
          .as[String]
          .exec[F]

      override def recoverProtocol(identifier: String): F[Option[Protocol]] =
        Hammock
          .request(Method.GET, uri"$baseUrl/protocol/", Map())
          .as[Option[Protocol]]
          .exec[F]
    }
  }
}
