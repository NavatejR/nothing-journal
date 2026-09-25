package com.nothingjournal.data

import com.nothingjournal.data.local.EntryEntity
import com.nothingjournal.data.local.EntryDao
import com.nothingjournal.data.local.MoodCountRow
import com.nothingjournal.data.local.MoodPointRow
import com.nothingjournal.data.model.EntryKind
import com.nothingjournal.data.model.EntrySource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

/** In-memory DAO double — the repository logic is what's under test. */
private class FakeEntryDao : EntryDao {
    val rows = LinkedHashMap<Long, EntryEntity>()
    private var nextId = 1L

    private fun put(e: EntryEntity): EntryEntity {
        val id = if (e.id == 0L) nextId++ else e.id
        val stored = e.copy(id = id)
        rows[id] = stored
        return stored
    }

    override fun observeNotes(): Flow<List<EntryEntity>> =
        kotlinx.coroutines.flow.flowOf(rows.values.filter { it.kind == EntryKind.NOTE && !it.archived })

    override fun searchNotes(q: String): Flow<List<EntryEntity>> = observeNotes()

    override fun observeArchivedNotes(): Flow<List<EntryEntity>> =
        kotlinx.coroutines.flow.flowOf(emptyList<EntryEntity>())

    override suspend fun byId(id: Long): EntryEntity? = rows[id]

    override fun observeById(id: Long): Flow<EntryEntity?> = kotlinx.coroutines.flow.flowOf(rows[id])

    override suspend fun insert(entry: EntryEntity): Long = put(entry).id

    override suspend fun update(entry: EntryEntity) {
        rows[entry.id] = entry
    }

    override suspend fun setArchived(id: Long, archived: Boolean, updatedAt: Long) {
        rows[id] = rows[id]?.copy(archived = archived, updatedAt = updatedAt) ?: return
    }

    override suspend fun setPinned(id: Long, pinned: Boolean, updatedAt: Long) {
        rows[id] = rows[id]?.copy(pinned = pinned, updatedAt = updatedAt) ?: return
    }

    override suspend fun deleteById(id: Long) {
        rows.remove(id)
    }

    override suspend fun journalByDay(dayIndex: Long): EntryEntity? =
        rows.values.firstOrNull { it.kind == EntryKind.JOURNAL_ENTRY && it.dayIndex == dayIndex }

    override fun observeJournalByDay(dayIndex: Long): Flow<EntryEntity?> =
        MutableStateFlow(rows.values.firstOrNull { it.kind == EntryKind.JOURNAL_ENTRY && it.dayIndex == dayIndex })

    override fun observeRecentJournal(limit: Int): Flow<List<EntryEntity>> =
        kotlinx.coroutines.flow.flowOf(rows.values.filter { it.kind == EntryKind.JOURNAL_ENTRY })

    override fun observeMoodCounts(): Flow<List<MoodCountRow>> =
        kotlinx.coroutines.flow.flowOf(emptyList<MoodCountRow>())

    override fun observeMoodSeries(): Flow<List<MoodPointRow>> =
        kotlinx.coroutines.flow.flowOf(emptyList<MoodPointRow>())

    override fun observeJournalDays(): Flow<List<Long>> =
        kotlinx.coroutines.flow.flowOf(
            rows.values.filter { it.kind == EntryKind.JOURNAL_ENTRY && it.dayIndex != null }
                .map { it.dayIndex!! }
                .distinct()
                .sorted()
        )

    override fun observeNoteCount(): Flow<Long> = kotlinx.coroutines.flow.flowOf(0L)

    override fun observeAllNoteTags(): Flow<List<String>> =
        kotlinx.coroutines.flow.flowOf(emptyList<String>())
}

class EntryRepositoryTest {

    private fun repo(): Pair<EntryRepository, FakeEntryDao> {
        val dao = FakeEntryDao()
        return EntryRepository(dao) to dao
    }

    @Test
    fun `todayEntry creates exactly one row per day`() = runTest {
        val (repository, dao) = repo()
        val e1 = repository.todayEntry(ZoneId.of("UTC"))
        val e2 = repository.todayEntry(ZoneId.of("UTC"))
        assertEquals(e1.id, e2.id)
        assertEquals(1, dao.rows.size)
        assertEquals(EntryKind.JOURNAL_ENTRY, e1.kind)
        assertEquals(LocalDate.now(ZoneId.of("UTC")).toEpochDay(), e1.dayIndex)
    }

    @Test
    fun `appendToToday stacks a timestamped paragraph`() = runTest {
        val (repository, _) = repo()
        val zone = ZoneId.of("UTC")
        repository.appendToToday("First thought.", EntrySource.DICTATED, zone)
        val entry = repository.todayEntry(zone)
        repository.appendToToday("Second thought.", EntrySource.ASSISTANT, zone)
        val updated = repository.todayEntry(zone)
        assertTrue(updated.body.contains("First thought."))
        assertTrue(updated.body.contains("Second thought."))
    }

    @Test
    fun `upsertNote creates then updates without duplicating`() = runTest {
        val (repository, dao) = repo()
        val id = repository.upsertNote(null, "Shopping", "oat milk", listOf("errand"))
        assertEquals(1, dao.rows.size)
        repository.upsertNote(id, "Shopping", "oat milk, rye bread", listOf("errand", "food"))
        assertEquals(1, dao.rows.size)
        val note = dao.rows.getValue(id)
        assertEquals("oat milk, rye bread", note.body)
        assertEquals(listOf("errand", "food"), Tags.decode(note.tagsJson))
    }

    @Test
    fun `pin and archive mutate flags only`() = runTest {
        val (repository, dao) = repo()
        val id = repository.upsertNote(null, "T", "B", emptyList())
        repository.setPinned(id, true)
        repository.setArchived(id, true)
        val note = dao.rows.getValue(id)
        assertTrue(note.pinned)
        assertTrue(note.archived)
        repository.delete(id)
        assertNull(dao.byId(id))
    }

    @Test
    fun `journal summary is stored on the day row`() = runTest {
        val (repository, _) = repo()
        val e = repository.todayEntry(ZoneId.of("UTC"))
        repository.setJournalSummary(e.id, "A slow day, mostly rain.")
        assertEquals("A slow day, mostly rain.", repository.todayEntry(ZoneId.of("UTC")).summary)
    }
}
