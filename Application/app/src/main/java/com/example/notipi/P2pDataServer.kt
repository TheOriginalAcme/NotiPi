package com.example.notipi

import android.os.AsyncTask
import android.util.Log
import android.widget.TextView
import java.net.ServerSocket

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
