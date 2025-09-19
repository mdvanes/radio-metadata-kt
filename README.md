# radio-metadata

A Kotlin library for fetching and normalizing live radio metadata from multiple sources (HTTP JSON station APIs and ICY stream headers). Provides schema-driven extraction, built‑in presets (NPO Radio 2, Sky Radio) and a strategy interface for extension.

## Features
- Schema-based extraction from arbitrary JSON endpoints (`RadioSchema`, `SchemaPaths`)
- Built-in presets: `npo2`, `sky`
- ICY header strategy (`IcyMetadataStrategy`) parsing classic `StreamTitle` strings
- Concurrent fetch of multiple endpoint URLs with path picking
- Lightweight immutable data classes (`RadioMetadata`, `SongInfo`, `BroadcastInfo`, `TimeInfo`)

## Quick Start
Add the GitHub Packages repository and dependency to your `build.gradle.kts`:
```kotlin
repositories {
    mavenCentral()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/OWNER/radio-metadata-kt")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
        }
    }
}

dependencies {
    implementation("nl.mdworld:radio-metadata:0.1.0") // Adjust version
}
```

### Fetch using a preset (API strategy)
```kotlin
import kotlinx.coroutines.runBlocking
import nl.mdworld.radiometadata.strategies.ApiMetadataUtil

fun main() = runBlocking {
  val tracks = ApiMetadataUtil.getRadioMetaData("npo2")
  val current = tracks.firstOrNull()
  println("Current: ${current?.song?.artist} - ${current?.song?.title}")
}
```

### Examples Module
This repository now includes an `examples` subproject with a runnable entrypoint demonstrating preset usage.

Run the example (defaults to `npo2`):
```bash
sdk use java 21.0.8-tem
./gradlew :examples:run
./gradlew :examples:run --quiet --args="--table"
```

Pass a station via arguments or environment variable:
```bash
./gradlew :examples:run --args="sky"
STATION=sky ./gradlew :examples:run
```

Output format:
```
Current: <artist?> - <title?>
```

### Fetch via ICY headers
```kotlin
import kotlinx.coroutines.runBlocking
import nl.mdworld.radiometadata.strategies.IcyMetadataStrategy

fun main() = runBlocking {
  val icyStrategy = IcyMetadataStrategy()
  val meta = icyStrategy.fetchMetadata("https://example.com/stream.mp3")
  println(meta.firstOrNull()?.song)
}
```

### Custom schema
```kotlin
import nl.mdworld.radiometadata.*
import nl.mdworld.radiometadata.strategies.ApiMetadataUtil
import kotlinx.coroutines.runBlocking

val custom = RadioSchema(
  name = "myStation",
  urls = listOf(UrlConfig("tracks_", "https://api.example.com/now")),
  paths = SchemaPaths(
    tracks = listOf("tracks_", "items"),
    song = SchemaPaths.SongInfo(
      artist = listOf("artist"),
      title = listOf("title"),
      imageUrl = listOf("art")
    )
  )
)

fun main() = runBlocking {
  val tracks = ApiMetadataUtil.getRadioMetaData(custom)
  println(tracks.map { it.song })
}
```

## Gradle Wrapper
The repository should already contain the wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/*`). If starting fresh (e.g. after cloning without wrapper) regenerate it:
```bash
# From project root
./gradlew wrapper --gradle-version 8.9 --distribution-type all
```
Or if no wrapper yet (brand new machine with only system Gradle):
```bash
gradle wrapper --gradle-version 8.9 --distribution-type all
```
Commit the generated files so consumers build with a consistent Gradle version.

## Publishing to GitHub Packages

This library is published to GitHub Packages. To publish a new version:

### Setup
1. Create a Personal Access Token (PAT) with `read:packages` and `write:packages` permissions. Only "tokens (classic)" is supported at this time: https://docs.github.com/en/packages/learn-github-packages/publishing-a-package#publishing-a-package
2. Set environment variables:
   ```bash
   export GITHUB_ACTOR=your-github-username
   export GITHUB_TOKEN=your-personal-access-token
   export RELEASE_VERSION=0.1.0  # or desired version
   ```

### Publishing
```bash
./gradlew publishAllPublicationsToGitHubPackagesRepository
```

### Consuming the Package
To use this library in your project, add the GitHub Packages repository to your `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/OWNER/radio-metadata-kt")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
        }
    }
}

dependencies {
    implementation("nl.mdworld:radio-metadata:0.1.0")
}
```

Set your credentials via environment variables or `gradle.properties`:
```bash
export USERNAME=your-github-username
export TOKEN=your-personal-access-token
```

Or in `~/.gradle/gradle.properties`:
```properties
gpr.user=your-github-username
gpr.key=your-personal-access-token
```

## NOTES

<!-- export JAVA_HOME=$(/usr/libexec/java_home -v 17)
echo $JAVA_HOME
java -version -->
using sdkman:
sdk use java 21.0.8-tem
gradle wrapper --gradle-version 8.9 --distribution-type all

## TODO

- use KTOR
- specify usage