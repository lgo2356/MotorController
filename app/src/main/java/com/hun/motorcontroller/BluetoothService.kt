package com.hun.motorcontroller

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import com.hun.motorcontroller.data.BTSocket
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.lang.Exception
import java.nio.charset.StandardCharsets.US_ASCII

class BluetoothService {

    private inner class ConnectThread(private val device: BluetoothDevice) : Thread() {

        override fun run() {
            try {
                val createMethod = device.javaClass
                    .getMethod(
                        "createInsecureRfcommSocket",
                        *arrayOf<Class<*>?>(Int::class.javaPrimitiveType)
                    )
                val socket = createMethod.invoke(device, 1) as BluetoothSocket
                socket.connect()

                BTSocket.socket = socket
                BTSocket.inputStream = socket.inputStream
                BTSocket.outputStream = socket.outputStream
            } catch (e: Exception) {
                Log.e("Debug", "Couldn't connect to your device")
                e.printStackTrace()
            }
        }
    }

    inner class IOThread(private val handler: Handler) : Thread() {

        private val buffer: ByteArray = ByteArray(1024)
        private var bufferPosition = 0
        private val delimiter: Byte = 0x0A

        override fun run() {
//            val b: Byte = 0x0A
//            val msg = handler.obtainMessage(Constants.MESSAGE_READ, b)
//            msg.sendToTarget()

            try {
                while (true) {
                    val bytesAvailable = BTSocket.inputStream?.available()!!

                    if (bytesAvailable > 0) {
                        val packetBytes = ByteArray(bytesAvailable)
                        BTSocket.inputStream?.read(packetBytes)

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
            } catch (e: UnsupportedEncodingException) {
                Log.d("Debug", "Unsupported encoding format", e)
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

        fun write(bytes: ByteArray) {
            try {
//                outputStream.write(bytes)
                val isConnected: Boolean
                BTSocket.outputStream?.write(1)

                val readMsg = handler.obtainMessage(Constants.MESSAGE_READ, bytes)
                readMsg.sendToTarget()
            } catch (e: IOException) {
                Log.e("Debug", "Error occurred when sending data", e)

//                val writeErrorMsg = handler.obtainMessage(Constants.MESSAGE_TOAST)
//                val bundle = Bundle().apply {
//                    putString("toast", "Couldn't send data to the other device.")
//                }
//                writeErrorMsg.data = bundle
//                handler.sendMessage(writeErrorMsg)

                val errorMessage = handler.obtainMessage(Constants.MESSAGE_TOAST, "데이터 전송에 실패했습니다")
                errorMessage.sendToTarget()
            } catch (e: UninitializedPropertyAccessException) {
                e.printStackTrace()
                val errorMessage = handler.obtainMessage(Constants.MESSAGE_TOAST, "블루투스를 연결해 주세요")
                errorMessage.sendToTarget()
            }
        }
    }
}
