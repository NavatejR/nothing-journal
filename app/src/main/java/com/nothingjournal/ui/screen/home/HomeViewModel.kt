package com.nothingjournal.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nothingjournal.data.EntryRepository
import com.nothingjournal.data.SettingsRepository
import com.nothingjournal.data.local.EntryEntity
import com.nothingjournal.data.model.Mood
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    entryRepository: EntryRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    /** All notes, pinned first (the DAO orders them). */
    val notes: StateFlow<List<EntryEntity>> = entryRepository.observeNotes("")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Days (epochDay) with a journal entry — powers the streak stat. */
    val journalDays: StateFlow<List<Long>> = entryRepository.observeJournalDays()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Today's journal mood, if set. */
    val todayMood: StateFlow<Mood?> = entryRepository.observeJournalByDay(EntryRepository.todayIndex())
        .map { it?.mood?.let(Mood::fromValue) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val noteCount: StateFlow<Int> = entryRepository.observeNoteCount()
        .map { it.toInt() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val reducedMotion: StateFlow<Boolean> = settingsRepository.settings
        .map { it.reducedMotion }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /** Consecutive days (ending today, tolerating a blank today) with entries. */
    fun streak(days: List<Long>): Int =
        computeStreak(days.toSet(), EntryRepository.todayIndex())

    companion object {
        /**
         * Consecutive days ending at [today] with a journal entry. Today
         * itself may still be blank — the streak survives until yesterday
         * goes dark too.
         */
        fun computeStreak(days: Set<Long>, today: Long): Int {
            if (days.isEmpty()) return 0
            var cursor = today
            if (cursor !in days) cursor -= 1 // today not written yet; anchor on yesterday
            var count = 0
            while (cursor in days) {
                count++
                cursor -= 1
            }
            return count
        }
    }
}
