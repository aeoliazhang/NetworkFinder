package com.example.networkfinder.utils;

public interface INetworkFinderListener {	
	public void onSignalChanged();	
	public void onPhoneLocationChanged();
	public void onTowerLocationChanged();
	public void onLocationProviderStatusChanged();
	public void onError(String errMsg);
}
