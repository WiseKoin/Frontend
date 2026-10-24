package com.wisekoin.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyFormatterTest {
    @Test
    fun `parses US and Vietnamese separators according to locale`() {
        assertEquals(
            BigDecimal("12345.67"),
            MoneyFormatter.parse("12,345.67", "USD", Locale.US)
        )
        assertEquals(
            BigDecimal("12345.67"),
            MoneyFormatter.parse("12.345,67", "USD", Locale.forLanguageTag("vi-VN"))
        )
    }

    @Test
    fun `VND uses zero scale and bankers rounding`() {
        assertEquals(0, MoneyFormatter.currencyScale("VND"))
        assertEquals(RoundingMode.HALF_EVEN, MoneyFormatter.roundingMode)
        assertEquals(BigDecimal("10000"), MoneyFormatter.normalize(BigDecimal("10000.4"), "VND"))
    }

    @Test
    fun `rejects trailing unparsed data`() {
        assertNull(MoneyFormatter.parse("12,345oops", "VND", Locale.US))
    }

    @Test
    fun `formatting preserves supported fractional precision`() {
        assertEquals("12,345.67", MoneyFormatter.format(BigDecimal("12345.67"), "USD", Locale.US, false))
    }
}
