package com.hun.motorcontroller.recycler_adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.ToggleButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hun.motorcontroller.R
import com.hun.motorcontroller.data.Motor
import io.realm.Realm

class MotorRecyclerAdapter(val checkedArray: Array<Boolean>) :
    RecyclerView.Adapter<MotorRecyclerAdapter.ViewHolder>() {

    var motors: List<Motor> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private val buttons: Array<Button?> = arrayOfNulls(16)
    private val toggles: Array<ToggleButton?> = arrayOfNulls(16)

    private var touchListener: OnItemTouchListener? = null
    private var buttonTouchListener: OnButtonTouchListener? = null
    private var toggleClickListener: OnToggleClickListener? = null
    private var iconClickListener: OnIconClickListener? = null

    interface OnItemTouchListener {
        fun onItemTouchActionDown(view: View, motionEvent: MotionEvent, position: Int)
        fun onItemTouchActionUp(view: View, motionEvent: MotionEvent, position: Int)
    }

    interface OnButtonTouchListener {
        fun onButtonTouchActionDown(view: View, motionEvent: MotionEvent, position: Int)
        fun onButtonTouchActionUp(view: View, motionEvent: MotionEvent, position: Int)
        fun onButtonTouchActionCancel(view: View, motionEvent: MotionEvent, position: Int)
    }

    interface OnToggleClickListener {
        fun onToggleClick(view: View, position: Int, isChecked: Boolean) {

        }
    }

    interface OnIconClickListener {
        fun onIconClick(view: View, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.layout_motor_list_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        buttons[position] = holder.buttonSendData
        toggles[position] = holder.toggleSendData

        holder.textMotorName.text = motors[position].name
        holder.toggleSendData.isChecked = checkedArray[position]

        holder.imageBin.setOnClickListener {
            deleteMotor(position)
        }

        holder.imageRename.setOnClickListener {
            iconClickListener?.onIconClick(it, position)
        }

        holder.buttonSendData.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (!holder.toggleSendData.isChecked) {
                        buttonTouchListener?.onButtonTouchActionDown(view, motionEvent, position)
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if (!holder.toggleSendData.isChecked) {
                        buttonTouchListener?.onButtonTouchActionUp(view, motionEvent, position)
                    }
                }

                MotionEvent.ACTION_CANCEL -> {
                    if (!holder.toggleSendData.isChecked) {
                        buttonTouchListener?.onButtonTouchActionCancel(view, motionEvent, position)
                    }
                }
            }
            false
        }

        holder.toggleSendData.setOnClickListener {
            toggleClickListener?.onToggleClick(it, position, holder.toggleSendData.isChecked)
        }
    }

    override fun getItemCount(): Int {
        return motors.size
    }

    fun getButton(position: Int): Button? {
        if (buttons[position] == null) {
            return null
        }

        return buttons[position]
    }

    fun getToggleButton(position: Int): ToggleButton? {
        if (toggles[position] == null) {
            return null
        }

        return toggles[position]
    }

    private fun deleteMotor(position: Int) {
        Realm.getDefaultInstance().use {
            val foundMotor: Motor? = it.where(Motor::class.java).findAll()[position]

            it.executeTransaction {
                foundMotor?.deleteFromRealm()
            }
        }
    }

    fun setOnItemTouchListener(listener: OnItemTouchListener) {
        this.touchListener = listener
    }

    fun setOnButtonTouchListener(listener: OnButtonTouchListener) {
        this.buttonTouchListener = listener
    }

    fun setOnToggleClickListener(listener: OnToggleClickListener) {
        this.toggleClickListener = listener
    }

    fun setOnIconClickListener(listener: OnIconClickListener) {
        this.iconClickListener = listener
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textMotorName: TextView = itemView.findViewById(R.id.text_motor_name)
        val buttonSendData: Button = itemView.findViewById(R.id.button_send_data)
        val toggleSendData: ToggleButton = itemView.findViewById(R.id.toggle_send_data)
        val imageBin: ImageView = itemView.findViewById(R.id.image_bin)
        val imageRename: ImageView = itemView.findViewById(R.id.image_rename)
    }
}
