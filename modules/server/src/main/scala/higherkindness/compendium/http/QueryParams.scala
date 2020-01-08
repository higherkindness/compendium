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

package higherkindness.compendium.http

import higherkindness.compendium.core.refinements.ProtocolVersion
import higherkindness.compendium.models.IdlName
import org.http4s.QueryParamDecoder
import org.http4s.dsl.impl.{
  OptionalValidatingQueryParamDecoderMatcher,
  ValidatingQueryParamDecoderMatcher
}

object QueryParams {

  implicit val queryIdlDecoderQueryParam: QueryParamDecoder[IdlName] =
    QueryParamDecoder.fromUnsafeCast[IdlName](qp => IdlName.withName(qp.value))("IdlName")

  object TargetParam  extends ValidatingQueryParamDecoderMatcher[IdlName]("target")
  object IdlNameParam extends ValidatingQueryParamDecoderMatcher[IdlName]("idlName")

  implicit val versionQueryParamDecoder: QueryParamDecoder[ProtocolVersion] =
    QueryParamDecoder.fromUnsafeCast[ProtocolVersion](param =>
      ProtocolVersion.unsafeFrom(param.value.toInt))("ProtocolVersion")

  object ProtoVersion extends OptionalValidatingQueryParamDecoderMatcher[ProtocolVersion]("version")
}
