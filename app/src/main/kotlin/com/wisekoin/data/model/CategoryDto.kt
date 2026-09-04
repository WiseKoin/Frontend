package com.wisekoin.data.model

import com.google.gson.annotations.SerializedName

data class CategoryResponse(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("icon")
    val icon: String? = null
)

data class CategoryRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("icon")
    val icon: String? = null
)
