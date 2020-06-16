package com.hun.motorcontroller

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hun.motorcontroller.recycler_adapter.BTDialogRecyclerAdapter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.internal.operators.observable.ObservableCreate
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.lang.IllegalStateException
import kotlin.collections.ArrayList

class BluetoothDialogFragment : DialogFragment() {

    private val btPairedDevices: ArrayList<BTDialogRecyclerAdapter.Devices> = ArrayList()
    private val btDiscoveredDevices: ArrayList<BTDialogRecyclerAdapter.Devices> = ArrayList()
    private val observableList: ObservableList<String> = ObservableList()
    private val btDialogPairedAdapter = BTDialogRecyclerAdapter(btPairedDevices)
    private val btDialogDiscoveredAdapter = BTDialogRecyclerAdapter(btDiscoveredDevices)
    private var bluetoothController: BluetoothController? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view: View = inflater.inflate(R.layout.layout_bluetooth_dialog, null)
            val pairedDevicesRecycler = view.findViewById<RecyclerView>(R.id.recycler_paired_devices)
            val discoveredDevicesRecycler = view.findViewById<RecyclerView>(R.id.recycler_discovered_devices)

            val deviceScanObservable = Observable.fromIterable(btDiscoveredDevices)

            pairedDevicesRecycler.adapter = btDialogPairedAdapter
            pairedDevicesRecycler.layoutManager = LinearLayoutManager(context)
            discoveredDevicesRecycler.adapter = btDialogDiscoveredAdapter
            discoveredDevicesRecycler.layoutManager = LinearLayoutManager(context)

            // Get paired devices ...
            btDialogPairedAdapter.addItem("Paired 01", "Paired")
            btDialogPairedAdapter.addItem("Paired 01", "Paired")
            btDialogPairedAdapter.addItem("Paired 01", "Paired")
            btDialogPairedAdapter.addItem("Paired 01", "Paired")
            btDialogDiscoveredAdapter.addItem("Discovered 01", "Discovered")
            btDialogDiscoveredAdapter.addItem("Discovered 02", "Discovered")
            btDialogDiscoveredAdapter.addItem("Discovered 03", "Discovered")
            btDialogDiscoveredAdapter.addItem("Discovered 04", "Discovered")

            // Start bluetooth devices scan -> discovered -> list up
//            bluetoothController = BluetoothController(activity!!)
//            bluetoothController!!.scanBluetoothDevices(btDialogPairedAdapter)

            // 생성자 = scan, 소비자 = adapter
            deviceScanObservable
                .subscribe { device ->
                    Log.d("Test", "Device name: ${device.devicesName}")
                }

            observableList.getObservable()
                .subscribe { str ->
                    Log.d("Test", str)
                }
            observableList.add("DD 01")

            // Paired recycler 항목 이벤트 리스너 객체 생성
            btDialogPairedAdapter.setOnItemClickListener(object : BTDialogRecyclerAdapter.OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    Toast.makeText(context, "Clicked", Toast.LENGTH_SHORT).show()
                    btDialogPairedAdapter.addItem("Device 02", "Paired")
                }
            })

            // Discovered recycler 항목 이벤트 리스터 객체 생성
            btDialogDiscoveredAdapter.setOnItemClickListener(object : BTDialogRecyclerAdapter.OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    Toast.makeText(context, "Position: $position", Toast.LENGTH_SHORT).show()
                    btDialogDiscoveredAdapter.addItem("Device 02", "Discovered")
                }
            })

            builder.setView(view)
                .setMessage("블루투스 설정")
                .setNegativeButton("취소") { _, _ -> }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    class ObservableList<T> {

        private val list: ArrayList<T> = ArrayList()
        private var onAdd: PublishSubject<T> = PublishSubject.create()

        fun add(value: T) {
            list.add(value)
            onAdd.onNext(value)
        }

        fun getObservable(): Observable<T> {
            return onAdd
        }
    }
}
