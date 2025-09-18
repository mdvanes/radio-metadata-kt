package nl.mdworld.radiometadata.presets

import nl.mdworld.radiometadata.RadioSchema
import nl.mdworld.radiometadata.SchemaPaths
import nl.mdworld.radiometadata.UrlConfig

val SKY_PRESET = RadioSchema(
    name = "sky",
    urls = listOf(
        UrlConfig(
            "tracks_",
            "https://graph.talparad.io/?query=%7B%0A%20%20station(slug%3A%20%22sky-radio%22)%20%7B%0A%20%20%20%20title%0A%20%20%20%20playouts(profile%3A%20%22%22%2C%20limit%3A%2010)%20%7B%0A%20%20%20%20%20%20broadcastDate%0A%20%20%20%20%20%20track%20%7B%0A%20%20%20%20%20%20%20%20id%0A%20%20%20%20%20%20%20%20title%0A%20%20%20%20%20%20%20%20artistName%0A%20%20%20%20%20%20%20%20isrc%0A%20%20%20%20%20%20%20%20images%20%7B%0A%20%20%20%20%20%20%20%20%20%20type%0A%20%20%20%20%20%20%20%20%20%20uri%0A%20%20%20%20%20%20%20%20%20%20__typename%0A%20%20%20%20%20%20%20%20%7D%0A%20%20%20%20%20%20%20%20__typename%0A%20%20%20%20%20%20%7D%0A%20%20%20%20%20%20__typename%0A%20%20%20%20%7D%0A%20%20%20%20__typename%0A%20%20%7D%0A%7D&variables=%7B%7D",
            headers = mapOf("x-api-key" to "")
        )
    ),
    paths = SchemaPaths(
        tracks = listOf("tracks_", "data", "station", "playouts"),
        time = SchemaPaths.TimeInfo(
            start = listOf("broadcastDate"),
            end = listOf("broadcastDate")
        ),
        song = SchemaPaths.SongInfo(
            artist = listOf("track", "artistName"),
            title = listOf("track", "title"),
            imageUrl = listOf("track", "images", 0, "uri")
        )
    )
)
