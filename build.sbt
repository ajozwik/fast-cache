import java.time.LocalDate

val `scalaVersion_3`    = "3.3.1"
val `scalaVersion_2.13` = "2.13.11"

crossScalaVersions := Seq(`scalaVersion_2.13`, `scalaVersion_3`)

ThisBuild / scalaVersion := sys.props.getOrElse("scala.version", `scalaVersion_3`)

ThisBuild / organization := "com.github.ajozwik"

name := "fast-cache"

val targetJdk = "11"

ThisBuild / scalafixDependencies += "com.github.vovapolu" %% "scaluzzi" % "0.1.23"

ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)

ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-language:_",
  s"-release:$targetJdk"
) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
  case Some((2, _)) =>
    Seq(
      "-Ycache-plugin-class-loader:last-modified",
      "-Ycache-macro-class-loader:last-modified",
      "-Ywarn-dead-code",
      "-Xlint",
      "-Yrangepos",
      "-Xsource:3",
      "-Xmaxwarns",
      200.toString,
      "-Wconf:cat=lint-multiarg-infix:silent",
      "-Xlint:-byname-implicit",
      "-Ymacro-annotations"
    )
  case _ =>
    Seq(
      "-Wunused:imports",
      "-Wunused:linted",
      "-Wunused:locals",
      "-Wunused:params",
      "-Wunused:privates",
      "-language:implicitConversions"
    )
})

publish / skip := true

val scalatestVersion = "3.2.17"

val `ch.qos.logback_logback-classic`           = "ch.qos.logback"              % "logback-classic" % "1.2.12"
val `com.github.blemale_scaffeine`             = "com.github.blemale"         %% "scaffeine"       % "5.2.1"
val `com.typesafe.scala-logging_scala-logging` = "com.typesafe.scala-logging" %% "scala-logging"   % "3.9.5"
val `org.scalatest_scalatest`                  = "org.scalatest"              %% "scalatest"       % scalatestVersion       % Test
val `org.scalatestplus_scalacheck`             = "org.scalatestplus"          %% "scalacheck-1-17" % s"$scalatestVersion.0" % Test

lazy val `root` = projectName("root", file("."))
  .settings(
    libraryDependencies ++= Seq(
      `com.github.blemale_scaffeine`,
      `ch.qos.logback_logback-classic`,
      `com.typesafe.scala-logging_scala-logging`,
      `org.scalatest_scalatest`,
      `org.scalatestplus_scalacheck`
    )
  )
  .enablePlugins(PackPlugin)

def projectName(name: String, file: File): Project =
  Project(name, file).settings(
    libraryDependencies ++= Seq(
      `org.scalatest_scalatest`,
      `org.scalatestplus_scalacheck`
    ),
    licenseReportTitle       := s"Copyright (c) ${LocalDate.now.getYear} Andrzej Jozwik",
    licenseSelection         := Seq(LicenseCategory.MIT),
    Compile / doc / sources  := Seq.empty,
    Test / parallelExecution := false
  )
