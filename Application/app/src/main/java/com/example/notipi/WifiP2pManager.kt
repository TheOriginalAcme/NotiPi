package com.example.notipi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log

class WifiP2pManager (
    private var activity: MainActivity
)
{
    private val manager: WifiP2pManager? by lazy(LazyThreadSafetyMode.NONE) {
        activity.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager?
    }
    private var mChannel: WifiP2pManager.Channel
    private var receiver: BroadcastReceiver? = null
    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

    private val connectionListener = WifiP2pManager.ConnectionInfoListener { info ->

        // InetAddress from WifiP2pInfo struct.
        val groupOwnerAddress: String = info.groupOwnerAddress.hostAddress

        // After the group negotiation, we can determine the group owner
        // (server).
        if (info.groupFormed && info.isGroupOwner) {
            // Do whatever tasks are specific to the group owner.
            // One common case is creating a group owner thread and accepting
            // incoming connections.
        } else if (info.groupFormed) {
            // The other device acts as the peer (client). In this case,
            // you'll want to create a peer thread that connects
            // to the group owner.
        }
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
                Log.d("WifiP2pManager", "Successfully discovered Peers")
            }

            override fun onFailure(reasonCode: Int) {
                Log.d("WifiP2pManager", "Failed to discover Peers (reason: $reasonCode)")
                activity.showToast("Failed to discover peers")
            }
        })

    }

    fun updateDeviceList() {
        manager?.requestPeers(mChannel) { peers ->
            activity.currentDeviceList = peers?.deviceList!!
        }
        if (activity.piConnectionState == MainActivity.connectionState.NOT_CONNECTED &&
            activity.currentDeviceList.any { device: WifiP2pDevice -> device.deviceName == "NotiPi" }) {
            activity.piConnectionState = MainActivity.connectionState.CONNECTING
            connectToPiDevice(activity.currentDeviceList.filter { device: WifiP2pDevice -> device.deviceName == "NotiPi" }[0].deviceAddress)
        }
    }

    private fun connectToPiDevice(deviceAddress : String?) {
        val config = WifiP2pConfig()

        config.deviceAddress = deviceAddress
        config.wps.pin = "13371337"
        config.wps.setup = WpsInfo.PBC

        manager?.connect(mChannel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d("WifiP2pManager", "Connected to NotiPi device (${config.deviceAddress})")
                Log.d("WifiP2pManager", "Starting server...")
            }

            override fun onFailure(reason: Int) {
                activity.piConnectionState = MainActivity.connectionState.NOT_CONNECTED
                Log.d(
                    "WifiP2pManager",
                    "Failed to connect to NotiPi device (${config.deviceAddress}), reason: $reason"
                )
            }
        })
    }

}