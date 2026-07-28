package com.aurora.music.core.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import com.aurora.music.R

/**
 * Channels must exist before `POST_NOTIFICATIONS` is requested on Android 13+
 * (spec Section 11 item 10), so this runs from `Application.onCreate`.
 */
object NotificationChannels {

    const val PLAYBACK = "aurora_playback"
    const val SCAN = "aurora_scan"

    fun ensureCreated(context: Context) {
        val manager = context.getSystemService<NotificationManager>() ?: return

        val playback = NotificationChannel(
            PLAYBACK,
            context.getString(R.string.notification_channel_playback),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = context.getString(R.string.notification_channel_playback_description)
            setShowBadge(false)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }

        val scan = NotificationChannel(
            SCAN,
            context.getString(R.string.notification_channel_scan),
            NotificationManager.IMPORTANCE_MIN,
        ).apply {
            description = context.getString(R.string.notification_channel_scan_description)
            setShowBadge(false)
        }

        manager.createNotificationChannels(listOf(playback, scan))
    }
}
