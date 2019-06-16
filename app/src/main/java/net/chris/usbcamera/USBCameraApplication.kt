package net.chris.usbcamera

import android.app.Application
import timber.log.Timber



class USBCameraApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}