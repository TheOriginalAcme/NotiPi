package com.example.notipi

import android.app.Notification
import android.content.ComponentName
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity


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

}
