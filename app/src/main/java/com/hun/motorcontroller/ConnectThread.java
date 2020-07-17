package com.hun.motorcontroller;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.hun.motorcontroller.data.BTSocket;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

public class ConnectThread extends Thread {

    private final BluetoothDevice mDevice;
    private final BluetoothSocket mSocket;
    private final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public ConnectThread(BluetoothDevice device) {
        mDevice = device;
        BluetoothSocket tmp = null;

        try {
            Method createMethod = device.getClass()
                    .getMethod("createInsecureRfcommSocket", new Class[]{int.class});
            tmp = (BluetoothSocket) createMethod.invoke(mDevice, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mSocket = tmp;
    }

    @Override
    public void run() {
        try {
            mSocket.connect();
            BTSocket.Companion.setSocket(mSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
