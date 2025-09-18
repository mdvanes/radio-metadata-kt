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
    val argList = args.toList()
    val formatFlag = argList.firstOrNull { it == "--table" || it.startsWith("--format=") }
    val format = when {
        formatFlag == "--table" -> "table"
        formatFlag?.substringAfter("--format=") == "table" -> "table"
        else -> "verbose"
    }
    val station = argList.firstOrNull { !it.startsWith("--") } ?: System.getenv("STATION") ?: "npo2"

    val tracks = ApiMetadataUtil.getRadioMetaData(station)
    if (tracks.isEmpty()) {
        println("No metadata returned for station '$station'")
        return@runBlocking
    }

    if (format == "table") {
        renderTable(station, tracks)
    } else {
        renderVerbose(station, tracks)
    }
}

private fun renderVerbose(station: String, tracks: List<nl.mdworld.radiometadata.RadioMetadata>) {
    println("Fetched ${tracks.size} track(s) for station '$station':")
    tracks.forEachIndexed { index, meta ->
        val t = meta.song
        val time = meta.time
        val broadcast = meta.broadcast
        println("\n#${index + 1}")
        println("  Artist     : ${t.artist ?: "?"}")
        println("  Title      : ${t.title ?: "?"}")
        if (!t.imageUrl.isNullOrBlank()) println("  Song Image : ${t.imageUrl}")
        if (!t.listenUrl.isNullOrBlank()) println("  Listen URL : ${t.listenUrl}")
        if (time != null && (time.start != null || time.end != null)) {
            println("  Time       : start=${time.start ?: "?"} end=${time.end ?: "?"}")
        }
        if (broadcast != null && (broadcast.title != null || broadcast.presenters != null || broadcast.imageUrl != null)) {
            println("  Broadcast  : title=${broadcast.title ?: "?"} presenters=${broadcast.presenters ?: "?"}")
            if (!broadcast.imageUrl.isNullOrBlank()) println("  Broadcast Image: ${broadcast.imageUrl}")
        }
        if (meta.tracks.isNotEmpty()) {
            println("  Nested Tracks: ${meta.tracks.size}")
        }
    }
}

private fun renderTable(station: String, tracks: List<nl.mdworld.radiometadata.RadioMetadata>) {
    // Determine column widths
    val headers = listOf("#", "Artist", "Title", "Start", "End", "Broadcast", "Presenters")
    val rows = tracks.mapIndexed { idx, meta ->
        val t = meta.song
        val time = meta.time
        val b = meta.broadcast
        listOf(
            (idx + 1).toString(),
            (t.artist ?: "?").truncate(30),
            (t.title ?: "?").truncate(40),
            time?.start ?: "",
            time?.end ?: "",
            (b?.title ?: "").truncate(30),
            (b?.presenters ?: "").truncate(25)
        )
    }
    val widths = headers.indices.map { col ->
        (listOf(headers[col]) + rows.map { it[col] }).maxOf { it.length }
    }
    println("Tracks for '$station' (${tracks.size}):")
    printRow(headers, widths)
    printSeparator(widths)
    rows.forEach { printRow(it, widths) }
}

private fun String.truncate(max: Int): String = if (length <= max) this else take(max - 1) + "…"

private fun printRow(cols: List<String>, widths: List<Int>) {
    val line = cols.mapIndexed { i, v -> v.padEnd(widths[i]) }.joinToString(" | ")
    println(line)
}

private fun printSeparator(widths: List<Int>) {
    println(widths.joinToString("-+-") { "".padEnd(it, '-') })
}
