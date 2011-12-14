package com.dwvaniersel.rcremote;

import android.app.Activity;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MainActivity extends Activity {
	
	private final String TAG = "MainActivity";
	
	float mSpeed = 50.0f;
	
	Car car = new Car();
	
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
		
		mBtnConnect = (Button) findViewById(R.id.btnConnect);
		mBtnDisconnect = (Button) findViewById(R.id.btnDisconnect);
		mBtnForward = (Button) findViewById(R.id.btnForward);
		mSkbTurnRate = (SeekBar) findViewById(R.id.skbTurnRate);
		
		mSkbTurnRate.setProgress(50);
		
		car.setupBtMonitor();
		
		touchListenerStarter(mBtnForward);
		seekBarChangeListenerStarter(mSkbTurnRate);
		
//		mBtnForward.setOnTouchListener(new OnTouchListener() {
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				if (event.getAction() == MotionEvent.ACTION_DOWN) {
//					car.travel(mSpeed);
//					return true;
//				}
//				else if (event.getAction() == MotionEvent.ACTION_UP) {
//					car.stop();
//					return true;
//				}
//				return false;
//			}
//		});
//		
//		mSkbTurnRate.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
//			
//			@Override
//			public void onStopTrackingTouch(SeekBar seekBar) {
//			}
//			
//			@Override
//			public void onStartTrackingTouch(SeekBar seekBar) {
//			}
//			
//			@Override
//			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//				float turnRate = (float) ((mSkbTurnRate.getProgress()-50.0)*2.0);
//				car.steer(turnRate);
//			}
//		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		registerReceiver(car.btMonitor, new IntentFilter("android.bluetooth.device.action.ACL_CONNECTED"));
    	registerReceiver(car.btMonitor, new IntentFilter("android.bluetooth.device.action.ACL_DISCONNECTED"));
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		unregisterReceiver(car.btMonitor);
		car.disconnect();
	}
	
	public void touchListenerStarter(Button button) {
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
	
	public void seekBarChangeListenerStarter(SeekBar seekBar) {
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
	
	public void connect(View view) {
		car.connect();
	}
	
	public void disconnect(View view) {
		car.disconnect();
	}
}
