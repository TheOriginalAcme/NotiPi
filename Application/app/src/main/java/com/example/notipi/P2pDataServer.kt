package com.example.notipi

import android.os.AsyncTask
import android.util.Log
import android.widget.TextView
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.Socket

class DataServerAsyncTask(
    private var statusText: TextView
) : AsyncTask<Void, Void, String?>() {

    override fun doInBackground(vararg params: Void): String? {
        /**
         * Create a server socket.
         */
        val ds = DatagramSocket()
        val dp = DatagramPacket("Hello".toByteArray(), 5, InetAddress.getByName("192.168.1.2"), 13109)
        ds.broadcast = true
        while (true) {
            ds.send(dp)
            Log.d("DataServerAsyncTask", "Sent message")
            Thread.sleep(1000)
        }

        return "Hello"
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
