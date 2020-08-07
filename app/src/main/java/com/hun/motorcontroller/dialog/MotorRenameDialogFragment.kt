package com.hun.motorcontroller.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.hun.motorcontroller.R
import com.hun.motorcontroller.data.Motor
import io.realm.Realm

class MotorRenameDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val layout: View = inflater.inflate(R.layout.layout_motor_rename_dialog, null)
        val editText: EditText = layout.findViewById(R.id.edit_rename)

        val position: Int = arguments?.getInt("position") ?: -1

        val builder = AlertDialog.Builder(activity).apply {
            this
                .setView(layout)
                .setTitle("모터 이름 설정")
                .setPositiveButton("확인") { _, _ ->
                    renameMotor(position, editText.text.toString())
                }
                .setNegativeButton("취소") { _, _ -> }
        }

        return builder.create()
    }

    private fun renameMotor(position: Int, name: String) {
        if (position != -1) {
            Realm.getDefaultInstance().use {
                val foundMotor: Motor? = it.where(Motor::class.java).findAll()[position]

                it.executeTransaction {
                    foundMotor?.name = name
                }
            }
        } else {
            Log.d("Debug", "Invalid position error")
        }
    }
}