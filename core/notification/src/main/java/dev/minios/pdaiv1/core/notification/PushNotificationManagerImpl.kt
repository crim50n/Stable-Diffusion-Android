package dev.minios.pdaiv1.core.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dev.minios.pdaiv1.core.common.extensions.isAppInForeground
import dev.minios.pdaiv1.core.common.log.debugLog
import dev.minios.pdaiv1.core.model.UiText
import dev.minios.pdaiv1.core.model.asUiText

internal class PushNotificationManagerImpl(
    private val context: Context,
    private val manager: NotificationManagerCompat,
) : PushNotificationManager {

    @SuppressLint("MissingPermission")
    override fun createAndShowInstant(title: UiText, body: UiText) {
        val inForeground = context.isAppInForeground()
        if (inForeground) {
            debugLog("App is in foreground, skipping...")
            return
        }

        val permission = hasNotificationPermission()
        if (permission != PackageManager.PERMISSION_GRANTED) {
            debugLog("Missing permissions for POST_NOTIFICATIONS, skipping...")
            return
        }

        val notification = createNotification(title, body)
        debugLog("Show PN => title: $title, body: $body")
        show(System.currentTimeMillis().toInt(), notification)
    }

    override fun createAndShowInstant(title: String, body: String) {
        createAndShowInstant(title.asUiText(), body.asUiText())
    }

    @SuppressLint("MissingPermission")
    override fun show(id: Int, notification: Notification) {
        createNotificationChannel()
        manager.notify(id, notification)
    }

    override fun createNotification(
        title: UiText,
        body: UiText?,
        block: NotificationCompat.Builder.() -> Unit
    ): Notification = with(
        NotificationCompat.Builder(context, PDAI_NOTIFICATION_CHANNEL_ID)
    ) {
        setSmallIcon(R.drawable.ic_notification)
        setContentTitle(title.asString(context))
        body?.asString(context)?.let {
            setContentText(it)
        }
        apply(block)
        build()
    }

    override fun createNotification(
        title: String,
        body: String?,
        block: NotificationCompat.Builder.() -> Unit
    ): Notification {
        return createNotification(title.asUiText(), body?.asUiText(), block)
    }

    override fun createProgressNotification(
        title: String,
        body: String?,
        block: NotificationCompat.Builder.() -> Unit
    ): Notification = with(
        NotificationCompat.Builder(context, PDAI_PROGRESS_CHANNEL_ID)
    ) {
        setSmallIcon(R.drawable.ic_notification)
        setContentTitle(title)
        body?.let { setContentText(it) }
        apply(block)
        build()
    }

    override fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (manager.getNotificationChannel(PDAI_NOTIFICATION_CHANNEL_ID) == null) {
                debugLog("Creating notification channel")

                manager.createNotificationChannel(
                    NotificationChannel(
                        PDAI_NOTIFICATION_CHANNEL_ID,
                        "PDAI Notifications",
                        NotificationManager.IMPORTANCE_HIGH,
                    ).also { channel ->
                        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    }
                )
            }
            // Create a separate channel for progress notifications with low priority
            if (manager.getNotificationChannel(PDAI_PROGRESS_CHANNEL_ID) == null) {
                debugLog("Creating progress notification channel")

                manager.createNotificationChannel(
                    NotificationChannel(
                        PDAI_PROGRESS_CHANNEL_ID,
                        "PDAI Progress",
                        NotificationManager.IMPORTANCE_LOW,
                    ).also { channel ->
                        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                        channel.setSound(null, null)
                        channel.enableVibration(false)
                    }
                )
            }
        }
    }

    private fun hasNotificationPermission(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        private const val PDAI_NOTIFICATION_CHANNEL_ID = "PDAI_NOTIFICATION_CHANNEL"
        const val PDAI_PROGRESS_CHANNEL_ID = "PDAI_PROGRESS_CHANNEL"
    }
}
