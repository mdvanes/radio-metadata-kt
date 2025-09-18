package nl.mdworld.radiometadata

data class RadioMetadata(
    val time: TimeInfo? = null,
    val broadcast: BroadcastInfo? = null,
    val song: SongInfo,
    val tracks: List<TrackInfo> = listOf()
)

data class TrackInfo(
    val time: TimeInfo? = null,
    val broadcast: BroadcastInfo? = null,
    val song: SongInfo
)

data class TimeInfo(
    val start: String? = null,
    val end: String? = null
)

data class BroadcastInfo(
    val title: String? = null,
    val presenters: String? = null,
    val imageUrl: String? = null
)

data class SongInfo(
    val artist: String? = null,
    val title: String? = null,
    val imageUrl: String? = null,
    val listenUrl: String? = null
)

data class UrlConfig(
    val name: String,
    val url: String,
    val headers: Map<String, String>? = null
)

typealias PickPath = List<Any>

data class SchemaPaths(
    val tracks: PickPath,
    val time: TimeInfo? = null,
    val broadcast: BroadcastInfo? = null,
    val song: SongInfo
) {
    data class TimeInfo(
        val start: PickPath? = null,
        val end: PickPath? = null
    )

    data class BroadcastInfo(
        val title: PickPath? = null,
        val presenters: PickPath? = null,
        val imageUrl: PickPath? = null
    )

    data class SongInfo(
        val artist: PickPath? = null,
        val title: PickPath,
        val imageUrl: PickPath? = null,
        val listenUrl: PickPath? = null
    )
}

data class RadioSchema(
    val name: String,
    val urls: List<UrlConfig>,
    val paths: SchemaPaths
)
