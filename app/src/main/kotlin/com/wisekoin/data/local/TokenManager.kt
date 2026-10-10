package com.wisekoin.data.local

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

interface SessionTokenStore {
    fun saveTokens(accessToken: String, refreshToken: String)
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun clearTokens()
    fun hasSession(): Boolean
}

class TokenManager(context: Context) : SessionTokenStore {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFERENCES_NAME = "wisekoin_prefs"
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "wisekoin_session_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_SIZE_BYTES = 12
        private const val GCM_TAG_SIZE_BITS = 128
        private const val KEY_ACCESS_TOKEN = "access_token_encrypted"
        private const val KEY_REFRESH_TOKEN = "refresh_token_encrypted"
        private const val LEGACY_ACCESS_TOKEN = "jwt_token"
    }

    @Synchronized
    override fun saveTokens(accessToken: String, refreshToken: String) {
        require(accessToken.isNotBlank()) { "Access token must not be blank" }
        require(refreshToken.isNotBlank()) { "Refresh token must not be blank" }

        val committed = prefs.edit()
            .putString(KEY_ACCESS_TOKEN, encrypt(accessToken))
            .putString(KEY_REFRESH_TOKEN, encrypt(refreshToken))
            .remove(LEGACY_ACCESS_TOKEN)
            .commit()
        check(committed) { "Unable to persist the authenticated session" }
    }

    @Synchronized
    override fun getAccessToken(): String? = readToken(KEY_ACCESS_TOKEN)

    @Synchronized
    override fun getRefreshToken(): String? = readToken(KEY_REFRESH_TOKEN)

    @Synchronized
    override fun clearTokens() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(LEGACY_ACCESS_TOKEN)
            .commit()
    }

    @Synchronized
    override fun hasSession(): Boolean {
        return !getAccessToken().isNullOrBlank() && !getRefreshToken().isNullOrBlank()
    }

    private fun readToken(key: String): String? {
        val encrypted = prefs.getString(key, null) ?: return null
        return try {
            decrypt(encrypted)
        } catch (_: Exception) {
            clearTokens()
            null
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
        require(payload.size > IV_SIZE_BYTES) { "Invalid encrypted token" }
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

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return keyGenerator.generateKey()
    }
}
