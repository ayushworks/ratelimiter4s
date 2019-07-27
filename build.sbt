lazy val root = project.in(file("."))
  .settings(coreSettings: _*)
  .settings(noPublishSettings: _*)
  .aggregate(ratelimiter4sJVM, ratelimiter4sZioJVM, ratelimiter4sCatsJVM)
  .dependsOn(ratelimiter4sJVM, ratelimiter4sZioJVM, ratelimiter4sCatsJVM)

lazy val versions = new {
  val scala = "2.12.8"
  val scala211 = "2.11.12"
  val scalatest = "3.0.5"
  val zio = "1.0.0-RC10-1"
  val rateLimiter = "0.17.0"
  val catseffect = "1.3.1"
}

lazy val dependencies = Seq(
  libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  libraryDependencies += "org.scalatest" %%% "scalatest" % versions.scalatest % "test",
  libraryDependencies += "io.github.resilience4j" % "resilience4j-ratelimiter" % versions.rateLimiter
)

lazy val ratelimiter4s = crossProject.crossType(CrossType.Pure)
  .settings(
    moduleName := "ratelimiter4s",
    name := "ratelimiter4s",
    description := "Adding scala effect types on top of resilience4j"
  )
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .settings(dependencies: _*)

lazy val ratelimiter4sJVM = ratelimiter4s.jvm

lazy val ratelimiter4sZio = crossProject.crossType(CrossType.Pure)
  .settings(
    moduleName := "ratelimiter4sZio", name := "ratelimiter4sZio",
    description := "ZIO integration for ratelimiter4s"
  )
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .settings(dependencies: _*)
  .settings(
    libraryDependencies += "dev.zio" %%% "zio" % versions.zio % "test,provided"
  )
  .dependsOn(ratelimiter4s % "compile->compile;test->test")

lazy val ratelimiter4sZioJVM = ratelimiter4sZio.jvm

lazy val ratelimiter4sCats = crossProject.crossType(CrossType.Pure)
  .settings(
    moduleName := "ratelimiter4sCats", name := "ratelimiter4sCats",
    description := "Cats-effect integration for ratelimiter4s"
  )
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .settings(dependencies: _*)
  .settings(
    libraryDependencies += "org.typelevel" %% "cats-effect" % versions.catseffect % "test,provided"
  )
  .dependsOn(ratelimiter4s % "compile->compile;test->test")

lazy val ratelimiter4sCatsJVM = ratelimiter4sCats.jvm

lazy val coreSettings = commonSettings ++ publishSettings

lazy val commonSettings = Seq(
  scalaVersion := versions.scala,
  crossScalaVersions := Seq(versions.scala211, versions.scala),
  scalacOptions := commonScalacOptions
) ++ lintUnused

lazy val commonScalacOptions = Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:higherKinds",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Xexperimental"
)

lazy val lintUnused = Seq(
  scalacOptions ++= {
    if (scalaVersion.value <= "2.12.1") Seq() else Seq("-Xlint:-unused")
  }
)

lazy val publishSettings = Seq(
  organization := "com.github.ayushworks",
  homepage := Some(url("https://github.com/ayushworks/ratelimiter4s")),
  licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  scmInfo := Some(ScmInfo(url("https://github.com/ayushworks/ratelimiter4s"), "scm:git:git@github.com:ayushworks/ratelimiter4s.git")),
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  pomExtra := (
    <developers>
      <developer>
        <id>ayushworks</id>
        <name>Ayush</name>
        <url>http://github.com/ayushworks</url>
      </developer>
    </developers>
    )
)

lazy val noPublishSettings = Seq(
  skip in publish := true,
  publishArtifact := false
)

