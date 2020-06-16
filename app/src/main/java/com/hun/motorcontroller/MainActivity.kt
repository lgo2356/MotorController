package com.hun.motorcontroller

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.hun.motorcontroller.recycler_adapter.MotorRecyclerAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val motorItems: ArrayList<MotorRecyclerAdapter.Motors> = ArrayList()
    private val motorAdapter: MotorRecyclerAdapter = MotorRecyclerAdapter(motorItems)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycler_motor_buttons.adapter = motorAdapter
        recycler_motor_buttons.layoutManager = GridLayoutManager(this, 2)

        for (i in 0..20) {
            motorAdapter.addItem("Motor ${i + 1}")
        }

        button_bluetooth_setting.setOnClickListener {
//            BluetoothController(this).activateBluetooth()
            val bluetoothDialog = BluetoothDialogFragment()
            bluetoothDialog.show(supportFragmentManager, "missiles")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            Constants.REQUEST_ENABLE_BLUETOOTH -> {
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth 설정에 성공했습니다.",
                        Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Bluetooth 설정에 실패했습니다.",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
