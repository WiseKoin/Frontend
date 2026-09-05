package com.wisekoin.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class WalletResponseDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("balance")
    val balance: BigDecimal
)

data class WalletSummaryResponseDto(
    @SerializedName("totalBalance")
    val totalBalance: BigDecimal,
    @SerializedName("wallets")
    val wallets: List<WalletResponseDto>
)

data class WalletRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("walletType")
    val walletType: String,
    @SerializedName("initialBalance")
    val initialBalance: BigDecimal
)
