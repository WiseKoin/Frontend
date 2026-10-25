package com.wisekoin.notification

import android.app.Notification
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

        val parser = BankNotificationParserRegistry.parserFor(notification.packageName) ?: return

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
        val candidate = parser.parse(
            BankNotificationContent(
                notificationKey = notification.key,
                sourceApp = parser.descriptor.displayName,
                postedAtEpochMillis = notification.postTime,
                textParts = textParts
            )
        )
        runCatching {
            val added = store.recordObservation(
                sourceId = parser.descriptor.sourceId,
                parserVersion = parser.descriptor.version,
                candidate = candidate
            )
            if (added && candidate != null) {
                BankNotificationAlertManager.show(applicationContext, candidate)
                BankNotificationSyncManager.sync(applicationContext)
            } else if (candidate == null) {
                BankNotificationSyncManager.sync(applicationContext)
            }
        }
    }
}
