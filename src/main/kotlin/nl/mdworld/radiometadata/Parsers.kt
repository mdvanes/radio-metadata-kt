package nl.mdworld.radiometadata

/** Utility parsing helpers for raw stream metadata strings. */
object MetadataParsers {
    private val dashRegex = Regex("\\s+-\\s+")
    private val separators = listOf(dashRegex, Regex(" :: "), Regex(" \u2013 "))

    /** Attempt to parse an ARTIST - TITLE string. Returns Pair(artist, title). */
    fun parseArtistTitle(raw: String): Pair<String?, String?> {
        val trimmed = raw.trim().removePrefix("Now Playing:").trim()
        for (sep in separators) {
            val parts = trimmed.split(sep)
            if (parts.size == 2) {
                val artist = parts[0].takeIf { it.isNotBlank() }
                val title = parts[1].takeIf { it.isNotBlank() }
                return artist to title
            }
        }
        return null to trimmed.takeIf { it.isNotBlank() }
    }
}
