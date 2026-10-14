package com.wisekoin.data.model

import com.google.gson.annotations.SerializedName

enum class OtpPurpose {
    CHANGE_PASSWORD,
    DELETE_ACCOUNT
}

data class OtpRequest(
    @SerializedName("purpose") val purpose: OtpPurpose
)

data class OtpRequestResponse(
    @SerializedName("expiresInSeconds") val expiresInSeconds: Long,
    @SerializedName("resendAfterSeconds") val resendAfterSeconds: Long,
    @SerializedName("destination") val destination: String
)

data class OtpVerifyRequest(
    @SerializedName("purpose") val purpose: OtpPurpose,
    @SerializedName("code") val code: String
)

data class OtpVerifyResponse(
    @SerializedName("verificationToken") val verificationToken: String,
    @SerializedName("expiresInSeconds") val expiresInSeconds: Long
)

data class ChangePasswordRequest(
    @SerializedName("currentPassword") val currentPassword: String,
    @SerializedName("newPassword") val newPassword: String,
    @SerializedName("verificationToken") val verificationToken: String
)

data class DeleteAccountRequest(
    @SerializedName("currentPassword") val currentPassword: String,
    @SerializedName("verificationToken") val verificationToken: String
)
