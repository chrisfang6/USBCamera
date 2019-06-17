package net.chris.usbcamera

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Process

class USBUncaughtExceptionHandler(private val activity: Activity) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread?, e: Throwable?) {
        val baseContext = USBCameraApplication.instance.applicationContext
        val intent = Intent(baseContext, MainActivity::class.java)
            .putExtra("crash", true)
            .addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        or Intent.FLAG_ACTIVITY_NEW_TASK
            )
        val pendingIntent = PendingIntent.getActivity(
            baseContext,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT
        )
        val mgr = baseContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent)
        activity.finish()
        Process.killProcess(Process.myPid())
    }
}