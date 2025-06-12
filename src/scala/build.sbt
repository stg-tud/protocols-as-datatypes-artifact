import Settings.scala3defaults

lazy val proBench = project.in(file("benchmarks"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    scala3defaults,
    Universal / packageName := "probench",
    Universal / name        := "probench",
    Settings.javaOutputVersion(17),
    Settings.explicitNulls(Compile / compile),
    Settings.safeInit(Compile / compile),
    libraryDependencies ++= Seq(
      "com.github.plokhotnyuk.jsoniter-scala" %%% "jsoniter-scala-core"   % "2.36.3",
      "com.github.plokhotnyuk.jsoniter-scala" %%% "jsoniter-scala-macros" % "2.36.3" % Provided
    ),
    // internal dependencies
    libraryDependencies += "de.tu-darmstadt.stg" %% "channels" % "0.37.0+302-b72ae242",
    libraryDependencies += "de.tu-darmstadt.stg" %% "replication" % "0.37.0+302-b72ae242",
    libraryDependencies += "de.tu-darmstadt.stg" %% "rdts" % "0.37.0+302-b72ae242",
    libraryDependencies += "de.tu-darmstadt.stg" %% "reactives" % "0.37.0+302-b72ae242",
    // external dependencies
    libraryDependencies += "org.scalameta"         %%% "munit"                  % "1.1.1"  % Test,
    libraryDependencies += "org.scalameta"         %%% "munit-scalacheck"       % "1.1.0"  % Test,
    libraryDependencies += "de.rmgk.slips"         %%% "partypack"              % "0.14.0",
    libraryDependencies += "io.etcd"                 % "jetcd-core"             % "0.8.5",
    libraryDependencies += "com.lihaoyi"           %%% "pprint"                 % "0.9.0",
  )
