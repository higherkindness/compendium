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

package higherkindness.compendium.models

import cats.{Eq, Show}
import cats.implicits._
import io.circe.{Decoder, Encoder}
import io.circe._
import shapeless.tag
import shapeless.tag.@@
import org.http4s.QueryParamDecoder

trait Tagged[T] {
  sealed trait Tag
  type Type = T @@ Tag
  def apply(t: T): T @@ Tag = tag[Tag][T](t)
}

object Tagged {

  abstract class DeriveCodecEqShow[T: Decoder: Encoder: Eq: Show] extends Tagged[T] {
    implicit val decoderTagged: Decoder[Type] =
      Decoder[T].map(apply)

    implicit val encoderTagged: Encoder[Type] =
      Encoder[T].narrow

    implicit val eqTagged: Eq[Type] =
      Eq[T].narrow

    implicit val showTagged: Show[Type] =
      Show[T].narrow
  }

  class Str extends DeriveCodecEqShow[String] {
    override implicit val showTagged: Show[Type] = Show[String].narrow
  }

  class Number extends DeriveCodecEqShow[Int] {
    override implicit val showTagged: Show[Type] = Show[Int].narrow
  }

  trait TaggedQueryParamDecoder[T] { self: Tagged[T] =>
    implicit def queryParamDecoder(implicit base: QueryParamDecoder[T]): QueryParamDecoder[Type] =
      base.map(apply)
  }
}
