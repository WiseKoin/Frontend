package com.wisekoin.data.remote

import com.wisekoin.BuildConfig
import com.google.gson.Gson
import com.wisekoin.data.local.SessionTokenStore
import com.wisekoin.data.model.AuthResponse
import com.wisekoin.data.model.RefreshTokenRequest
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

data class ErrorResponse(
    val code: String? = null,
    val error: String? = null,
    val message: String? = null
)

class RetrofitClient(
    private val tokenManager: SessionTokenStore,
    private val onSessionExpired: () -> Unit
) {
    private val gson = Gson()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        redactHeader("Authorization")
        redactHeader("Cookie")
        level = httpLoggingLevel(BuildConfig.DEBUG)
    }

    private fun baseClientBuilder(): OkHttpClient.Builder = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)

    private val unauthenticatedClient by lazy {
        baseClientBuilder().build()
    }

    private val unauthenticatedRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(unauthenticatedClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val authApi: AuthApiService by lazy {
        unauthenticatedRetrofit.create(AuthApiService::class.java)
    }

    private val okHttpClient by lazy {
        baseClientBuilder()
            .addInterceptor { chain ->
                val token = tokenManager.getAccessToken()
                val request = if (token.isNullOrBlank() || chain.request().header("Authorization") != null) {
                    chain.request()
                } else {
                    chain.request().newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                }
                chain.proceed(request)
            }
            .authenticator(
                RefreshTokenAuthenticator(
                    tokenStore = tokenManager,
                    refreshTokens = ::refreshTokens,
                    onSessionExpired = onSessionExpired
                )
            )
            .build()
    }

    private fun refreshTokens(refreshToken: String): AuthResponse? {
        return try {
            val response = authApi.refresh(RefreshTokenRequest(refreshToken)).execute()
            if (response.isSuccessful) {
                response.body()
            } else {
                response.errorBody()?.close()
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val receiptApi: ReceiptApiService by lazy {
        retrofit.create(ReceiptApiService::class.java)
    }

    val categoryApi: CategoryApiService by lazy {
        retrofit.create(CategoryApiService::class.java)
    }

    val userApi: UserApiService by lazy {
        retrofit.create(UserApiService::class.java)
    }

    val walletApi: WalletApiService by lazy {
        retrofit.create(WalletApiService::class.java)
    }

    val transactionApi: TransactionApiService by lazy {
        retrofit.create(TransactionApiService::class.java)
    }

    val goalApi: GoalApiService by lazy {
        retrofit.create(GoalApiService::class.java)
    }

    val debtApi: DebtApiService by lazy {
        retrofit.create(DebtApiService::class.java)
    }
}

internal fun httpLoggingLevel(isDebug: Boolean): HttpLoggingInterceptor.Level =
    if (isDebug) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE

internal class RefreshTokenAuthenticator(
    private val tokenStore: SessionTokenStore,
    private val onSessionExpired: () -> Unit = {},
    private val refreshTokens: (String) -> AuthResponse?
) : Authenticator {
    private val refreshLock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.responseCount() >= 2) {
            expireSession()
            return null
        }

        val requestToken = response.request.header("Authorization")
            ?.removePrefix("Bearer ")
            ?.takeIf { it.isNotBlank() }

        return synchronized(refreshLock) {
            val latestAccessToken = tokenStore.getAccessToken()
            if (!latestAccessToken.isNullOrBlank() && latestAccessToken != requestToken) {
                return@synchronized response.request.withBearerToken(latestAccessToken)
            }

            val refreshToken = tokenStore.getRefreshToken()
            if (refreshToken.isNullOrBlank()) {
                expireSession()
                return@synchronized null
            }

            val refreshed = refreshTokens(refreshToken)
            if (refreshed == null || refreshed.accessToken.isBlank() || refreshed.refreshToken.isBlank()) {
                expireSession()
                return@synchronized null
            }

            tokenStore.saveTokens(refreshed.accessToken, refreshed.refreshToken)
            response.request.withBearerToken(refreshed.accessToken)
        }
    }

    private fun expireSession() {
        tokenStore.clearTokens()
        onSessionExpired()
    }
}

private fun Request.withBearerToken(token: String): Request =
    newBuilder().header("Authorization", "Bearer $token").build()

private fun Response.responseCount(): Int {
    var count = 1
    var current = priorResponse
    while (current != null) {
        count++
        current = current.priorResponse
    }
    return count
}
