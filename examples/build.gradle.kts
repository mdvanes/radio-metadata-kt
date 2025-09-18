plugins {
    kotlin("jvm") version "1.9.25"
    application
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(project(path = ":"))
}

application {
    mainClass.set("nl.mdworld.radiometadata.examples.FetchCurrentKt")
}
