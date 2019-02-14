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
import hammock._
import hammock.apache.ApacheInterpreter
import hammock.circe.implicits._
import hammock.marshalling._
import higherkindness.compendium.models.Protocol

trait CompendiumClient[F[_]] {

  /** Stores a protocol
   *
   * @param protocol a protocol
   * @return the identifier of the protocol
   */
  def storeProtocol(protocol: Protocol): F[Int]

  /** Retrieve a Protocol by its id
   *
   * @param identifier the protocol identifier
   * @return a protocol
   */
  def recoverProtocol(identifier: Int): F[Option[Protocol]]
}

object CompendiumClient {

  private[this] implicit val interpTrans = ApacheInterpreter[IO]

  val response = Hammock
    .request(Method.GET, uri"https://localhost:8080/protocol", Map())
    .as[List[String]]
    .exec[IO]

  def impl[F[_]]: CompendiumClient[F] = {

    new CompendiumClient[F] {

      override def storeProtocol(protocol: Protocol): F[Int] = ???
      /*{
        val response = Hammock
          .request(Method.GET, uri"https://localhost:8080/protocol", Map())
          .as[Int]
          .exec[F]
      }*/

      override def recoverProtocol(identifier: Int): F[Option[Protocol]] = ???
    }
  }
}
