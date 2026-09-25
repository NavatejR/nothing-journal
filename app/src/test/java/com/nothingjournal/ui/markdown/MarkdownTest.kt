package com.nothingjournal.ui.markdown

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownTest {

    // ---- Parsing -------------------------------------------------------

    @Test
    fun `parses headings bullets quotes and checkboxes`() {
        assertEquals(Markdown.Block.Heading(1, "Title"), Markdown.parseLine("# Title"))
        assertEquals(Markdown.Block.Heading(3, "Deep"), Markdown.parseLine("### Deep"))
        assertEquals(Markdown.Block.ListItem("milk"), Markdown.parseLine("- milk"))
        assertEquals(Markdown.Block.Quote("wisdom"), Markdown.parseLine("> wisdom"))
        assertEquals(Markdown.Block.CheckItem(false, "eggs"), Markdown.parseLine("- [ ] eggs"))
        assertEquals(Markdown.Block.CheckItem(true, "jam"), Markdown.parseLine("- [x] jam"))
        assertEquals(Markdown.Block.Paragraph("plain"), Markdown.parseLine("plain"))
    }

    @Test
    fun `inline spans detect bold italic and code`() {
        val spans = Markdown.spans("a **bold** and *it* plus `code` end")
        assertTrue(spans.contains(Markdown.Span.Bold("bold")))
        assertTrue(spans.contains(Markdown.Span.Italic("it")))
        assertTrue(spans.contains(Markdown.Span.Code("code")))
        assertEquals(7, spans.size)
    }

    // ---- Wrapping ------------------------------------------------------

    @Test
    fun `bold wraps the word at the cursor`() {
        val edit = Markdown.toggleWrap("say hello world", 8, "**") // cursor inside "hello"
        assertEquals("say **hello** world", edit.text)
        assertEquals(13, edit.cursor) // after closing marker
    }

    @Test
    fun `bold unwraps when the selection is already wrapped`() {
        val edit = Markdown.toggleWrap("say **hello** world", 8, "**")
        assertEquals("say hello world", edit.text)
    }

    @Test
    fun `wrap in a word gap inserts an empty pair`() {
        val edit = Markdown.toggleWrap("a b", 1, "*") // cursor between words
        assertEquals("a** b", edit.text)
        assertEquals(2, edit.cursor) // between the two markers
    }

    @Test
    fun `wrap with the cursor inside a word wraps that word`() {
        val edit = Markdown.toggleWrap("abc", 1, "*")
        assertEquals("*abc*", edit.text)
        assertEquals(5, edit.cursor)
    }

    // ---- Line prefixes ---------------------------------------------------

    @Test
    fun `heading cycles levels`() {
        val add = Markdown.toggleLinePrefix("line", 2, Markdown.PrefixKind.HEADING)
        assertEquals("# line", add.text)
        val bump = Markdown.toggleLinePrefix("# line", 4, Markdown.PrefixKind.HEADING)
        assertEquals("## line", bump.text)
        val drop = Markdown.toggleLinePrefix("### line", 6, Markdown.PrefixKind.HEADING)
        assertEquals("line", drop.text)
    }

    @Test
    fun `bullet adds and removes itself`() {
        val add = Markdown.toggleLinePrefix("item", 0, Markdown.PrefixKind.BULLET)
        assertEquals("- item", add.text)
        val remove = Markdown.toggleLinePrefix("- item", 4, Markdown.PrefixKind.BULLET)
        assertEquals("item", remove.text)
    }

    @Test
    fun `checkbox prefix is toggleable`() {
        val add = Markdown.toggleLinePrefix("task", 1, Markdown.PrefixKind.CHECK)
        assertEquals("- [ ] task", add.text)
        val remove = Markdown.toggleLinePrefix("- [ ] task", 8, Markdown.PrefixKind.CHECK)
        assertEquals("task", remove.text)
    }

    // ---- Checkboxes ------------------------------------------------------

    @Test
    fun `toggleCheckbox flips the state of the tapped line`() {
        val source = "intro\n- [ ] eggs\n- [x] jam"
        val flipped = Markdown.toggleCheckbox(source, source.indexOf("- [ ] eggs"))
        assertTrue(flipped.contains("- [x] eggs"))
        assertTrue(flipped.contains("- [x] jam")) // other line untouched, still checked
        val back = Markdown.toggleCheckbox(flipped, flipped.indexOf("- [x] eggs"))
        assertTrue(back.contains("- [ ] eggs"))
    }

    @Test
    fun `isCheckLine detects checklist lines only`() {
        val source = "intro\n- [ ] eggs\nplain"
        assertTrue(Markdown.isCheckLine(source, source.indexOf("- [ ]")))
        assertFalse(Markdown.isCheckLine(source, 0))
    }
}
