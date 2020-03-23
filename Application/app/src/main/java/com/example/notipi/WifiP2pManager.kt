package com.example.notipi

import android.app.AlertDialog
import android.content.*
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi

class WifiP2pManager (
    private var activity: MainActivity
)
{
    private val manager: WifiP2pManager? by lazy(LazyThreadSafetyMode.NONE) {
        activity.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager?
    }
    lateinit var mChannel: WifiP2pManager.Channel
    var receiver: BroadcastReceiver? = null
    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

    init {
        mChannel = manager?.initialize(activity, activity.mainLooper, null)!!
    }

    fun registerP2pReceiver() {
        receiver = WiFiDirectBroadcastReceiver(manager, mChannel, activity)
        activity.registerReceiver(receiver, intentFilter)
    }

    fun unregisterP2pReceiver() {
        activity.unregisterReceiver(receiver)
    }

    fun discoverPeers() {
        manager?.discoverPeers(mChannel, object: WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d("MainActivity", "Successfully discovered Peers")
            }

            override fun onFailure(reasonCode: Int) {
                Log.d("MainActivity", "Failed to discover Peers (reason: $reasonCode)")
                activity.showToast("Failed to discover peers")
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
                activity.connectedToPi = true
                DataServerAsyncTask(activity.findViewById<TextView>(R.id.textView)).execute()
            }

            override fun onFailure(reason: Int) {
                Log.d(
                    "MainActivity",
                    "Failed to connect to NotiPi device (${config.deviceAddress})"
                )
            }
        })
    }

}