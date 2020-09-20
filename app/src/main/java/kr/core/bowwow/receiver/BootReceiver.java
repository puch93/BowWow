package kr.core.bowwow.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import kr.core.bowwow.service.ForegroundService;

public class BootReceiver extends BroadcastReceiver {

    public static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            Log.d(TAG, "ACTION_BOOT_COMPLETED => 서비스 재실행");

//            Intent serviceIntent = new Intent(context, ForegroundService.class);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
//                context.startForegroundService(serviceIntent);
//            }else{
//                context.startService(serviceIntent);
//            }

        }
    }
}
