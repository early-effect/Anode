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
  version := "0.0.1",
  organization := "earlyeffect.rocks",
  scalaVersion := "2.12.8",
  scalacOptions += "-P:scalajs:sjsDefinedByDefault",
  libraryDependencies ++= Seq(
    "org.scala-js"  %%% "scalajs-dom" % "0.9.7",
    "org.scalatest" %%% "scalatest"   % "3.0.8" % Test
  ),
  requireJsDomEnv in Test := true,
  version in installJsdom := "15.1.1",
  scalaJSModuleKind := ModuleKind.CommonJSModule,
  scalaJSUseMainModuleInitializer := true,
  version in webpack := "4.35.2",
  webpackCliVersion := "3.3.5",
  version in startWebpackDevServer := "3.7.2"
)

lazy val core = project
  .in(file("core"))
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(
    baseSettings,
    name := "core",
    webpackEmitSourceMaps in Compile := true,
    npmDependencies in Compile ++= Seq(
      "preact"       -> "10.0.0-beta.3",
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
    scalaJSModuleKind in Test := ModuleKind.CommonJSModule,
    libraryDependencies += "io.suzaku" %%% "diode" % "1.1.5"
  )
lazy val demo = project
  .in(file("demo-app"))
  .enablePlugins(ScalaJSBundlerPlugin)
  .dependsOn(core, demoModel % "test->test;compile->compile")
  .settings(
    baseSettings,
    name := "demo-app",
    scalacOptions += "-P:scalajs:sjsDefinedByDefault",
    // webpack stuff
    npmDevDependencies in Compile += "webpack-merge"       -> "4.1.2",
    npmDevDependencies in Compile += "html-webpack-plugin" -> "3.2.0",
    npmDevDependencies in Compile += "copy-webpack-plugin" -> "4.5.1",
    npmDevDependencies in Compile += "crypto-js"           -> "3.1.8",
    requiresDOM in Test := true,
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
    test := {
      println("No tests - it just packages. :)")
    },
    publish := {},
    publishLocal := {}
  )
