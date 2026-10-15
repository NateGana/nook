package com.example.nook.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.nook.MainActivity
import com.example.nook.R
import com.example.nook.data.NookRepository

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("reminder_title") ?: "Nook Reminder"
        val note = intent.getStringExtra("reminder_note") ?: ""
        val item = intent.getStringExtra("reminder_item") ?: ""
        val zone = intent.getStringExtra("reminder_zone") ?: ""
        val userEmail = intent.getStringExtra("user_email") ?: ""

        val prefs = NookRepository.loadNotificationPrefs(context, userEmail)
        val notificationsAllowed = prefs.first && prefs.second

        if (!notificationsAllowed) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "nook_reminders_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Nook belongings & item reminders"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = buildString {
            if (note.isNotBlank()) append(note)
            if (item.isNotBlank()) {
                if (isNotEmpty()) append(" • ")
                append("Item: ").append(item)
            }
            if (zone.isNotBlank()) {
                if (isNotEmpty()) append(" • ")
                append("Zone: ").append(zone)
            }
            if (isEmpty()) append("Don't forget your belongings!")
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Reminder: $title")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), notification)
    }
}
