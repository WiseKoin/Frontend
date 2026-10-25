package com.wisekoin.notification

import android.content.Context
import com.wisekoin.data.local.BankNotificationStoreProvider
import com.wisekoin.data.local.TokenManager
import com.wisekoin.data.remote.RetrofitClient
import com.wisekoin.data.repository.NetworkBankNotificationSuggestionRepository
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object BankNotificationSyncManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val syncing = AtomicBoolean(false)
    private val requested = AtomicBoolean(false)

    fun sync(context: Context) {
        requested.set(true)
        if (!syncing.compareAndSet(false, true)) return
        val appContext = context.applicationContext
        scope.launch {
            try {
                val client = RetrofitClient(TokenManager(appContext)) { }
                val syncer = BankNotificationSyncer(
                    store = BankNotificationStoreProvider.get(appContext),
                    repository = NetworkBankNotificationSuggestionRepository(client.aiSuggestionApi)
                )
                do {
                    requested.set(false)
                    syncer.sync()
                } while (requested.get())
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                // The encrypted queue remains intact and the review screen retries later.
            } finally {
                syncing.set(false)
                if (requested.get()) sync(appContext)
            }
        }
    }
}
