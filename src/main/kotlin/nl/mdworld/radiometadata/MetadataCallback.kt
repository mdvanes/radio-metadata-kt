package nl.mdworld.radiometadata

interface MetadataCallback {
    fun onMetadataUpdate(metadata: RadioMetadata)
    fun onMetadataError(error: String)
}
