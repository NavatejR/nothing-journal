package com.nothingjournal.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteTemplatesTest {

    @Test
    fun `all templates ship with stable ids and readable titles`() {
        val ids = NoteTemplates.All.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
        NoteTemplates.All.forEach {
            if (it !== NoteTemplates.Blank) {
                assertTrue("template ${it.id} needs a title", it.title.isNotBlank())
            }
        }
    }

    @Test
    fun `checklist templates use markdown checkbox syntax`() {
        assertTrue(NoteTemplates.ShoppingList.body.contains("- [ ] "))
        assertTrue(NoteTemplates.TodoList.body.contains("- [ ] "))
        assertTrue(NoteTemplates.ShoppingList.isChecklist)
    }

    @Test
    fun `byId falls back to blank for unknown or null ids`() {
        assertEquals(NoteTemplates.Blank, NoteTemplates.byId("nope"))
        assertEquals(NoteTemplates.Blank, NoteTemplates.byId(null))
        assertEquals(NoteTemplates.ShoppingList, NoteTemplates.byId("shopping"))
    }
}
