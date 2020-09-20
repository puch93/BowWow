package kr.core.bowwow.dto.pref;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingPref {
    public static boolean isPushReceive(Context context){
        SharedPreferences pref = context.getSharedPreferences("setting",context.MODE_PRIVATE);
        return pref.getBoolean("ispushreceive",true);
    }

    public static void setPushReceive(Context context, boolean isPushReceive){
        SharedPreferences pref = context.getSharedPreferences("setting",context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("ispushreceive",isPushReceive);
        editor.commit();
    }
}
