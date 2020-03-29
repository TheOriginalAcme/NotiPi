package com.example.notipi

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresApi

/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
@RequiresApi(Build.VERSION_CODES.M)
class WiFiDirectBroadcastReceiver(
    private val manager: WifiP2pManager?,
    private val channel: WifiP2pManager.Channel,
    private val activity: MainActivity
) : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("LongLogTag")
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                // Check to see if Wi-Fi is enabled and notify appropriate activity
                Log.d("WifiDirectBroadcastReceiver", "Wifi state changed")
                when (intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)) {
                    WifiP2pManager.WIFI_P2P_STATE_ENABLED -> {
                        // Wifi P2P is enabled
                        Log.d("WifiDirectBroadcastReceiver", "-> Wifi P2P is enabled")
                        activity.showToast("Wifi is enabled")
                        activity.discoverPeers()
                    }
                    else -> {
                        // Wi-Fi P2P is not enabled
                        Log.d("WifiDirectBroadcastReceiver", "-> Wifi P2P is not enabled")
                        activity.showToast("Wifi is disabled")
                        activity.permissionRequester.requestWifiServices()
                    }
                }
            }
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                // Call WifiP2pManager.requestPeers() to get a list of current peers
                Log.d("WifiDirectBroadcastReceiver", "Wifi peers changed")
                activity.updateDeviceList()
            }
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                // Respond to new connection or disconnections
                // Applications can use requestConnectionInfo(), requestNetworkInfo(),
                // or requestGroupInfo() to retrieve the current connection information.
                Log.d("WifiDirectBroadcastReceiver", "new connection/disconnection")
                val networkState: NetworkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO)

                if (networkState.isConnected()) {
                    activity.showToast("Connection Status: Connected")
                    Log.d("WifiDirectBroadcastReceiver", "Connected")
                    manager!!.requestConnectionInfo(channel) {info: WifiP2pInfo? ->  Log.d("Wifithing", "$info")
                        DataServerAsyncTask(activity.findViewById<TextView>(R.id.textView),
                            info?.groupOwnerAddress
                        ).execute()}
                    activity.piConnectionState.value = MainActivity.ConnectionState.CONNECTED
                } else {
                    activity.showToast("Connection Status: Disconnected")
                    Log.d("WifiDirectBroadcastReceiver", "Not Connected")
                    activity.piConnectionState.value = MainActivity.ConnectionState.NOT_CONNECTED
                    manager!!.cancelConnect(channel, null)
                    activity.updateDeviceList()
                }

            }
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                // Respond to this device's wifi state changing
                // Applications can use requestDeviceInfo() to retrieve the current connection information.
                Log.d("WifiDirectBroadcastReceiver", "device's wifi state changed")
            }
        }

    }

}
