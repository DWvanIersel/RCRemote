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
	
	// Floats for controlling the car
	float mConvertedPitch = 0.0f;
	float mLastTurnRate = 0.0f;
	float mLastSpeed = 0.0f;
	// End floats
	
	// Bt variables
	private BluetoothAdapter mBtAdapter;
	private BluetoothSocket mBtSocket;
	private BluetoothDevice mBtDevice;
	DataOutputStream mOs = null;
	BroadcastReceiver mBtMonitor = null;
	boolean mBConnected = false;
	// End bt variables
	
	// Command variables
	public static final byte COMMAND_TRAVEL    = 1 << 0; // Followed by speed:     positive = forward, negative = backward
    public static final byte COMMAND_STEER     = 1 << 1; // Followed by turnRate:  positive = right,   negative = left
	// End command variables
	
	public Car() {
	}
	
	public void setupBtMonitor() {
		Log.i(TAG, "BTMonitor started");
		mBtMonitor = new BroadcastReceiver() {
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
			mOs = new DataOutputStream(mBtSocket.getOutputStream());
			mBConnected = true;
		} catch (Exception e) {
			mOs = null;
		}
	}
	
	private void handleDisconnected() {
		disconnect();
	}
	
	public void connect() {
		try {
			mBtAdapter = BluetoothAdapter.getDefaultAdapter();
			Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
			for (BluetoothDevice someDevice : pairedDevices) {
				if (someDevice.getName().equalsIgnoreCase(DEVICENAME)) {
					mBtDevice = someDevice;
					break;
				}
			}
			if (mBtDevice == null) {
				Log.i(TAG, "Could not find the car.");
			}
			else {
				connectToCar();
			}
		} catch (Exception e) {
			Log.e(TAG, "Failed in connect() (" + e.getMessage() + ")");
		}
	}
	
	private void connectToCar() {
		try {
			mBtSocket = mBtDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
			mBtSocket.connect();
		} catch (Exception e) {
			Log.e(TAG, "Failed in connecting to the car (" + e.getMessage() + ")");
		}
	}
	
	public void disconnect() {
		try {
			mBtSocket.close();
			mBtDevice = null;
			mBConnected = false;
		} catch (Exception e) {
			Log.e(TAG, "Failed in disconnecting from the car (" + e.getMessage() + ")");
		}
	}
	
	public boolean getConnectionState() {
		return mBConnected;
	}
	
	public void drive(float roll) {
		float speed = ((roll/90.0f)*50.0f)+25.0f;
		float speedDiff = Math.abs(speed - mLastSpeed);
		
		if (speedDiff > 1.0f) {
			travel(speed);
			mLastSpeed = speed;
		}
	}
	
	public void turn(float pitch) {
		if (pitch < -90) {
			mConvertedPitch = -180 - pitch;
		}
		else if (pitch > 90) {
			mConvertedPitch = 180 - pitch;
		}
		else {
			mConvertedPitch = pitch;
		}
		
		float turnRate = (mConvertedPitch/90.0f)*150.0f;
		float turnRateDiff = Math.abs(turnRate - mLastTurnRate);
		
		if (turnRateDiff > 2.0f) {
			steer(turnRate);
			mLastTurnRate = turnRate;
		}
	}
	
	public void travel(float speed) {
		Log.i(TAG, "Travel called: " + speed);
		try {
			mOs.writeByte(COMMAND_TRAVEL);
			mOs.writeFloat(speed);
			mOs.flush();
		} catch (Exception e) {
			Log.e(TAG, "Could not send commands (" + e.getMessage() + ")");
		}
	}
	
	public void steer(float turnRate) {
		Log.i(TAG, "Steer called: " + turnRate);
		try {
			mOs.writeByte(COMMAND_STEER);
			mOs.writeFloat(turnRate);
			mOs.flush();
		} catch (Exception e) {
			Log.e(TAG, "Could not send commands (" + e.getMessage() + ")");
		}
	}
}
