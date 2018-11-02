// Project name (artifact name in Maven)
name := "nicUtil"

// orgnization name (e.g., the package name of the project)
organization := "tmt.nic.util"

version := "0.1"

// project description
description := "Low-level Java utility classes for NIC."

crossPaths := false

// library dependencies. (organization name) % (project name) % (version)
libraryDependencies ++= Seq(
  "com.novocode" % "junit-interface" % "0.11" % "test"
)

