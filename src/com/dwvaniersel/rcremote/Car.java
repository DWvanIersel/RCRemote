package com.dwvaniersel.rcremote;

import java.io.DataOutputStream;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Car {
	
	final static String TAG = "Car";
	final static String DEVICENAME = "PFWSNXT";
	
	// Bt variables
	private BluetoothAdapter btAdapter;
	private BluetoothSocket btSocket;
	private BluetoothDevice btDevice;
	DataOutputStream os = null;
	BroadcastReceiver btMonitor = null;
	boolean bConnected = false;
	// End bt variables
	
	// Command variables
	public static final byte COMMAND_SETSPEED	= 1 << 0; // Followed by speed: positive = forward, negative = backward
	public static final byte COMMAND_TRAVEL		= 1 << 1;
	public static final byte COMMAND_STEER		= 1 << 2; // Followed by turnRate: positive = right, negative = left
	public static final byte COMMAND_STOP		= 1 << 3;
	// End command variables
	
	public Car() {
	}
	
	public void setupBtMonitor() {
		Log.i(TAG, "BTMonitor started");
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
			os = new DataOutputStream(btSocket.getOutputStream());
			bConnected = true;
		} catch (Exception e) {
			os = null;
		}
	}
	
	private void handleDisconnected() {
		disconnect();
	}
	
	public void connect() {
		try {
			btAdapter = BluetoothAdapter.getDefaultAdapter();
			Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
			for (BluetoothDevice someDevice : pairedDevices) {
				if (someDevice.getName().equalsIgnoreCase(DEVICENAME)) {
					btDevice = someDevice;
					break;
				}
			}
			if (btDevice == null) {
				Log.i(TAG, "Could not connect to the device.");
			}
			else {
				connectToCar();
			}
		} catch (Exception e) {
			Log.e(TAG, "Failed in findDevice() " + e.getMessage());
		}
	}
	
	private void connectToCar() {
		try {
			btSocket = btDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
			btSocket.connect();
		} catch (Exception e) {
			Log.e(TAG, "Failed in connectToDevice() " + e.getMessage());
		}
	}
	
	public void disconnect() {
		try {
			btSocket.close();
			btDevice = null;
			bConnected = false;
		} catch (Exception e) {
			Log.e(TAG, "Failed in disconnectFromDevice() " + e.getMessage());
		}
	}
	
	public void travel() {
		
	}
	
	public void steer() {
		
	}
	
	public void stop() {
		
	}
}
