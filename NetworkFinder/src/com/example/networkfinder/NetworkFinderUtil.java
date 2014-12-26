package com.example.networkfinder;

import android.content.Context;
import android.location.Location;

import com.example.networkfinder.utils.CellInfo;
import com.example.networkfinder.utils.INetworkFinderListener;
import com.example.networkfinder.utils.LocationInfo;
import com.example.networkfinder.utils.Models.NetworkType;
import com.example.networkfinder.utils.Models.SignalLevel;
import com.example.networkfinder.utils.Utils;


public class NetworkFinderUtil {
	
	private Context mContext;
	private CellInfo mCellInfo;
	private LocationInfo mLocationInfo;
	private INetworkFinderListener mListener;


   public NetworkFinderUtil(Context context, INetworkFinderListener listener) {	    	
    	mContext = context;
    	mListener = listener;
    	mCellInfo = new CellInfo(mContext, mListener);
    	mLocationInfo = new LocationInfo(mContext, mListener);
    }
    
    //cell info
    public NetworkType getNetworkType() {
    	return mCellInfo.getNetworkType();
    }
    
	public String getOperatorName()
	{
		return mCellInfo.getOperatorName();
	}
	
	public boolean isRoaming()
	{
		return mCellInfo.isRoaming();
	}
	
	public int getSignalStrength() {		
		return mCellInfo.getSignalStrength();
	}
	
    public SignalLevel getSignalLevel() {
    	return mCellInfo.getSignalLevel();
    } 
    
    //location info     
	public boolean isLocationSettingsOpened() {
		return mLocationInfo.isLocationSettingsOpened();
	}
	
	public void openLocationSettings() {
		mLocationInfo.openLocationSettings();		
	}
	
	public Location getPhoneLocation() {	
		return mLocationInfo.getPhoneLocation();		
	} 
	
	public  Location getTowerLocation() {	
		return mLocationInfo.getTowerLocation();	
	}
	
	public void requestLocation()
	{	
		mLocationInfo.requestTowerLocation();	
		mLocationInfo.requestLocationUpdates();
	}	
	

	public double getDistance(double lat_a, double lng_a, double lat_b,double lng_b) {		
		return Utils.getDistance(lat_a, lng_a, lat_b, lng_b);
	}

	public double getOrientation(double lat_a, double lng_a, double lat_b,double lng_b) {	
		return Utils.getOrientation(lat_a, lng_a, lat_b, lng_b);
	}

}
