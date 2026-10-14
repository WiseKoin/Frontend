package com.wisekoin.data.model

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountSecurityDtoTest {
    private val gson = Gson()

    @Test
    fun `otp request serializes backend purpose exactly`() {
        val json = gson.toJson(OtpRequest(OtpPurpose.DELETE_ACCOUNT))

        assertEquals("{\"purpose\":\"DELETE_ACCOUNT\"}", json)
    }

    @Test
    fun `password request includes verification token without accidental fields`() {
        val json = gson.toJson(ChangePasswordRequest("Current1!", "NewPassword2!", "token"))

        assertTrue(json.contains("\"currentPassword\":\"Current1!\""))
        assertTrue(json.contains("\"newPassword\":\"NewPassword2!\""))
        assertTrue(json.contains("\"verificationToken\":\"token\""))
        assertFalse(json.contains("otp"))
    }
}
