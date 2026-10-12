package com.wisekoin.viewmodel

import java.util.concurrent.atomic.AtomicBoolean

internal class SubmissionGate {
    private val inFlight = AtomicBoolean(false)

    fun tryStart(): Boolean = inFlight.compareAndSet(false, true)

    fun finish() {
        inFlight.set(false)
    }
}
