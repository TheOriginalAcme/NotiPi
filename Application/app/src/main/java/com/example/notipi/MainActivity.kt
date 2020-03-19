package com.example.notipi

import android.annotation.SuppressLint
import android.app.Notification
import android.content.*
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Yanai says", "We are up and running")
        setContentView(R.layout.activity_main)

        if (!isNotificationServiceEnabled())
        {
            buildNotificationServiceAlertDialog().show()
        }

        Log.d("yanai says", "Finished on create")
    }

    override fun onDestroy() {
        super.onDestroy()
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
    private val manager: WifiP2pManager,
    private val channel: WifiP2pManager.Channel
//    private val activity: MyWifiActivity
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
                    }
                    else -> {
                        // Wi-Fi P2P is not enabled
                    }
                }
            }
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                // Call WifiP2pManager.requestPeers() to get a list of current peers
                Log.d("WifiDirectBroadcastReceiver", "Wifi peers changed")
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
