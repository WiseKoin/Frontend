package com.wisekoin.notification

import com.wisekoin.data.model.BankNotificationCandidate
import com.wisekoin.data.model.BankNotificationDirection
import java.math.BigDecimal
import java.security.MessageDigest
import java.text.Normalizer
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class BankNotificationContent(
    val notificationKey: String,
    val sourceApp: String,
    val postedAtEpochMillis: Long,
    val textParts: List<String>
)

object BankNotificationParser {
    private val amountRegex = Regex(
        pattern = "(?i)(?:(VND|VNĐ|USD|EUR|GBP|JPY|KRW|SGD|AUD|CAD|₫|đ)\\s*)?" +
            "([+-]?\\s*\\d+(?:[.,\\s]\\d+)*)\\s*" +
            "(VND|VNĐ|USD|EUR|GBP|JPY|KRW|SGD|AUD|CAD|₫|đ)?"
    )
    private val amountWords = listOf("amount", "transaction", "payment", "purchase", "withdraw", "giao dich", "thanh toan", "rut tien")
    private val balanceWords = listOf("balance", "available", "so du")
    private val creditWords = listOf("credited", "credit", "received", "deposit", "incoming", "ghi co", "nhan", "cong tien", "chuyen den")
    private val debitWords = listOf("debited", "debit", "spent", "purchase", "payment", "withdraw", "outgoing", "ghi no", "tru tien", "thanh toan", "rut tien", "chuyen di")

    fun parse(
        content: BankNotificationContent,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): BankNotificationCandidate? {
        val combined = content.textParts
            .asSequence()
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
            .joinToString(" · ")
        if (combined.isBlank()) return null

        val normalized = normalize(combined)
        val directionHint = directionFromWords(normalized)
        val matches = amountRegex.findAll(combined).mapNotNull { match ->
            val currency = currencyCode(match.groupValues[1].ifBlank { match.groupValues[3] })
            val rawNumber = match.groupValues[2].replace(" ", "")
            val hasExplicitSign = rawNumber.startsWith('+') || rawNumber.startsWith('-')
            val contextStart = (match.range.first - 28).coerceAtLeast(0)
            val contextEnd = (match.range.last + 29).coerceAtMost(combined.length)
            val context = normalize(combined.substring(contextStart, contextEnd))
            val hasCurrency = match.groupValues[1].isNotBlank() || match.groupValues[3].isNotBlank()
            val hasAmountWord = amountWords.any(context::contains)
            if (!hasCurrency && !hasExplicitSign && !hasAmountWord) return@mapNotNull null

            val amount = parseAmount(rawNumber, currency) ?: return@mapNotNull null
            if (amount <= BigDecimal.ZERO) return@mapNotNull null

            val score = (if (hasCurrency) 4 else 0) +
                (if (hasExplicitSign) 3 else 0) +
                (if (hasAmountWord) 2 else 0) -
                (if (balanceWords.any(context::contains)) 6 else 0)
            ParsedAmount(
                amount = amount,
                currency = currency,
                direction = when {
                    rawNumber.startsWith('-') -> BankNotificationDirection.EXPENSE
                    rawNumber.startsWith('+') -> BankNotificationDirection.INCOME
                    else -> directionHint
                },
                score = score,
                position = match.range.first
            )
        }.filter { it.direction != null }
            .sortedWith(compareByDescending<ParsedAmount> { it.score }.thenBy { it.position })
            .toList()

        val parsed = matches.firstOrNull() ?: return null
        val direction = requireNotNull(parsed.direction)
        val occurredAt = Instant.ofEpochMilli(content.postedAtEpochMillis)
            .atZone(zoneId)
            .toLocalDateTime()
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val source = content.sourceApp.trim().take(60).ifBlank { "Finance app" }
        val id = sha256(
            listOf(
                content.notificationKey,
                parsed.amount.toPlainString(),
                parsed.currency,
                direction.name,
                content.postedAtEpochMillis.toString()
            ).joinToString("|")
        )

        return BankNotificationCandidate(
            id = id,
            amount = parsed.amount,
            currency = parsed.currency,
            direction = direction,
            sourceApp = source,
            occurredAt = occurredAt,
            capturedAtEpochMillis = content.postedAtEpochMillis
        )
    }

    private fun directionFromWords(text: String): BankNotificationDirection? {
        val creditIndex = creditWords.map(text::indexOf).filter { it >= 0 }.minOrNull()
        val debitIndex = debitWords.map(text::indexOf).filter { it >= 0 }.minOrNull()
        return when {
            creditIndex == null && debitIndex == null -> null
            debitIndex == null -> BankNotificationDirection.INCOME
            creditIndex == null -> BankNotificationDirection.EXPENSE
            creditIndex < debitIndex -> BankNotificationDirection.INCOME
            else -> BankNotificationDirection.EXPENSE
        }
    }

    private fun parseAmount(raw: String, currency: String): BigDecimal? {
        val unsigned = raw.removePrefix("+").removePrefix("-")
        if (unsigned.isBlank()) return null
        val commaCount = unsigned.count { it == ',' }
        val dotCount = unsigned.count { it == '.' }
        val normalized = when {
            currency == "VND" -> unsigned.replace(",", "").replace(".", "")
            commaCount > 0 && dotCount > 0 -> {
                val decimalSeparator = if (unsigned.lastIndexOf(',') > unsigned.lastIndexOf('.')) ',' else '.'
                val groupingSeparator = if (decimalSeparator == ',') '.' else ','
                unsigned.replace(groupingSeparator.toString(), "")
                    .replace(decimalSeparator, '.')
            }
            commaCount + dotCount > 1 -> unsigned.replace(",", "").replace(".", "")
            commaCount == 1 -> normalizeSingleSeparator(unsigned, ',')
            dotCount == 1 -> normalizeSingleSeparator(unsigned, '.')
            else -> unsigned
        }
        return normalized.toBigDecimalOrNull()
    }

    private fun normalizeSingleSeparator(value: String, separator: Char): String {
        val fractionLength = value.length - value.lastIndexOf(separator) - 1
        return if (fractionLength in 1..2) value.replace(separator, '.') else value.replace(separator.toString(), "")
    }

    private fun currencyCode(raw: String): String = when (raw.uppercase(Locale.ROOT)) {
        "USD", "EUR", "GBP", "JPY", "KRW", "SGD", "AUD", "CAD" -> raw.uppercase(Locale.ROOT)
        else -> "VND"
    }

    private fun normalize(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace("đ", "d", ignoreCase = true)
        .replace(Regex("\\p{Mn}+"), "")
        .lowercase(Locale.ROOT)

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    private data class ParsedAmount(
        val amount: BigDecimal,
        val currency: String,
        val direction: BankNotificationDirection?,
        val score: Int,
        val position: Int
    )
}
