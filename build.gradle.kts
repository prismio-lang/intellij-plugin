plugins {
  id("java")
  id("org.jetbrains.intellij.platform") version "2.1.0"
}

repositories {
  mavenCentral()

  intellijPlatform {
    defaultRepositories()
  }
}

sourceSets {
  main {
    java {
      srcDirs("src/main/gen")
    }
  }
}

dependencies {
  intellijPlatform {
    // Targets IntelliJ IDEA Community — the plugin uses only platform-level APIs
    // so it runs on both IntelliJ IDEA and CLion (and all JetBrains IDEs).
    intellijIdeaCommunity("2024.2.4")

    pluginVerifier()
    zipSigner()
    instrumentationTools()
  }
  testImplementation("junit:junit:4.13.2")
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
}

// See https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellijPlatform {
  group = "io.prismio"
  buildSearchableOptions = true
  projectName = project.name

  pluginConfiguration {
    version = "1.0.0"
    ideaVersion {
      sinceBuild = "242"
      untilBuild = "253.*"
    }
  }
}

//tasks {
//  patchPluginXml {
//    version.set("${project.version}")
//    sinceBuild.set("233")
//    untilBuild.set("242.*")
//  }
//}