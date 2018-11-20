import microsites._
import sbtorgpolicies.OrgPoliciesPlugin.autoImport._
import sbtorgpolicies.model._
import sbtorgpolicies.runnable.syntax._
import sbtorgpolicies.templates._
import sbtorgpolicies.templates.badges._

lazy val V = new {
  val betterMonadicFor: String = "0.2.4"
  val cats: String             = "1.4.0"
  val catsScalacheck: String   = "0.1.0"
  val circe: String            = "0.10.1"
  val kindProjector: String    = "0.9.8"
  val paradise: String         = "2.1.1"
  val scala: String            = "2.12.7"
  val skeumorph: String        = "0.0.1"
  val specs2: String           = "4.3.5"
}

lazy val root = project
  .in(file("."))
  .settings(commonSettings)
  .settings(
    name := "catamorph"
  )

lazy val docs = project
  .in(file("docs"))
  .dependsOn(root)
  .settings(moduleName := "catamorph-docs")
  .settings(commonSettings)
  .settings(sbtMicrositesSettings)
  .settings(noPublishSettings)
  .settings(
    micrositeName := "Catamorph",
    micrositeDescription := "Schema catalog service",
    micrositeBaseUrl := "/catamorph",
    micrositeGithubOwner := "higherkindness",
    micrositeGithubRepo := "catamorph",
    micrositeHighlightTheme := "tomorrow",
    micrositeOrganizationHomepage := "http://www.47deg.com",
    includeFilter in Jekyll := "*.html" | "*.css" | "*.png" | "*.jpg" | "*.gif" | "*.js" | "*.swf" | "*.md",
    micrositePushSiteWith := GitHub4s,
    micrositeExtraMdFiles := Map(
      file("README.md") -> ExtraMdFileConfig(
        "index.md",
        "home",
        Map("title" -> "Home", "section" -> "home", "position" -> "0")
      ),
      file("CHANGELOG.md") -> ExtraMdFileConfig(
        "changelog.md",
        "home",
        Map("title" -> "changelog", "section" -> "changelog", "position" -> "99")
      )
    ),
    scalacOptions in Tut ~= filterConsoleScalacOptions,
    scalacOptions in Tut += "-language:postfixOps"
  )
  .enablePlugins(MicrositesPlugin)

pgpPassphrase := Some(getEnvVar("PGP_PASSPHRASE").getOrElse("").toCharArray)
pgpPublicRing := file(s"$gpgFolder/pubring.gpg")
pgpSecretRing := file(s"$gpgFolder/secring.gpg")

// General Settings
lazy val commonSettings = Seq(
  orgProjectName := "Catamorph",
  orgGithubSetting := GitHubSettings(
    organization = "higherkindness",
    project = (name in LocalRootProject).value,
    organizationName = "47 Degrees",
    groupId = "io.higherkindness",
    organizationHomePage = url("http://47deg.com"),
    organizationEmail = "hello@47deg.com"
  ),
  startYear := Some(2018),
  scalaVersion := V.scala,
  crossScalaVersions := Seq(scalaVersion.value, "2.11.12"),
  ThisBuild / scalacOptions -= "-Xplugin-require:macroparadise",
  libraryDependencies ++= Seq(
    %%("cats-core", V.cats),
    "io.frees" %% "skeuomorph" % V.skeumorph,
    %%("circe-core", V.circe),
    %%("specs2-core", V.specs2)       % Test,
    %%("specs2-scalacheck", V.specs2) % Test,
    "io.chrisdavenport"               %% "cats-scalacheck" % V.catsScalacheck % Test
  ),
  orgScriptTaskListSetting := List(
    (clean in Global).asRunnableItemFull,
    (compile in Compile).asRunnableItemFull,
    (test in Test).asRunnableItemFull,
    "docs/tut".asRunnableItem,
  ),
  orgMaintainersSetting := List(
    Dev("developer47deg", Some("47 Degrees (twitter: @47deg)"), Some("hello@47deg.com"))),
  // format: OFF
  orgBadgeListSetting := List(
    TravisBadge.apply,
    CodecovBadge.apply, { info => MavenCentralBadge.apply(info.copy(libName = "catamorph")) },
    ScalaLangBadge.apply,
    LicenseBadge.apply, { info => GitterBadge.apply(info.copy(owner = "higherkindness", repo = "catamorph")) },
    GitHubIssuesBadge.apply
  ),
  orgEnforcedFilesSetting := List(
    LicenseFileType(orgGithubSetting.value, orgLicenseSetting.value, startYear.value),
    ContributingFileType( orgProjectName.value, orgGithubSetting.value.copy(organization = "higherkindness", project = "catamorph")),
    AuthorsFileType(name.value, orgGithubSetting.value, orgMaintainersSetting.value, orgContributorsSetting.value),
    NoticeFileType(orgProjectName.value, orgGithubSetting.value, orgLicenseSetting.value, startYear.value),
    VersionSbtFileType,
    ChangelogFileType,
    ReadmeFileType(orgProjectName.value, orgGithubSetting.value, startYear.value, orgLicenseSetting.value, orgCommitBranchSetting.value, sbtPlugin.value, name.value, version.value, scalaBinaryVersion.value, sbtBinaryVersion.value, orgSupportedScalaJSVersion.value, orgBadgeListSetting.value ),
    ScalafmtFileType,
    TravisFileType(crossScalaVersions.value, orgScriptCICommandKey, orgAfterCISuccessCommandKey)
    // format: ON
  )
) ++ compilerPlugins

lazy val compilerPlugins = Seq(
  libraryDependencies ++= Seq(
    compilerPlugin("org.spire-math"  % "kind-projector"      % V.kindProjector cross CrossVersion.binary),
    compilerPlugin("com.olegpy"      %% "better-monadic-for" % V.betterMonadicFor),
    compilerPlugin("org.scalamacros" % "paradise"            % V.paradise cross CrossVersion.patch)
  )
)

// check for library updates whenever the project is [re]load
// format: OFF
onLoad in Global := { s => "dependencyUpdates" :: s }
// format: ON