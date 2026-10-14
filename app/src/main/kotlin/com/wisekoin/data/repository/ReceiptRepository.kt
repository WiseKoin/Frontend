package com.wisekoin.data.repository

import com.wisekoin.data.model.ReceiptResponseDto
import com.wisekoin.data.model.VoiceResponseDto
import com.wisekoin.data.remote.ReceiptApiService
import okhttp3.MultipartBody

interface ReceiptRepository {
    suspend fun extractReceipt(file: MultipartBody.Part): ReceiptResponseDto
    suspend fun extractVoice(file: MultipartBody.Part): VoiceResponseDto
}

class NetworkReceiptRepository(
    private val api: ReceiptApiService
) : ReceiptRepository {
    override suspend fun extractReceipt(file: MultipartBody.Part): ReceiptResponseDto =
        api.extractReceipt(file)

    override suspend fun extractVoice(file: MultipartBody.Part): VoiceResponseDto =
        api.extractVoice(file)
}
