package com.example.notipi

import android.content.Intent
import android.os.Build
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
    private var notificationManager: NotificationManager = NotificationManager(this)
    private var permissionRequester : PermissionRequester = PermissionRequester(this)
    private lateinit var wifiP2pManager : WifiP2pManager
    private lateinit var nameInput: EditText
    var connectedToPi : Boolean = false

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

        wifiP2pManager = WifiP2pManager(this)

        Log.d("MainActivity", "Finished on create")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        permissionRequester.getNeededPermissions()
    }

    fun discoverPeers() {
        wifiP2pManager.discoverPeers()
    }

    fun findPiDeviceAndConnect() {
        wifiP2pManager.findPiDeviceAndConnect()
    }

    /** register the BroadcastReceiver with the intent values to be matched  */
    override fun onResume() {
        super.onResume()
        wifiP2pManager.registerP2pReceiver()
    }

    override fun onPause() {
        super.onPause()
        wifiP2pManager.unregisterP2pReceiver()
    }

    override fun onDestroy() {
        super.onDestroy()
        wifiP2pManager.unregisterP2pReceiver()
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


