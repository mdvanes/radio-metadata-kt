package nl.mdworld.radiometadata

import java.time.Instant

/** Simple aggregator that normalizes raw metadata lines into structured objects. */
class RadioMetadataAggregator(private val station: StationInfo) {

    @Volatile
    private var last: RadioMetadata = RadioMetadata(station, null)

    fun ingestRaw(rawLine: String): RadioMetadata {
        val (artist, title) = MetadataParsers.parseArtistTitle(rawLine)
        val track = TrackInfo(artist = artist, title = title, raw = rawLine, startedAt = Instant.now())
        val snapshot = RadioMetadata(station = station, currentTrack = track, updatedAt = Instant.now())
        last = snapshot
        return snapshot
    }

    fun current(): RadioMetadata = last
}
