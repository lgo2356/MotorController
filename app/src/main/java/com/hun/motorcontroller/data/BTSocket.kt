package com.hun.motorcontroller.data

import android.bluetooth.BluetoothSocket
import java.io.InputStream
import java.io.OutputStream

class BTSocket {

    companion object {
        var socket: BluetoothSocket? = null
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
    }
}