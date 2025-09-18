# radio-metadata

A Kotlin library for fetching and normalizing live radio metadata from multiple sources (HTTP JSON station APIs and ICY stream headers). Provides schema-driven extraction, built‑in presets (NPO Radio 2, Sky Radio) and a strategy interface for extension.

## Features
- Schema-based extraction from arbitrary JSON endpoints (`RadioSchema`, `SchemaPaths`)
- Built-in presets: `npo2`, `sky`
- ICY header strategy (`IcyMetadataStrategy`) parsing classic `StreamTitle` strings
- Concurrent fetch of multiple endpoint URLs with path picking
- Lightweight immutable data classes (`RadioMetadata`, `SongInfo`, `BroadcastInfo`, `TimeInfo`)

## Quick Start
Add the dependency (after publishing) to your `build.gradle.kts`:
```kotlin
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

## Versioning Strategy
- Use semantic versions: MAJOR.MINOR.PATCH
- Append `-SNAPSHOT` while iterating between releases
- CI can set `RELEASE_VERSION` env var for reproducible builds (falls back to `0.1.0-SNAPSHOT`)

## Publishing (Maven Central / OSSRH)
Configured plugins: `maven-publish` + `signing` (in-memory PGP). Publication name: `maven`.

### Required Environment Variables
| Variable | Purpose |
|----------|---------|
| `OSSRH_USERNAME` | Sonatype account username |
| `OSSRH_PASSWORD` | Sonatype (OSSRH) password / token |
| `SIGNING_KEY` | ASCII-armored PGP private key block (no passphrase lines trimmed) |
| `SIGNING_PASSPHRASE` | Passphrase for the above key |
| `RELEASE_VERSION` | Explicit version (e.g. 0.2.0 or 0.2.1-SNAPSHOT) |

### Generating a PGP Key (if you do not have one)
```bash
gpg --full-generate-key # RSA 4096 recommended
gpg --list-secret-keys --keyid-format=long
# Export armored private key for SIGNING_KEY
KEYID=REPLACE_WITH_LONG_KEYID
gpg --export-secret-keys --armor "$KEYID" > private.asc
# Export public key (optional: upload to keyserver) 
gpg --export --armor "$KEYID" > public.asc
```
Copy the entire contents of `private.asc` (including BEGIN/END lines) into `SIGNING_KEY` environment variable in CI secret storage.

### Local One-Off Publish (Snapshot)
```bash
export OSSRH_USERNAME=youruser
export OSSRH_PASSWORD=yourpass
export SIGNING_KEY="$(cat private.asc)"
export SIGNING_PASSPHRASE=yourphrase
export RELEASE_VERSION=0.1.1-SNAPSHOT
./gradlew clean publish
```
Snapshot artifacts go to: https://s01.oss.sonatype.org/content/repositories/snapshots/

### Local Release Publish
```bash
export RELEASE_VERSION=0.1.1
./gradlew clean publish
```
Then log into Sonatype UI:
1. Close the staging repository (validates & signs)  
2. Release it (sync to Maven Central starts; may take 10–20 minutes)

### Automating Release (CI Idea)
Tag-driven pipeline sets `RELEASE_VERSION` from tag `vX.Y.Z`:
```yaml
# .github/workflows/release.yml
name: Release
on:
  push:
    tags: [ 'v*.*.*' ]
jobs:
  publish:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17
          cache: gradle
          server-id: OSSRH
          server-username: OSSRH_USERNAME
          server-password: OSSRH_PASSWORD
          gpg-private-key: ${{ secrets.SIGNING_KEY }}
          gpg-passphrase: ${{ secrets.SIGNING_PASSPHRASE }}
      - name: Derive version
        run: echo "RELEASE_VERSION=${GITHUB_REF_NAME#v}" >> $GITHUB_ENV
      - name: Publish
        env:
          OSSRH_USERNAME: ${{ secrets.OSSRH_USERNAME }}
          OSSRH_PASSWORD: ${{ secrets.OSSRH_PASSWORD }}
          SIGNING_KEY: ${{ secrets.SIGNING_KEY }}
          SIGNING_PASSPHRASE: ${{ secrets.SIGNING_PASSPHRASE }}
        run: ./gradlew publish
```

### Verifying Published Artifact
After sync completes:
```bash
# Check Maven Central (might need a few minutes)
open "https://central.sonatype.com/search?name=radio-metadata"
# Pull with Gradle dependency insight (example)
./gradlew dependencyInsight --configuration runtimeClasspath --dependency radio-metadata
```

## GitHub Packages (Optional Secondary Registry)
Uncomment the GitHub repo block in `build.gradle.kts` and replace `OWNER` with your org/user.
Environment variables:
- `GITHUB_ACTOR`
- `GITHUB_TOKEN` (a PAT with `read:packages write:packages`)  
Publish:
```bash
export RELEASE_VERSION=0.1.1-SNAPSHOT
export GITHUB_ACTOR=yourhandle
export GITHUB_TOKEN=ghp_xxx
./gradlew publishAllPublicationsToGitHubPackagesRepository
```
Consumers must add the GitHub Packages repository to their settings to resolve.

## Updating POM Metadata
Edit in `build.gradle.kts` inside `pom {}`:
- Replace placeholder SCM URLs (set canonical repo URL)
- Update `group` (now `nl.mdworld` – change only if needed)
- Set real developer id/name/email
- Change license if not Apache 2.0

## Troubleshooting
- Unsigned artifacts error: ensure `SIGNING_KEY` and `SIGNING_PASSPHRASE` are set and wrapper picks them up (no stray quotes).  
- 401 from OSSRH: verify credentials & that user has correct staging permissions.  
- Module not visible on Maven Central yet: confirm staging repository released; allow propagation time.
- GPG key issues: run `gpg --list-keys` to ensure key present; if using CI import via `actions/setup-java` parameters instead of manual env injection.

## Customize Coordinates
Artifact coordinates: `nl.mdworld:radio-metadata:<version>`.

## License
Apache 2.0 (default in POM). Replace if needed.

## NOTES

<!-- export JAVA_HOME=$(/usr/libexec/java_home -v 17)
echo $JAVA_HOME
java -version -->
using sdkman:
sdk use java 21.0.8-tem
gradle wrapper --gradle-version 8.9 --distribution-type all

## TODO

- choose license
- use KTOR
- specify usage