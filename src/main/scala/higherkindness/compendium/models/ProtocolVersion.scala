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
import cats.kernel.Monoid

/**
 * A case class that contains the version of a Protocol.
 * Used this as guideline: https://snowplowanalytics.com/blog/2014/05/13/introducing-schemaver-for-semantic-versioning-of-schemas/
 *
 * This versioning is quite similar to SemVer, but applied to Schemas.
 *
 * @param model equivalent to Major. Changes that will break interaction with historical data.
 * @param revision equivalent to Minor. Changes that may, or may not, break interaction with historical data.
 * @param addition equivalent to Patch. Changes that do not break the interaction with all historical data.
 */

final case class ProtocolVersion(
    model: ModelVersion,
    revision: RevisionVersion,
    addition: AdditionVersion
)

object ProtocolVersion {

  /**
   * This function pretends to set a new patch version.
   * E.g.:
   *
   * val ver = ProtocolVersion(ModelVersion(0), RevisionVersion(0), AdditionVersion(0))
   * val newPatch = AdditionVersion(100)
   * ProtocolVersion.setPatch(ver, newPatch) == ProtocolVersion(ModelVersion(0), RevisionVersion(0), AdditionVersion(100))
   *
   * @param pv Protocol Version
   * @return a nre ProtocolVersion
   */
  def setAddition(pv: ProtocolVersion, newAddition: AdditionVersion): ProtocolVersion =
    pv.copy(addition = newAddition)

  /**
   * This function pretends to set a new minor version.
   * When a new Revision is set, the Addition is reset to 0.
   * E.g.:
   *
   * val ver = ProtocolVersion(ModelVersion(0), RevisionVersion(0), AdditionVersion(10))
   * val newMinor = RevisionVersion(100)
   * ProtocolVersion.setMinor(ver, newMinor) == ProtocolVersion(ModelVersion(0), RevisionVersion(100), AdditionVersion(0))
   *
   * @param pv Protocol Version
   * @return a nre ProtocolVersion
   */
  def setRevision(pv: ProtocolVersion, newRevision: RevisionVersion): ProtocolVersion =
    setAddition(pv.copy(revision = newRevision), Monoid[AdditionVersion].empty)

  /**
   * This function pretends to set a new minor version.
   * When a new model is set, the Revision and the Addition are set to 0.
   * E.g.:
   *
   * val ver = ProtocolVersion(ModelVersion(0), RevisionVersion(1), AdditionVersion(2))
   * val newMajor = ModelVersion(100)
   * ProtocolVersion.setMinor(ver, newMajor) == ProtocolVersion(ModelVersion(100), RevisionVersion(0), AdditionVersion(0))
   *
   * @param pv Protocol Version
   * @return a new ProtocolVersion
   */
  def setModel(pv: ProtocolVersion, newModel: ModelVersion): ProtocolVersion =
    setRevision(pv.copy(model = newModel), Monoid[RevisionVersion].empty)

  /**
   * This function pretends to increment the addition version by one.
   * E.g.:
   *
   * val ver = ProtocolVersion(ModelVersion(0), RevisionVersion(0), AdditionVersion(0))
   * ProtocolVersion.incPatch(ver) == ProtocolVersion(ModelVersion(0), RevisionVersion(0), AdditionVersion(1))
   *
   * @param pv Protocol Version
   * @return a new ProtocolVersion
   */
  def incAddition(pv: ProtocolVersion): ProtocolVersion =
    setAddition(pv, pv.addition |+| AdditionVersion(1))

  /**
   * This function pretends to increment the minor version by one.
   * E.g.:
   *
   * val ver = ProtocolVersion(ModelVersion(0), RevisionVersion(1), AdditionVersion(0))
   * ProtocolVersion.incMinor(ver) == ProtocolVersion(ModelVersion(0), RevisionVersion(1), AdditionVersion(0))
   *
   * @param pv Protocol Version
   * @return a new ProtocolVersion
   */
  def incRevision(pv: ProtocolVersion): ProtocolVersion =
    setRevision(pv, pv.revision |+| RevisionVersion(1))

