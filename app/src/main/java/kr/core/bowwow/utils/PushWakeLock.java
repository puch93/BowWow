package kr.core.bowwow.utils;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.PowerManager;

public class PushWakeLock {
	
	private static PowerManager.WakeLock sCpuWakeLock;
	private static KeyguardManager.KeyguardLock mKeyguardLock;
	private static boolean isScreenLock;
	
	public static void acquireCpuWakeLock(Context context) {
		if(sCpuWakeLock != null) {
			return;
		}
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		sCpuWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "hello");
		sCpuWakeLock.acquire();
	}

	public static void releaseCpuLock() {
		if(sCpuWakeLock != null) {
			sCpuWakeLock.release();
			sCpuWakeLock = null;
		}
	}

}
