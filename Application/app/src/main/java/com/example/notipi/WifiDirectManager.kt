package com.example.notipi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log

class WifiDirectManager (
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

    fun resetConnection() {
        manager?.cancelConnect(mChannel, null)
        manager?.stopPeerDiscovery(mChannel, null)
        manager?.removeGroup(mChannel, null)
        unregisterP2pReceiver()
        registerP2pReceiver()
        activity.piConnectionState.value = MainActivity.ConnectionState.NOT_CONNECTED
//        discoverPeers()
        updateDeviceList()
    }

    fun updateDeviceList() {
        manager?.requestPeers(mChannel) { peers ->
            activity.currentDeviceList = peers?.deviceList!!
        }
        if (activity.piConnectionState.value == MainActivity.ConnectionState.NOT_CONNECTED &&
            activity.currentDeviceList.any { device: WifiP2pDevice -> device.deviceName == "NotiPi" }) {
            Log.d("updateDeviceList", "Connecting")
            activity.piConnectionState.value = MainActivity.ConnectionState.CONNECTING
            connectToPiDevice(activity.currentDeviceList.filter { device: WifiP2pDevice -> device.deviceName == "NotiPi" }[0].deviceAddress)
        }
    }

    private fun connectToPiDevice(deviceAddress : String?) {
        val config = WifiP2pConfig()

        config.deviceAddress = deviceAddress
        config.wps.pin = "13371337"
        config.wps.setup = WpsInfo.KEYPAD
        config.groupOwnerIntent = 0

        manager?.connect(mChannel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d("WifiP2pManager", "Invited NotiPi device (${config.deviceAddress})")
                Log.d("WifiP2pManager", "Waiting to get connected...")
            }

            override fun onFailure(reason: Int) {
                activity.piConnectionState.value = MainActivity.ConnectionState.NOT_CONNECTED
                Log.d(
                    "WifiP2pManager",
                    "Failed to connect to NotiPi device (${config.deviceAddress}), reason: $reason"
                )
            }
        })
    }

    fun getConnectionStatus() {
        manager!!.requestConnectionInfo(mChannel, WifiConnectionListener)
    }

    private object WifiConnectionListener : WifiP2pManager.ConnectionInfoListener {
        override fun onConnectionInfoAvailable(info: WifiP2pInfo?) {
            Log.d("WifiConnectionListener", info.toString())
//            DataServerAsyncTask(activity.findViewById<TextView>(R.id.textView)).execute()
        }

    }

}