  /**
   * This function pretends to increment the minor version by one.
   * E.g.:
   *
   * val ver = ProtocolVersion(ModelVersion(0), RevisionVersion(0), AdditionVersion(0))
   * ProtocolVersion.incMajor(ver) == ProtocolVersion(ModelVersion(1), RevisionVersion(0), AdditionVersion(0))
   *
   * @param pv Protocol Version
   * @return a new ProtocolVersion
   */
  def incModel(pv: ProtocolVersion): ProtocolVersion = setModel(pv, pv.model |+| ModelVersion(1))

  /**
   * A simple function for creating a ProtocolVersion from a String
   * E.g.:
   * val version = "10.1.9"
   * ProtocolVersion.fromString(version) == Right(
   * ProtocolVersion(ModelVersion(10), RevisionVersion(1), AdditionVersion(9))
   * )
   *
    * @param s the string to be parsed
   * @return a ProtocolVersion
   */
  def fromString(s: String): Either[ProtocolVersionError, ProtocolVersion] = {
    val matcher = "([0-9]+)".r

    def protocolRight(
        mV: ModelVersion,
        rV: RevisionVersion = Monoid[RevisionVersion].empty,
        aV: AdditionVersion = Monoid[AdditionVersion].empty
    ) = ProtocolVersion(mV, rV, aV).asRight[ProtocolVersionError]

    matcher.findAllIn(s).toList.map(_.toInt) match {
      case List(model, revision, addition) =>
        protocolRight(ModelVersion(model), RevisionVersion(revision), AdditionVersion(addition))
      case List(mode, revision) =>
        protocolRight(ModelVersion(mode), RevisionVersion(revision))
      case List(mode) =>
        protocolRight(ModelVersion(mode))
      case _ => ProtocolVersionError(s"$s is not a valid version string.").asLeft
    }
  }

  /** A simple val fpr defining de initial version: 1.0.0 */
  val initial: ProtocolVersion =
    ProtocolVersion(ModelVersion(1), Monoid[RevisionVersion].empty, Monoid[AdditionVersion].empty)

  /**
   * E.g.:
   *
   * val ver = ProtocolVersion(ModelVersion(10), RevisionVersion(3), AdditionVersion(54))
   * Show[ProtocolVersion].show(ProtocolVersion) == "10.3.54"
   *
   */
  implicit val protocolVersionShow: Show[ProtocolVersion] =
    Show.show(pv => s"${pv.model}.${pv.revision}.${pv.addition}")

  /**
   * E.g.:
   *
   * val ver1 = ProtocolVersion(ModelVersion(10), RevisionVersion(3), AdditionVersion(54))
   * val ver2 = ProtocolVersion(ModelVersion(10), RevisionVersion(3), AdditionVersion(54))
   * Eq[ProtocolVersion].eqv(ver1, ver2) == true
   *
   */
  implicit val protocolVersionEq: Eq[ProtocolVersion] = new Eq[ProtocolVersion] {
    def eqv(x: ProtocolVersion, y: ProtocolVersion): Boolean =
      x.model === y.model && x.revision === x.revision && x.addition === y.addition
  }

  /**
   * Syntax object for ProtocolVersion
   *
    * @param pv
   */
  implicit class ProtocolVersionOps(val pv: ProtocolVersion) extends AnyVal {
    def incAddition                               = ProtocolVersion.incAddition(pv)
    def incRevision                               = ProtocolVersion.incRevision(pv)
    def incModel                                  = ProtocolVersion.incModel(pv)
    def setAddition(newAddition: AdditionVersion) = ProtocolVersion.setAddition(pv, newAddition)
    def setRevision(newRevision: RevisionVersion) = ProtocolVersion.setRevision(pv, newRevision)
    def setModel(newModel: ModelVersion)          = ProtocolVersion.setModel(pv, newModel)
  }
}
