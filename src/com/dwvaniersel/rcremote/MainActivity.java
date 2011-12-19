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
	
	PowerManager powerManager;
	WakeLock wakeLock;
	
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
		
		powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
		
		mCar.setupBtMonitor();
		
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		
		mBtnConnect = (Button) findViewById(R.id.btnConnect);
		mBtnDisconnect = (Button) findViewById(R.id.btnDisconnect);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		wakeLock.acquire();
		mSensorManager.registerListener(this, mOrientationSensor, SensorManager.SENSOR_DELAY_NORMAL);
		registerReceiver(mCar.btMonitor, new IntentFilter("android.bluetooth.device.action.ACL_CONNECTED"));
    	registerReceiver(mCar.btMonitor, new IntentFilter("android.bluetooth.device.action.ACL_DISCONNECTED"));
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		wakeLock.release();
		mSensorManager.unregisterListener(this);
		unregisterReceiver(mCar.btMonitor);
		mCar.disconnect();
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		float pitch = event.values[1];
		float roll = event.values[2];
		
		float turnRate = (pitch/90.0f)*150.0f;
		float turnRateDiff = Math.abs(turnRate - mLastTurnRate);
		
		if (turnRateDiff > 2.0f) {
			mCar.steer(turnRate);
			mLastTurnRate = turnRate;
		}
		
		float speed = (roll/90.0f)*30.0f;
		float speedDiff = Math.abs(speed - mLastSpeed);
		
		if (speedDiff > 1.0f) {
			mCar.travel(speed);
			mLastSpeed = speed;
		}
	}
	
	public void connect(View view) {
		mCar.connect();
	}
	
	public void disconnect(View view) {
		mCar.disconnect();
	}
}
