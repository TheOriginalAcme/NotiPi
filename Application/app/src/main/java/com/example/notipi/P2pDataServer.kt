package com.example.notipi

import android.os.AsyncTask
import android.util.Log
import android.widget.TextView
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.Socket

class DataServerAsyncTask(
    private var address: InetAddress?,
    private var activity: MainActivity
) : AsyncTask<Void, Void, String?>() {

    override fun doInBackground(vararg params: Void): String? {
        /**
         * Create a server socket.
         */
        val ds = DatagramSocket()
        val dp = DatagramPacket("Hello".toByteArray(), 5, address, 13109)

        ds.reuseAddress = true
        ds.broadcast = true


        while (MainActivity.ConnectionState.CONNECTED ==  activity.piConnectionState.value) {
            ds.send(dp)
            Log.d("DataServerAsyncTask", "Sent message")
            Thread.sleep(1000)
        }

        return "Hello"
    }

}
