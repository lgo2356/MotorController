package com.hun.motorcontroller.recycler_adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hun.motorcontroller.R
import kotlinx.android.synthetic.main.layout_bluetooth_device_item.view.*

class BTDialogRecyclerAdapter(private val items: ArrayList<Devices>) :
    RecyclerView.Adapter<BTDialogRecyclerAdapter.ViewHolder>() {

    private var listener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.layout_bluetooth_device_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.deviceName.text = items[position].devicesName
        holder.deviceAddress.text = items[position].deviceAddress

        holder.itemView.setOnClickListener {
            listener?.onItemClick(it, position)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.textview_device_name
        val deviceAddress: TextView = itemView.textview_device_address
    }

    data class Devices(val devicesName: String, val deviceAddress: String)

    fun addItem(name: String, address: String) {
        val item = Devices(name, address)
        items.add(item)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }
}
