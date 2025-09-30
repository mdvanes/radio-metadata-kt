plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.serialization") version "1.9.25"
    `maven-publish`
    signing
}

group = "nl.mdworld"
version = System.getenv("RELEASE_VERSION") ?: "0.1.0-SNAPSHOT"

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.2")
    
    // Ktor client dependencies
    implementation("io.ktor:ktor-client-core:2.3.5")
    implementation("io.ktor:ktor-client-cio:2.3.5")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.5")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set("radio-metadata")
                description.set("A Kotlin library for aggregating and normalizing live radio stream metadata (station, current track, artist, timing)")
                url.set("https://github.com/${System.getenv("GITHUB_ACTOR")}/radio-metadata-kt")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("yourid")
                        name.set("Your Name")
                        email.set("you@example.com")
                    }
                }
                scm {
                    url.set("https://github.com/${System.getenv("GITHUB_ACTOR")}/radio-metadata-kt")
                    connection.set("scm:git:git://github.com/${System.getenv("GITHUB_ACTOR")}/radio-metadata-kt.git")
                    developerConnection.set("scm:git:ssh://github.com:${System.getenv("GITHUB_ACTOR")}/radio-metadata-kt.git")
                }
            }
        }
    }
    repositories {
        // Example for Maven Central (via OSSRH) staging or GitHub Packages - customize as needed
        val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
        val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven {
            name = "OSSRH"
            url = if (version.toString().endsWith("-SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            credentials {
                username = System.getenv("OSSRH_USERNAME")
                password = System.getenv("OSSRH_PASSWORD")
            }
        }
        // GitHub Packages
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/${System.getenv("GITHUB_ACTOR")}/radio-metadata-kt")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

signing {
    val signingKey: String? = System.getenv("SIGNING_KEY")
    val signingPassphrase: String? = System.getenv("SIGNING_PASSPHRASE")
    if (!signingKey.isNullOrBlank() && !signingPassphrase.isNullOrBlank()) {
        useInMemoryPgpKeys(signingKey, signingPassphrase)
        sign(publishing.publications)
    } else {
        logger.lifecycle("Signing disabled: SIGNING_KEY or SIGNING_PASSPHRASE not provided")
    }
}
