package com.example.notipi

import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class MyNotificationListener : NotificationListenerService() {
    override fun onBind(intent: Intent) : IBinder?
    {
        return super.onBind(intent)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?)
    {
        Log.d("Notification", sbn.toString())
        var intent = Intent("com.example.notipi")
        intent.putExtra("NotificationString", sbn.toString())
        sendBroadcast(intent)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?)
    {
        super.onNotificationRemoved(sbn)
    }
}

class MainActivity : AppCompatActivity() {

    lateinit var notificationReceiver : NotificationReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Yanai says", "We are up and running")
        setContentView(R.layout.activity_main)

        if (!isNotificationServiceEnabled())
        {
            buildNotificationServiceAlertDialog().show()
        }

        var intentfilter = IntentFilter()
        intentfilter.addAction("com.example.notipi")
        notificationReceiver = NotificationReceiver()
        registerReceiver(notificationReceiver, intentfilter)

        Log.d("yanai says", "Finished on create")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(notificationReceiver)
    }

    fun getNotificationsPressed(view: View)
    {
        Log.d("Yanai says", "Button was pressed")
    }

    fun isNotificationServiceEnabled() : Boolean
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

    fun buildNotificationServiceAlertDialog() : AlertDialog
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

    class NotificationReceiver : BroadcastReceiver()
    {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("Notification Receiver", intent.toString())
        }
    }
}
