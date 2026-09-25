package com.nothingjournal.ui.screen.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeViewModelTest {

    private val today = 20_000L

    @Test
    fun `streak counts consecutive days ending today`() {
        val days = setOf(today, today - 1, today - 2)
        assertEquals(3, HomeViewModel.computeStreak(days, today))
    }

    @Test
    fun `streak survives a not-yet-written today`() {
        val days = setOf(today - 1, today - 2, today - 3)
        assertEquals(3, HomeViewModel.computeStreak(days, today))
    }

    @Test
    fun `streak breaks at the first missing day`() {
        val days = setOf(today, today - 1, today - 3, today - 4)
        assertEquals(2, HomeViewModel.computeStreak(days, today))
    }

    @Test
    fun `streak is zero with no entries`() {
        assertEquals(0, HomeViewModel.computeStreak(emptySet(), today))
    }
}
