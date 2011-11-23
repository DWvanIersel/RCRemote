package com.dwvaniersel.rcremote;

import java.io.DataOutputStream;
import java.lang.reflect.Method;
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
	private BluetoothSocket socket;
	private BluetoothDevice device;
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
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //force landscape orientation
		setContentView(R.layout.main);
		
		setupBtMonitor();
		findDevice();
		
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
			os = new DataOutputStream(socket.getOutputStream());
			bConnected = true;
		} catch (Exception e) {
			os = null;
			disconnectFromDevice();
		}
	}
	
	private void handleDisconnected() {
		bConnected = false;
	}
	
	public void findDevice() {
		try {
			btInterface = BluetoothAdapter.getDefaultAdapter();
			Set<BluetoothDevice> pairedDevices = btInterface.getBondedDevices();
			for (BluetoothDevice someDevice : pairedDevices) {
				if (someDevice.getName().equalsIgnoreCase(DEVICENAME)) {
					device = someDevice;
					break;
				}
			}
			if (device == null) {
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
			
			Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
	        socket = (BluetoothSocket) m.invoke(device, 1);
			Log.i(TAG, "Voor connect");
			socket.connect();
			Log.i(TAG, "Na connect");
		} catch (Exception e) {
			Log.e(TAG, "Failed in connectToDevice() " + e.getMessage());
		}
	}
	
	private void disconnectFromDevice() {
		try {
			socket.close();
			device = null;
		} catch (Exception e) {
			Log.e(TAG, "Failed in disconnectFromDevice() " + e.getMessage());
		}
	}
}
