package nl.mdworld.radiometadata

/** Represents a radio station's core identifying info. */
data class StationInfo(
    val id: String,
    val name: String,
    val streamUrl: String? = null,
    val homepage: String? = null,
    val genre: String? = null,
    val country: String? = null
)

