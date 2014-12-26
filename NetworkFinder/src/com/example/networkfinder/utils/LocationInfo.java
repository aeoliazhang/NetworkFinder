package com.example.networkfinder.utils;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.example.networkfinder.utils.Models.TowerData;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

public class LocationInfo {

	private static final String APIKEY = "9d77651897c19488720c18b0769cff3d";
	private Context mContext;
	private INetworkFinderListener mListener;
	private LocationManager mLocationManager;
	private Location mPhoneLocation;
	private Location mTowerLocation = new Location("openSignal");

	public static final int MSG_ERROR = 0x800;
	public static final int MSG_GETTOWER_LOCATION = 0x801;

	public LocationInfo(Context context, INetworkFinderListener listener) {
		mContext = context;
		mListener = listener;
		mLocationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
	}

	public boolean isLocationSettingsOpened() {

		boolean bRes = false;
		boolean gps = mLocationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean network = mLocationManager
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		if (gps || network) {
			bRes = true;
		}

		return bRes;
	}

	public void openLocationSettings() {
		Intent intent = new Intent();
		intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			mContext.startActivity(intent);

		} catch (ActivityNotFoundException ex) {

			intent.setAction(Settings.ACTION_SETTINGS);
			try {
				mContext.startActivity(intent);
			} catch (Exception e) {
			}
		}
	}

	public void requestLocationUpdates() {
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				5000, 1, onLocationChange);

		mLocationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 5000, 1, onLocationChange);
	}

	public void removeLocationUpdates() {
		mLocationManager.removeUpdates(onLocationChange);
	}

	public Location getPhoneLocation() {
		mPhoneLocation = mLocationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		if (mPhoneLocation == null) {
			mPhoneLocation = mLocationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (mPhoneLocation == null) {
				mListener.onError("can not get phone location");
			}
		}

		return mPhoneLocation;
	}

	LocationListener onLocationChange = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			mPhoneLocation = location;
			mListener.onPhoneLocationChanged();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		@Override
		public void onProviderEnabled(String provider) {
			mListener.onLocationProviderStatusChanged();
		}

		@Override
		public void onProviderDisabled(String provider) {
			mListener.onLocationProviderStatusChanged();
		}
	};

	public Location getTowerLocation() {
		return mTowerLocation;
	}

	// 2G 测试OK，联通3G经常测试失败
	public void getTowerLocation(int cid, int lac, int sid, String phone_type,
			int network, String apikey) {

		try {

			HttpClient httpClient = new DefaultHttpClient();
			URI url = new URI(
					"http://api.opensignal.com/v2/towerinfo.json?cid=" + cid
							+ "&lac=" + lac + "&sid=" + sid + "&phone_type="
							+ phone_type + "&network_id=" + network
							+ "&apikey=" + apikey);

			NetworkFinderLog.writeLogToFile(url.toString());
			HttpGet get = new HttpGet(url);

			HttpResponse response = httpClient.execute(get);
			String resultString = EntityUtils.toString(response.getEntity());

			JSONObject jsonresult = new JSONObject(resultString);
			if (jsonresult.optJSONObject("tower1") != null) {
				JSONObject tower = jsonresult.optJSONObject("tower1");
				mTowerLocation.setLatitude(tower.optDouble("est_lat"));
				mTowerLocation.setLongitude(tower.optDouble("est_lng"));

				mLocationHander.obtainMessage(MSG_GETTOWER_LOCATION, null)
						.sendToTarget();
			} else {
				mLocationHander.obtainMessage(MSG_ERROR,
						"opensignal: Can not find tower").sendToTarget();

				NetworkFinderLog
						.writeLogToFile("opensignal: Can not find tower");
			}
		} catch (Exception e) {
			e.printStackTrace();

			if (e.getMessage() != null) {
				mLocationHander.obtainMessage(MSG_ERROR, e.getMessage())
						.sendToTarget();
				NetworkFinderLog.writeLogToFile(e.getMessage());
			}
		}
	}

	private Handler mLocationHander = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_ERROR:
				mListener.onError((String) msg.obj);
				break;

			case MSG_GETTOWER_LOCATION:
				mListener.onTowerLocationChanged();
				break;
			}
		}
	};

	private TowerData getTowerData() {
		TelephonyManager tm = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);

		TowerData td = new TowerData();
		GsmCellLocation gcl = (GsmCellLocation) tm.getCellLocation();
		if(gcl != null)
		{
			td.cid = gcl.getCid();
			td.lac = gcl.getLac();
		}
		else
		{
			mListener.onError("no sim card or airplane mode or no service");
		}

		return td;
	}

	public void requestTowerLocation() {
		TowerData data = getTowerData();
		GetTowerLocationTask task = new GetTowerLocationTask(data);
		task.start();
	}

	public class GetTowerLocationTask extends Thread {

		private TowerData mData;

		public GetTowerLocationTask(TowerData data) {
			mData = data;
		}

		@Override
		public void run() {
			getTowerLocation(mData.cid, mData.lac, 0, "GSM", 0, APIKEY);
		}
	}

}
