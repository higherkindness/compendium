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
  val http4s: String           = "0.18.21"
  val shapeless: String        = "2.3.3"
  val pureConfig: String       = "0.9.0"
}

lazy val root = project
  .in(file("."))
  .settings(commonSettings)
  .settings(
    name := "compendium"
  )

lazy val docs = project
  .in(file("docs"))
  .dependsOn(root)
  .settings(moduleName := "compendium-docs")
  .settings(commonSettings)
  .settings(sbtMicrositesSettings)
  .settings(noPublishSettings)
  .settings(
    micrositeName := "Compendium",
    micrositeDescription := "Schema catalog service",
    micrositeBaseUrl := "/compendium",
    micrositeGithubOwner := "higherkindness",
    micrositeGithubRepo := "compendium",
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
  orgProjectName := "Compendium",
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
    %%("shapeless", V.shapeless),
    %%("pureconfig", V.pureConfig),
    "io.frees" %% "skeuomorph" % V.skeumorph,
    %%("http4s-dsl", V.http4s),
    %%("http4s-blaze-server", V.http4s),
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
    CodecovBadge.apply, { info => MavenCentralBadge.apply(info.copy(libName = "compendium")) },
    ScalaLangBadge.apply,
    LicenseBadge.apply, { info => GitterBadge.apply(info.copy(owner = "higherkindness", repo = "compendium")) },
    GitHubIssuesBadge.apply
  ),
  orgEnforcedFilesSetting := List(
    LicenseFileType(orgGithubSetting.value, orgLicenseSetting.value, startYear.value),
    ContributingFileType( orgProjectName.value, orgGithubSetting.value.copy(organization = "higherkindness", project = "compendium")),
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