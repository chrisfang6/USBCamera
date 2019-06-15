package net.chris.usbcamera

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
    }

    override fun onStop() {
        super.onStop()
        viewModel.unregisterUSB()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.release()
    }

    private fun showShortMsg(msg: String) =
        Snackbar.make(fab, msg, Snackbar.LENGTH_SHORT).show()

    private fun showShortMsg(msg: String, action: String, listener: View.OnClickListener?) =
        Snackbar.make(fab, msg, Snackbar.LENGTH_SHORT).setAction(action, listener).show()

}
