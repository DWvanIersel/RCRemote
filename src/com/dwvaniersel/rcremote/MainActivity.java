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

public class MainActivity extends Activity {
	
	
	float mSpeed = 50.0f;
	
	Car car = new Car();
	
	// Sensor variables
	SensorManager sensorManager;
	Sensor orientationSensor;
//	SensorEventListener sensorEventListener;
	// End sensor variables
	
	// Layout variables
	Button mBtnConnect;
	Button mBtnDisconnect;
	Button mBtnForward;
	SeekBar mSkbTurnRate;
	// End layout variables
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE); //force reversed landscape orientation
		setContentView(R.layout.main);
		
		car.setupBtMonitor();
		
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
//		setupSensorEventListener();
		
		mBtnConnect = (Button) findViewById(R.id.btnConnect);
		mBtnDisconnect = (Button) findViewById(R.id.btnDisconnect);
		mBtnForward = (Button) findViewById(R.id.btnForward);
		mSkbTurnRate = (SeekBar) findViewById(R.id.skbTurnRate);
		
		mSkbTurnRate.setProgress(50);
		
		setupTouchListener(mBtnForward);
		setupSeekBarChangeListener(mSkbTurnRate);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
//		sensorManager.registerListener(sensorEventListener, orientationSensor, SensorManager.SENSOR_DELAY_GAME);
		registerReceiver(car.btMonitor, new IntentFilter("android.bluetooth.device.action.ACL_CONNECTED"));
    	registerReceiver(car.btMonitor, new IntentFilter("android.bluetooth.device.action.ACL_DISCONNECTED"));
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
//		sensorManager.unregisterListener(sensorEventListener, orientationSensor);
		unregisterReceiver(car.btMonitor);
		car.disconnect();
	}
	
	public void setupTouchListener(Button button) {
		button.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					car.travel(mSpeed);
					return true;
				}
				else if (event.getAction() == MotionEvent.ACTION_UP) {
					car.stop();
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
				car.steer(turnRate);
			}
		});
	}
	
//	public void setupSensorEventListener() {
//		sensorEventListener = new SensorEventListener() {
//
//			@Override
//			public void onAccuracyChanged(Sensor sensor, int accuracy) {
//			}
//
//			@Override
//			public void onSensorChanged(SensorEvent event) {
//				
//			}
//		};
//	}
	
	public void connect(View view) {
		car.connect();
	}
	
	public void disconnect(View view) {
		car.disconnect();
	}
}
