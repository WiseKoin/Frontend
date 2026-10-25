package com.wisekoin.notification

import java.text.Normalizer
import java.util.Locale

object BankSourceClassifier {
    private val financeMarkers = listOf(
        "bank", "ngan hang", "vietcombank", "techcombank", "vietinbank", "bidv",
        "agribank", "vpbank", "tpbank", "sacombank", "seabank", "mbbank", "mb bank",
        "momo", "zalopay", "shopeepay", "vnpay", "tnex"
    )
    private val shortFinanceMarkers = setOf("acb", "vib", "shb", "ocb")

    fun isLikelyFinanceApp(packageName: String, applicationLabel: String): Boolean {
        val normalized = normalize("$packageName $applicationLabel")
        val terms = normalized.split(Regex("[^a-z0-9]+"))
        return financeMarkers.any(normalized::contains) || terms.any(shortFinanceMarkers::contains)
    }

    private fun normalize(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace("đ", "d", ignoreCase = true)
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase(Locale.ROOT)
}
