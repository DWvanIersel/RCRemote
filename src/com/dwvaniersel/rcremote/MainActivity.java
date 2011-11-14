package com.dwvaniersel.rcremote;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {
	
	private final String TAG = "MainActivity";
	private final String DEVICENAME = "PFWSNXT";
	
	
	// BT variables
	private BluetoothAdapter btInterface;
	private Set<BluetoothDevice> pairedDevices;
	private BluetoothSocket socket;
	InputStream is = null;
	OutputStream os = null;
	boolean bConnected = false;
	// End BT variables
	
	// Broadcast receiver to handle bt events
	BroadcastReceiver btMonitor = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //force landscape orientation
		setContentView(R.layout.main);
		
		
	}
	
	private void setupBtMonitor() {
		btMonitor = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals("android.bluetooth.device.action.ACL_CONNECTED")) {
					handleConnected();
				}
				if (intent.getAction().equals("android.bluetooth.device.action.ACL_DISCONNECTED")) {
					handleDisconnected();
				}
			}
			
		};
	}
	
	private void handleConnected() {
		try {
			is = socket.getInputStream();
			os = socket.getOutputStream();
		} catch (Exception e) {
			is = null;
			os = null;
			disconnectFromDevice();
		}
	}
	
	private void handleDisconnected() {
		//TODO create method
	}
	
	public void findDevice() {
		try {
			btInterface = BluetoothAdapter.getDefaultAdapter();
			pairedDevices = btInterface.getBondedDevices();
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
	
	private void connectToDevice(BluetoothDevice bd) {
		try {
			socket = bd.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
			socket.connect();
		} catch (Exception e) {
			Log.e(TAG, "Failed in connectToDevice() " + e.getMessage());
		}
	}
	
	private void disconnectFromDevice() {
		//TODO create method
	}
}
