package com.nothingjournal.data

import com.nothingjournal.data.model.Mood
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MoodTest {

    @Test
    fun `round-trips through the numeric value`() {
        Mood.entries.forEach { mood ->
            assertEquals(mood, Mood.fromValue(mood.value))
        }
    }

    @Test
    fun `unknown values resolve to null`() {
        assertNull(Mood.fromValue(0))
        assertNull(Mood.fromValue(6))
        assertNull(Mood.fromValue(null))
    }

    @Test
    fun `keyword map covers common words`() {
        assertEquals(Mood.GREAT, Mood.fromKeyword("happy"))
        assertEquals(Mood.LOW, Mood.fromKeyword("stressed"))
        assertEquals(Mood.AWFUL, Mood.fromKeyword("exhausted"))
        assertEquals(Mood.OKAY, Mood.fromKeyword("fine"))
        assertEquals(Mood.GOOD, Mood.fromKeyword("grateful"))
    }

    @Test
    fun `keywords are case-insensitive`() {
        assertEquals(Mood.GREAT, Mood.fromKeyword("  AMAZING "))
    }

    @Test
    fun `unknown words yield null`() {
        assertNull(Mood.fromKeyword("xylophone"))
    }

    @Test
    fun `mood colors are distinct`() {
        val colors = Mood.entries.map { it.color }
        assertEquals(colors.size, colors.distinct().size)
    }
}

class TagsTest {

    @Test
    fun `encodes and decodes a list`() {
        val tags = listOf("work", "deep-thoughts", "gym")
        assertEquals(tags, Tags.decode(Tags.encode(tags)))
    }

    @Test
    fun `decodes an empty tag store`() {
        assertTrue(Tags.decode("[]").isEmpty())
    }

    @Test
    fun `corrupt JSON decodes to empty instead of crashing`() {
        assertTrue(Tags.decode("not json").isEmpty())
        assertTrue(Tags.decode("{\"oops\":1}").isEmpty())
    }
}
