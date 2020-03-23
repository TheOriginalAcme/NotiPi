package com.example.notipi

import android.content.Context
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

@RequiresApi(Build.VERSION_CODES.M)
class PermissionRequester(
    private var context: Context
)
{
    private val neededPermissions = arrayOf(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE,
                                    Manifest.permission.ACCESS_FINE_LOCATION)
    private val deniedPermissions = mutableListOf<String>()

    fun getNeededPermissions() {
        checkPermissions()
        requestPermissions()
    }

    private fun checkPermissions() {
        for (permission in neededPermissions) {
            if (PackageManager.PERMISSION_GRANTED != context.checkSelfPermission(permission)) {
                Log.d("checkPermission", permission)
                deniedPermissions.add(permission)
            }
        }
    }

    private fun requestPermissions() {
        if (deniedPermissions.isNotEmpty()) {
            Log.d("requestPermissions", deniedPermissions[0])
            ActivityCompat.requestPermissions(context as Activity, deniedPermissions.toTypedArray(), 3)
        }
    }
}
