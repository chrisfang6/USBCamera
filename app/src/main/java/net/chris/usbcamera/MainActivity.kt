package net.chris.usbcamera

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    private lateinit var handler: Handler

    private val homeReceiver = HomeReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_main)

        Thread.setDefaultUncaughtExceptionHandler(USBUncaughtExceptionHandler(this))

        handler = Handler()

        fab.apply {
            setOnClickListener {
                showShortMsg("TODO: setting", "Action", null)
            }
            hide()
        }

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java).apply {
            initUSBMonitor(this@MainActivity, camera_view)
            listenToGPIO()
            message.observe(this@MainActivity, Observer { showShortMsg(it) })
            cameraSwitcher.observe(this@MainActivity, Observer {
                when (it) {
                    true -> viewModel.startPreview()
                    false -> viewModel.stopPreview()
                }
            })
        }

        camera_view.apply {
            setCallback(viewModel)
//            postDelayed({ showShortMsg("test") }, 2000)
        }

        viewModel.registerUSB()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                100
            )
        } else {
            viewModel.startAudioPlay()
        }

        registerReceiver(homeReceiver, IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
    }

    override fun onDestroy() {
        viewModel.stopListenToGPIO()
        viewModel.unregisterUSB()
        viewModel.stopAudioPlay()
        unregisterReceiver(homeReceiver)
        super.onDestroy()
        viewModel.release()
        viewModel.releaseAudioPlay()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            100 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    viewModel.startAudioPlay()
                } else {
                    Timber.w("permission denied by user")
                }
                return
            }
        }
    }

    override fun onBackPressed() {
    }

    private fun showShortMsg(msg: String) =
        msg.apply {
            Timber.w(this)
            handler.post { Snackbar.make(fab, this, Snackbar.LENGTH_SHORT).show() }
        }

    private fun showShortMsg(msg: String, action: String, listener: View.OnClickListener?) =
        handler.post { Snackbar.make(fab, msg, Snackbar.LENGTH_SHORT).setAction(action, listener).show() }

}
