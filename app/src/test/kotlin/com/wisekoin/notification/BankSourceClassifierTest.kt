package com.wisekoin.notification

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BankSourceClassifierTest {
    @Test
    fun `accepts known finance package names`() {
        assertTrue(BankSourceClassifier.isLikelyFinanceApp("com.example.mobilebank", "Example"))
    }

    @Test
    fun `accepts known Vietnamese finance labels without accents`() {
        assertTrue(BankSourceClassifier.isLikelyFinanceApp("vn.example", "Ngân hàng số"))
        assertTrue(BankSourceClassifier.isLikelyFinanceApp("vn.example", "MBBank"))
    }

    @Test
    fun `rejects unrelated applications`() {
        assertFalse(BankSourceClassifier.isLikelyFinanceApp("com.example.news", "News Reader"))
    }
}
