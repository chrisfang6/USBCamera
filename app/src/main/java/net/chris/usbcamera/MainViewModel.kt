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

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message


    private val cameraHelper: UVCCameraHelper = UVCCameraHelper.getInstance()

    private var isRequest: Boolean = false
    private var isPreview: Boolean = false

    private val listener = object : UVCCameraHelper.OnMyDevConnectListener {

        override fun onAttachDev(device: UsbDevice) {
            if (cameraHelper.usbDeviceCount == 0) {
                showShortMsg("check no usb camera")
                return
            }
            // request open permission
            if (!isRequest) {
                showShortMsg("attaching device")
                isRequest = true
                cameraHelper.requestPermission(0)
            }
        }

        override fun onDettachDev(device: UsbDevice) {
            // close camera
            if (isRequest) {
                showShortMsg("detaching device")
                isRequest = false
                cameraHelper.closeCamera()
                showShortMsg(device.deviceName + " is out")
            }
        }

        override fun onConnectDev(device: UsbDevice, isConnected: Boolean) {
            if (!isConnected) {
                showShortMsg("fail to connect,please check resolution params")
                isPreview = false
            } else {
                isPreview = true
                showShortMsg("connecting")
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
            showShortMsg("disconnecting")
        }
    }

    init {
        cameraHelper.setDefaultFrameFormat(UVCCameraHelper.FRAME_FORMAT_YUYV)
        cameraHelper.setOnPreviewFrameListener {
            // onPreviewResult
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    override fun onSurfaceCreated(view: CameraViewInterface?, surface: Surface?) {
        if (!isPreview && cameraHelper.isCameraOpened) {
            showShortMsg("start preview")
            cameraHelper.startPreview(cameraView)
            isPreview = true
        }
    }

    override fun onSurfaceChanged(view: CameraViewInterface?, surface: Surface?, width: Int, height: Int) {
        showShortMsg("surface changed")
    }

    override fun onSurfaceDestroy(view: CameraViewInterface?, surface: Surface?) {
        if (isPreview && cameraHelper.isCameraOpened) {
            showShortMsg("stop preview")
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

    fun showShortMsg(msg: String) {
        _message.value = msg
    }

    fun registerUSB() {
        showShortMsg("register usb")
        cameraHelper.registerUSB()
    }

    fun unregisterUSB() {
        showShortMsg("unregister usb")
        cameraHelper.unregisterUSB()
    }

    fun release() {
        showShortMsg("release")
        cameraHelper.release()
    }
}