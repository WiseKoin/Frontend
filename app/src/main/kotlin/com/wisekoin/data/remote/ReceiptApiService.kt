package com.wisekoin.data.remote

import com.wisekoin.data.model.ReceiptResponseDto
import com.wisekoin.data.model.VoiceResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ReceiptApiService {
    @Multipart
    @POST("api/receipts/extract")
    suspend fun extractReceipt(
        @Part file: MultipartBody.Part,
        @Part("locale") locale: RequestBody,
        @Part("time_zone") timeZone: RequestBody
    ): ReceiptResponseDto

    @Multipart
    @POST("api/voice/extract")
    suspend fun extractVoice(
        @Part file: MultipartBody.Part,
        @Part("locale") locale: RequestBody,
        @Part("time_zone") timeZone: RequestBody
    ): VoiceResponseDto
}
