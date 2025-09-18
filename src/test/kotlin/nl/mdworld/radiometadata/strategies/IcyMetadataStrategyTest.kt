package nl.mdworld.radiometadata.strategies

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class IcyMetadataStrategyTest {
    private val strategy = IcyMetadataStrategy()

    @Test
    fun `parse artist and title`() {
        val meta = strategy.parseIcyMetadata("StreamTitle='Daft Punk - One More Time';")
        val first = meta.first()
        assertEquals("Daft Punk", first.song.artist)
        assertEquals("One More Time", first.song.title)
    }

    @Test
    fun `parse title only when no dash`() {
        val meta = strategy.parseIcyMetadata("StreamTitle='Instrumental Mix';")
        val first = meta.first()
        assertNull(first.song.artist)
        assertEquals("Instrumental Mix", first.song.title)
    }
}
