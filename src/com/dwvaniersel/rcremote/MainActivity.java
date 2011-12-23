package com.dwvaniersel.rcremote;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity implements SensorEventListener {
	
	final static String TAG = "MainActivity";
	
	Car mCar = new Car();
	
	float mLastTurnRate = 0.0f;
	float mLastSpeed = 0.0f;
	
	PowerManager mPowerManager;
	WakeLock mWakeLock;
	
	// Sensor variables
	SensorManager mSensorManager;
	Sensor mOrientationSensor;
	SensorEventListener mSensorEventListener;
	// End sensor variables
	
	// Layout variables
	Button mBtnConnect;
	Button mBtnDisconnect;
	// End layout variables
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE); //force reversed landscape orientation
		setContentView(R.layout.main);
		
		mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
		
		mCar.setupBtMonitor();
		
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		
		mBtnConnect = (Button) findViewById(R.id.btnConnect);
		mBtnDisconnect = (Button) findViewById(R.id.btnDisconnect);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		mWakeLock.acquire();
		mSensorManager.registerListener(this, mOrientationSensor, SensorManager.SENSOR_DELAY_NORMAL);
		registerReceiver(mCar.mBtMonitor, new IntentFilter("android.bluetooth.device.action.ACL_CONNECTED"));
    	registerReceiver(mCar.mBtMonitor, new IntentFilter("android.bluetooth.device.action.ACL_DISCONNECTED"));
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		mWakeLock.release();
		mSensorManager.unregisterListener(this);
		unregisterReceiver(mCar.mBtMonitor);
		mCar.disconnect();
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		boolean connected = mCar.getConnectionState();
		
		if (connected) {
			float pitch = event.values[1];
			float roll = event.values[2];
			
			mCar.turn(pitch);
			mCar.drive(roll);
		}
	}
	
//	public void updateView() {
//		boolean connected = mCar.getConnectionState();
//		
//		if (connected) {
//			mBtnConnect.setVisibility(View.GONE);
//			mBtnDisconnect.setVisibility(View.VISIBLE);
//		}
//		else {
//			mBtnConnect.setVisibility(View.VISIBLE);
//			mBtnDisconnect.setVisibility(View.GONE);
//		}
//	}
	
	public void connect(View view) {
		mCar.connect();
	}
	
	public void disconnect(View view) {
		mCar.disconnect();
	}
}
