package com.wisekoin.data.model

import com.google.gson.Gson
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordRecoveryDtoTest {
    private val gson = Gson()

    @Test
    fun `recovery verify contract uses email and six digit code fields`() {
        val json = gson.toJson(
            PasswordRecoveryVerifyRequest("person@example.com", "123456")
        )

        assertTrue(json.contains("\"email\":\"person@example.com\""))
        assertTrue(json.contains("\"code\":\"123456\""))
    }

    @Test
    fun `recovery reset contract never includes a confirmation password`() {
        val json = gson.toJson(
            PasswordRecoveryResetRequest(
                email = "person@example.com",
                newPassword = "New-password1!",
                verificationToken = "opaque-token"
            )
        )

        assertTrue(json.contains("\"verificationToken\":\"opaque-token\""))
        assertFalse(json.contains("confirm", ignoreCase = true))
    }

    @Test
    fun `base currency update uses backend contract field`() {
        val json = gson.toJson(BaseCurrencyUpdateRequest("USD"))

        assertTrue(json.contains("\"baseCurrency\":\"USD\""))
    }
}
