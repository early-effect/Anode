import java.nio.file.Files

enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)

val cachedAssets = taskKey[Seq[String]]("the list of files scala.js bundler produces via webpack")

val copyWorker = taskKey[Unit]("Moves worker.js to public")

val diodeVersion = "1.1.14"

lazy val token = sys.env.getOrElse("GITHUB_TOKEN", "No Token")

val publishSettings = Seq(
  version := "0.3.2-SNAPSHOT",
  versionScheme := Some(sbt.VersionScheme.SemVerSpec),
  organization := "rocks.earlyeffect",
  organizationName := "earlyeffect",
  organizationHomepage := Some(url("http://www.earlyeffect.rocks")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/russwyte/Anode"),
      "scm:git@github.com:early-effect/Anode.git",
    )
  ),
  developers := List(
    Developer(
      id = "russwyte",
      name = "Russ White",
      email = "russ.white@earlyeffect.rocks",
      url = url("http://www.earlyeffect.rocks"),
    )
  ),
  description := "Anode - a library for creating web apps in Scala.js.",
  licenses := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  homepage := Some(url("http://www.earlyeffect.rocks")),
  // Remove all additional repository other than Maven Central from POM
  pomIncludeRepository := { _ => false },
  publishTo := {
    val nexus = "https://s01.oss.sonatype.org/"
    if (isSnapshot.value) Some("snapshots".at(nexus + "content/repositories/snapshots"))
    else Some("releases".at(nexus + "service/local/staging/deploy/maven2"))
  },
  publishMavenStyle := true,
)

val baseSettings = Seq(
  scalaVersion := "2.13.5",
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
  libraryDependencies ++= Seq(
    "org.scala-js"  %%% "scalajs-dom" % "1.1.0",
    "org.scalameta" %%% "munit"       % "0.7.25" % Test,
  ),
  testFrameworks += new TestFramework("munit.Framework"),
  installJsdom / version.withRank(KeyRanks.Invisible) := "16.4.0",
  scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
  Test / requireJsDomEnv := true,
  webpack / version.withRank(KeyRanks.Invisible) := "4.46.0",
  webpackCliVersion.withRank(KeyRanks.Invisible) := "3.3.11",
  startWebpackDevServer / version.withRank(KeyRanks.Invisible) := "3.11.2",
)

lazy val anode = project
  .in(file("."))
  .aggregate(core,formable, diodeSupport, demo, demoModel, demoWorker)
  .settings(
    publish / skip := true,
    scalaVersion := "2.13.5",
    name := "anode",
    publish := {},
    publishLocal := {},
  )

lazy val core = project
  .in(file("core"))
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(
    baseSettings,
    publishSettings,
//    crossScalaVersions := Seq("2.13.5","3.0.0-RC1"),
    name := "anode-core",
    Compile / fastOptJS / webpackEmitSourceMaps := true,
    Compile / fullOptJS / webpackEmitSourceMaps := false,
    Compile / npmDependencies ++= Seq(
      "preact"       -> "10.5.10",
      "autoprefixer" -> "10.2.3",
      "postcss"      -> "8.2.6",
    ),
    Test / webpackConfigFile := Some(
      baseDirectory.value / "webpack" / "no-fs-config.js"
    ),
  )
lazy val jsdocs = project
  .enablePlugins(ScalaJSBundlerPlugin)
  .dependsOn(core)
  .settings(
    webpackBundlingMode := BundlingMode.LibraryOnly(),
    scalaJSUseMainModuleInitializer := true,
    scalaVersion := "2.13.5",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.1.0"
  )
lazy val docs = project
  .in(file("core-docs"))
  .enablePlugins(MdocPlugin)
  .dependsOn(core)
  .settings(
    publish / skip := true,
    scalaVersion := "2.13.5",
    mdocJS := Some(jsdocs),
    mdocJSLibraries := webpack.in(jsdocs, Compile, fullOptJS).value
  )


lazy val diodeSupport = project
  .in(file("diode-support"))
  .dependsOn(core)
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(
    baseSettings,
    publishSettings,
    name := "anode-diode-support",
    libraryDependencies += "io.suzaku" %%% "diode" % diodeVersion,
    Compile / fastOptJS / webpackEmitSourceMaps := true,
    Compile / fullOptJS / webpackEmitSourceMaps := false,
  )
