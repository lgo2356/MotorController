package com.hun.motorcontroller

import java.util.*

class Constants {
    companion object {
        // Bluetooth
        const val REQUEST_ENABLE_BLUETOOTH = 1000
        const val REQUEST_PERMISSIONS = 2000

        // IO
        const val MESSAGE_READ: Int = 0
        const val MESSAGE_WRITE: Int = 1
        const val MESSAGE_TOAST: Int = 2

        val UUID_SERIAL_PORT: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
}
