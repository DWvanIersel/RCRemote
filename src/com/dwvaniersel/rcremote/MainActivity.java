package com.dwvaniersel.rcremote;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {
	
	private final String TAG = "MainActivity";
	private final String DEVICENAME = "PFWSNXT";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //force landscape orientation
		setContentView(R.layout.main);
		
		
	}
	
	public void findDevice() {
		try {
			BluetoothAdapter btInterface = BluetoothAdapter.getDefaultAdapter();
			Set<BluetoothDevice> pairedDevices = btInterface.getBondedDevices();
			Iterator<BluetoothDevice> it = pairedDevices.iterator();
			while (it.hasNext()) {
				BluetoothDevice bd = it.next();
				if (bd.getName().equalsIgnoreCase(DEVICENAME)) {
					connectToDevice(bd);
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "Failed in findDevice() " + e.getMessage());
		}
	}
	
	public void connectToDevice(BluetoothDevice bd) {
		try {
			BluetoothSocket socket = bd.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
			socket.connect();
		} catch (Exception e) {
			Log.e(TAG, "Failed in connectToDevice() " + e.getMessage());
		}
	}
}