/*
 * Copyright 2018 47 Degrees, LLC. <http://www.47deg.com>
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

package higherkindness.db

import cats.effect.IO
import higherkindness.models.Protocol
import higherkindness.storage.Storage

object DBServiceStorage {

  def impl(storage: Storage[IO]): DBService[IO] =
    new DBService[IO] {

      override def addProtocol(protocol: Protocol): IO[Int] =
        for {
          number <- storage.numberProtocol()
          _      <- storage.store(number + 1, protocol)
        } yield number + 1

      override def lastProtocol(): IO[Option[Protocol]] =
        for {
          number   <- storage.numberProtocol()
          protocol <- storage.recover(number)
        } yield protocol
    }
}
