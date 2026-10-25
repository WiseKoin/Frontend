package com.wisekoin.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.google.gson.Gson
import com.wisekoin.data.model.BankNotificationCandidate
import java.security.KeyStore
import java.security.MessageDigest
import java.time.Clock
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class BankNotificationStoreState(
    val trackingEnabled: Boolean = false,
    val activeAccountHash: String? = null,
    val candidates: List<BankNotificationCandidate> = emptyList()
)

interface BankNotificationCandidateStore {
    val state: StateFlow<BankNotificationStoreState>
    fun bindAccount(accountIdentifier: String?)
    fun setTrackingEnabled(enabled: Boolean)
    fun add(candidate: BankNotificationCandidate)
    fun remove(candidateId: String)
    fun clearCandidates()
}

class EncryptedBankNotificationStore(
    context: Context,
    private val clock: Clock = Clock.systemUTC(),
    private val gson: Gson = Gson()
) : BankNotificationCandidateStore {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val mutableState = MutableStateFlow(readState())
    override val state: StateFlow<BankNotificationStoreState> = mutableState.asStateFlow()

    init {
        val current = mutableState.value
        val retained = BankNotificationRetention.retainCurrent(current.candidates, clock.millis())
        if (retained != current.candidates) {
            persist(current.copy(candidates = retained))
        }
    }

    @Synchronized
    override fun bindAccount(accountIdentifier: String?) {
        val accountHash = accountIdentifier
            ?.trim()
            ?.lowercase(Locale.ROOT)
            ?.takeIf(String::isNotBlank)
            ?.let(::sha256)
        val current = mutableState.value
        if (current.activeAccountHash == accountHash) return
        persist(
            BankNotificationStoreState(
                trackingEnabled = false,
                activeAccountHash = accountHash,
                candidates = emptyList()
            )
        )
    }

    @Synchronized
    override fun setTrackingEnabled(enabled: Boolean) {
        val current = mutableState.value
        val allowed = enabled && current.activeAccountHash != null
        persist(current.copy(trackingEnabled = allowed))
    }

    @Synchronized
    override fun add(candidate: BankNotificationCandidate) {
        val current = mutableState.value
        if (!current.trackingEnabled || current.activeAccountHash == null) return
        val updated = BankNotificationRetention.add(
            candidates = current.candidates,
            candidate = candidate,
            nowEpochMillis = clock.millis()
        )
        if (updated != current.candidates) {
            persist(current.copy(candidates = updated))
        }
    }

    @Synchronized
    override fun remove(candidateId: String) {
        val current = mutableState.value
        persist(current.copy(candidates = current.candidates.filterNot { it.id == candidateId }))
    }

    @Synchronized
    override fun clearCandidates() {
        persist(mutableState.value.copy(candidates = emptyList()))
    }

    private fun persist(value: BankNotificationStoreState) {
        val encrypted = encrypt(gson.toJson(value))
        check(preferences.edit().putString(KEY_STATE, encrypted).commit()) {
            "Unable to persist bank notification candidates"
        }
        mutableState.value = value
    }

    private fun readState(): BankNotificationStoreState {
        val encrypted = preferences.getString(KEY_STATE, null) ?: return BankNotificationStoreState()
        return runCatching {
            gson.fromJson(decrypt(encrypted), BankNotificationStoreState::class.java)
                ?: BankNotificationStoreState()
        }.getOrElse {
            preferences.edit().remove(KEY_STATE).commit()
            BankNotificationStoreState()
        }
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(cipher.iv + encrypted, Base64.NO_WRAP)
    }

    private fun decrypt(value: String): String {
        val payload = Base64.decode(value, Base64.NO_WRAP)
        require(payload.size > IV_SIZE_BYTES) { "Invalid encrypted notification state" }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateSecretKey(),
            GCMParameterSpec(GCM_TAG_SIZE_BITS, payload.copyOfRange(0, IV_SIZE_BYTES))
        )
        return cipher.doFinal(payload.copyOfRange(IV_SIZE_BYTES, payload.size))
            .toString(Charsets.UTF_8)
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    private companion object {
        const val PREFERENCES_NAME = "bank_notification_candidates"
        const val KEY_STATE = "encrypted_state"
        const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        const val KEY_ALIAS = "wisekoin_bank_notification_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_SIZE_BYTES = 12
        const val GCM_TAG_SIZE_BITS = 128
    }
}

object BankNotificationStoreProvider {
    @Volatile
    private var instance: BankNotificationCandidateStore? = null

    fun get(context: Context): BankNotificationCandidateStore = instance ?: synchronized(this) {
        instance ?: EncryptedBankNotificationStore(context.applicationContext).also { instance = it }
    }
}
