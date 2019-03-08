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
import cats.free.Free
import cats.implicits._
import hammock._
import hammock.circe.implicits._
import higherkindness.compendium.http.Encoders._
import higherkindness.compendium.http.Decoders._
import higherkindness.compendium.models._

trait CompendiumClient[F[_]] {

  /** Stores a protocol
   *
   * @param identifier the protocol identifier
   * @param protocol a protocol
   * @return the identifier of the protocol
   */
  def storeProtocol(identifier: String, protocol: Protocol): F[Int]

  /** Retrieve a Protocol by its id
   *
   * @param identifier the protocol identifier
   * @return a protocol
   */
  def recoverProtocol(identifier: String): F[Option[Protocol]]
}

object CompendiumClient {

  def apply[F[_]](implicit F: CompendiumClient[F]): CompendiumClient[F] = F

  implicit def impl[F[_]: Sync: InterpTrans](
      implicit clientConfig: CompendiumConfig): CompendiumClient[F] = {

    val baseUrl: String = s"${clientConfig.http.host}:${clientConfig.http.port}"

    new CompendiumClient[F] {

      override def storeProtocol(identifier: String, protocol: Protocol): F[Int] = {
        val request =
          Hammock.request(Method.POST, uri"$baseUrl/v0/protocol/$identifier", Map(), Some(protocol))

        for {
          status <- request.map(_.status).exec[F]
          _ <- status match {
            case Status.OK => Sync[F].unit
            case Status.BadRequest =>
              asError(request, new SchemaError(_))
            case Status.Conflict => asError(request, new ProtocolAlreadyExists(_))
            case Status.InternalServerError =>
              Sync[F].raiseError(new UnknownError(s"Error in compendium server"))
            case _ =>
              Sync[F].raiseError(new UnknownError(s"Unknown error with status code $status"))
          }
        } yield status.code
      }

      override def recoverProtocol(identifier: String): F[Option[Protocol]] = {
        val request: Free[HttpF, HttpResponse] = Hammock
          .request(Method.GET, uri"$baseUrl/v0/protocol/$identifier", Map())

        for {
          status <- request.map(_.status).exec[F]
          out <- status match {
            case Status.OK       => request.as[Protocol].map(Some(_)).exec[F]
            case Status.NotFound => Sync[F].pure(None)
            case Status.InternalServerError =>
              Sync[F].raiseError(new UnknownError(s"Error in compendium server"))
            case _ =>
              Sync[F].raiseError(new UnknownError(s"Unknown error with status code $status"))
          }
        } yield out
      }

      private def asError(request: Free[HttpF, HttpResponse], error: String => Exception): F[Unit] =
        request
          .as[ErrorResponse]
          .exec[F]
          .flatMap(rsp => Sync[F].raiseError(error(rsp.message)))
    }
  }
}
