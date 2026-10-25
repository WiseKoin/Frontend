package com.wisekoin.notification

import com.wisekoin.data.model.BankNotificationCandidate
import java.time.ZoneId
import java.util.Locale

data class BankNotificationParserDescriptor(
    val sourceId: String,
    val displayName: String,
    val packageNames: Set<String>,
    val version: String
)

interface AllowlistedBankNotificationParser {
    val descriptor: BankNotificationParserDescriptor

    fun parse(
        content: BankNotificationContent,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): BankNotificationCandidate? = BankNotificationParser.parse(
        content.copy(sourceApp = descriptor.displayName),
        zoneId
    )?.copy(
        sourceId = descriptor.sourceId,
        parserVersion = descriptor.version,
        sourceApp = descriptor.displayName
    )
}

object VcbBankNotificationParser : AllowlistedBankNotificationParser {
    override val descriptor = BankNotificationParserDescriptor(
        sourceId = "VCB",
        displayName = "VCB Digibank",
        packageNames = setOf("com.VCB"),
        version = "vcb-v1"
    )
}

object MbBankNotificationParser : AllowlistedBankNotificationParser {
    override val descriptor = BankNotificationParserDescriptor(
        sourceId = "MB",
        displayName = "MB Bank",
        packageNames = setOf("com.mbmobile"),
        version = "mb-v1"
    )
}

object TechcombankNotificationParser : AllowlistedBankNotificationParser {
    override val descriptor = BankNotificationParserDescriptor(
        sourceId = "TECHCOMBANK",
        displayName = "Techcombank Mobile",
        packageNames = setOf("vn.com.techcombank.bb.app"),
        version = "techcombank-v1"
    )
}

object MomoNotificationParser : AllowlistedBankNotificationParser {
    override val descriptor = BankNotificationParserDescriptor(
        sourceId = "MOMO",
        displayName = "MoMo",
        packageNames = setOf("com.mservice.momotransfer"),
        version = "momo-v1"
    )
}

object BankNotificationParserRegistry {
    val parsers: List<AllowlistedBankNotificationParser> = listOf(
        VcbBankNotificationParser,
        MbBankNotificationParser,
        TechcombankNotificationParser,
        MomoNotificationParser
    )

    private val byPackage = parsers.flatMap { parser ->
        parser.descriptor.packageNames.map { packageName ->
            packageName.lowercase(Locale.ROOT) to parser
        }
    }.toMap()

    fun parserFor(packageName: String): AllowlistedBankNotificationParser? =
        byPackage[packageName.lowercase(Locale.ROOT)]
}
