package com.hun.motorcontroller

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.hun.motorcontroller.data.Motor
import com.hun.motorcontroller.dialog.BluetoothDialogFragment
import com.hun.motorcontroller.dialog.MotorSettingDialogFragment
import com.hun.motorcontroller.recycler_adapter.MotorRecyclerAdapter
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val motorAdapter: MotorRecyclerAdapter = MotorRecyclerAdapter()
    private val motorRealm: Realm = Realm.getDefaultInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycler_motor_buttons.adapter = motorAdapter
        recycler_motor_buttons.layoutManager = GridLayoutManager(this, 2)

        motorAdapter.motors = motorRealm.where(Motor::class.java).findAll()
        motorRealm.addChangeListener { motorAdapter.notifyDataSetChanged() }

        if (motorAdapter.motors.isEmpty()) {
            val motorSettingDialog = MotorSettingDialogFragment()
            motorSettingDialog.show(supportFragmentManager, "missiles")
        }

        button_bluetooth_setting.setOnClickListener {
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

    override fun onDestroy() {
        super.onDestroy()

        motorRealm.close()
    }
}
