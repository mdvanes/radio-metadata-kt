package nl.mdworld.radiometadata

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class IcyMetadataFetcher {
    suspend fun fetchICYMetadata(streamUrl: String): Map<String, String> = withContext(Dispatchers.IO) {
        val metadata = mutableMapOf<String, String>()
        try {
            val url = URL(streamUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("Icy-MetaData", "1")
            connection.setRequestProperty("User-Agent", "RadioMetadata Library")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.connect()
            connection.headerFields.forEach { (key, values) ->
                if (key?.startsWith("icy-", ignoreCase = true) == true && values.isNotEmpty()) {
                    val cleanKey = key.removePrefix("icy-").lowercase()
                    metadata[cleanKey] = values.first()
                }
            }
            connection.disconnect()
        } catch (e: Exception) {
            println("IcyMetadataFetcher: Error fetching ICY metadata: ${e.message}")
        }
        metadata
    }
}
