plugins {
    kotlin("jvm") version "1.9.25"
    application
}

dependencies {
    implementation(project(path = ":"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
}

application {
    mainClass.set("nl.mdworld.radiometadata.examples.FetchCurrentKt")
}
