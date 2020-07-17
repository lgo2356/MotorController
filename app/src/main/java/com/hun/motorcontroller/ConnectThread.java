package com.hun.motorcontroller;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.hun.motorcontroller.data.BTSocket;

import java.lang.reflect.Method;
import java.util.UUID;

public class ConnectThread extends Thread {

    private final BluetoothDevice mDevice;
    private final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public ConnectThread(BluetoothDevice device) {
        mDevice = device;
    }

    @Override
    public void run() {
        try {
            Method createMethod = mDevice.getClass()
                    .getMethod("createInsecureRfcommSocket", new Class[]{int.class});
            BluetoothSocket mSocket = (BluetoothSocket) createMethod.invoke(mDevice, 1);

            if (mSocket != null) {
                mSocket.connect();
                BTSocket.Companion.setSocket(mSocket);
            } else {
                Log.e("Debug", "Couldn't connect to your device.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
