import microsites._
import sbtorgpolicies.OrgPoliciesPlugin.autoImport._
import sbtorgpolicies.model._
import sbtorgpolicies.runnable.syntax._
import sbtorgpolicies.templates._
import sbtorgpolicies.templates.badges._

lazy val V = new {
  val betterMonadicFor: String = "0.3.0"
  val cats: String             = "1.6.1"
  val catsScalacheck: String   = "0.1.1"
  val mouse: String            = "0.20"
  val circe: String            = "0.11.1"
  val kindProjector: String    = "0.10.3"
  val paradise: String         = "2.1.1"
  val scala: String            = "2.12.8"
  val skeumorph: String        = "0.0.10"
  val specs2: String           = "4.6.0"
  val enumeratum: String       = "1.5.13"
  val enumeratumCirce: String  = "1.5.21"
  val http4s: String           = "0.20.3"
  val shapeless: String        = "2.3.3"
  val pureConfig: String       = "0.11.1"
  val hammock: String          = "0.9.2"
  val doobie: String           = "0.7.1"
  val flyway: String           = "5.2.4"
  val refined: String          = "0.9.8"
}

lazy val root = project
  .in(file("."))
  .settings(name := "compendium")
  .settings(commonSettings)
  .settings(noPublishSettings)
  .aggregate(server, client, common)

lazy val common = project
  .in(file("modules/common"))
  .settings(commonSettings)
  .settings(
    name := "compendium-common"
  )

lazy val server = project
  .enablePlugins(UniversalPlugin, JavaAppPackaging, BuildInfoPlugin)
  .in(file("modules/server"))
  .settings(commonSettings)
  .settings(serverSettings)
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "buildinfo",
    name := "compendium-server"
  )
  .dependsOn(common)

lazy val client = project
  .in(file("modules/client"))
  .settings(commonSettings)
  .settings(clientSettings)
  .settings(
    name := "compendium-client"
  )
  .dependsOn(common)

lazy val docs = project
  .in(file("docs"))
  .dependsOn(server, client)
  .settings(moduleName := "compendium-docs")
  .settings(commonSettings)
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
    micrositeGithubToken := getEnvVar(orgGithubTokenSetting.value),
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
    scalacOptions in console ~= filterConsoleScalacOptions,
    scalacOptions in doc ~= filterConsoleScalacOptions,
    scalacOptions in Tut ~= filterConsoleScalacOptions,
    scalacOptions in Tut += "-language:postfixOps"
  )
  .enablePlugins(MicrositesPlugin)

pgpPassphrase := Some(getEnvVar("PGP_PASSPHRASE").getOrElse("").toCharArray)
pgpPublicRing := file(s"$gpgFolder/pubring.gpg")
pgpSecretRing := file(s"$gpgFolder/secring.gpg")

//General Settings
lazy val commonSettings = Seq(
  orgProjectName := "compendium",
  orgGithubSetting := GitHubSettings(
    organization = "higherkindness",
    project = (name in LocalRootProject).value,
    organizationName = "47 Degrees",
    groupId = "io.higherkindness",
    organizationHomePage = url("http://47deg.com"),
    organizationEmail = "hello@47deg.com"
  ),
  startYear := Some(2018),
  scalaVersion := "2.12.8",
  crossScalaVersions := Seq(scalaVersion.value),
  ThisBuild / scalacOptions -= "-Xplugin-require:macroparadise",
  libraryDependencies ++= Seq(
    %%("cats-core", V.cats),
    "org.typelevel" %% "mouse" % V.mouse,
    %%("shapeless", V.shapeless),
    %%("pureconfig", V.pureConfig),
    "com.github.pureconfig" %%  "pureconfig-generic"      % V.pureConfig,
    "com.github.pureconfig" %%  "pureconfig-cats-effect"  % V.pureConfig,
    "com.github.pureconfig" %%  "pureconfig-enumeratum"  % V.pureConfig,
    "io.higherkindness"     %%  "skeuomorph"              % V.skeumorph,
    %%("http4s-dsl", V.http4s),
    %%("http4s-blaze-server", V.http4s),
    %%("http4s-circe", V.http4s),
    %%("circe-core", V.circe),
    %%("circe-generic", V.circe),
    %%("doobie-core", V.doobie),
    %%("doobie-postgres", V.doobie),
    %%("doobie-hikari", V.doobie),
    "org.tpolecat"                    %% "doobie-refined" % V.doobie,
    "com.beachape"                    %% "enumeratum" % V.enumeratum,
    "com.beachape"                    %% "enumeratum-circe" % V.enumeratumCirce,
    "org.flywaydb"                    % "flyway-core" % V.flyway,
    %%("specs2-core", V.specs2)       % Test,
    %%("specs2-scalacheck", V.specs2) % Test,
    %%("doobie-specs2", V.doobie)     % Test,
    "io.chrisdavenport"               %% "cats-scalacheck" % V.catsScalacheck % Test,
    "io.chrisdavenport"               %% "testcontainers-specs2" % "0.1.0" % Test,
    "org.testcontainers"              % "postgresql" % "1.11.3" % Test
  ),
  orgScriptTaskListSetting := List(
    (clean in Global).asRunnableItemFull,
    (compile in Compile).asRunnableItemFull,
    (test in Test).asRunnableItemFull,
    "docs/tut".asRunnableItem,
  ),
  orgMaintainersSetting := List(
    Dev("47degdev", Some("47 Degrees (twitter: @47deg)"), Some("hello@47deg.com"))),
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

//Settings
lazy val clientSettings = Seq(
  libraryDependencies ++= Seq(
    "com.pepegar" %% "hammock-core"            % V.hammock,
    "com.pepegar" %% "hammock-asynchttpclient" % V.hammock,
    "com.pepegar" %% "hammock-circe"           % V.hammock
  )
)

lazy val serverSettings = Seq(
  parallelExecution in Test := false,
  libraryDependencies ++= Seq(
    "org.slf4j"         % "slf4j-simple"        % "1.7.26",
    "eu.timepit"        %% "refined"            % V.refined,
    "eu.timepit"        %% "refined-scalacheck" % V.refined,
    "io.chrisdavenport" %% "cats-scalacheck"    % V.catsScalacheck % Test,
  )
)

lazy val compilerPlugins = Seq(
  libraryDependencies ++= Seq(
    compilerPlugin("org.typelevel"   % "kind-projector"      % V.kindProjector cross CrossVersion.binary),
    compilerPlugin("com.olegpy"      %% "better-monadic-for" % V.betterMonadicFor),
    compilerPlugin("org.scalamacros" % "paradise"            % V.paradise cross CrossVersion.patch)
  )
)

val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false,
  skip in publish := true
)

// check for library updates whenever the project is [re]load
// format: OFF
onLoad in Global := { s => "dependencyUpdates" :: s }
// format: ON
