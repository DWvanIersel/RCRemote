package com.dwvaniersel.rcremote;

import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.widget.Toast;

public class Car {
	public Car() {
		
	}
	
	public void connect(BluetoothDevice btDevice) {
		try {
			btSocket = btDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
			btSocket.connect();
			Toast.makeText(this, "Connected to " + DEVICENAME, Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Log.e(TAG, "Failed in connectToDevice() " + e.getMessage());
			Toast.makeText(this, "Connecting to the NXT bot failed", Toast.LENGTH_LONG).show();
		}
	}
	
	public void disconnect() {
		
	}
	
	public void travel() {
		
	}
	
	public void steer() {
		
	}
	
	public void stop() {
		
	}
}
