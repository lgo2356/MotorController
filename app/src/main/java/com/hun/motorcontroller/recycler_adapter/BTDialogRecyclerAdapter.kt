package com.hun.motorcontroller.recycler_adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hun.motorcontroller.R
import com.hun.motorcontroller.data.Device
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.layout_bluetooth_device_item.view.*

class BTDialogRecyclerAdapter(private val items: ArrayList<Device>) :
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
        holder.deviceName.text = items[position].deviceName
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

    fun addItem(name: String, address: String) {
        val item = Device(name, address)
        items.add(item)
        notifyDataSetChanged()
    }

    fun removeItem(address: String) {
        for (item in items) {
            if (address == item.deviceAddress) {
                items.remove(item)
            }
        }
    }

    fun getItems(): List<Device> {
        return this.items
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }
}
