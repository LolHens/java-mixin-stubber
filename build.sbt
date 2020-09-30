organization := "de.lolhens"
name := "java-mixin-stubber"
version := "0.0.1-SNAPSHOT"

javacOptions ++= Seq(
  "-source", "1.8",
  "-target", "1.8",
  "-encoding", "UTF-8"
)

scalaVersion := "2.13.3"
crossPaths := false

licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))

homepage := Some(url("https://github.com/LolHens/java-mixin-stubber"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/LolHens/java-mixin-stubber"),
    "scm:git@github.com:LolHens/java-mixin-stubber.git"
  )
)
developers := List(
  Developer(id = "LolHens", name = "Pierre Kisters", email = "pierrekisters@gmail.com", url = url("https://github.com/LolHens/"))
)

libraryDependencies ++= Seq(
  "com.github.javaparser" % "javaparser-core" % "3.16.1"
)

Compile / doc / sources := Seq.empty

version := {
  val tagPrefix = "refs/tags/"
  sys.env.get("CI_VERSION").filter(_.startsWith(tagPrefix)).map(_.drop(tagPrefix.length)).getOrElse(version.value)
}

publishMavenStyle := true

publishTo := sonatypePublishToBundle.value

credentials ++= (for {
  username <- sys.env.get("SONATYPE_USERNAME")
  password <- sys.env.get("SONATYPE_PASSWORD")
} yield Credentials(
  "Sonatype Nexus Repository Manager",
  "oss.sonatype.org",
  username,
  password
)).toList
