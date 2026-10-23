package com.wisekoin.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class TransactionResponseDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("amount")
    val amount: BigDecimal,
    @SerializedName("type")
    val type: String,
    @SerializedName("note")
    val note: String?,
    @SerializedName("transactionDate")
    val transactionDate: String,
    @SerializedName("wallet")
    val wallet: WalletInfoDto,
    @SerializedName("category")
    val category: CategoryInfoDto? = null,
    @SerializedName("debt")
    val debt: DebtInfoDto? = null,
    @SerializedName("goal")
    val goal: GoalInfoDto? = null
)

data class WalletInfoDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String
)

data class CategoryInfoDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("icon")
    val icon: String?
)

data class PagedTransactionResponse(
    @SerializedName("content")
    val content: List<TransactionResponseDto>,
    @SerializedName("totalElements")
    val totalElements: Long,
    @SerializedName("number")
    val number: Int,
    @SerializedName("totalPages")
    val totalPages: Int
)

data class TransactionRequest(
    @SerializedName("walletId")
    val walletId: Long,
    @SerializedName("categoryId")
    val categoryId: Long?,
    @SerializedName("destinationWalletId")
    val destinationWalletId: Long? = null,
    @SerializedName("amount")
    val amount: BigDecimal,
    @SerializedName("type")
    val type: String,
    @SerializedName("note")
    val note: String? = null,
    @SerializedName("transactionDate")
    val transactionDate: String? = null,
    @SerializedName("goalId")
    val goalId: Long? = null,
    @SerializedName("debtId")
    val debtId: Long? = null
)

data class DebtInfoDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("counterpartName")
    val counterpartName: String
)

data class GoalInfoDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String
)
