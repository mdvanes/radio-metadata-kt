package nl.mdworld.radiometadata

/** Simple aggregator that normalizes raw metadata lines into a list of tracks. */
class RadioMetadataAggregator(private val station: StationInfo) {

    @Volatile
    private var last: RadioMetadata = RadioMetadata(
        song = SongInfo(artist = null, title = null)
    )

    fun ingestRaw(rawLine: String): RadioMetadata {
        val (artist, title) = MetadataParsers.parseArtistTitle(rawLine)
        val track = TrackInfo(
            song = SongInfo(artist = artist, title = title)
        )
        // Represent latest track as top-level song plus tracks history (prepend)
        last = RadioMetadata(
            time = last.time,
            broadcast = last.broadcast,
            song = track.song,
            tracks = listOf(track) + last.tracks
        )
        return last
    }

    fun current(): RadioMetadata = last
}
