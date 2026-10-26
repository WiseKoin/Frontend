package com.wisekoin.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.ParsePosition
import java.util.Currency
import java.util.Locale

object MoneyFormatter {
    const val DEFAULT_CURRENCY_CODE = "VND"
    val roundingMode: RoundingMode = RoundingMode.HALF_EVEN

    fun currencyScale(currencyCode: String): Int = runCatching {
        Currency.getInstance(currencyCode.uppercase(Locale.ROOT)).defaultFractionDigits
    }.getOrDefault(2).coerceIn(0, 4)

    fun normalize(amount: BigDecimal, currencyCode: String): BigDecimal =
        amount.setScale(currencyScale(currencyCode), roundingMode)

    fun format(
        amount: BigDecimal,
        currencyCode: String = DEFAULT_CURRENCY_CODE,
        locale: Locale = Locale.getDefault(),
        includeCurrencyCode: Boolean = true
    ): String {
        val scale = currencyScale(currencyCode)
        val formatter = numberFormatter(locale, scale)
        val number = formatter.format(normalize(amount, currencyCode))
        return if (includeCurrencyCode) "$number ${currencyCode.uppercase(Locale.ROOT)}" else number
    }

    fun parse(
        raw: String,
        currencyCode: String = DEFAULT_CURRENCY_CODE,
        locale: Locale = Locale.getDefault()
    ): BigDecimal? {
        val currencySuffix = currencyCode.uppercase(Locale.ROOT)
        val input = raw.trim()
            .removeSuffix(currencySuffix)
            .trim()
            .filterNot(Char::isWhitespace)
        if (input.isEmpty()) return null

        val formatter = numberFormatter(locale, currencyScale(currencyCode)).apply {
            isParseBigDecimal = true
        }
        val position = ParsePosition(0)
        val parsed = formatter.parse(input, position) as? BigDecimal ?: return null
        if (position.index != input.length || position.errorIndex >= 0) return null
        return normalize(parsed, currencyCode)
    }

    fun formatIntegralInput(
        raw: String,
        locale: Locale = Locale.getDefault()
    ): String {
        val digits = raw.filter(Char::isDigit)
        if (digits.isEmpty()) return ""
        return numberFormatter(locale, scale = 0).format(BigDecimal(digits))
    }

    fun formatInput(
        raw: String,
        currencyCode: String = DEFAULT_CURRENCY_CODE,
        locale: Locale = Locale.getDefault()
    ): String {
        val scale = currencyScale(currencyCode)
        if (scale == 0) return formatIntegralInput(raw, locale)

        val decimalSeparator = DecimalFormatSymbols.getInstance(locale).decimalSeparator
        if (raw.none(Char::isDigit) && !raw.contains(decimalSeparator)) return ""
        val decimalIndex = raw.indexOf(decimalSeparator)
        val integerDigits = raw.substring(
            startIndex = 0,
            endIndex = decimalIndex.takeIf { it >= 0 } ?: raw.length
        ).filter(Char::isDigit)
        val integerPart = formatIntegralInput(integerDigits.ifEmpty { "0" }, locale)
        if (decimalIndex < 0) return integerPart

        val fractionalDigits = raw.substring(decimalIndex + 1)
            .filter(Char::isDigit)
            .take(scale)
        return "$integerPart$decimalSeparator$fractionalDigits"
    }

    private fun numberFormatter(locale: Locale, scale: Int): DecimalFormat =
        (NumberFormat.getNumberInstance(locale) as DecimalFormat).apply {
            isGroupingUsed = true
            minimumFractionDigits = scale
            maximumFractionDigits = scale
            roundingMode = MoneyFormatter.roundingMode
        }
}
