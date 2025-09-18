package nl.mdworld.radiometadata.examples

import kotlinx.coroutines.runBlocking
import nl.mdworld.radiometadata.strategies.ApiMetadataUtil

/**
 * Simple example that fetches current track metadata for a given station preset.
 * Preferred station order:
 *   1. First CLI argument
 *   2. STATION environment variable
 *   3. Default "npo2"
 */
fun main(args: Array<String>) = runBlocking {
    val station = args.firstOrNull() ?: System.getenv("STATION") ?: "npo2"
    val tracks = ApiMetadataUtil.getRadioMetaData(station)
    val current = tracks.firstOrNull()
    val artist = current?.song?.artist ?: "?"
    val title = current?.song?.title ?: "?"
    println("Current: $artist - $title")
}
