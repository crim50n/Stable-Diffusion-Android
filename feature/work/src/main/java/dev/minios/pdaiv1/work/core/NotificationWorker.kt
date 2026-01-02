package dev.minios.pdaiv1.work.core

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dev.minios.pdaiv1.core.common.appbuild.ActivityIntentProvider
import dev.minios.pdaiv1.core.common.extensions.isAppInForeground
import dev.minios.pdaiv1.core.notification.PushNotificationManager
import io.reactivex.rxjava3.core.Single

internal abstract class NotificationWorker(
    context: Context,
    workerParameters: WorkerParameters,
    private val pushNotificationManager: PushNotificationManager,
    private val activityIntentProvider: ActivityIntentProvider,
) : Rx3Worker(context, workerParameters) {

    abstract val notificationId: Int

    abstract val genericNotificationId: Int

    override val foregroundInfo: Single<ForegroundInfo>
        get() = Single.fromCallable {
            pushNotificationManager.createNotificationChannel()
            val notification = pushNotificationManager.createNotification(
                "Generation in progress",
                "Your image is being generated..."
            ) {
                setOngoing(true)
                setProgress(0, 0, true)
                setAutoCancel(false)
                setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                setContentIntent(
                    PendingIntent.getActivity(
                        applicationContext,
                        0,
                        activityIntentProvider().also { intent ->
                            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                        },
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                    ),
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ForegroundInfo(notificationId, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
            } else {
                ForegroundInfo(notificationId, notification)
            }
        }

    fun showGenericNotification(text: String, body: String?) {
        pushNotificationManager.createNotificationChannel()
        val notification = pushNotificationManager.createNotification(text, body) {
            setContentIntent(
                PendingIntent.getActivity(
                    applicationContext,
                    0,
                    activityIntentProvider().also { intent ->
                        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    },
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                ),
            )
            setVibrate(longArrayOf(10000L, 2000L, 1000L))
            setAutoCancel(true)
            setTicker(text)
            setSilent(applicationContext.isAppInForeground())
        }
        pushNotificationManager.show(genericNotificationId, notification)
    }

    fun setForegroundNotification(
        title: String,
        body: String,
        subText: String? = null,
        progress: Pair<Int, Int>? = null,
        silent: Boolean = false,
        canCancel: Boolean = false,
    ) {
        pushNotificationManager.createNotificationChannel()
        // Use progress notification channel to avoid heads-up notifications
        val notification = pushNotificationManager.createProgressNotification(title, body) {
            setSilent(silent)
            setTicker(title)
            setOngoing(true)

            progress?.let { (step, max) ->
                setProgress(max, step, false)
            } ?: run {
                setProgress(0, 0, true)
            }
            subText?.let { setSubText(it) }

            if (canCancel) {
                val intent = WorkManager
                    .getInstance(applicationContext)
                    .createCancelPendingIntent(id)
                addAction(android.R.drawable.ic_delete, "Cancel", intent)
            }

            setAutoCancel(false)
            setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)

            setContentIntent(
                PendingIntent.getActivity(
                    applicationContext,
                    0,
                    activityIntentProvider().also { intent ->
                        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    },
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                ),
            )
        }

        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(notificationId, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notificationId, notification)
        }
        setForegroundAsync(info)
    }
}
