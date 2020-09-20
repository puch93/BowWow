package kr.core.bowwow.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

import java.util.Calendar;
import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;
import static androidx.legacy.content.WakefulBroadcastReceiver.startWakefulService;


/**
 * Intent 플래그
 * FLAG_ONE_SHOT : 한번만 사용하고 다음에 이 PendingIntent가 불려지면 Fail을 함
 * FLAG_NO_CREATE : PendingIntent를 생성하지 않음. PendingIntent가 실행중인것을 체크를 함
 * FLAG_CANCEL_CURRENT : 실행중인 PendingIntent가 있다면 기존 인텐트를 취소하고 새로만듬
 * FLAG_UPDATE_CURRENT : 실행중인 PendingIntent가 있다면  Extra Data만 교체함
 * <p>
 * AlarmType
 * RTC_WAKEUP : 대기모드에서도 알람이 작동함을 의미함
 * RTC : 대기모드에선 알람을 작동안함
 */

public class CoupangReceiver extends BroadcastReceiver {
    private Context context;
    public static final String TAG = "TEST_HOME";
    Intent intent;

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("TEST_HOME", "onReceive");
            this.context = context;
            this.intent = intent;

            doProcess();
            setAlarm();
    }

    private void doProcess() {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if(!pm.isScreenOn()) {
            //화면 깨우기
            PushWakeLock.acquireCpuWakeLock(context);
            PushWakeLock.releaseCpuLock();
        }

        if (isAppOnForeground(context)) {
            //앱이 포그라운드 상태에서 알람매니저의 시간이 되는 경우. 단순히 쿠팡페이지 실행
            intent = new Intent(context, LayoutWebView.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            //앱이 백그라운드 상태에서 알람매니저의 시간이 되는 경우.
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                    PowerManager.ACQUIRE_CAUSES_WAKEUP |
                    PowerManager.ON_AFTER_RELEASE, "My:Tag");
            wakeLock.acquire(5000);

            ComponentName comp = new ComponentName(context.getPackageName(),
                    CoupangReceiver.class.getName());
            startWakefulService(context, (intent.setComponent(comp)));
            setResultCode(Activity.RESULT_OK);

            intent = new Intent(context, LayoutWebView.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intent);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    context.startActivity(intent);
                }
            },1000);

            //기기에 화면을 꺼놓은  경우, 쿠팡페이지 실행이 잘 되지않는 이슈가 있음. 액티비티를 1초간격으로 2회 실행 처리.
        }
    }

    public boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessInfos = activityManager.getRunningAppProcesses();
        if (appProcessInfos == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfos) {
            if (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcessInfo.processName.equalsIgnoreCase(packageName)) {
                return true;
            }
        }
        return false;
    }

    private void setAlarm() {
        Log.e(TAG, "setAlarm");

        /* initialize alarm service */
        Calendar mCalendar = Calendar.getInstance();

        /* set alarm manager */
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //알람매니저의 알람으로 리시버가 실행된 이후 다음 알람 시간은 23시간 30분 후로 설정.
        //푸시 알림과 최대한 겺치지 않기 위해서 30분 값을 더하여 알람 예약.
        mCalendar.add(Calendar.HOUR_OF_DAY, 23);
        mCalendar.add(Calendar.MINUTE, 30);

        PendingIntent pendingIntent01 = PendingIntent.getBroadcast(context, 10, new Intent(context, CoupangReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);

        /* register alarm (버전별로 따로) */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.e(TAG, "first");
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), pendingIntent01);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Log.e("TEST_HOME", "second");
            manager.setExact(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), pendingIntent01);
        } else {
            Log.e("TEST_HOME", "third");
            manager.set(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), pendingIntent01);
        }
    }
}
