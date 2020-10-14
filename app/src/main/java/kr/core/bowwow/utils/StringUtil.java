package kr.core.bowwow.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class StringUtil {
    public static final String TAG = "TEST_HOME";
    public static final String TAG_BARK = "TEST_BARK";
    public static final String TAG_PUSH = "TEST_HOME";

    public static final String CID_POPULAR = "10071";
    public static final String CID_RECOMMEND = "10073";

    public static boolean isNull(String str) {
        if (str == null || str.length() == 0 || str.equals("null")) {
            return true;
        } else {
            return false;
        }
    }

    public static String convertCallTime(long original, String patten) {
        DateFormat df = new SimpleDateFormat(patten, java.util.Locale.getDefault());
        return df.format(original);
    }

    public static String convertDateFormat(String original, String after) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", java.util.Locale.getDefault());
        sf.setTimeZone(TimeZone.getTimeZone("KST"));
        try {
            Date old_date = sf.parse(original);
            Date after_date = sf.parse(after);

            long calDate = after_date.getTime() - old_date.getTime();
            Log.i(TAG, "calDate: " + calDate);
            Date date = new Date(calDate);
            SimpleDateFormat dateFormat1 = new SimpleDateFormat("mm:ss", java.util.Locale.getDefault());
            return dateFormat1.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String convertTime(String original, String pattern) {
        //아이템별 시간
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", java.util.Locale.getDefault());
        Date date1 = null;
        try {
            date1 = dateFormat1.parse(original);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat dateFormat2 = new SimpleDateFormat(pattern, java.util.Locale.getDefault());
        return dateFormat2.format(date1);
    }

    public static String setNumComma(int price) {
        DecimalFormat format = new DecimalFormat("###,###");
        return format.format(price);
    }

    public static String calcAge(String byear) {
        // 현재 연도에서 출생 연도를 뺀다. (2018 - 2000 = 18)
        // 1살을 더한다. (18 + 1 = 19)
        Calendar c = Calendar.getInstance();
//        Log.i(TAG,"year: "+(c.get(Calendar.YEAR)-Integer.parseInt(byear)+1));
        int lastYear = c.get(Calendar.YEAR) - Integer.parseInt(byear) + 1;

        return String.valueOf(lastYear);
    }

    public static String getStr(JSONObject jo, String key) {
        String s = null;
        try {
            if (jo.has(key)) {
                s = jo.getString(key);
                if (s.equalsIgnoreCase("null"))
                    s = "";
            } else {
                s = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    public static int getInt(JSONObject jo, String key) {
        int s = 0;
        try {
            if (jo.has(key)) {
                s = jo.getInt(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    public static double getDouble(JSONObject jo, String key) {
        double s = 0.0;
        try {
            if (jo.has(key)) {
                s = jo.getDouble(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    public static void logLargeString(String str) {
        if (str.length() > 1500) {
            Log.i(StringUtil.TAG, str.substring(0, 1500));
            logLargeString(str.substring(1500));
        } else {
            Log.i(StringUtil.TAG, str); // continuation
        }
    }
}
