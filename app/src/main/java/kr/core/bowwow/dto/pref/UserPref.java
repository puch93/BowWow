package kr.core.bowwow.dto.pref;

import android.content.Context;
import android.content.SharedPreferences;

import kr.core.bowwow.app;

public class UserPref {

    public static String getFCheck(Context context){
        SharedPreferences pref = context.getSharedPreferences("user",context.MODE_PRIVATE);
        return pref.getString("fcheck",null);
    }

    public static void setFCheck(Context context,String idx){
        SharedPreferences pref = context.getSharedPreferences("user",context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("fcheck",idx);
        editor.commit();
    }

    public static String getAlarmCoupa(Context context){
        SharedPreferences pref = context.getSharedPreferences("user",context.MODE_PRIVATE);
        return pref.getString("alarm_coupa",null);
    }

    public static void setAlarmCoupa(Context context,String idx){
        SharedPreferences pref = context.getSharedPreferences("user",context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("alarm_coupa",idx);
        editor.commit();
    }

    public static String getFcm(Context context){
        SharedPreferences pref = context.getSharedPreferences("user",context.MODE_PRIVATE);
        return pref.getString("fcm","");
    }

    public static void setFcm(Context context,String idx){
        SharedPreferences pref = context.getSharedPreferences("user",context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("fcm",idx);
        editor.commit();
    }


    public static String getIdx(Context context){
        SharedPreferences pref = context.getSharedPreferences("user",context.MODE_PRIVATE);
        return pref.getString("idx","");
    }

    public static void setIdx(Context context,String idx){
        SharedPreferences pref = context.getSharedPreferences("user",context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("idx",idx);
        editor.commit();
    }

    public static String getDogIdx(Context context){
        SharedPreferences pref = context.getSharedPreferences("user",context.MODE_PRIVATE);
        return pref.getString("dogidx","");
    }

    public static void setDogIdx(Context context,String idx){
        SharedPreferences pref = context.getSharedPreferences("user",context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("dogidx",idx);
        editor.commit();
    }

    public static String getDeviceId(Context context){
        SharedPreferences pref = context.getSharedPreferences("user",context.MODE_PRIVATE);
        return pref.getString("deviceid","");
    }

    public static void setDeviceId(Context context,String deviceid){
        SharedPreferences pref = context.getSharedPreferences("user",context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("deviceid",deviceid);
        editor.commit();
    }

    public static String getSubscribeState(Context context){
        if(context != null) {
            SharedPreferences pref = context.getSharedPreferences("user", context.MODE_PRIVATE);
            return pref.getString("substate", "N");
        } else {
            SharedPreferences pref = app.ctx.getSharedPreferences("user", context.MODE_PRIVATE);
            return pref.getString("substate", "N");
        }
    }

    public static void setSubscribeState(Context context,String substate){
        SharedPreferences pref = context.getSharedPreferences("user",context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("substate",substate);
        editor.commit();
    }

    public static boolean isDetected(Context context){
        SharedPreferences pref = context.getSharedPreferences("user",context.MODE_PRIVATE);
        return pref.getBoolean("isdetect",true);
    }

    public static void setDetected(Context context, boolean isDetect){
        SharedPreferences pref = context.getSharedPreferences("user",context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isdetect",isDetect);
        editor.commit();
    }

}
