package nl.mdworld.radiometadata.strategies

import nl.mdworld.radiometadata.RadioMetadata

interface MetadataStrategy {
    suspend fun fetchMetadata(streamUrl: String): List<RadioMetadata>
}
