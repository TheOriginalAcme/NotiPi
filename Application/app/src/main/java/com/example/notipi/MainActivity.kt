package com.example.notipi

import android.content.Intent
import android.os.Build
import android.net.wifi.p2p.WifiP2pDevice
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

@RequiresApi(Build.VERSION_CODES.M)
class MainActivity : AppCompatActivity()
{
    enum class connectionState {
        NOT_CONNECTED, CONNECTING, CONNECTED
    }

    private var notificationManager: NotificationManager = NotificationManager(this)
    private var permissionRequester : PermissionRequester = PermissionRequester(this)
    private lateinit var wifiDirectManager : WifiDirectManager
    private lateinit var nameInput: EditText
    var piConnectionState : connectionState = connectionState.NOT_CONNECTED
    var currentDeviceList : MutableCollection<WifiP2pDevice> = mutableListOf<WifiP2pDevice>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "Setting content view")
        setContentView(R.layout.activity_main)
        Log.d("MainActivity", "We are up and running")

        Log.d("MainActivity", "Checking that NotificationService is enabled")

        permissionRequester.getNeededPermissions()
        if (!notificationManager.isNotificationServiceEnabled())
        {
            notificationManager.buildNotificationServiceAlertDialog().show()
        }

        wifiDirectManager = WifiDirectManager(this)

        Log.d("MainActivity", "Finished on create")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        permissionRequester.getNeededPermissions()
    }

    fun updateDeviceList() {
        wifiDirectManager.updateDeviceList()
    }

    fun discoverPeers() {
        wifiDirectManager.discoverPeers()
    }

    /** register the BroadcastReceiver with the intent values to be matched  */
    override fun onResume() {
        super.onResume()
        wifiDirectManager.registerP2pReceiver()
    }

    override fun onPause() {
        super.onPause()
        wifiDirectManager.unregisterP2pReceiver()
    }

    override fun onDestroy() {
        super.onDestroy()
        wifiDirectManager.unregisterP2pReceiver()
    }

    fun submitName(view: View) {
        nameInput = findViewById<EditText>(R.id.nameInput)
        Log.d("DEBUG", nameInput.text.toString())
        showToast(nameInput.text.toString())
    }

    fun showToast(text: String) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
    }
}


