package com.hun.motorcontroller.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.hardware.input.InputManager
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hun.motorcontroller.MainActivity
import com.hun.motorcontroller.R
import com.hun.motorcontroller.data.Motor
import com.hun.motorcontroller.recycler_adapter.MotorNameRecyclerAdapter
import io.realm.Realm

class MotorNameDialogFragment : DialogFragment() {

    private val motors = ArrayList<Motor>()
    private val motorNameRecyclerAdapter = MotorNameRecyclerAdapter(motors)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val layout: View = inflater.inflate(R.layout.layout_motor_name_dialog, null)
        val motorNameRecycler: RecyclerView = layout.findViewById(R.id.recycler_motor_name)

        motorNameRecycler.adapter = motorNameRecyclerAdapter
        motorNameRecycler.layoutManager = LinearLayoutManager(context)

        val count: Int = arguments?.getInt("count") ?: 0
        for (i in 0 until count) motorNameRecyclerAdapter.addItem("")

        val builder = AlertDialog.Builder(activity)
        builder
            .setView(layout)
            .setTitle("모터 이름 설정")
            .setPositiveButton("확인") { _, _ ->
                for (i in 0 until count) {
                    val motorName = motorNameRecyclerAdapter.getName(i)
                    setMotorName(motorName)
                }
            }
            .setNegativeButton("취소") { _, _ -> }

        return builder.create()
    }

    private fun setMotorName(name: String) {
        Realm.getDefaultInstance().use {
            it.executeTransaction { realm ->
                val motor = Motor()
                val maxId: Long? = realm.where(Motor::class.java).max("id") as Long?
                motor.id = maxId?.plus(1) ?: 0

                if (name.isNotEmpty()) motor.name = name
                else motor.name = "Motor"

                realm.copyToRealm(motor)
            }
        }
    }
}
