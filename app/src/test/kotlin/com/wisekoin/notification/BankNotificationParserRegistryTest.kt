package com.wisekoin.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class BankNotificationParserRegistryTest {
    @Test
    fun `registry contains only the pilot package allowlist`() {
        assertEquals("VCB", BankNotificationParserRegistry.parserFor("com.VCB")?.descriptor?.sourceId)
        assertEquals("MB", BankNotificationParserRegistry.parserFor("com.mbmobile")?.descriptor?.sourceId)
        assertEquals(
            "TECHCOMBANK",
            BankNotificationParserRegistry.parserFor("vn.com.techcombank.bb.app")
                ?.descriptor?.sourceId
        )
        assertEquals(
            "MOMO",
            BankNotificationParserRegistry.parserFor("com.mservice.momotransfer")
                ?.descriptor?.sourceId
        )
        assertNull(BankNotificationParserRegistry.parserFor("com.example.mobilebank"))
    }

    @Test
    fun `allowlisted parser adds stable source and parser identifiers`() {
        val candidate = VcbBankNotificationParser.parse(
            BankNotificationContent(
                notificationKey = "key",
                sourceApp = "spoofed label",
                postedAtEpochMillis = 1_800_000_000_000,
                textParts = listOf("GD -125.000 VND. So du 2.000.000 VND")
            )
        )

        assertEquals("VCB", candidate?.sourceId)
        assertEquals("vcb-v1", candidate?.parserVersion)
        assertEquals("VCB Digibank", candidate?.sourceApp)
    }

    @Test
    fun `every pilot parser recognizes a signed transaction template`() {
        BankNotificationParserRegistry.parsers.forEach { parser ->
            val candidate = parser.parse(
                BankNotificationContent(
                    notificationKey = parser.descriptor.sourceId,
                    sourceApp = parser.descriptor.displayName,
                    postedAtEpochMillis = 1_800_000_000_000,
                    textParts = listOf("Giao dich -50.000 VND. So du 2.000.000 VND")
                )
            )

            assertNotNull(parser.descriptor.sourceId, candidate)
            assertEquals(parser.descriptor.sourceId, candidate?.sourceId)
            assertEquals(parser.descriptor.version, candidate?.parserVersion)
        }
    }
}
