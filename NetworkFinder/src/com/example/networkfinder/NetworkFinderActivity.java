package com.example.networkfinder;

import com.example.networkfinder.utils.INetworkFinderListener;
import com.example.networkfinder.utils.Models.NetworkType;
import com.example.networkfinder.utils.Models.SignalLevel;
import com.example.networkfinder.utils.NetworkFinderLog;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class NetworkFinderActivity extends Activity {

	private SensorManager mSensorManager;
	private Sensor mOrientationSensor;

	private static final int EXIT_TIME = 2000;
	private final float MAX_ROATE_DEGREE = 1.0f;
	private float mDirection;
	private float mTargetDirection;
	private AccelerateInterpolator mInterpolator;
	protected final Handler mHandler = new Handler();
	private boolean mStopDrawing;
	private long firstExitTime = 0L;

	NetworkFinderUtil mNetworkFinderUtil;
	private Location mPhoneLocation;
	private Location mTowerLocation = new Location("openSignal");
	View mPointerView;
	ImageView mPointer;

	private ImageView m_networkTypeImageView = null;
	private ImageView m_networkRoamImageView = null;
	private ImageView m_signalImageView = null;
	private TextView m_connectToNetworkTextView = null;
	private SignalLevel mSLevel = SignalLevel.level_unknown;
	private NetworkType mType = NetworkType.type_unknown;
	private boolean isRoaming = false;
	public String mOperatorName = new String();

	TextView mDistance;
	private float mDistanceValue;
	private float direction1 = 0.0f;

	protected Runnable mPointerViewUpdater = new Runnable() {
		@SuppressLint("NewApi")
		@Override
		public void run() {
			if (mPointer != null && !mStopDrawing) {
				if (mDirection != mTargetDirection) {

					// calculate the short routine
					float to = mTargetDirection;
					if (to - mDirection > 180) {
						to -= 360;
					} else if (to - mDirection < -180) {
						to += 360;
					}

					// limit the max speed to MAX_ROTATE_DEGREE
					float distance = to - mDirection;
					if (Math.abs(distance) > MAX_ROATE_DEGREE) {
						distance = distance > 0 ? MAX_ROATE_DEGREE
								: (-1.0f * MAX_ROATE_DEGREE);
					}

					// need to slow down if the distance is short
					mDirection = normalizeDegree(mDirection
							+ ((to - mDirection) * mInterpolator
									.getInterpolation(Math.abs(distance) > MAX_ROATE_DEGREE ? 0.4f
											: 0.3f)));
					updateDirection(mDirection);

				}
				updateSignal();
				updateDistance();
				mHandler.postDelayed(mPointerViewUpdater, 20);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initServices();
		initResources();	
	}

	@Override
	public void onBackPressed() {
		long curTime = System.currentTimeMillis();
		if (curTime - firstExitTime < EXIT_TIME) {
			finish();
		} else {
			Toast.makeText(this, R.string.exit_toast, Toast.LENGTH_SHORT)
					.show();
			firstExitTime = curTime;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mOrientationSensor != null) {
			mSensorManager.registerListener(mOrientationSensorEventListener,
					mOrientationSensor, SensorManager.SENSOR_DELAY_GAME);
		} else {
			Toast.makeText(this, "Can not find Orientation sensor",
					Toast.LENGTH_SHORT).show();
			NetworkFinderLog.writeLogToFile("Can not find Orientation sensor");
		}

		if (mNetworkFinderUtil.isLocationSettingsOpened()) {
			mPhoneLocation = mNetworkFinderUtil.getPhoneLocation();
			mNetworkFinderUtil.requestLocation();
			mStopDrawing = false;
			mHandler.postDelayed(mPointerViewUpdater, 20);
		} else {
			Toast.makeText(this, "Pls open your location setting",
					Toast.LENGTH_SHORT).show();
			NetworkFinderLog.writeLogToFile("GPS or wifi is not open");
		}

		mPhoneLocation = mNetworkFinderUtil.getPhoneLocation();
		mNetworkFinderUtil.requestLocation();
		mStopDrawing = false;
		mHandler.postDelayed(mPointerViewUpdater, 20);

	}

	@Override
	protected void onPause() {
		super.onPause();
		mStopDrawing = true;
		if (mOrientationSensor != null) {
			mSensorManager.unregisterListener(mOrientationSensorEventListener);
		}
	}

	private SensorEventListener mOrientationSensorEventListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {
			float direction = event.values[0] * -1.0f;
			mTargetDirection = normalizeDegree(direction + direction1);

		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};


	// init view
	private void initResources() {
		mDirection = 0.0f;
		mTargetDirection = 0.0f;
		mInterpolator = new AccelerateInterpolator();
		mStopDrawing = true;

		// ui
		m_networkTypeImageView = (ImageView) findViewById(R.id.connct_network_type);
		m_networkRoamImageView = (ImageView) findViewById(R.id.connect_roam);
		m_signalImageView = (ImageView) findViewById(R.id.connct_signal);
		m_connectToNetworkTextView = (TextView) findViewById(R.id.connect_network);

		mPointerView = findViewById(R.id.view_nfinder);
		mPointer = (ImageView) findViewById(R.id.nfinder_pointer);
		mDistance = (TextView) findViewById(R.id.value_distance);

		// data
		mSLevel = mNetworkFinderUtil.getSignalLevel();
		mType = mNetworkFinderUtil.getNetworkType();
		isRoaming = mNetworkFinderUtil.isRoaming();
		mOperatorName = mNetworkFinderUtil.getOperatorName();

		mDistanceValue = 0;
	}

	private void initServices() {
		mNetworkFinderUtil = new NetworkFinderUtil(this,
				mINetworkFinderListener);
		// sensor manager
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mOrientationSensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ORIENTATION);

		if (mOrientationSensor == null) {
			NetworkFinderLog.writeLogToFile("Can not find Orientation sensor");
		}

	}

	private INetworkFinderListener mINetworkFinderListener = new INetworkFinderListener() {

		public void onSignalChanged() {
			mSLevel = mNetworkFinderUtil.getSignalLevel();
			mType = mNetworkFinderUtil.getNetworkType();
			isRoaming = mNetworkFinderUtil.isRoaming();
			mOperatorName = mNetworkFinderUtil.getOperatorName();
		}

		public void onPhoneLocationChanged() {
			mPhoneLocation = mNetworkFinderUtil.getPhoneLocation();
			if (mPhoneLocation != null) {
				direction1 = ((float) mNetworkFinderUtil.getOrientation(
						mPhoneLocation.getLatitude(),
						mPhoneLocation.getLongitude(),
						mTowerLocation.getLatitude(),
						mTowerLocation.getLongitude()))
						* -1.0f;
				mDistanceValue = (float) mNetworkFinderUtil.getDistance(
						mPhoneLocation.getLatitude(),
						mPhoneLocation.getLongitude(),
						mTowerLocation.getLatitude(),
						mTowerLocation.getLongitude());
			}
		}

		public void onTowerLocationChanged() {
			mTowerLocation = mNetworkFinderUtil.getTowerLocation();
			if (mPhoneLocation != null) {
				direction1 = ((float) mNetworkFinderUtil.getOrientation(
						mPhoneLocation.getLatitude(),
						mPhoneLocation.getLongitude(),
						mTowerLocation.getLatitude(),
						mTowerLocation.getLongitude()))
						* -1.0f;
				mDistanceValue = (float) mNetworkFinderUtil.getDistance(
						mPhoneLocation.getLatitude(),
						mPhoneLocation.getLongitude(),
						mTowerLocation.getLatitude(),
						mTowerLocation.getLongitude());
			}
		}

		public void onLocationProviderStatusChanged() {
			if (mPhoneLocation != null) {
				direction1 = ((float) mNetworkFinderUtil.getOrientation(
						mPhoneLocation.getLatitude(),
						mPhoneLocation.getLongitude(),
						mTowerLocation.getLatitude(),
						mTowerLocation.getLongitude()))
						* -1.0f;
				mDistanceValue = (float) mNetworkFinderUtil.getDistance(
						mPhoneLocation.getLatitude(),
						mPhoneLocation.getLongitude(),
						mTowerLocation.getLatitude(),
						mTowerLocation.getLongitude());
			}
		}

		public void onError(String errMsg) {
			Toast.makeText(NetworkFinderActivity.this, errMsg,
					Toast.LENGTH_LONG).show();
			NetworkFinderLog.writeLogToFile(errMsg);
		}
	};


	private float normalizeDegree(float direction) {
		return (direction + 720) % 360;
	}


	private void showSignalAndNetworkType() {		
		//show roaming
		if(isRoaming == true) 
			m_networkRoamImageView.setVisibility(View.VISIBLE);
		else
			m_networkRoamImageView.setVisibility(View.INVISIBLE);
		
		//show signal strength
		if(mSLevel == SignalLevel.level_unknown)
			m_signalImageView.setBackgroundResource(R.drawable.signal_0);
		if (mSLevel == SignalLevel.level_poor)
			m_signalImageView.setBackgroundResource(R.drawable.signal_1);
		if (mSLevel == SignalLevel.level_moderate)
			m_signalImageView.setBackgroundResource(R.drawable.signal_2);
		if (mSLevel == SignalLevel.level_good)
			m_signalImageView.setBackgroundResource(R.drawable.signal_3);
		if (mSLevel == SignalLevel.level_greate)
			m_signalImageView.setBackgroundResource(R.drawable.signal_4);
		if (mSLevel == SignalLevel.level_max)
			m_signalImageView.setBackgroundResource(R.drawable.signal_5);

		
		//show network type
		if (mType == NetworkType.type_unknown)
			m_networkTypeImageView.setVisibility(View.INVISIBLE);

		if (mType == NetworkType.type_2G) {
			m_networkTypeImageView
					.setBackgroundResource(R.drawable.network_type_2g);
			m_networkTypeImageView.setVisibility(View.VISIBLE);
		}
	
		if (mType == NetworkType.type_3G) {
			m_networkTypeImageView
					.setBackgroundResource(R.drawable.network_type_3g);
			m_networkTypeImageView.setVisibility(View.VISIBLE);
		}
		
		//4G			

		if (mType == NetworkType.type_LTE) {
			m_networkTypeImageView
					.setBackgroundResource(R.drawable.network_type_4g);
			m_networkTypeImageView.setVisibility(View.VISIBLE);
		}
	}

	private void showNetworkState() {
		m_connectToNetworkTextView.setText(mOperatorName);
	}

	private void updateSignal() {
		showSignalAndNetworkType();
		showNetworkState();
	}

	private void updateDistance() {
		mDistance.setText(String.valueOf(mDistanceValue) + "m");
	}

	@SuppressLint("NewApi")
	private void updateDirection(float mDirection) {
		mPointer.setRotation(mDirection);
	}

}
