package com.wisekoin.data.model

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class AiExtractionDtoTest {
    private val gson = Gson()

    @Test
    fun receiptResponse_preservesDecimalAmountAndStableCategoryId() {
        val response = gson.fromJson(
            """
            {
              "store_name": "Coffee House",
              "category_id": 12,
              "category": "Dining",
              "amount": 45000.25
            }
            """.trimIndent(),
            ReceiptResponseDto::class.java
        )

        assertEquals("Coffee House", response.storeName)
        assertEquals(12L, response.categoryId)
        assertEquals("Dining", response.category)
        assertEquals(BigDecimal("45000.25"), response.amount)
        assertNull(response.currency)
    }

    @Test
    fun voiceResponse_preservesUnknownClassificationForUserClarification() {
        val response = gson.fromJson(
            """
            {
              "amount": 100.50,
              "currency": "USD",
              "type": "UNKNOWN",
              "category_id": null,
              "category": "Unknown",
              "note": "Ambiguous transfer",
              "transcript": "Transferred one hundred"
            }
            """.trimIndent(),
            VoiceResponseDto::class.java
        )

        assertEquals(BigDecimal("100.50"), response.amount)
        assertEquals("USD", response.currency)
        assertEquals("UNKNOWN", response.type)
        assertNull(response.categoryId)
    }

    @Test
    fun receiptResponse_parsesTypedFactsWarningsCandidatesAndDuplicates() {
        val response = gson.fromJson(
            """
            {
              "store_name": "Market",
              "category_id": 12,
              "category": "Groceries",
              "amount": 110.00,
              "currency": "VND",
              "occurred_at": "2026-10-22T08:30:00",
              "subtotal": 100.00,
              "tax": 10.00,
              "discount": 0,
              "tip": 0,
              "payment_method": "CARD",
              "category_candidates": [
                {"category_id": 12, "category": "Groceries", "rank": 1}
              ],
              "requires_clarification": true,
              "ambiguities": [{"field": "amount", "code": "ARITHMETIC_MISMATCH"}],
              "duplicate_candidates": [{
                "transaction_id": 9,
                "amount": 110.00,
                "type": "EXPENSE",
                "transaction_date": "2026-10-22T08:30:00",
                "match_reasons": ["EXACT_AMOUNT"]
              }]
            }
            """.trimIndent(),
            ReceiptResponseDto::class.java
        )

        assertEquals("VND", response.currency)
        assertEquals(BigDecimal("100.00"), response.subtotal)
        assertEquals("CARD", response.paymentMethod)
        assertEquals(12L, response.categoryCandidates?.single()?.categoryId)
        assertEquals(9L, response.duplicateCandidates?.single()?.transactionId)
        assertTrue(response.requiresClarification)
    }

    @Test
    fun voiceResponse_parsesMultipleTransactionsAndTransferHints() {
        val response = gson.fromJson(
            """
            {
              "amount": 50000,
              "currency": "VND",
              "type": "EXPENSE",
              "category_id": 10,
              "category": "Dining",
              "note": "Lunch",
              "transcript": "Lunch then move money to savings",
              "occurred_at": "2026-10-22T12:00:00",
              "requires_clarification": true,
              "transactions": [
                {
                  "amount": 50000,
                  "currency": "VND",
                  "type": "EXPENSE",
                  "category_id": 10,
                  "category": "Dining",
                  "note": "Lunch",
                  "occurred_at": "2026-10-22T12:00:00",
                  "category_candidates": [{"category_id": 10, "category": "Dining", "rank": 1}],
                  "requires_clarification": false,
                  "ambiguities": [],
                  "duplicate_candidates": []
                },
                {
                  "amount": 200000,
                  "currency": "VND",
                  "type": "TRANSFER",
                  "category_id": null,
                  "category": "Unknown",
                  "note": "Move money",
                  "occurred_at": "2026-10-23T09:00:00",
                  "source_wallet_hint": "Cash",
                  "destination_wallet_hint": "Savings",
                  "category_candidates": [],
                  "requires_clarification": true,
                  "ambiguities": [{"field": "wallets", "code": "USER_SELECTION_REQUIRED"}],
                  "duplicate_candidates": []
                }
              ]
            }
            """.trimIndent(),
            VoiceResponseDto::class.java
        )

        assertEquals(2, response.transactions?.size)
        assertEquals("TRANSFER", response.transactions?.get(1)?.type)
        assertEquals("Savings", response.transactions?.get(1)?.destinationWalletHint)
        assertNull(response.transactions?.get(1)?.categoryId)
    }

    @Test
    fun transferRequest_serializesNullableCategoryAndDestinationWallet() {
        val request = TransactionRequest(
            walletId = 1,
            categoryId = null,
            destinationWalletId = 2,
            amount = BigDecimal("250000"),
            type = "TRANSFER"
        )

        val json = gson.toJson(request)

        assertTrue(json.contains("\"destinationWalletId\":2"))
        assertTrue(!json.contains("categoryId"))
    }
}
