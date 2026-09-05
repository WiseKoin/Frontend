package com.wisekoin.data.remote

import com.google.gson.Gson
import com.wisekoin.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

data class ErrorResponse(
    val error: String? = null,
    val message: String? = null
)

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"
    private var tokenManager: TokenManager? = null
    private val gson = Gson()

    fun init(tokenManager: TokenManager) {
        this.tokenManager = tokenManager
    }

    fun getGson(): Gson = gson

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val errorInterceptor = Interceptor { chain ->
        val response = chain.proceed(chain.request())
        if (response.code >= 400 && response.code < 600) {
            val body = response.body?.string()
            if (!body.isNullOrBlank()) {
                try {
                    val errorResponse = gson.fromJson(body, ErrorResponse::class.java)
                    val message = errorResponse?.error ?: errorResponse?.message ?: body
                    throw RuntimeException(message)
                } catch (e: Exception) {
                    throw RuntimeException("HTTP ${response.code}: $body")
                }
            }
        }
        response
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(errorInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                val token = tokenManager?.getToken()
                if (!token.isNullOrEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(requestBuilder.build())
            }
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
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
