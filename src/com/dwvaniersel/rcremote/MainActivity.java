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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MainActivity extends Activity implements SensorEventListener {
	
	final static String TAG = "MainActivity";
	float mSpeed = 30.0f;
	float mTurnRate;
	
	Car mCar = new Car();
	
	// Sensor variables
	SensorManager mSensorManager;
	Sensor mOrientationSensor;
	SensorEventListener mSensorEventListener;
	// End sensor variables
	
	// Layout variables
	Button mBtnConnect;
	Button mBtnDisconnect;
	Button mBtnForward;
	SeekBar mSkbTurnRate;
	// End layout variables
	
	// Orientation variables
	float mPitch;
	// End orientation variables
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE); //force reversed landscape orientation
		setContentView(R.layout.main);
		
		mCar.setupBtMonitor();
		
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		
		mBtnConnect = (Button) findViewById(R.id.btnConnect);
		mBtnDisconnect = (Button) findViewById(R.id.btnDisconnect);
		mBtnForward = (Button) findViewById(R.id.btnForward);
		mSkbTurnRate = (SeekBar) findViewById(R.id.skbTurnRate);
		
		mSkbTurnRate.setProgress(50);
		
		setupTouchListener(mBtnForward);
//		setupSeekBarChangeListener(mSkbTurnRate);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		mSensorManager.registerListener(this, mOrientationSensor, SensorManager.SENSOR_DELAY_GAME);
		registerReceiver(mCar.btMonitor, new IntentFilter("android.bluetooth.device.action.ACL_CONNECTED"));
    	registerReceiver(mCar.btMonitor, new IntentFilter("android.bluetooth.device.action.ACL_DISCONNECTED"));
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		mSensorManager.unregisterListener(this);
		unregisterReceiver(mCar.btMonitor);
		mCar.disconnect();
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		mPitch = event.values[1];
		mTurnRate = (mPitch/90)*150;
		mCar.steer(mTurnRate);
	}
	
	public void setupTouchListener(Button button) {
		button.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mCar.travel(mSpeed);
					return true;
				}
				else if (event.getAction() == MotionEvent.ACTION_UP) {
					mCar.stop();
					return true;
				}
				return false;
			}
		});
	}
	
	public void setupSeekBarChangeListener(SeekBar seekBar) {
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				float turnRate = (float) ((progress-50)*2);
				mCar.steer(turnRate);
			}
		});
	}
	
	public void connect(View view) {
		mCar.connect();
	}
	
	public void disconnect(View view) {
		mCar.disconnect();
	}
}
