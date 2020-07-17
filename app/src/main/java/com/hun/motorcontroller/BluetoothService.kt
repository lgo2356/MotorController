package com.hun.motorcontroller

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.hun.motorcontroller.data.BTSocket
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class BluetoothService(private val handler: Handler) {

    private var socket: BluetoothSocket? = null

    private inner class ConnectThread(device: BluetoothDevice, btAdapter: BluetoothAdapter) : Thread() {

        private val socket: BluetoothSocket? =
            device.createRfcommSocketToServiceRecord(Constants.UUID_SERIAL_PORT)
        private val bluetoothAdapter: BluetoothAdapter? = btAdapter

        private val device = device

        override fun run() {
            bluetoothAdapter?.cancelDiscovery()

            val createMethod = device.javaClass.getMethod("createInsecureRfcommSocket")

            socket?.use {
                try {
                    socket.connect()
                    BTSocket.socket = socket
                    BTSocket.inputStream = socket.inputStream
                    BTSocket.outputStream = socket.outputStream
                } catch (e: IOException) {
                    Log.d("Debug", "Failed to connect")
                }
            }
        }

        fun cancel() {
            try {
                socket?.close()
            } catch (e: IOException) {
                Log.e("Debug", "Could not close the client socket", e)
            }
        }
    }

    inner class IOThread(socket: BluetoothSocket) : Thread() {

        private val inputStream: InputStream = socket.inputStream
        private val outputStream: OutputStream = socket.outputStream
        private val buffer: ByteArray = ByteArray(1024)

        override fun run() {
            var numBytes: Int

            while (true) {
                numBytes = try {
                    inputStream.read(buffer)
                } catch (e: IOException) {
                    Log.d("Debug", "Input stream was disconnected", e)
                    break
                }

                val readMsg = handler.obtainMessage(Constants.MESSAGE_READ, numBytes, -1, buffer)
                readMsg.sendToTarget()
            }
        }

        fun write(bytes: ByteArray) {
            try {
//                outputStream.write(bytes)
                outputStream.write(1)
            } catch (e: IOException) {
                Log.e("Debug", "Error occurred when sending data", e)

                val writeErrorMsg = handler.obtainMessage(Constants.MESSAGE_TOAST)
                val bundle = Bundle().apply {
                    putString("toast", "Couldn't send data to the other device.")
                }
                writeErrorMsg.data = bundle
                handler.sendMessage(writeErrorMsg)
                return
            }
        }

        fun cancel() {
            try {
                BTSocket.socket?.close()
            } catch (e: IOException) {
                Log.e("Debug", "Could not close the connect socket", e)
            }
        }
    }
}
