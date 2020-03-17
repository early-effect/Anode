libraryDependencies += "org.scala-js" %% "scalajs-env-jsdom-nodejs" % "1.0.0"
//libraryDependencies += "org.scala-js" %% "scalajs-linker"           % "1.0.0"

addSbtPlugin("org.scala-js"      % "sbt-scalajs"         % "1.0.1")
addSbtPlugin("ch.epfl.scala"     % "sbt-scalajs-bundler" % "0.17.0")
addSbtPlugin("org.foundweekends" % "sbt-bintray"         % "0.5.4")
addSbtPlugin("com.eed3si9n"      % "sbt-buildinfo"       % "0.9.0")
