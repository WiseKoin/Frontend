package com.wisekoin.viewmodel

import com.wisekoin.data.model.BankNotificationCandidate
import com.wisekoin.data.model.BankNotificationDirection
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BankNotificationReviewValidatorTest {
    private val candidate = BankNotificationCandidate(
        id = "candidate",
        amount = BigDecimal("45000"),
        currency = "VND",
        direction = BankNotificationDirection.EXPENSE,
        sourceApp = "Bank",
        occurredAt = "2026-10-25T08:00:00",
        capturedAtEpochMillis = 1L
    )

    @Test
    fun `accepts a positive amount with owned wallet and category`() {
        assertNull(
            validate(
                parsedAmount = BigDecimal("45000"),
                selectedWalletId = 1,
                ownedWalletIds = setOf(1),
                selectedCategoryId = 2,
                ownedCategoryIds = setOf(2)
            )
        )
    }

    @Test
    fun `rejects wallet outside the loaded owned options`() {
        assertEquals(
            BankNotificationReviewError.SELECT_WALLET,
            validate(
                parsedAmount = BigDecimal("45000"),
                selectedWalletId = 99,
                ownedWalletIds = setOf(1),
                selectedCategoryId = 2,
                ownedCategoryIds = setOf(2)
            )
        )
    }

    @Test
    fun `rejects currency that does not match account base currency`() {
        assertEquals(
            BankNotificationReviewError.CURRENCY_MISMATCH,
            BankNotificationReviewValidator.validate(
                candidate = candidate.copy(currency = "USD"),
                parsedAmount = BigDecimal("12.50"),
                baseCurrency = "VND",
                selectedWalletId = 1,
                ownedWalletIds = setOf(1),
                selectedCategoryId = 2,
                ownedCategoryIds = setOf(2)
            )
        )
    }

    private fun validate(
        parsedAmount: BigDecimal?,
        selectedWalletId: Long?,
        ownedWalletIds: Set<Long>,
        selectedCategoryId: Long?,
        ownedCategoryIds: Set<Long>
    ) = BankNotificationReviewValidator.validate(
        candidate = candidate,
        parsedAmount = parsedAmount,
        baseCurrency = "VND",
        selectedWalletId = selectedWalletId,
        ownedWalletIds = ownedWalletIds,
        selectedCategoryId = selectedCategoryId,
        ownedCategoryIds = ownedCategoryIds
    )
}
