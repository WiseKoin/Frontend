package com.wisekoin.data.repository

import com.wisekoin.data.model.ReceiptResponseDto
import com.wisekoin.data.model.VoiceResponseDto
import com.wisekoin.data.remote.ReceiptApiService
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.ZoneId
import java.util.Locale

interface ReceiptRepository {
    suspend fun extractReceipt(file: MultipartBody.Part): ReceiptResponseDto
    suspend fun extractVoice(file: MultipartBody.Part): VoiceResponseDto
}

class NetworkReceiptRepository(
    private val api: ReceiptApiService
) : ReceiptRepository {
    override suspend fun extractReceipt(file: MultipartBody.Part): ReceiptResponseDto =
        api.extractReceipt(file, localeBody(), timeZoneBody())

    override suspend fun extractVoice(file: MultipartBody.Part): VoiceResponseDto =
        api.extractVoice(file, localeBody(), timeZoneBody())

    private fun localeBody() = Locale.getDefault().toLanguageTag().toRequestBody()

    private fun timeZoneBody() = ZoneId.systemDefault().id.toRequestBody()
}
