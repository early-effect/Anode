import scala.collection.immutable.ListMap

import sbt.protocol.testing.TestResult

val diodeVersion = "1.1.14"
val scala213     = "2.13.18"

// sbt 2.x scopes bare build.sbt settings to ThisBuild.
scalaVersion         := scala213
organization         := "rocks.earlyeffect"
organizationName     := "Early Effect"
organizationHomepage := Some(url("https://www.earlyeffect.rocks"))
versionScheme        := Some("early-semver")
version              := "0.3.0-SNAPSHOT"

homepage := Some(url("https://github.com/early-effect/Anode"))
licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/early-effect/Anode"),
    "scm:git@github.com:early-effect/Anode.git",
  )
)
developers := List(
  Developer(
    id = "russwyte",
    name = "Russ White",
    email = "356303+russwyte@users.noreply.github.com",
    url = url("https://github.com/russwyte"),
  )
)

description := "Anode - a Scala.js facade for Preact with emphasis on stateless components."

// Sonatype Central Portal (sbt 2 localStaging / publishSigned / sonaRelease).
publishTo := {
  val centralSnapshots = "https://central.sonatype.com/repository/maven-snapshots/"
  if (isSnapshot.value) Some("central-snapshots" at centralSnapshots)
  else localStaging.value
}
publishMavenStyle    := true
pomIncludeRepository := { _ => false }

// CI-only publishing: key hex from PGP_KEY_HEX (early-effect org secret).
usePgpKeyHex(sys.env.getOrElse("PGP_KEY_HEX", "MISSING_KEY_HEX"))

// zipx: Aggregate verify + Central publish + Scala Steward. Demo Pages stay in publish-demo-site.yml.
zipxJavaVersion  := "25"
zipxScalaSteward := true
zipxCapabilities += Capability.once(
  name = "fmt",
  command = "scalafmtCheckAll",
)
zipxCapabilities += Capability.once(
  name = "test",
  command = "test",
  needsCapabilities = List("fmt"),
  extraSteps = _ =>
    List(
      Step(
        name = Some("Set up Node"),
        uses = Some("actions/setup-node@v4"),
        `with` = ListMap(
          "node-version" -> "24",
          "cache"        -> "npm",
        ),
      ),
      Step(
        name = Some("Install Node dependencies (preact, jsdom)"),
        run = Some("npm ci"),
      ),
    ),
)
zipxCapabilities += ZipxCentral.release

val copyWorker   = taskKey[Unit]("Copy demoWorker fullLinkJS output into demo-app/public/worker.js")
val cachedAssets = settingKey[Seq[String]]("Static demo assets the service worker may cache")

/** jsdom-backed Test jsEnv. Requires root `npm ci` (jsdom). Vendored JSDOMNodeJSEnv for sbt 2. */
val jsdomTestEnv = Def.settings(
  Test / jsEnv := Def.uncached(new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv())
)

val publishSettings = Seq(
  publishMavenStyle    := true,
  pomIncludeRepository := { _ => false },
)

/** Skip tests without overriding the TestResult return type. */
val noTests = Seq(Test / sources := Nil)

/** Shared Scala.js library settings. CommonJS so Node/jsdom tests can `require("preact")`.
  * In sbt 2, `%%` is platform-aware (replaces sbt 1 `%%%` for JS).
  */
val libSettings = Seq(
  scalaVersion := scala213,
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
  libraryDependencies ++= Seq(
    "org.scala-js"  %% "scalajs-dom"              % "2.8.0",
    "org.scala-js"  %% "scalajs-java-securerandom" % "1.0.0",
    "org.scalameta" %% "munit"                    % "1.0.4" % Test,
  ),
  testFrameworks += new TestFramework("munit.Framework"),
  scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
) ++ jsdomTestEnv

lazy val anode = project
  .in(file("."))
  .aggregate(core, formable, diodeSupport, demo, demoModel, demoWorker)
  .settings(
    name           := "anode",
    publish / skip := true,
  )

lazy val core = project
  .in(file("core"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    libSettings,
    publishSettings,
    name := "anode-core",
  )

lazy val diodeSupport = project
  .in(file("diode-support"))
  .dependsOn(core)
  .enablePlugins(ScalaJSPlugin)
  .settings(
    libSettings,
    publishSettings,
    name := "anode-diode-support",
    libraryDependencies += "io.suzaku" %% "diode" % diodeVersion,
  )

lazy val formable = project
  .in(file("formable"))
  .dependsOn(core)
  .enablePlugins(ScalaJSPlugin)
  .settings(
    libSettings,
    publishSettings,
    name := "anode-formable",
    libraryDependencies += "com.propensive" %% "magnolia"       % "0.17.0",
    libraryDependencies += "org.scala-lang" %  "scala-reflect" % scalaVersion.value,
  )

lazy val demoModel = project
  .in(file("demo-model"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    libSettings,
    noTests,
    name           := "demo-model",
    publish / skip := true,
    libraryDependencies += "io.suzaku" %% "diode" % diodeVersion,
  )

lazy val demoWorker = project
  .in(file("demo-worker"))
  .enablePlugins(ScalaJSPlugin, BuildInfoPlugin)
  .settings(
    scalaVersion   := scala213,
    name           := "demo-worker",
    publish / skip := true,
    noTests,
    libraryDependencies += "org.scala-js" %% "scalajs-dom" % "2.8.0",
    // Vite-served public assets the service worker may cache (hashed Vite chunks are not listed).
    cachedAssets := Seq(
      "index.html",
      "favicon.ico",
      "images/boltface-192.png",
      "images/boltface-512.png",
      "worker.js",
    ),
    buildInfoKeys         := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, cachedAssets),
    buildInfoPackage      := "worker",
    buildInfoOptions += BuildInfoOption.Traits("worker.Context"),
    Compile / mainClass             := Some("worker.ServiceWorker"),
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    copyWorker := Def.uncached {
      (Compile / fullLinkJS).value
      val outDir = (Compile / fullLinkJS / scalaJSLinkerOutputDirectory).value
      val mainJs = outDir / "main.js"
      val dest   = (demo / baseDirectory).value / "public" / "worker.js"
      if (!mainJs.exists)
        sys.error(s"Expected $mainJs after fullLinkJS")
      IO.createDirectory(dest.getParentFile)
      IO.copyFile(mainJs, dest)
      streams.value.log.info(s"copyWorker: $mainJs → $dest")
    },
  )

lazy val demo = project
  .in(file("demo-app"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(core, formable, diodeSupport, demoModel % "test->test;compile->compile")
  .settings(
    scalaVersion   := scala213,
    name           := "demo-app",
    publish / skip := true,
    noTests,
    Compile / mainClass             := Some("todo.Main"),
    scalaJSUseMainModuleInitializer := true,
    // Vite consumes ES modules via the vendored vite-plugin-scalajs.
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    libraryDependencies += "org.scala-js" %% "scalajs-dom" % "2.8.0",
  )

addCommandAlias("worker", "demoWorker/copyWorker")
addCommandAlias("demoDev", "demo/fastLinkJS")
