package com.wisekoin.notification

import com.wisekoin.data.model.BankNotificationDirection
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class BankNotificationParserTest {
    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")

    @Test
    fun `parses Vietnamese debit and ignores reported balance`() {
        val result = BankNotificationParser.parse(
            content(
                "Giao dịch -45.000 VND. Số dư: 2.500.000 VND. Thanh toán thành công."
            ),
            zone
        )

        assertNotNull(result)
        assertEquals(BigDecimal("45000"), result?.amount)
        assertEquals(BankNotificationDirection.EXPENSE, result?.direction)
        assertEquals("VND", result?.currency)
    }

    @Test
    fun `parses signed incoming transfer`() {
        val result = BankNotificationParser.parse(
            content("Tài khoản được ghi có +1,250,000 VND. Số dư 8,000,000 VND."),
            zone
        )

        assertEquals(BigDecimal("1250000"), result?.amount)
        assertEquals(BankNotificationDirection.INCOME, result?.direction)
    }

    @Test
    fun `preserves decimal currency precision`() {
        val result = BankNotificationParser.parse(
            content("Card purchase amount USD 12.45. Available balance USD 90.00"),
            zone
        )

        assertEquals(BigDecimal("12.45"), result?.amount)
        assertEquals("USD", result?.currency)
        assertEquals(BankNotificationDirection.EXPENSE, result?.direction)
    }

    @Test
    fun `rejects non-transaction notification with an unrelated number`() {
        assertNull(BankNotificationParser.parse(content("Your verification code is 123456"), zone))
    }

    @Test
    fun `does not retain raw notification content`() {
        val result = requireNotNull(
            BankNotificationParser.parse(
                content("Payment -50,000 VND from account 123456789 at Secret Merchant"),
                zone
            )
        )

        assertEquals("Bank App", result.sourceApp)
        assertEquals("2026-10-25T08:00:00", result.occurredAt)
    }

    @Test
    fun `parses deidentified Vietnamese increase and decrease corpus templates`() {
        val expense = BankNotificationParser.parse(
            content("TK ****1234 giảm 125.000 VND lúc 09:12. Nội dung: THANH TOAN CUA HANG."),
            zone
        )
        val income = BankNotificationParser.parse(
            content("TK ****1234 tăng 3.500.000 VND lúc 08:00. Nội dung: LUONG THANG 10."),
            zone
        )

        assertEquals(BigDecimal("125000"), expense?.amount)
        assertEquals(BankNotificationDirection.EXPENSE, expense?.direction)
        assertEquals(BigDecimal("3500000"), income?.amount)
        assertEquals(BankNotificationDirection.INCOME, income?.direction)
    }

    private fun content(text: String) = BankNotificationContent(
        notificationKey = "notification-key",
        sourceApp = "Bank App",
        postedAtEpochMillis = Instant.parse("2026-10-25T01:00:00Z").toEpochMilli(),
        textParts = listOf(text)
    )
}
