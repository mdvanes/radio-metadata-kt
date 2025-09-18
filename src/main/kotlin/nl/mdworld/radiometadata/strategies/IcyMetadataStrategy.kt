package nl.mdworld.radiometadata.strategies

import nl.mdworld.radiometadata.IcyMetadataFetcher
import nl.mdworld.radiometadata.RadioMetadata
import nl.mdworld.radiometadata.SongInfo

class IcyMetadataStrategy : MetadataStrategy {
    private val icyFetcher = IcyMetadataFetcher()

    fun parseIcyMetadata(icyString: String): List<RadioMetadata> {
        val regex = Regex("StreamTitle='(.*?)';")
        val match = regex.find(icyString)
        val streamTitle = match?.groups?.get(1)?.value ?: icyString
        val split = streamTitle.split(" - ", limit = 2)
        val artist = if (split.size == 2) split[0].trim().ifEmpty { null } else null
        val title = if (split.size == 2) split[1].trim().ifEmpty { null } else streamTitle.trim().ifEmpty { null }
        return listOf(RadioMetadata(song = SongInfo(artist = artist, title = title)))
    }

    override suspend fun fetchMetadata(streamUrl: String): List<RadioMetadata> = try {
        val icyData = icyFetcher.fetchICYMetadata(streamUrl)
        if (icyData.isNotEmpty()) {
            val streamTitle = icyData["streamtitle"]
            if (!streamTitle.isNullOrEmpty()) return parseIcyMetadata("StreamTitle='$streamTitle';")
            val title = icyData["title"] ?: icyData["name"] ?: "Unknown"
            val artist = icyData["artist"] ?: icyData["name"]
            listOf(RadioMetadata(song = SongInfo(artist = artist, title = title)))
        } else emptyList()
    } catch (e: Exception) {
        println("IcyMetadataStrategy: Error fetching ICY metadata: ${e.message}")
        emptyList()
    }
}
