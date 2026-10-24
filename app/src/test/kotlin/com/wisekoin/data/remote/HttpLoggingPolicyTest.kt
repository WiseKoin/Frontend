package com.wisekoin.data.remote

import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assert.assertEquals
import org.junit.Test

class HttpLoggingPolicyTest {
    @Test
    fun `debug logging never includes request or response bodies`() {
        assertEquals(HttpLoggingInterceptor.Level.BASIC, httpLoggingLevel(isDebug = true))
    }

    @Test
    fun `release logging is disabled`() {
        assertEquals(HttpLoggingInterceptor.Level.NONE, httpLoggingLevel(isDebug = false))
    }
}
