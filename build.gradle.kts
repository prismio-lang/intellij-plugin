import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
  id("java")
  id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "io.prismio"
version = "1.1.0"

repositories {
  mavenCentral()

  intellijPlatform {
    defaultRepositories()
    intellijDependencies()
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

// The corpus test lexes every .psm in a real checkout. Point it at one to run
// it; without the property it skips, so the suite passes for a contributor who
// only has this repository.
tasks.withType<Test>().configureEach {
  System.getProperty("prismio.checkout")?.let { systemProperty("prismio.checkout", it) }
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
