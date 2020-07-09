package com.hun.motorcontroller.dialog

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.hun.motorcontroller.R
import java.lang.IllegalStateException

class MotorSettingDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { fragmentActivity ->
            val builder = AlertDialog.Builder(fragmentActivity)
            val inflater = requireActivity().layoutInflater
            val view: View = inflater.inflate(R.layout.layout_motor_number_dialog, null)
            val motorCountEditText = view.findViewById<EditText>(R.id.edit_motor_count)

            builder.setView(view)
                .setTitle("모터 개수 입력")
                .setPositiveButton("확인") { _, _ ->
                    val count: String = motorCountEditText.text.toString()
                    showNameSetDialog(fragmentActivity, count.toInt())
                }
                .setNegativeButton("취소") { _, _ -> }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun showNameSetDialog(fragmentActivity: FragmentActivity, count: Int) {
        MotorNameDialogFragment().apply {
            val args = Bundle()
            args.putInt("count", count)
            this.arguments = args
            this.show(fragmentActivity.supportFragmentManager, "missiles")
        }
    }
}