package com.wisekoin.viewmodel

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SubmissionGateTest {
    @Test
    fun `overlapping submission is rejected`() {
        val gate = SubmissionGate()

        assertTrue(gate.tryStart())
        assertFalse(gate.tryStart())
    }

    @Test
    fun `submission can start after completion`() {
        val gate = SubmissionGate()

        assertTrue(gate.tryStart())
        gate.finish()

        assertTrue(gate.tryStart())
    }
}
