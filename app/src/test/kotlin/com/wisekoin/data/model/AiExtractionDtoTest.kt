package com.wisekoin.data.model

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
}
