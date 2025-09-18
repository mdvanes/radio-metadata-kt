package nl.mdworld.radiometadata

import java.time.Instant

/** Represents a radio station's core identifying info. */
data class StationInfo(
    val id: String,
    val name: String,
    val streamUrl: String? = null,
    val homepage: String? = null,
    val genre: String? = null,
    val country: String? = null
)

/** Represents a single track playing on a station. */
data class TrackInfo(
    val artist: String?,
    val title: String?,
    val album: String? = null,
    val startedAt: Instant? = null,
    val durationSeconds: Int? = null,
    val raw: String? = null
) {
    val display: String = sequenceOf(artist, title)
        .filterNot { it.isNullOrBlank() }
        .joinToString(" - ")
}

/** Container for aggregated metadata snapshot. */
data class RadioMetadata(
    val station: StationInfo,
    val currentTrack: TrackInfo?,
    val updatedAt: Instant = Instant.now()
)
