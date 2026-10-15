package com.example.nook.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.nook.data.NookRepository

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            NookRepository.rescheduleAllRemindersOnBoot(context)
        }
    }
}
