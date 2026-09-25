package com.nothingjournal.ui.orb

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OrbEngineTest {

    private fun engine(state: OrbState = OrbState.IDLE): OrbEngine =
        Shdr14.newEngine().apply { this.state = state }

    @Test
    fun `idle synthesizes near-zero input and some output`() {
        val e = engine(OrbState.IDLE)
        val (i, o) = targetVolumes(OrbState.IDLE, 0.0)
        assertEquals(0f, i, 1e-6f)
        assertTrue(o > 0f && o < 0.5f)
        // One big snap step lands the frame on its targets.
        val frame = e.step(1f, snap = true)
        assertEquals(i, frame.input, 1e-4f)
        assertEquals(o, frame.output, 1e-4f)
    }

    @Test
    fun `speaking output is louder than idle output`() {
        val speaking = targetVolumes(OrbState.SPEAKING, 0.0).second
        val idle = targetVolumes(OrbState.IDLE, 0.0).second
        assertTrue(speaking > idle)
    }

    @Test
    fun `snap lands params exactly on the state preset`() {
        val e = engine(OrbState.THINKING)
        val frame = e.step(1f, snap = true)
        val preset = Shdr14.statePresets.getValue(OrbState.THINKING)
        preset.forEach { (key, value) ->
            val def = Shdr14.params.first { it.key == key }
            if (!def.integrate) {
                // Direct params land exactly on the preset when snapped.
                assertEquals(value, frame.params.getValue(key), 1e-4f)
            } else {
                // Integrated params upload a CLOCK (dt*speed*value), so it
                // only has to be positive and then keep advancing.
                assertTrue(frame.params.getValue(key) > 0f)
            }
        }
        val clockBefore = frame.params.getValue("speed")
        val next = e.step(1 / 60f)
        assertTrue(next.params.getValue("speed") > clockBefore)
    }

    @Test
    fun `integrated clocks advance with speed and never reset`() {
        val e = engine(OrbState.IDLE)
        val f1 = e.step(1 / 60f)
        val f2 = e.step(1 / 60f)
        val clock1 = f1.params.getValue("speed")
        val clock2 = f2.params.getValue("speed")
        assertTrue(clock2 > clock1)
    }

    @Test
    fun `colors glide toward the state palette`() {
        val e = engine(OrbState.SPEAKING)
        // Many small steps should drive the paper colour to the speaking amber.
        repeat(400) { e.step(1 / 60f) }
        val frame = e.step(1 / 60f)
        val paper = frame.colors.getValue("paper")
        // #ffd9a4 normalized, with tolerance for the spring not being settled.
        assertEquals(1f, paper[0], 0.05f)
        assertEquals(0.85f, paper[1], 0.08f)
        assertEquals(0.64f, paper[2], 0.08f)
    }

    @Test
    fun `pinned input overrides the synthesized volume`() {
        val e = engine(OrbState.IDLE)
        e.pinnedInput = 0.9f
        repeat(60) { e.step(1 / 60f) }
        val frame = e.step(1 / 60f)
        assertTrue("input should approach the pinned level", frame.input > 0.5f)
    }

    @Test
    fun `listening state exists with a teal palette`() {
        val palette = Shdr14.stateColors.getValue(OrbState.LISTENING)
        assertEquals("#b8e6e0", palette.getValue("paper"))
    }

    @Test
    fun `sleeping is dimmer than idle`() {
        val sleepGain = Shdr14.statePresets.getValue(OrbState.SLEEPING).getValue("gain")
        val idleGain = Shdr14.statePresets.getValue(OrbState.IDLE).getValue("gain")
        assertTrue(sleepGain < idleGain)
    }
}
