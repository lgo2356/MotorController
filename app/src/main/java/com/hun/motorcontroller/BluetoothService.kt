package com.hun.motorcontroller

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import java.lang.Exception
import java.nio.charset.StandardCharsets.US_ASCII
import java.util.*

class BluetoothService(private val handler: Handler) {

    private val insecureUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private var mSocket: BluetoothSocket? = null
    private var mOutputStream: OutputStream? = null
    private var mInputStream: InputStream? = null

    private val readJob = Job()
    private val writeByteJob = Job()
    private val writeBytesJob = Job()
    private val readScope = CoroutineScope(Dispatchers.Main + readJob)
    private val writeByteScope = CoroutineScope(Dispatchers.Main + writeByteJob)
    private val writeBytesScope = CoroutineScope(Dispatchers.IO + writeBytesJob)

    fun connect(device: BluetoothDevice) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                mSocket = device.createInsecureRfcommSocketToServiceRecord(insecureUuid)

                if (mSocket != null) {
                    mSocket?.connect()
                    mOutputStream = mSocket?.outputStream
                    mInputStream = mSocket?.inputStream

                    handler.sendEmptyMessage(Constants.MESSAGE_CONNECTED)
                } else {
                    sendErrorMessage("블루투스 연결에 실패했습니다")
                }
            } catch (e: IOException) {
                Log.d("Debug", "Couldn't connect to your device", e)
                sendErrorMessage("블루투스 연결에 실패했습니다")
            }
        }
    }

    fun writeBytes(bytes: ByteArray) {
//        writeBytesScope.launch(Dispatchers.IO) {
//            try {
//                mOutputStream?.write(bytes) ?: sendErrorMessage("패킷 전송에 실패했습니다")
//            } catch (e: IOException) {
//                Log.d("Debug", "패킷 전송에 실패했습니다")
//                writeBytesJob.cancel()
//                throw IOException()
//            }
//        }
        try {
            mOutputStream?.write(bytes) ?: sendErrorMessage("패킷 전송에 실패했습니다")
        } catch (e: IOException) {
            Log.d("Debug", "패킷 전송에 실패했습니다")
            val msg = handler.obtainMessage(Constants.MESSAGE_ERROR, "데이터 전송에 실패했습니다")
            msg.sendToTarget()
            throw IOException()
        }
    }

//    fun writeByte(byte: Int) {
//        writeByteScope.launch(Dispatchers.IO) {
//            try {
//                mOutputStream?.write(byte) ?: sendErrorMessage("패킷 전송에 실패했습니다")
//
//                val writeMsg = handler.obtainMessage(Constants.MESSAGE_WRITE, byte)
//                writeMsg.sendToTarget()
//                Log.d("Debug", "데이타 전송에 성공했습니다")
//            } catch (e: IOException) {
//                Log.d("Debug", "데이타 전송에 실패했습니다")
//                writeByteJob.cancel()
//                val errorMessage = handler.obtainMessage(Constants.MESSAGE_TOAST, "패킷 전송에 실패했습니다")
//                errorMessage.sendToTarget()
//
//                throw IOException()
//            }
//        }
//    }

    fun startRead() {
        readScope.launch(Dispatchers.IO) {
            val buffer = ByteArray(1024)
            var bufferPosition = 0
            val delimiter: Byte = 0x0A

            try {
                while (mSocket != null) {
                    var bytesAvailable = 0

                    if (mSocket != null && mSocket?.isConnected == true) {
                        bytesAvailable = mInputStream?.available() ?: 0
                    }

                    if (bytesAvailable > 0) {
                        val packetBytes = ByteArray(bytesAvailable)
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
                readJob.cancel()
                close()
            } catch (e: UnsupportedEncodingException) {
                Log.d("Debug", "Unsupported encoding format", e)
                val errorMsg = handler.obtainMessage(Constants.MESSAGE_TOAST, "Unsupported encoding format")
                errorMsg.sendToTarget()
            } catch (e: KotlinNullPointerException) {
                val errorMsg = handler.obtainMessage(Constants.MESSAGE_TOAST, "Kotlin null pointer exception")
                errorMsg.sendToTarget()
                e.printStackTrace()
            }
        }
    }

    fun close() {
        try {
            writeByteJob.cancel()
            mOutputStream?.close()
            mOutputStream = null
            Log.d("Debug", "Output stream closed")

            readJob.cancel()
            mInputStream?.close()
            mInputStream = null
            Log.d("Debug", "Input stream closed")

            mSocket?.close()
            mSocket = null
            Log.d("Debug", "Socket closed")
        } catch (e: IOException) {
            Log.d("Debug", "Couldn't close the client socket", e)
            throw IOException("Couldn't close the client socket")
        }
    }

    private fun sendErrorMessage(msg: String) {
        val errorMsg = handler.obtainMessage(Constants.MESSAGE_ERROR, msg)
        errorMsg.sendToTarget()
    }

    fun isConnected(): Boolean {
        return if (mSocket == null) {
            false
        } else {
            mSocket?.isConnected ?: false
        }
    }
}
