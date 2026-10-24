/*
 * This file contains the build configuration for the Java application.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/9.8.2/userguide/building_java_projects.html in the Gradle documentation.
 */

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

tasks.jar {
    // Build a fat jar so the output can run with a plain `java -jar`.
    // The classpath is wrapped in a provider so it is resolved at execution
    // time rather than during configuration.
    from(provider { configurations.runtimeClasspath.get().filter { it.name.endsWith(".jar") }.map { zipTree(it) } })
    // Merged dependencies bring along overlapping metadata files (licenses,
    // module-info, ...) which would otherwise fail the build.
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = "factorio.train.analyser.App"
    }
}

dependencies {
    // Use JUnit test framework.
    testImplementation("junit:junit:4.13.2")

    // This dependency is used by the application.
    implementation("com.google.guava:guava:33.7.2-jre")
    implementation("com.google.code.gson:gson:2.15.0")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.test {
    // JUnit 4 has to be selected explicitly; Gradle no longer infers it.
    useJUnit()
}

application {
    // Define the main class for the application.
    mainClass.set("factorio.train.analyser.App")
}
