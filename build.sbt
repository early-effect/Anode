enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)

lazy val root = project
  .in(file("."))
  .aggregate(core, demo, demoModel)
  .settings(
    name := "root",
    publish := {},
    publishLocal := {}
  )

val baseSettings = Seq(
  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  version := "0.1.0-SNAPSHOT",
  bintrayRepository := "maven",
  organization := "rocks.earlyeffect",
  scalaVersion := "2.13.1",
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
  libraryDependencies ++= Seq(
    "io.suzaku"     %%% "diode"       % "1.1.7-local",
    "org.scala-js"  %%% "scalajs-dom" % "0.9.8",
    "org.scalatest" %%% "scalatest"   % "3.1.0" % Test
  ),
  requireJsDomEnv in Test := true,
  version in installJsdom := "11.12.0",
  scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
  scalaJSUseMainModuleInitializer := true,
  version in webpack := "4.35.2",
  webpackCliVersion := "3.3.5",
  version in startWebpackDevServer := "3.7.2",
  bintrayReleaseOnPublish := !isSnapshot.value,
  publishMavenStyle := true,
  publishTo := {
    val pt = publishTo.value
    if (isSnapshot.value) {
      Some(
        "Artifactory Realm" at "https://oss.jfrog.org/artifactory/oss-snapshot-local;build.timestamp=" + new java.util.Date().getTime
      )
    } else pt
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
      "preact"       -> "10.0.1",
      "autoprefixer" -> "9.6.1"
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
    libraryDependencies += "io.suzaku" %%% "diode" % "1.1.7-local",
    test := {}
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
    webpackDevServerExtraArgs := Seq("--inline"),
    webpackEmitSourceMaps in Compile := true,
    addCommandAlias(
      "demo",
      ";demo/fastOptJS::startWebpackDevServer;~demo/fastOptJS"
    ),
    test := {},
    skip in publish := true,
    publish := {},
    publishLocal := {},
    skip in publish := true,
    mainClass := Some("demo.Main")
//    scalaJSMainModuleInitializer := Some("demo.Main")
  )
