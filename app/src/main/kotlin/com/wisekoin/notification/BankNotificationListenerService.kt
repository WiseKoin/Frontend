package com.wisekoin.notification

import android.app.Notification
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.wisekoin.data.local.BankNotificationStoreProvider

class BankNotificationListenerService : NotificationListenerService() {
    override fun onNotificationPosted(notification: StatusBarNotification?) {
        if (notification == null || notification.packageName == packageName || notification.isOngoing) {
            return
        }

        val store = BankNotificationStoreProvider.get(applicationContext)
        if (!store.state.value.trackingEnabled) return

        val applicationInfo = applicationInfo(notification.packageName) ?: return
        val sourceLabel = packageManager.getApplicationLabel(applicationInfo).toString()
        if (!BankSourceClassifier.isLikelyFinanceApp(notification.packageName, sourceLabel)) return

        val extras = notification.notification.extras
        val textParts = buildList {
            extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.let(::add)
            extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.let(::add)
            extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.let(::add)
            extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()?.let(::add)
            extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
                ?.map(CharSequence::toString)
                ?.let(::addAll)
        }
        val candidate = BankNotificationParser.parse(
            BankNotificationContent(
                notificationKey = notification.key,
                sourceApp = sourceLabel,
                postedAtEpochMillis = notification.postTime,
                textParts = textParts
            )
        ) ?: return

        runCatching { store.add(candidate) }
    }

    private fun applicationInfo(packageName: String): ApplicationInfo? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getApplicationInfo(
                packageName,
                PackageManager.ApplicationInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getApplicationInfo(packageName, 0)
        }
    }.getOrNull()
}
