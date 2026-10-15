package com.wisekoin.data.model

import com.google.gson.annotations.SerializedName

data class PasswordRecoveryRequest(
    @SerializedName("email") val email: String
)

data class PasswordRecoveryRequestResponse(
    @SerializedName("expiresInSeconds") val expiresInSeconds: Long,
    @SerializedName("resendAfterSeconds") val resendAfterSeconds: Long
)

data class PasswordRecoveryVerifyRequest(
    @SerializedName("email") val email: String,
    @SerializedName("code") val code: String
)

data class PasswordRecoveryResetRequest(
    @SerializedName("email") val email: String,
    @SerializedName("newPassword") val newPassword: String,
    @SerializedName("verificationToken") val verificationToken: String
)

data class BaseCurrencyUpdateRequest(
    @SerializedName("baseCurrency") val baseCurrency: String
)
