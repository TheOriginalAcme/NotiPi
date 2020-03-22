package com.example.notipi

import android.annotation.SuppressLint
import android.content.*
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.net.ServerSocket


@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class MyNotificationListener : NotificationListenerService() {
    override fun onBind(intent: Intent) : IBinder?
    {
        return super.onBind(intent)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onNotificationPosted(sbn: StatusBarNotification?)
    {
        val pack = sbn!!.packageName.substringAfter("com.")
        var ticker = ""
        if (sbn.notification.tickerText != null) {
            ticker = sbn.notification.tickerText.toString()
        }
        val extras = sbn.notification.extras
        val title = extras.getString("android.title")
        val text = extras.getCharSequence("android.text").toString()

        Log.i("Notification", "############################")
//        Log.d("Notification", sbn.toString())
        Log.i("Package", pack)
        Log.i("Ticker", ticker)
        Log.i("Title", title)
        Log.i("Text", text)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?)
    {
        super.onNotificationRemoved(sbn)
        Log.d("Notification", "removed")
    }
}

class MainActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private val manager: WifiP2pManager? by lazy(LazyThreadSafetyMode.NONE) {
        getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager?
    }
    lateinit var mChannel: WifiP2pManager.Channel
    var receiver: BroadcastReceiver? = null
    val intentFilter = IntentFilter().apply {
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
        if (!isNotificationServiceEnabled())
        {
            buildNotificationServiceAlertDialog().show()
        }

        Log.d("MainActivity", "Wifi P2P stuff...")
        mChannel = manager?.initialize(this, mainLooper, null)!!

        Log.d("MainActivity", "Finished on create")
    }

    public fun discoverAndHandlePeers() {
        manager?.discoverPeers(mChannel, object: WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d("MainActivity", "Successfully discovered Peers")
            }

            override fun onFailure(reasonCode: Int) {
                Log.d("MainActivity", "Failed to discover Peers (reason: $reasonCode)")
            }
        })

    }

    public fun tryConnectToPi() {
        var piDevice: WifiP2pDevice? = getPiDevice()

        if (null != piDevice) {
            Log.d("tryConnectPi", "Failed to find pi device")
        }

        connectToPiDevice(piDevice?.deviceAddress)
    }

    private fun getPiDevice() : WifiP2pDevice? {
        var piDevice: WifiP2pDevice? = null
        manager?.requestPeers(mChannel, object: WifiP2pManager.PeerListListener {
            override fun onPeersAvailable(peers: WifiP2pDeviceList?) {
                Log.d("MainActivity", "Found available peers:")
                Log.d("-> available peers:", peers?.deviceList.toString())
                for (device in peers?.deviceList!!) {
                    if (device.deviceName == "NotiPi") {
                        Log.d("MainActivity", "Found NotiPi peer: $device")
                        piDevice = device
                        break
                    }
                }
            }
        })
        return piDevice
    }

    private fun connectToPiDevice(deviceAddress : String?) {
        val config = WifiP2pConfig()

        config.deviceAddress = deviceAddress
        mChannel.also { channel ->
            manager?.connect(channel, config, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.d("MainActivity", "Connected to NotiPi device (${config.deviceAddress})")
                    Log.d("MainActivity", "Starting server...")
                    connectedToPi = true
                    DataServerAsyncTask(findViewById<TextView>(R.id.textView)).execute()
                }

                override fun onFailure(reason: Int) {
                    Log.d("MainActivity", "Failed to connect to NotiPi device (${config.deviceAddress})")
                }
            }
            )}
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

    fun getNotificationsPressed(view: View)
    {
        Log.d("Yanai says", "Button was pressed")
    }

    private fun isNotificationServiceEnabled() : Boolean
    {
        var flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")

        if (flat != "")
        {
            var names = flat.split(':')

            for (name in names)
            {
                var cn = ComponentName.unflattenFromString(name)
                if (cn != null)
                {
                    if (TextUtils.equals(packageName, cn.packageName))
                    {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun buildNotificationServiceAlertDialog() : AlertDialog
    {
        var alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Please let me see your notifications")
        alertDialogBuilder.setMessage("Hello. I want to see your notifications so click yes")
        alertDialogBuilder.setPositiveButton("yes", DialogInterface.OnClickListener()
            {
                    dialogInterface: DialogInterface, i: Int -> startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            })
        alertDialogBuilder.setNegativeButton("no", DialogInterface.OnClickListener()
        {
                dialogInterface: DialogInterface, i: Int -> Toast.makeText(applicationContext, "Your application might suck now", Toast.LENGTH_SHORT).show()
        })
        return alertDialogBuilder.create()
    }

    fun submitName(view: View) {
        nameInput = findViewById<EditText>(R.id.nameInput)
        Log.d("DEBUG", nameInput.text.toString())
        showToast(nameInput.text.toString())
    }

    private fun showToast(text: String) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
    }
}

/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
class WiFiDirectBroadcastReceiver(
    private val manager: WifiP2pManager?,
    private val channel: WifiP2pManager.Channel,
    private val activity: MainActivity
) : BroadcastReceiver() {

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
                        activity.discoverAndHandlePeers()
                    }
                    else -> {
                        // Wi-Fi P2P is not enabled
                        Log.d("WifiDirectBroadcastReceiver", "-> Wifi P2P is not enabled")
                    }
                }
            }
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                // Call WifiP2pManager.requestPeers() to get a list of current peers
                Log.d("WifiDirectBroadcastReceiver", "Wifi peers changed")
                if (!activity.connectedToPi)
                {
                    activity.tryConnectToPi()
                }
            }
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                // Respond to new connection or disconnections
                // Applications can use requestConnectionInfo(), requestNetworkInfo(),
                // or requestGroupInfo() to retrieve the current connection information.
                Log.d("WifiDirectBroadcastReceiver", "new connection/disconnection")
            }
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                // Respond to this device's wifi state changing
                // Applications can use requestDeviceInfo() to retrieve the current connection information.
                Log.d("WifiDirectBroadcastReceiver", "device's wifi state changed")
            }
        }
    }
}

class DataServerAsyncTask(
    private var statusText: TextView
) : AsyncTask<Void, Void, String?>() {

    override fun doInBackground(vararg params: Void): String? {
        /**
         * Create a server socket.
         */
        val serverSocket = ServerSocket(8888)
        Log.d("DataServer", "Socket opened")
        return serverSocket.use {
            /**
             * Wait for client connections. This call blocks until a
             * connection is accepted from a client.
             */
            val client = serverSocket.accept()
            Log.d("DataServer", "Connection done")
            /**
             * If this code is reached, a client has connected and transferred data
             * Save the input stream from the client
             */
            val inputStream = client.getInputStream()
            Log.d("DataServer", inputStream.toString())
            serverSocket.close()
            "Data: $inputStream"
        }
    }

    /**
     * Start activity that can handle input stream
     */
    override fun onPostExecute(result: String?) {
        result?.run {
            statusText.text = "Data received - $result"
        }
    }
}