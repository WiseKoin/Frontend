package com.wisekoin.data.local

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("wisekoin_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_JWT_TOKEN = "jwt_token"
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_JWT_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_JWT_TOKEN, null)
    }

    fun clearToken() {
        prefs.edit().remove(KEY_JWT_TOKEN).apply()
    }

    fun hasToken(): Boolean {
        return !getToken().isNullOrEmpty()
    }
}
