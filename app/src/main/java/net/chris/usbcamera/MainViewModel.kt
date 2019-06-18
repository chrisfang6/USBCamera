package net.chris.usbcamera

import android.app.Activity
import android.hardware.usb.UsbDevice
import android.os.Looper
import android.view.Surface
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jiangdg.usbcamera.UVCCameraHelper
import com.serenegiant.usb.widget.CameraViewInterface
import timber.log.Timber

class MainViewModel : ViewModel(), CameraViewInterface.Callback {


    private lateinit var cameraView: CameraViewInterface

    private val micPlayer: MicPlayer = MicPlayer()

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _cameraSwitcher = MutableLiveData<Boolean>()
    val cameraSwitcher: LiveData<Boolean> = _cameraSwitcher

    private val cameraHelper: UVCCameraHelper = UVCCameraHelper.getInstance()

    private var isRequest: Boolean = false
    private var isPreview: Boolean = false

    private var isReadingGPIO: Boolean = false

    private val sizeOfGpioData = 5
    private val gpioData: Array<String?> = arrayOfNulls(sizeOfGpioData)

    private var isSurfaceAvailable = false

    private val listener = object : UVCCameraHelper.OnMyDevConnectListener {

        override fun onAttachDev(device: UsbDevice) {
            if (cameraHelper.usbDeviceCount == 0) {
                showMessage("check no usb camera")
                return
            }
            // request open permission
            if (!isRequest) {
                showMessage("attaching device")
                isRequest = true
                cameraHelper.requestPermission(0)
            }
        }

        override fun onDettachDev(device: UsbDevice) {
            // close camera
            if (isRequest) {
                showMessage("detaching device")
                isRequest = false
                cameraHelper.closeCamera()
                showMessage(device.deviceName + " is out")
            }
        }

        override fun onConnectDev(device: UsbDevice, isConnected: Boolean) {
            if (!isConnected) {
                showMessage("fail to connect,please check resolution params")
                isPreview = false
            } else {
                isPreview = true
                showMessage("connecting")
                // initialize seekbar
                // need to wait UVCCamera initialize over
                Thread(Runnable {
                    try {
                        Thread.sleep(2500)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                    Looper.prepare()
                    if (cameraHelper.isCameraOpened) {
//                        mSeekBrightness.setProgress(cameraHelper.getModelValue(UVCCameraHelper.MODE_BRIGHTNESS))
//                        mSeekContrast.setProgress(cameraHelper.getModelValue(UVCCameraHelper.MODE_CONTRAST))
                    }
                    Looper.loop()
                }).start()
            }
        }

        override fun onDisConnectDev(device: UsbDevice) {
            showMessage("disconnecting")
        }
    }

    init {
//        cameraHelper.setDefaultFrameFormat(UVCCameraHelper.FRAME_FORMAT_YUYV)
        cameraHelper.setOnPreviewFrameListener {
            // onPreviewResult
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    override fun onSurfaceCreated(view: CameraViewInterface?, surface: Surface?) {
        isSurfaceAvailable = true
    }

    override fun onSurfaceChanged(view: CameraViewInterface?, surface: Surface?, width: Int, height: Int) {
        showMessage("surface changed")
    }

    override fun onSurfaceDestroy(view: CameraViewInterface?, surface: Surface?) {
        isSurfaceAvailable = false
    }

    fun initUSBMonitor(
        activity: Activity,
        cameraView: CameraViewInterface
    ) {
        cameraHelper.initUSBMonitor(activity, cameraView, listener)
        this.cameraView = cameraView
    }

    fun listenToGPIO() {
        isReadingGPIO = true
        Thread(Runnable {
            var count = 0
            while (isReadingGPIO) {
                gpioData[count] = ShellKit.adb("cat sys/class/gpio_sw/PD18/data")
                // check if all the gpio data are same
                var allSame = false
                var last: String? = null
                for (i in gpioData.indices) {
                    when (i) {
                        0 -> {
                            last = gpioData[i]
                            allSame = true
                        }
                        else -> {
                            allSame = allSame and (last != null) and (gpioData[i] == last)
                        }
                    }
                }
                if (allSame && isSurfaceAvailable) {
                    // change preview state
                    val state = if (gpioData[0] == "0") "Off" else "On"
                    Timber.w("GPIO has changed to $state")
                    when (gpioData[0]) {
                        "0" -> _cameraSwitcher.postValue(false)
                        "1" -> _cameraSwitcher.postValue(true)
                    }
                }
                try {
                    Thread.sleep(10)
                } catch (e: Exception) {
                    Timber.e(e)
                }
                count = (count + 1) % sizeOfGpioData
            }
        }).start()
    }

    fun stopListenToGPIO() {
        isReadingGPIO = false
    }

    fun showMessage(msg: String) {
        _message.postValue(msg)
    }

    fun registerUSB() {
        showMessage("register usb")
        cameraHelper.registerUSB()
    }

    fun unregisterUSB() {
        showMessage("unregister usb")
        cameraHelper.unregisterUSB()
    }

    fun release() {
        showMessage("release")
        cameraHelper.release()
    }

    fun startAudioPlay() {
//        micPlayer.start()
    }

    fun stopAudioPlay() {
        micPlayer.stop()
    }

    fun releaseAudioPlay() {
        micPlayer.release()
    }

    fun startPreview() {
        if (!isPreview && cameraHelper.isCameraOpened) {
            showMessage("start preview")
            cameraHelper.startPreview(cameraView)
            isPreview = true
        }
    }

    fun stopPreview() {
        if (isPreview && cameraHelper.isCameraOpened) {
            showMessage("stop preview")
            cameraHelper.stopPreview()
            isPreview = false
        }
    }

    companion object {
        const val GPIO_NAME = "PD18"
    }
}