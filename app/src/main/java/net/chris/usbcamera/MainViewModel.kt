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

class MainViewModel : ViewModel(), CameraViewInterface.Callback {


    private lateinit var cameraView: CameraViewInterface

    val micPlayer: MicPlayer = MicPlayer()

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message


    private val cameraHelper: UVCCameraHelper = UVCCameraHelper.getInstance()

    private var isRequest: Boolean = false
    private var isPreview: Boolean = false

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
        if (!isPreview && cameraHelper.isCameraOpened) {
            showMessage("start preview")
            cameraHelper.startPreview(cameraView)
            isPreview = true
        }
    }

    override fun onSurfaceChanged(view: CameraViewInterface?, surface: Surface?, width: Int, height: Int) {
        showMessage("surface changed")
    }

    override fun onSurfaceDestroy(view: CameraViewInterface?, surface: Surface?) {
        if (isPreview && cameraHelper.isCameraOpened) {
            showMessage("stop preview")
            cameraHelper.stopPreview()
            isPreview = false
        }
    }

    fun initUSBMonitor(
        activity: Activity,
        cameraView: CameraViewInterface
    ) {
        cameraHelper.initUSBMonitor(activity, cameraView, listener)
        this.cameraView = cameraView
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
        micPlayer.findAudioRecord()
    }

    fun stopAudioPlay() {
//        micPlayer.stop()
    }

    fun releaseAudioPlay() {
//        micPlayer.release()
    }
}