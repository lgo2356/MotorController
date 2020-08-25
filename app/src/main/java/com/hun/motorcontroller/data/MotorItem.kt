package com.hun.motorcontroller.data

import android.widget.Button
import android.widget.ToggleButton

data class MotorItem(
    var name: String,
    var buttonEnabled: Boolean,
    var toggleButtonEnabled: Boolean,
    var toggleButtonChecked: Boolean
)
