import org.scalablytyped.converter.internal.scalajs.Dep
import org.scalajs.linker.interface.ModuleSplitStyle

ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / organization := "org.aurora"
ThisBuild / scalaVersion := DependencyVersions.scalaVersion

ThisBuild / scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Yretain-trees" //necessary in zio-json if any case classes have default parameters
)


lazy val root = project.in(file(".")).
  aggregate(cross.js, cross.jvm).
  settings(
    publish := {},
    publishLocal := {}

  )

lazy val cross = crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Full).in(file("modules/common"))
  .settings(
    name := "ziohttpcross",
    version := "0.1-SNAPSHOT",
    libraryDependencies ++= Dependencies.zioJson.value,
    libraryDependencies ++= Dependencies.scalatest.value

  ).
  jvmSettings(
    // Add JVM-specific settings here
    ThisBuild / fork              := false,   //of rourse javascripts cannot fork as they are single threaded
    ThisBuild / semanticdbEnabled := true,
    ThisBuild / semanticdbVersion := scalafixSemanticdb.revision,
    ThisBuild / scalafixDependencies ++= List("com.github.liancheng" %% "organize-imports" % "0.6.0"),
    libraryDependencies += Dependencies.dataimportcsv3s


  ).
  jsSettings(
    // Add JS-specific settings here
    libraryDependencies ++= Dependencies.jsclientlibraries.value,


    // scalaJSUseMainModuleInitializer := true

  )



lazy val server = (project in file("modules/server"))
  .settings(
    semanticdbEnabled               := true,
    autoAPIMappings                 := true,
    Compile / mainClass             := Some(("com.axiom.server.MainApp")),
    
    libraryDependencies ++= Seq(
      Dependencies.zioHttp, 
      Dependencies.zioTest,
      Dependencies.zioTestSBT, 
      Dependencies.zioTestMagnolia,
      Dependencies.dataimportcsv3s,
      Dependencies.zioHttpTest
    )


  )
  .dependsOn(cross.jvm)

  lazy val client = (project in file("modules/client"))
  .enablePlugins(ScalaJSPlugin) // Enable the Scala.js plugin in this project
  .enablePlugins(ScalablyTypedConverterExternalNpmPlugin)
  .dependsOn(cross.js)
  .settings(

    // Tell Scala.js that this is an application with a main method
    scalaJSUseMainModuleInitializer := true,

    /* Configure Scala.js to emit modules in the optimal way to
     * connect to Vite's incremental reload.
     * - emit ECMAScript modules
     * - emit as many small modules as possible for classes in the "livechart" package
     * - emit as few (large) modules as possible for all other classes
     *   (in particular, for the standard library)
     */
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
        .withModuleSplitStyle(
          ModuleSplitStyle.SmallModulesFor(List("client")))
    },

    /*
     *add resolver for scalatest
     */
    resolvers += "Artima Maven Repository" at "https://repo.artima.com/releases",


    /* Depend on the scalajs-dom library.
     * It provides static types for the browser DOM APIs.
     */
    libraryDependencies ++= Dependencies.jsclientlibraries.value,
    libraryDependencies ++= Dependencies.scalatest.value,

    // Tell ScalablyTyped that we manage `npm install` ourselves
    externalNpm := baseDirectory.value,
  )  