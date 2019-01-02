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

package higherkindness.compendium.storage

import higherkindness.compendium.models.Protocol

trait StorageService[F[_]] {

  def storage: Storage[F]
  def store[S](id: Int, protocol: Protocol): F[Unit]
  def recover[S](id: Int): F[Option[Protocol]]
}

object StorageService {
  def impl[F[_]](st: Storage[F]): StorageService[F] = new StorageService[F] {

    override val storage: Storage[F] = st

    override def store[S](id: Int, protocol: Protocol): F[Unit] =
      storage.store(id, protocol)

    override def recover[S](id: Int): F[Option[Protocol]] =
      storage.recover(id)

  }
}
