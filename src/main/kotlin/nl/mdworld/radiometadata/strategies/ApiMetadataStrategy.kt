package nl.mdworld.radiometadata.strategies

import nl.mdworld.radiometadata.RadioMetadata

class ApiMetadataStrategy(private val presetName: String) : MetadataStrategy {
    override suspend fun fetchMetadata(streamUrl: String): List<RadioMetadata> = try {
        ApiMetadataUtil.getRadioMetaData(presetName)
    } catch (e: Exception) {
        println("ApiMetadataStrategy: Error fetching metadata for $presetName: ${e.message}")
        emptyList()
    }
}
