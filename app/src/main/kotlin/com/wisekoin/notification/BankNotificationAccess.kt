package com.wisekoin.notification

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

object BankNotificationAccess {
    fun isGranted(context: Context): Boolean {
        val component = ComponentName(context, BankNotificationListenerService::class.java)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            context.getSystemService(NotificationManager::class.java)
                .isNotificationListenerAccessGranted(component)
        } else {
            NotificationManagerCompat.getEnabledListenerPackages(context)
                .contains(context.packageName)
        }
    }

    fun settingsIntent(): Intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}
