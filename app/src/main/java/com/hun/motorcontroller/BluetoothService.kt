package com.hun.motorcontroller

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.util.Log
import android.widget.Toast
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.lang.Exception
import java.nio.charset.StandardCharsets.US_ASCII

class BluetoothService(private val handler: Handler, private val device: BluetoothDevice) {

    private var mSocket: BluetoothSocket? = null
//    private val mDevice: BluetoothDevice = device

    inner class ConnectThread : Thread() {

        override fun run() {
            try {
//                val createMethod = device.javaClass
//                    .getMethod(
//                        "createInsecureRfcommSocket",
//                        *arrayOf<Class<*>?>(Int::class.javaPrimitiveType)
//                    )
//                val socket = createMethod.invoke(device, 1) as BluetoothSocket
//                BTSocket.socket = socket
//                BTSocket.inputStream = socket.inputStream
//                BTSocket.outputStream = socket.outputStream


                mSocket = device.createInsecureRfcommSocketToServiceRecord(Constants.UUID_SERIAL_PORT)
                mSocket?.connect()
                handler.sendEmptyMessage(Constants.MESSAGE_CONNECTED)
            } catch (e: Exception) {
                Log.e("Debug", "Couldn't connect to your device")
                e.printStackTrace()

                val errorMsg = handler.obtainMessage(Constants.MESSAGE_TOAST, "블루투스 연결에 실패했습니다")
                errorMsg.sendToTarget()
                handler.sendEmptyMessage(Constants.MESSAGE_ERROR)
            }
        }

        fun cancel() {
            try {
                mSocket?.close()
            } catch (e: IOException) {
                Log.d("Debug", "Couldn't close the client socket", e)
            }
        }
    }

    inner class IOThread : Thread() {

        private val buffer: ByteArray = ByteArray(1024)
        private var bufferPosition = 0
        private val delimiter: Byte = 0x0A

        override fun run() {
//            val b: Byte = 0x0A
//            val msg = handler.obtainMessage(Constants.MESSAGE_READ, b)
//            msg.sendToTarget()

            try {
                while (true) {
//                    val bytesAvailable = BTS.inputStream?.available()!!
                    val bytesAvailable = mSocket?.inputStream?.available()!!

                    if (bytesAvailable > 0) {
                        val packetBytes = ByteArray(bytesAvailable)
//                        BTS.inputStream?.read(packetBytes)
                        mSocket?.inputStream?.read(packetBytes)

                        for (i in 0 until bytesAvailable) {
                            val byte: Byte = packetBytes[i]

                            if (byte == delimiter) {
                                val encodedBytes = ByteArray(bufferPosition)
                                System.arraycopy(buffer, 0, encodedBytes, 0, encodedBytes.size)

                                val readString = String(encodedBytes, US_ASCII)
                                bufferPosition = 0

                                val readMsg = handler.obtainMessage(Constants.MESSAGE_READ, readString)
                                readMsg.sendToTarget()
                            } else {
                                buffer[bufferPosition++] = byte
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                Log.d("Debug", "Input stream was disconnected", e)
                val errorMsg = handler.obtainMessage(Constants.MESSAGE_TOAST, "Input stream was disconnected")
                errorMsg.sendToTarget()
            } catch (e: UnsupportedEncodingException) {
                Log.d("Debug", "Unsupported encoding format", e)
                val errorMsg = handler.obtainMessage(Constants.MESSAGE_TOAST, "Unsupported encoding format")
                errorMsg.sendToTarget()
            } catch (e: KotlinNullPointerException) {
                val errorMsg = handler.obtainMessage(Constants.MESSAGE_TOAST, "Kotlin null pointer exception")
                errorMsg.sendToTarget()
                e.printStackTrace()
            }


//            var numBytes: Int
//            numBytes = try {
//                BTSocket.inputStream.read(buffer)
//            } catch (e: IOException) {
//                Log.d("Debug", "Input stream was disconnected", e)
//                break
//            }
//
//            val readMsg: Message = handler.obtainMessage(Constants.MESSAGE_READ, numBytes, -1, buffer)
//            readMsg.sendToTarget()
        }

        fun write(byte: Int) {
            try {
//                outputStream.write(bytes)
//                BTSocket.outputStream?.write(1)


//                mSocket?.outputStream?.write(bytes)
                mSocket?.outputStream?.write(byte)
                val readMsg = handler.obtainMessage(Constants.MESSAGE_WRITE, byte)
                readMsg.sendToTarget()
            } catch (e: IOException) {
                Log.e("Debug", "Error occurred when sending data", e)

                val errorMessage = handler.obtainMessage(Constants.MESSAGE_TOAST, e.toString())
                errorMessage.sendToTarget()
            } catch (e: UninitializedPropertyAccessException) {
                e.printStackTrace()
                val errorMessage = handler.obtainMessage(Constants.MESSAGE_TOAST, "블루투스를 연결해 주세요")
                errorMessage.sendToTarget()
            } catch (e: KotlinNullPointerException) {
                val errorMsg = handler.obtainMessage(Constants.MESSAGE_TOAST, "Kotlin null pointer exception")
                errorMsg.sendToTarget()
                e.printStackTrace()
            }
        }
    }

    fun isConnected(): Boolean {
        return if (mSocket == null) {
            false
        } else {
            mSocket!!.isConnected
        }
    }
}
