import java.nio.file.Files

enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)

val cachedAssets = taskKey[Seq[String]]("the list of files scala.js bundler produces via webpack")

val copyWorker = taskKey[Unit]("Moves worker.js to public")

val diodeVersion = "1.1.8"

lazy val root = project
  .in(file("."))
  .aggregate(core, demo, demoModel, demoWorker)
  .settings(
    scalaVersion := "2.13.1",
    name := "root",
    publish := {},
    publishLocal := {}
  )

val baseSettings = Seq(
  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  version := "0.3.7",
  bintrayRepository := "maven",
  organization := "rocks.earlyeffect",
  scalaVersion := "2.13.1",
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
  libraryDependencies ++= Seq(
    "io.suzaku"     %%% "diode"       % diodeVersion,
    "org.scala-js"  %%% "scalajs-dom" % "1.0.0",
    "org.scalatest" %%% "scalatest"   % "3.1.1" % Test
  ),
  requireJsDomEnv in Test := true,
  version in installJsdom := "11.12.0",
  scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
  scalaJSUseMainModuleInitializer := true,
  version in webpack := "4.42.1",
  webpackCliVersion := "3.3.11",
  version in startWebpackDevServer := "3.10.3",
  bintrayReleaseOnPublish := !isSnapshot.value,
  publishMavenStyle := true,
  publishTo := {
    val pt = publishTo.value
    if (isSnapshot.value) {
      Some(
        "Artifactory Realm" at "https://oss.jfrog.org/artifactory/oss-snapshot-local;build.timestamp=" + new java.util.Date().getTime
      )
    } else {
      Some(
        "Artifactory Realm" at "https://oss.jfrog.org/artifactory/oss-release-local"
      )
    }
  }
)

lazy val core = project
  .in(file("core"))
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(
    baseSettings,
    name := "core",
    webpackEmitSourceMaps in Compile := true,
    npmDependencies in Compile ++= Seq(
      "preact"       -> "10.3.4",
      "autoprefixer" -> "9.7.5"
    )
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
    skip in publish := true,
    scalaJSLinkerConfig in Test ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    libraryDependencies += "io.suzaku" %%% "diode" % diodeVersion,
    test := {}
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
    skip in publish := true,
    scalaJSLinkerConfig in Test ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    test := {},
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, demo / cachedAssets),
    buildInfoPackage := "worker",
    mainClass := Some("worker.ServiceWorker"),
    buildInfoOptions += BuildInfoOption.Traits("worker.Context"),
    artifactPath in fullOptJS in Compile := {
      (artifactPath in webpack in Compile in fullOptJS in demo).value.getParentFile / "dist/worker.js"
    },
    addCommandAlias("worker", "demoWorker/fullOptJS;demoWorker/copyWorker"),
    copyWorker := {
      val f    = (artifactPath in fullOptJS in Compile).value
      val path = ((baseDirectory in demo).value / "public/worker.js").toPath
      if (Files.exists(path)) Files.delete(path)
      Files.copy(f.toPath, ((baseDirectory in demo).value / "public/worker.js").toPath)
    }
  )
lazy val demo = project
  .in(file("demo-app"))
  .enablePlugins(ScalaJSBundlerPlugin)
  .dependsOn(core, demoModel % "test->test;compile->compile")
  .settings(
    baseSettings,
    name := "demo-app",
    // webpack stuff
    npmDevDependencies in Compile += "webpack-merge"       -> "4.1.2",
    npmDevDependencies in Compile += "html-webpack-plugin" -> "3.2.0",
    npmDevDependencies in Compile += "copy-webpack-plugin" -> "4.5.1",
    npmDevDependencies in Compile += "crypto-js"           -> "3.1.8",
    jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv(),
    webpackResources := baseDirectory.value / "webpack" * "*",
    webpackBundlingMode in fastOptJS := BundlingMode.LibraryOnly(),
    webpackConfigFile in fastOptJS := Some(
      baseDirectory.value / "webpack" / "webpack-fastopt.config.js"
    ),
    webpackConfigFile in fullOptJS := Some(
      baseDirectory.value / "webpack" / "webpack-opt.config.js"
    ),
    webpackConfigFile in Test := Some(
      baseDirectory.value / "webpack" / "webpack-core.config.js"
    ),
    //    webpackDevServerExtraArgs := Seq("--https", "--inline"),
    version in startWebpackDevServer := "3.9.0",
    webpackEmitSourceMaps in fastOptJS in Compile := true,
    webpackEmitSourceMaps in fullOptJS in Compile := false,
    addCommandAlias(
      "demo",
      "demo/fullOptJS::startWebpackDevServer;~worker"
    ),
    test := {},
    skip in publish := true,
    publish := {},
    publishLocal := {},
    skip in publish := true,
    mainClass := Some("demo.Main"),
    cachedAssets := {
      val files = (webpack in (Compile, fullOptJS)).value
      val res = files
        .filter(!_.data.isHidden)
        .map { file =>
          file.data.getPath.split("/main/dist/")(1)
        }
      println(res.mkString("\n"))
      res
    }
  )
