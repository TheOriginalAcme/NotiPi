package com.example.notipi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity


@RequiresApi(Build.VERSION_CODES.M)
class MainActivity : AppCompatActivity()
{
    var notificationManager: NotificationManager = NotificationManager(this)
    private var permissionRequester : PermissionRequester = PermissionRequester(this)
    private lateinit var nameInput: EditText
    private val manager: WifiP2pManager? by lazy(LazyThreadSafetyMode.NONE) {
        getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager?
    }
    lateinit var mChannel: WifiP2pManager.Channel
    var receiver: BroadcastReceiver? = null
    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }
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

        Log.d("MainActivity", "Wifi P2P stuff...")
        mChannel = manager?.initialize(this, mainLooper, null)!!

        Log.d("MainActivity", "Finished on create")
    }

    fun discoverPeers() {
        manager?.discoverPeers(mChannel, object: WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d("MainActivity", "Successfully discovered Peers")
            }

            override fun onFailure(reasonCode: Int) {
                Log.d("MainActivity", "Failed to discover Peers (reason: $reasonCode)")
                showToast("Failed to discover peers")
            }
        })

    }

    fun findPiDeviceAndConnect() {
        manager?.requestPeers(mChannel, object: WifiP2pManager.PeerListListener {
            override fun onPeersAvailable(peers: WifiP2pDeviceList?) {
                Log.d("MainActivity", "Found available peers:")
                Log.d("-> available peers:", peers?.deviceList.toString())
                for (device in peers?.deviceList!!) {
                    if (device.deviceName == "NotiPi") {
                        Log.d("MainActivity", "Found NotiPi peer: $device")
                        connectToPiDevice(device.deviceAddress)
                    }
                }
            }
        })
    }

    private fun connectToPiDevice(deviceAddress : String?) {
        val config = WifiP2pConfig()

        config.deviceAddress = deviceAddress
        config.wps.pin = "13371337"
        manager?.connect(mChannel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d("MainActivity", "Connected to NotiPi device (${config.deviceAddress})")
                Log.d("MainActivity", "Starting server...")
                connectedToPi = true
                DataServerAsyncTask(findViewById<TextView>(R.id.textView)).execute()
            }

            override fun onFailure(reason: Int) {
                Log.d(
                    "MainActivity",
                    "Failed to connect to NotiPi device (${config.deviceAddress})"
                )
            }
        })
    }

    /** register the BroadcastReceiver with the intent values to be matched  */
    override fun onResume() {
        super.onResume()
        receiver = WiFiDirectBroadcastReceiver(manager, mChannel, this)
        registerReceiver(receiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
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


