package com.example.networkfinder;

import android.app.Application;

public class NetworkFinderApplication extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext());	
	}
}