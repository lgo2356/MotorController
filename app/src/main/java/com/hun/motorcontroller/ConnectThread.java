package com.hun.motorcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

public class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private final BluetoothAdapter mmAdapter;

    public ConnectThread(BluetoothDevice device, BluetoothAdapter adapter) {
        BluetoothSocket tmp = null;
        mmDevice = device;
        mmAdapter = adapter;

        try {
            final Method method = device.getClass().getMethod("createInsecureRfcommSocket", int.class);
            tmp = (BluetoothSocket) method.invoke(device, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mmSocket = tmp;
    }

    public void run() {
        mmAdapter.cancelDiscovery();

        try {
            mmSocket.connect();
        } catch (IOException e) {
            try {
                mmSocket.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            e.printStackTrace();
        }
    }
}
