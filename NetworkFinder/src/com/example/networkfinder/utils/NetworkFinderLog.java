package com.example.networkfinder.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.os.Environment;

public class NetworkFinderLog {

	public static void writeLogToFile(String strLog) {
		if (isLogSwitchOn() == false)
			return;
		if (Environment.getExternalStorageState().equalsIgnoreCase(
				Environment.MEDIA_MOUNTED)) {
			FileOutputStream fos = null;
			try {
				
				String strLogFile = Environment.getExternalStorageDirectory().getAbsolutePath() +"/NetworkFinder/log.txt";
				
				File logFile = new File(strLogFile);		
				if (!logFile.exists()) {
					logFile.createNewFile();
				}

				SimpleDateFormat format = new SimpleDateFormat(
						"yyyy-MM-dd hh:mm:ss.SSS");
				String log = format.format(Calendar.getInstance().getTime())
						+ " " + strLog + "\r\n";

				fos = new FileOutputStream(logFile, true);
				fos.write(log.getBytes());
				fos.close();
				fos = null;

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public static boolean isLogSwitchOn() {
		String strLoginFile = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/NetworkFinder/LogEnable";
		File f = new File(strLoginFile);
		return f.exists();
	}

}