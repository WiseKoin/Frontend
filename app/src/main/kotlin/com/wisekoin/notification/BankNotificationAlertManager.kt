package com.wisekoin.notification

import android.annotation.SuppressLint
import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.wisekoin.MainActivity
import com.wisekoin.R
import com.wisekoin.data.model.BankNotificationCandidate

object BankNotificationAlertManager {
    const val EXTRA_CANDIDATE_ID = "bank_notification_candidate_id"
    private const val CHANNEL_ID = "bank_transaction_review"

    fun canNotify(context: Context): Boolean {
        val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        return permissionGranted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    @SuppressLint("MissingPermission")
    fun show(context: Context, candidate: BankNotificationCandidate) {
        if (!canNotify(context)) return
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        createChannel(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(EXTRA_CANDIDATE_ID, candidate.id)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            candidate.notificationId(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val publicNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.bank_notification_alert_title))
            .setContentText(context.getString(R.string.bank_notification_alert_private))
            .build()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.bank_notification_alert_title))
            .setContentText(
                context.getString(R.string.bank_notification_alert_message, candidate.sourceApp)
            )
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_launcher_foreground,
                context.getString(R.string.review),
                pendingIntent
            )
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicNotification)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(candidate.notificationId(), notification)
        } catch (_: SecurityException) {
            // Permission can be revoked between the check and the notify call.
        }
    }

    fun dismiss(context: Context, candidateId: String) {
        NotificationManagerCompat.from(context).cancel(candidateId.notificationId())
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.bank_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.bank_notification_channel_description)
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            }
        )
    }

    private fun BankNotificationCandidate.notificationId(): Int = id.notificationId()

    private fun String.notificationId(): Int = take(8).toLong(16).toInt()
}