lazy val formable = project
  .in(file("formable"))
  .dependsOn(core % "compile->compile", core % "test->test")
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(
    baseSettings,
    publishSettings,
    name := "anode-formable",
    libraryDependencies += "com.propensive" %%% "magnolia" % "0.17.0" ,
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    Compile / fastOptJS / webpackEmitSourceMaps := true,
    Compile / fullOptJS / webpackEmitSourceMaps := false,
    Test / webpackConfigFile := (core / Test / webpackConfigFile).value
  )

lazy val demoModel = project
  .in(file("demo-model"))
  .enablePlugins(ScalaJSPlugin)
  .disablePlugins(ScalaJSBundlerPlugin)
  .settings(
    baseSettings,
    name := "demo-model",
    publish := {},
    publishLocal := {},
    publish / skip := true,
    Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    libraryDependencies += "io.suzaku" %%% "diode" % diodeVersion,
    test := {},
  )

lazy val demoWorker = project
  .in(file("demo-worker"))
  .enablePlugins(ScalaJSPlugin, BuildInfoPlugin)
  .disablePlugins(ScalaJSBundlerPlugin)
  .settings(
    baseSettings,
    name := "demo-worker",
    publish := {},
    publishLocal := {},
    publish / skip := true,
    Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    test := {},
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, demo / cachedAssets),
    buildInfoPackage := "worker",
    mainClass.withRank(KeyRanks.Invisible) := Some("worker.ServiceWorker"),
    buildInfoOptions += BuildInfoOption.Traits("worker.Context"),
    Compile / fullOptJS / artifactPath := {
      (demo / Compile / webpack / fullOptJS / artifactPath).value.getParentFile / "dist/worker.js"
    },
    addCommandAlias("worker", "demoWorker/fullOptJS;demoWorker/copyWorker"),
    copyWorker := {
      val f    = (Compile / fullOptJS / artifactPath).value
      val path = ((demo / baseDirectory).value / "public/worker.js").toPath
      if (Files.exists(path)) Files.delete(path)
      Files.copy(f.toPath, path)
    },
  )

lazy val demo = project
  .in(file("demo-app"))
  .enablePlugins(ScalaJSBundlerPlugin)
  .dependsOn(core, formable, diodeSupport, demoModel % "test->test;compile->compile")
  .settings(
    baseSettings,
    name := "demo-app",
    // webpack stuff
    Compile / npmDependencies ++= Seq(
      "webpack-merge"       -> "4.1.2",
      "html-webpack-plugin" -> "4.5.2",
      "copy-webpack-plugin" -> "4.6.0",
      "crypto-js"           -> "4.0.0",
    ),
    webpackResources := baseDirectory.value / "webpack" * "*",
    fastOptJS / webpackBundlingMode := BundlingMode.LibraryOnly(),
    fastOptJS / webpackConfigFile := Some(
      baseDirectory.value / "webpack" / "webpack-fastopt.config.js"
    ),
    fullOptJS / webpackConfigFile := Some(
      baseDirectory.value / "webpack" / "webpack-opt.config.js"
    ),
    Test / webpackConfigFile := Some(
      baseDirectory.value / "webpack" / "webpack-core.config.js"
    ),
    fastOptJS / webpackDevServerExtraArgs := Seq("--inline", "--hot"),
    Compile / fastOptJS / webpackEmitSourceMaps := true,
    Compile / fullOptJS / webpackEmitSourceMaps := false,
    addCommandAlias(
      "demo",
      "demo/Compile/fastOptJS/startWebpackDevSer" +
        "ver;~demo/fastOptJS",
    ),
    addCommandAlias(
      "demoFull",
      "demo/Compile/fullOptJS/startWebpackDevServer;~demo/fullOptJS",
    ),
    test := {},
    publish / skip := true,
    publish := {},
    publishLocal := {},
    cachedAssets := {
      val files = (Compile / fullOptJS / webpack).value
      files
        .filter(!_.data.isHidden)
        .map { file =>
          file.data.getPath.split("/main/dist/")(1)
        }
    },
  )
