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
import cats.free.Free
import hammock._
import hammock.circe.implicits._
import higherkindness.compendium.http.Encoders._
import higherkindness.compendium.http.Decoders._
import higherkindness.compendium.models._

trait CompendiumClient {

  /** Stores a protocol
   *
   * @param identifier the protocol identifier
   * @param protocol a protocol
   * @return the identifier of the protocol
   */
  def storeProtocol(identifier: String, protocol: Protocol): IO[Int]

  /** Retrieve a Protocol by its id
   *
   * @param identifier the protocol identifier
   * @return a protocol
   */
  def recoverProtocol(identifier: String): IO[Option[Protocol]]

  /** Generates a client for a target and a protocol by its identifier
   *
   * @param target target for the protocol
   * @param identifier the protocol identifier
   * @return a client for that protocol and target
   */
  def generateClient(target: Target, identifier: String): IO[String]
}

object CompendiumClient {

  def apply()(implicit interp: InterpTrans[IO], clientConfig: CompendiumConfig): CompendiumClient =
    new CompendiumClient {

      val baseUrl: String = s"http://${clientConfig.http.host}:${clientConfig.http.port}"

      override def storeProtocol(identifier: String, protocol: Protocol): IO[Int] = {
        val request =
          Hammock.request(Method.POST, uri"$baseUrl/v0/protocol/$identifier", Map(), Some(protocol))

        for {
          status <- request.map(_.status).exec[IO]
          _ <- status match {
            case Status.Created => IO.unit
            case Status.OK      => IO.unit
            case Status.BadRequest =>
              asError(request, new SchemaError(_))
            case Status.InternalServerError =>
              IO.raiseError(new UnknownError(s"Error in compendium server"))
            case _ =>
              IO.raiseError(new UnknownError(s"Unknown error with status code $status"))
          }
        } yield status.code
      }

      override def recoverProtocol(identifier: String): IO[Option[Protocol]] = {
        val request: Free[HttpF, HttpResponse] = Hammock
          .request(Method.GET, uri"$baseUrl/v0/protocol/$identifier", Map())

        for {
          status <- request.map(_.status).exec[IO]
          out <- status match {
            case Status.OK       => request.as[Protocol].map(Some(_)).exec[IO]
            case Status.NotFound => IO(None)
            case Status.InternalServerError =>
              IO.raiseError(new UnknownError(s"Error in compendium server"))
            case _ =>
              IO.raiseError(new UnknownError(s"Unknown error with status code $status"))
          }
        } yield out
      }

      override def generateClient(target: Target, identifier: String): IO[String] = IO("")

      private def asError(
          request: Free[HttpF, HttpResponse],
          error: String => Exception): IO[Unit] =
        request
          .as[ErrorResponse]
          .exec[IO]
          .flatMap(rsp => IO.raiseError(error(rsp.message)))
    }
}
