package net.chris.usbcamera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.View
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        handler = Handler()

        fab.setOnClickListener {
            showShortMsg("TODO: setting", "Action", null)
        }

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java).apply {
            initUSBMonitor(this@MainActivity, camera_view)
            message.observe(this@MainActivity, Observer { showShortMsg(it) })
        }

        camera_view.apply {
            setCallback(viewModel)
//            postDelayed({ showShortMsg("test") }, 2000)
        }
    }

    override fun onStart() {
        super.onStart()
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
    }

    override fun onStop() {
        super.onStop()
        viewModel.unregisterUSB()
        viewModel.stopAudioPlay()
    }

    override fun onDestroy() {
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

    private fun showShortMsg(msg: String) =
        msg.apply {
            Timber.w(this)
            handler.post { Snackbar.make(fab, this, Snackbar.LENGTH_SHORT).show() }
        }

    private fun showShortMsg(msg: String, action: String, listener: View.OnClickListener?) =
        handler.post { Snackbar.make(fab, msg, Snackbar.LENGTH_SHORT).setAction(action, listener).show() }

}
