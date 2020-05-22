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

abstract class CompendiumError(error: String) extends Exception(error)

final case class FileNotFound(fileName: String)    extends CompendiumError(s"File with name $fileName not found")
final case class ProtocolIdError(msg: String)      extends CompendiumError(msg)
final case class ProtocolVersionError(msg: String) extends CompendiumError(msg)
final case class ProtocolNotFound(msg: String)     extends CompendiumError(msg)
final case class UnknownIdlName(msg: String)       extends CompendiumError(msg)
final case class SchemaError(msg: String)          extends CompendiumError(msg)
final case class UnknownError(msg: String)         extends CompendiumError(msg)
