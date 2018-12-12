// Project name (artifact name in Maven)
name := "nicUtil"

// orgnization name (e.g., the package name of the project)
organization := "nic.util"

version := "0.1"

// project description
description := "Low-level Java utility classes for NIC."

// So that "sbt test" will work 
crossPaths := false

// So that "sbt stage" will work. See project/plugins.sbt.
enablePlugins(JavaAppPackaging)

// library dependencies. (organization name) % (project name) % (version)
libraryDependencies ++= Seq(
  "junit" % "junit" % "4.12",
  "com.novocode" % "junit-interface" % "0.11" % "test"
)

