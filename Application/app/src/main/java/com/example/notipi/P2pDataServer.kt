package com.example.notipi

import android.os.AsyncTask
import android.util.Log
import android.widget.TextView
import java.net.Socket

class DataServerAsyncTask(
    private var statusText: TextView
) : AsyncTask<Void, Void, String?>() {

    override fun doInBackground(vararg params: Void): String? {
        /**
         * Create a server socket.
         */
        val clientSocket = Socket("192.168.1.2", 13109)
        Log.d("DataServer", "Socket opened")
        return clientSocket.use {
            /**
             * Wait for client connections. This call blocks until a
             * connection is accepted from a client.
             */
            clientSocket.connect(clientSocket.localSocketAddress)
            Log.d("DataServer", "Connection done")
            /**
             * If this code is reached, a client has connected and transferred data
             * Save the input stream from the client
             */
            val inputStream = clientSocket.getInputStream()
            Log.d("DataServer", inputStream.toString())
            clientSocket.close()
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
