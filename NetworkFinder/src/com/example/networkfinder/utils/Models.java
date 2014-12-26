package com.example.networkfinder.utils;

public class Models {
	public static enum NetworkType {
		type_unknown, type_2G, type_3G, type_LTE
	}

	public static enum SignalLevel {
		level_unknown, level_poor, level_moderate, level_good, level_greate, level_max
	}

	public static class TowerData {
		public int cid;
		public int lac;
	}
}
