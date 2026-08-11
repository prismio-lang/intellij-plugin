import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
  id("java")
  id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "io.prismio"
version = "1.0.0"

repositories {
  mavenCentral()

  intellijPlatform {
    defaultRepositories()
    intellijDependencies()
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
    // The unified IntelliJ IDEA distribution is the current platform artifact.
    // The plugin uses the shared platform plus its extracted spellchecker module.
    intellijIdea("2026.2.1")
    bundledModule("intellij.spellchecker")
    bundledModule("intellij.platform.debugger")
    testFramework(TestFrameworkType.Platform)

    pluginVerifier()
    zipSigner()
  }
  testImplementation("junit:junit:4.13.2")
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(26)
  }
  sourceCompatibility = JavaVersion.VERSION_25
  targetCompatibility = JavaVersion.VERSION_25
}

tasks.withType<JavaCompile>().configureEach {
  // Build with the current JDK while emitting bytecode loadable by the
  // Java 25 runtime bundled with IntelliJ Platform 2026.2.
  options.release = 25
}

// See https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellijPlatform {
  buildSearchableOptions = true
  projectName = project.name

  pluginConfiguration {
    // The minimum build defaults to the selected platform. No until-build is
    // declared so compatible future IDE updates are not needlessly blocked.
  }
}
