package nl.mdworld.radiometadata.strategies

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ApiMetadataUtilTest {
    @Test
    fun `unknown preset throws`() = runBlocking {
        var threw = false
        try {
            ApiMetadataUtil.getRadioMetaData("__missing__")
        } catch (e: IllegalArgumentException) {
            threw = true
            assertTrue(e.message!!.contains("No schema"))
        }
        assertTrue(threw, "Expected IllegalArgumentException for unknown preset")
    }
}
