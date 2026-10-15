package com.wisekoin.data.model

import com.google.gson.annotations.SerializedName

data class UserResponseDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("baseCurrency")
    val baseCurrency: String = "VND"
)
