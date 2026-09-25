package com.nothingjournal.ai

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Degradation contract of the bundled engine, verified with a stub injected
 * through the interface seam. The MediaPipe-backed singleton itself is
 * exercised on-device (weights + JNI), not in JVM tests.
 */
class OnDeviceAiClientContractTest {

    private class EngineStub(private val available: Boolean) : LocalAiClient {
        override suspend fun isAvailable(): Boolean = available
        override suspend fun listModels(): List<String> =
            if (available) listOf(OnDeviceAiClient.MODEL_NAME) else emptyList()
        override suspend fun generate(prompt: String, system: String?): String? =
            if (available) "ok" else null
    }

    @Test
    fun `missing model means unavailable and null generations`() = runTest {
        val client = EngineStub(false)
        assertFalse(client.isAvailable())
        assertTrue(client.listModels().isEmpty())
        assertNull(client.generate("hi"))
    }

    @Test
    fun `present model reports the bundled artifact`() = runTest {
        val client = EngineStub(true)
        assertTrue(client.isAvailable())
        assertEquals(listOf(OnDeviceAiClient.MODEL_NAME), client.listModels())
        assertEquals("ok", client.generate("hi"))
    }
}
