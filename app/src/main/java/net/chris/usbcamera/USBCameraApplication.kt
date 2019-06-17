package net.chris.usbcamera

import android.app.Application
import timber.log.Timber


class USBCameraApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        Timber.plant(Timber.DebugTree())
    }

    companion object {
        lateinit var instance: USBCameraApplication
    }
}