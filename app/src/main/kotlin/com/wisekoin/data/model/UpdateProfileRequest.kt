package com.wisekoin.data.model

import com.google.gson.annotations.SerializedName

data class UpdateProfileRequest(
    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("phone")
    val phone: String
)
