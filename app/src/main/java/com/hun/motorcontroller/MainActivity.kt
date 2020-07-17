package com.hun.motorcontroller

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.GridLayoutManager
import com.hun.motorcontroller.data.BTSocket
import com.hun.motorcontroller.data.Motor
import com.hun.motorcontroller.dialog.BluetoothDialogFragment
import com.hun.motorcontroller.dialog.MotorSettingDialogFragment
import com.hun.motorcontroller.recycler_adapter.MotorRecyclerAdapter
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val motorAdapter: MotorRecyclerAdapter = MotorRecyclerAdapter()
    private val motorRealm: Realm = Realm.getDefaultInstance()
    private var readDisposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar_main)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        recycler_motor_buttons.adapter = motorAdapter
//        recycler_motor_buttons.layoutManager = GridLayoutManager(this, 2)

        motorAdapter.motors = motorRealm.where(Motor::class.java).findAll()
        motorRealm.addChangeListener { motorAdapter.notifyDataSetChanged() }

        if (motorAdapter.motors.isEmpty()) {
            val motorSettingDialog = MotorSettingDialogFragment()
            motorSettingDialog.show(supportFragmentManager, "missiles")
        }

        switch_auto.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked) {

            } else {

            }
        }

        motorAdapter.setOnItemClickListener(object : MotorRecyclerAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                if (BTSocket.socket != null) {
                    val handler = Handler()
                    val bluetoothService = BluetoothService(handler)
//                    bluetoothService.IOThread(BTSocket.socket!!).start()

                    val testMsg = "test message"
                    bluetoothService.IOThread(BTSocket.socket!!).write(testMsg.toByteArray())
//                    bluetoothService.write(testMsg.toByteArray())
                }
            }
        })

        val handler = Handler {
            when (it.what) {
                Constants.MESSAGE_READ -> text_message.text = it.obj.toString()
            }
            true
        }

        button_test.setOnClickListener {
            if (BTSocket.socket != null) {
//                val handler = Handler()
//                val msg = handler.obtainMessage(Constants.MESSAGE_READ, "Jeon")
//                msg.sendToTarget()
                val bluetoothService = BluetoothService(handler)
                bluetoothService.IOThread(BTSocket.socket!!).start()
            }
        }
    }

    private fun clearMotorList() {
        Realm.getDefaultInstance().use {
            val results = it.where(Motor::class.java).findAll()

            it.executeTransaction {
                results.deleteAllFromRealm()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_bluetooth_setting -> {
                val bluetoothDialog = BluetoothDialogFragment()
                bluetoothDialog.show(supportFragmentManager, "missiles")
                true
            }

            R.id.action_motor_preference -> {
                clearMotorList()
                val motorSettingDialog = MotorSettingDialogFragment()
                motorSettingDialog.show(supportFragmentManager, "missiles")
                true
            }

            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            Constants.REQUEST_ENABLE_BLUETOOTH -> {
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth 설정에 성공했습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Bluetooth 설정에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        motorRealm.close()
        BTSocket.socket?.close()
    }
}
