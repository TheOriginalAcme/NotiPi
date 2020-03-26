package com.example.notipi

import android.content.Context
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task

@RequiresApi(Build.VERSION_CODES.M)
class PermissionRequester(
    private var context: Context
)
{
    private val neededPermissions = arrayOf(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION)
    private val deniedPermissions = mutableListOf<String>()

    fun getNeededPermissions() {
        checkPermissions()
        requestPermissions()
        requestLocationServices()
        requestWifiServices()
    }

    private fun checkPermissions() {
        for (permission in neededPermissions) {
            if (PackageManager.PERMISSION_GRANTED != context.checkSelfPermission(permission)) {
                Log.d("checkPermission", "permission for ${permission.replace("android.permission.", "")} needs to be granted")
                deniedPermissions.add(permission)
            }
        }
    }

    private fun requestPermissions() {
        if (deniedPermissions.isNotEmpty()) {
            Log.d("requestPermissions", "requesting permissions for ${deniedPermissions[0].replace("android.permission.", "")}")
            ActivityCompat.requestPermissions(context as Activity, deniedPermissions.toTypedArray(), 3)
        }
    }

    private fun requestLocationServices() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(context)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            Log.d("LocationRequest", "location settings are satisfied.")
            Log.d("LocationRequest", locationSettingsResponse.toString())
            // The client can initialize location requests here.
            // ...
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(context as Activity?, 0)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun requestWifiServices()
    {
        val wifiManager : WifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        Log.d("turnWifiOn", "Requesting to enable WiFi")
        if (!wifiManager.isWifiEnabled) {
            Log.d("turnWifiOn", "Wifi is currently off.")
            val alertDialogBuilder = AlertDialog.Builder(context)
            alertDialogBuilder.setMessage("This app needs WiFi enabled. Please turn on WiFi in Settings")
                .setTitle("WiFi is required")
                .setCancelable(false)
                .setPositiveButton("Settings") { dialogInterface: DialogInterface, i: Int -> context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) }
                .setNegativeButton("Cancel") { dialogInterface: DialogInterface, i: Int -> Toast.makeText(context, "The app won't work without wifi you fool", Toast.LENGTH_SHORT).show() }
            alertDialogBuilder.create().show()
        }
    }
}
