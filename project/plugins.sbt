libraryDependencies += "org.scala-js" %% "scalajs-env-jsdom-nodejs" % "1.0.0-RC2"
libraryDependencies += "org.scala-js" %% "scalajs-linker"           % "1.0.0-RC2"

addSbtPlugin("org.scala-js"      % "sbt-scalajs"         % "1.0.0-RC2")
addSbtPlugin("ch.epfl.scala"     % "sbt-scalajs-bundler" % "0.16.0")
addSbtPlugin("org.foundweekends" % "sbt-bintray"         % "0.5.4")
