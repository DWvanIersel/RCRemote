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
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private final String TAG = "MainActivity";
	private final String DEVICENAME = "PFWSNXT";
	
	float mSpeed = 10.0f;
	
	// Layout variables
	Button mBtnConnect;
	Button mBtnDisconnect;
	Button mBtnSendCommands;
	Button mBtnForward;
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
		mBtnForward = (Button) findViewById(R.id.btnForward);
		
		setupBtMonitor();
		
		mBtnForward.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					travel();
					return true;
				}
				else if (event.getAction() == MotionEvent.ACTION_UP) {
					stop();
					return true;
				}
				return false;
			}
		});
	}
	@Override
	public void onResume() {
		super.onResume();
		
		registerReceiver(btMonitor, new IntentFilter("android.bluetooth.device.action.ACL_CONNECTED"));
    	registerReceiver(btMonitor, new IntentFilter("android.bluetooth.device.action.ACL_DISCONNECTED"));
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		unregisterReceiver(btMonitor);
		disconnectFromDevice();
	}
	
	private void setupBtMonitor() {
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
		disconnectFromDevice();
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
				Toast.makeText(this, "The device couldn't be found.", Toast.LENGTH_LONG).show();
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
			btSocket.connect();
			Toast.makeText(this, "Connected to " + DEVICENAME, Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Log.e(TAG, "Failed in connectToDevice() " + e.getMessage());
			Toast.makeText(this, "Connecting to the NXT bot failed", Toast.LENGTH_LONG).show();
		}
	}
	
	private void disconnectFromDevice() {
		try {
			btSocket.close();
			btDevice = null;
			bConnected = false;
			Toast.makeText(this, "Succesfully disconnected from the NXT bot", Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Log.e(TAG, "Failed in disconnectFromDevice() " + e.getMessage());
			Toast.makeText(this, "Disconnecting from the NXT bot failed", Toast.LENGTH_LONG).show();
		}
	}
	
	public void disconnect(View view) {
		disconnectFromDevice();
	}
	
	public void sendCommands(View view) {
		float speedF = 10.0f;
		float turnRateL = -50.0f;
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void travel() {
		try {
			os.writeByte(COMMAND_SETSPEED);
			os.writeFloat(mSpeed);
			os.writeByte(COMMAND_TRAVEL);
			os.flush();
		} catch (Exception e) {
			Log.e(TAG, "Could not send commands " + e.getMessage());
		}
		
	}
	
	public void stop() {
		try {
			os.writeByte(COMMAND_STOP);
			os.flush();
		} catch (Exception e) {
			Log.e(TAG, "Could not send commands " + e.getMessage());
		}
	}
}
