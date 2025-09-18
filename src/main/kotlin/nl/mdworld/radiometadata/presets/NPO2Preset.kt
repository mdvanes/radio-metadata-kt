package nl.mdworld.radiometadata.presets

import nl.mdworld.radiometadata.RadioSchema
import nl.mdworld.radiometadata.SchemaPaths
import nl.mdworld.radiometadata.UrlConfig

val NPO2_PRESET = RadioSchema(
    name = "npo2",
    urls = listOf(
        UrlConfig(
            "tracks_",
            "https://www.nporadio2.nl/api/tracks",
        ),
        UrlConfig("broadcasts_", "https://www.nporadio2.nl/api/broadcasts")
    ),
    paths = SchemaPaths(
        tracks = listOf("tracks_", "data"),
        broadcast = SchemaPaths.BroadcastInfo(
            title = listOf("broadcasts_", "data", 0, "title"),
            presenters = listOf("broadcasts_", "data", 0, "presenters"),
            imageUrl = listOf("broadcasts_", "data", 0, "image_url_400x400")
        ),
        time = SchemaPaths.TimeInfo(
            start = listOf("startdatetime"),
            end = listOf("enddatetime")
        ),
        song = SchemaPaths.SongInfo(
            artist = listOf("artist"),
            title = listOf("title"),
            imageUrl = listOf("image_url_400x400"),
            listenUrl = listOf("spotify_url")
        )
    )
)
