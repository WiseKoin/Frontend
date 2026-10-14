package com.wisekoin.di

import org.junit.Assert.assertEquals
import org.junit.Test

class SessionExpiryNotifierTest {
    @Test
    fun `expiry events use the current lifecycle listener`() {
        val notifier = SessionExpiryNotifier()
        var notificationCount = 0

        notifier.setListener { notificationCount++ }
        notifier.notifyExpired()
        notifier.setListener(null)
        notifier.notifyExpired()

        assertEquals(1, notificationCount)
    }
}
