package com.example.networkfinder.utils;

import com.example.networkfinder.utils.Models.NetworkType;
import com.example.networkfinder.utils.Models.SignalLevel;

import android.content.Context;

import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

public class CellInfo {

	private Context mContext;
	private INetworkFinderListener mListener;
	private TelephonyManager mTelephonyManager;
	private int mSignalStrength = 0;
	private SignalLevel mSignalLevel = SignalLevel.level_unknown;

	public CellInfo(Context context, INetworkFinderListener listener) {
		mContext = context;	
		mListener = listener;
		mTelephonyManager = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		
		SignalListener signalListener = new SignalListener();
		mTelephonyManager.listen(signalListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
	}
	
	public NetworkType getNetworkType() {
		
		NetworkType type = NetworkType.type_unknown;
		int nType = mTelephonyManager.getNetworkType();
		switch (nType) {
		case TelephonyManager.NETWORK_TYPE_GPRS:
		case TelephonyManager.NETWORK_TYPE_EDGE:
		case TelephonyManager.NETWORK_TYPE_CDMA:
		case TelephonyManager.NETWORK_TYPE_1xRTT:
		case TelephonyManager.NETWORK_TYPE_IDEN:
			type = NetworkType.type_2G;
			break;

		case TelephonyManager.NETWORK_TYPE_UMTS:
		case TelephonyManager.NETWORK_TYPE_HSDPA:
		case TelephonyManager.NETWORK_TYPE_HSUPA:
		case TelephonyManager.NETWORK_TYPE_HSPA:
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
		case TelephonyManager.NETWORK_TYPE_EVDO_B:
		case TelephonyManager.NETWORK_TYPE_EHRPD:
		case TelephonyManager.NETWORK_TYPE_HSPAP:
			type = NetworkType.type_3G;
			break;

		case TelephonyManager.NETWORK_TYPE_LTE:
			type = NetworkType.type_LTE;
			break;

		default:
			type = NetworkType.type_unknown;
			break;
		}
		
		return type;
	}
	
	public int getSignalStrength() {		
		return mSignalStrength;
	}
	
    public SignalLevel getSignalLevel() {
    	return mSignalLevel;
    }   

	private class SignalListener extends PhoneStateListener {

		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);		

			int asu = signalStrength.getGsmSignalStrength();
			mSignalStrength = signalStrength.getGsmSignalStrength() * 2 - 113;
			if (asu <= 2 || asu == 99) {
				mSignalLevel = SignalLevel.level_unknown;
				mSignalStrength = 0;							
			}else if (asu >= 17) {
				mSignalLevel = SignalLevel.level_max;
			} 
			else if (asu >= 12) {
				mSignalLevel = SignalLevel.level_greate;
			}
			else if (asu >= 8) {
				mSignalLevel = SignalLevel.level_good;
			}
			else if (asu >= 5) {
				mSignalLevel = SignalLevel.level_moderate;
			}
			else
				mSignalLevel = SignalLevel.level_poor;
			
			mListener.onSignalChanged();
		}	
	}
	
	public String getOperatorName()
	{
		return mTelephonyManager.getNetworkOperatorName();
	}
	
	public boolean isRoaming()
	{
		return mTelephonyManager.isNetworkRoaming();
	}
}
