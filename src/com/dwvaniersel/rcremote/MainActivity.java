package com.dwvaniersel.rcremote;

import java.io.DataOutputStream;
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
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
	
	private final String TAG = "MainActivity";
	private final String DEVICENAME = "PFWSNXT";
	
	// Layout variables
	Button mBtnConnect;
	Button mBtnDisconnect;
	Button mBtnSendCommands;
	// End layout variables
	
	// BT variables
	private BluetoothAdapter btAdapter;
	private BluetoothSocket btSocket;
	private BluetoothDevice btDevice;
	DataOutputStream os = null;
	boolean bConnected = false;
	// End BT variables
	
	// Command variables
	public static final byte COMMAND_SETSPEED	= 1 << 0; // Followed by speed: positive = forward, negative = backward
	public static final byte COMMAND_TRAVEL		= 1 << 1;
	public static final byte COMMAND_STEER		= 1 << 2; // Followed by turnRate: positive = right, negative = left
	public static final byte COMMAND_STOP		= 1 << 3;
	// End command variables
	
	// Broadcast receiver to handle bt events
	BroadcastReceiver btMonitor = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE); //force landscape orientation
		setContentView(R.layout.main);
		mBtnConnect = (Button) findViewById(R.id.btnConnect);
		mBtnDisconnect = (Button) findViewById(R.id.btnDisconnect);
		mBtnSendCommands = (Button) findViewById(R.id.btnSendCommands);
		
		setupBtMonitor();
				
	}
	
	private void setupBtMonitor() {
		Log.i(TAG, "BTMonitor wordt uitgevoerd");
		btMonitor = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals("android.bluetooth.device.action.ACL_CONNECTED")) {
					Log.i(TAG, "voor handleConnected()");
					handleConnected();
					Log.i(TAG, "na handleConnected()");
				}
				if (intent.getAction().equals("android.bluetooth.device.action.ACL_DISCONNECTED")) {
					handleDisconnected();
				}
			}
			
		};
	}
	
	private void handleConnected() {
		try {
			Log.i(TAG, "begin handleConnected()");
			os = new DataOutputStream(btSocket.getOutputStream());
			bConnected = true;
			Log.i(TAG, "na handleConnected()");
		} catch (Exception e) {
			os = null;
			disconnectFromDevice();
		}
	}
	
	private void handleDisconnected() {
		bConnected = false;
	}
	
	public void findDevice(View view) {
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
				// fail
			}
			else {
				connectToDevice();
			}
		} catch (Exception e) {
			Log.e(TAG, "Failed in findDevice() " + e.getMessage());
		}
	}
	
	private void connectToDevice() {
		try {
			btSocket = btDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
			Log.i(TAG, "Voor connect");
			btSocket.connect();
			Log.i(TAG, "Na connect");
		} catch (Exception e) {
			Log.e(TAG, "Failed in connectToDevice() " + e.getMessage());
		}
	}
	
	private void disconnectFromDevice() {
		try {
			btSocket.close();
			btDevice = null;
		} catch (Exception e) {
			Log.e(TAG, "Failed in disconnectFromDevice() " + e.getMessage());
		}
	}
	
	public void disconnect(View view) {
		disconnectFromDevice();
	}
	
	public void sendCommands(View view) {
		float speedF = 10.0f;
		float speedB = -20.0f;
		float turnRateL = -50.0f;
		float turnRateR = 20.0f;
		try {
			os.writeByte(COMMAND_SETSPEED);
			os.writeFloat(speedF);
			os.writeByte(COMMAND_TRAVEL);
			os.flush();
			
			Thread.sleep(3000);

			os.writeByte(COMMAND_STEER);
			os.writeFloat(turnRateL);
			os.flush();
			
			Thread.sleep(2000);
			
			os.writeByte(COMMAND_STOP);
			os.flush();
			
			Thread.sleep(1000);
			
			os.writeByte(COMMAND_SETSPEED);
			os.writeFloat(speedB);
			os.writeByte(COMMAND_STEER);
			os.writeFloat(turnRateR);
			os.flush();
			
			Thread.sleep(2000);
			
			os.writeByte(COMMAND_STEER);
			os.writeFloat(0.0f);
			os.flush();
			
			Thread.sleep(1000);
			
			os.writeByte(COMMAND_STOP);
			os.flush();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
