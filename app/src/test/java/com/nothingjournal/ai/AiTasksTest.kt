package com.nothingjournal.ai

import com.nothingjournal.data.model.Mood
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** LocalAiClient stub — no engine, deterministic replies when asked. */
private class StubClient(private val reply: String?) : LocalAiClient {
    override suspend fun isAvailable(): Boolean = reply != null
    override suspend fun listModels(): List<String> = emptyList()
    override suspend fun generate(prompt: String, system: String?): String? = reply
}

class AiTasksTest {

    @Test
    fun `parses a bare JSON array of tags`() = runTest {
        val tasks = AiTasks(StubClient("""["work", "ideas"]"""))
        assertEquals(listOf("work", "ideas"), tasks.parseTagArray("""["work", "ideas"]"""))
    }

    @Test
    fun `parses tags wrapped in prose or code fences`() = runTest {
        val raw = "Here you go:\n```json\n[\"a\", \"b\"]\n```"
        assertEquals(listOf("a", "b"), AiTasks(StubClient(raw)).parseTagArray(raw))
    }

    @Test
    fun `unparseable replies yield no tags instead of crashing`() = runTest {
        assertTrue(AiTasks(StubClient("no array here")).parseTagArray("no array here").isEmpty())
        assertTrue(AiTasks(StubClient("[broken")).parseTagArray("[broken").isEmpty())
    }

    @Test
    fun `mood inference maps model words onto the scale`() = runTest {
        val tasks = AiTasks(StubClient("GOOD."))
        val result = tasks.inferMood("Had a steady, productive day.")
        assertTrue(result is AiResult.Success)
        assertEquals(Mood.GOOD, (result as AiResult.Success).value)
    }

    @Test
    fun `mood inference falls back to null on garbage replies`() = runTest {
        val result = AiTasks(StubClient("bananas")).inferMood("text")
        assertTrue(result is AiResult.Unavailable)
        assertNull((result as AiResult.Unavailable).fallback)
    }

    @Test
    fun `summarize returns engine output when available`() = runTest {
        val result = AiTasks(StubClient("A calm day.")).summarize("long text")
        assertEquals("A calm day.", (result as AiResult.Success).value)
    }

    @Test
    fun `summarize degrades to a text cut when the engine is down`() = runTest {
        val text = "word ".repeat(100)
        val result = AiTasks(StubClient(null)).summarize(text, fallback = "kept")
        assertEquals("kept", (result as AiResult.Unavailable).fallback)
    }
}
