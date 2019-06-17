package net.chris.usbcamera

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

class HomeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS &&
            intent.getStringExtra("reason") == "homekey"
        ) {
            Timber.d("home button is tapped")
            val baseContext = USBCameraApplication.instance.applicationContext
            for (i in 0..10) {
                val pendingIntent = PendingIntent.getActivity(
                    baseContext,
                    0,
                    Intent(
                        baseContext,
                        MainActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_NEW_TASK),
                    0
                )
                try {
                    pendingIntent.send()
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }
    }
}