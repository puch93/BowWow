package kr.core.bowwow.utils;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kr.core.bowwow.R;
import kr.core.bowwow.dto.pref.UserPref;

public class MyUtil {

    public static String TAG = "TEST_HOME";
    public static final String BANNER = "B";
    public static final String ADMOB = "A";
    public static final String NONE = "N";

    public static final String DOG = "dog";
    public static final String PERSON = "people";

    public interface OnAlertAfter {
        void onAfterOk();
        void onAfterCancel();
    }

    public static void showAlert(Activity act, String title, String contents, final OnAlertAfter onAlertAfter) {
        androidx.appcompat.app.AlertDialog.Builder alertDialog = new androidx.appcompat.app.AlertDialog.Builder(act);

        alertDialog.setCancelable(false);
        alertDialog.setTitle(title);
        alertDialog.setMessage(contents);

        // ok
        alertDialog.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        onAlertAfter.onAfterOk();
                        dialog.cancel();
                    }
                });
        // cancel
        alertDialog.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        onAlertAfter.onAfterCancel();
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }

    public static String getAge(String year) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formats;
        formats = new SimpleDateFormat("yyyy");

        //Finalvar.birth_year의 값은 1950년 1월 20일
        int time2 = Integer.parseInt(formats.format(cal.getTime()));
        int ageSum = Integer.parseInt(year);

        return Integer.toString(time2 - ageSum + 1);
    }

    public static String getDday(int a_year, int a_monthOfYear, int a_dayOfMonth) {
        Log.i(TAG, "a_year: " + a_year);
        Log.i(TAG, "a_monthOfYear: " + a_monthOfYear);
        Log.i(TAG, "a_dayOfMonth: " + a_dayOfMonth);
        final int ONE_DAY = 24 * 60 * 60 * 1000;

        // D-day 설정
        final Calendar ddayCalendar = Calendar.getInstance();
        ddayCalendar.set(a_year, a_monthOfYear, a_dayOfMonth);

        // D-day 를 구하기 위해 millisecond 으로 환산하여 d-day 에서 today 의 차를 구한다.
        final long dday = ddayCalendar.getTimeInMillis() / ONE_DAY;
        final long today = Calendar.getInstance().getTimeInMillis() / ONE_DAY;
        long result = today - dday;

        final String strCount = setNumComma(result);
        return strCount;
    }

    public static String getDeviceId(Context ctx) {

        String newId = null;
        //버전 업데이트 이후 위의 코드는 삭제 (DEVICEID를 CPref.TAG_PREF 하나로 관리
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            newId = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
        } else {
            if (isNull(newId)) {
                if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    newId = ((TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                }
            }

            if (isNull(newId)) {

                newId = "35" +
                        Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
                        Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
                        Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
                        Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
                        Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
                        Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
                        Build.USER.length() % 10;
            }

            if (isNull(newId)) {

                newId = "w" + ((int) (Math.random() * 1000 * 1000 * 1000));
            }
        }

        if (!MyUtil.isNull(newId)) {
            //실제
            UserPref.setDeviceId(ctx,newId);
            //테스트
//            UserPref.setDeviceId(ctx, "355325072241290");
        }

        // 실제
        return newId;
    }

    public static boolean isNull(String str) {
        if (str == null || str.length() == 0 || str.equals("null")) {
            return true;
        } else {
            return false;
        }
    }

    public static String setNumComma(long price) {
        DecimalFormat format = new DecimalFormat("###,###");
        return format.format(price);
    }

    public static List<String> getBreedInfo(Context context, String breed) {
        List<String> result = new ArrayList<>();
        switch (breed) {
            case "요크셔테리어":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed1)));
                break;
            case "비글":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed2)));
                break;
            case "닥스훈트":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed3)));
                break;
            case "푸들":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed4)));
                break;
            case "시추":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed5)));
                break;
            case "슈나우저":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed6)));
                break;
            case "치와와":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed7)));
                break;
            case "포메라니안":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed8)));
                break;
            case "셔틀랜드 쉽독":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed9)));
                break;
            case "보스턴테리어":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed10)));
                break;
            case "말티즈":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed11)));
                break;
            case "파피용":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed12)));
                break;
            case "비숑 프리제":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed13)));
                break;
            case "미니어처 핀셔(미니핀)":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed14)));
                break;
            case "페키니즈":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed15)));
                break;
            case "불독":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed16)));
                break;
            case "잉글리쉬 코커 스패니얼":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed17)));
                break;
            case "아메리칸 코커 스패니얼":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed18)));
                break;
            case "웰시코기 펨브로크":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed19)));
                break;
            case "웰시코기 카디건":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed20)));
                break;
            case "보더콜리":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed21)));
                break;
            case "샤페이":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed22)));
                break;
            case "사모예드":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed23)));
                break;
            case "재패니즈 스피츠":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed24)));
                break;
            case "진돗개":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed25)));
                break;
            case "래브라도 리트리버":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed26)));
                break;
            case "저먼 세퍼드 도그":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed27)));
                break;
            case "골든 리트리버":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed28)));
                break;
            case "로트와일러":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed29)));
                break;
            case "그레이트 데인":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed30)));
                break;
            case "시베리안 허스키":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed31)));
                break;
            case "러프 콜리":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed32)));
                break;
            case "스무드 콜리":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed33)));
                break;
            case "알래스칸 맬러뮤트":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed34)));
                break;
            case "달마시안":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed35)));
                break;
            case "그레이트 피레니즈":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed36)));
                break;
            case "올드 잉글리쉬 쉽독":
                result.addAll(Arrays.asList(context.getResources().getStringArray(R.array.breed37)));
                break;
        }
        return result;
    }

    public static int getDogImage(String name) {
        switch (name) {
            case "요크셔테리어":
                return R.drawable.d01;
            case "비글":
                return R.drawable.d02;
            case "닥스훈트":
                return R.drawable.d03;
            case "푸들":
                return R.drawable.d04;
            case "시추":
                return R.drawable.d05;
            case "슈나우저":
                return R.drawable.d06;
            case "치와와":
                return R.drawable.d07;
            case "포메라니안":
                return R.drawable.d08;
            case "셔틀랜드 쉽독":
                return R.drawable.d09;
            case "보스턴테리어":
                return R.drawable.d10;
            case "말티즈":
                return R.drawable.d11;
            case "파피용":
                return R.drawable.d12;
            case "비숑 프리제":
                return R.drawable.d13;
            case "미니어처 핀셔(미니핀)":
                return R.drawable.d14;
            case "페키니즈":
                return R.drawable.d15;
            case "불독":
                return R.drawable.d16;
            case "잉글리쉬 코커 스패니얼":
                return R.drawable.d17;
            case "아메리칸 코커 스패니얼":
                return R.drawable.d18;
            case "웰시코기 펨브로크":
                return R.drawable.d19;
            case "웰시코기 카디건":
                return R.drawable.d20;
            case "보더콜리":
                return R.drawable.d21;
            case "샤페이":
                return R.drawable.d22;
            case "사모예드":
                return R.drawable.d23;
            case "재패니즈 스피츠":
                return R.drawable.d24;
            case "진돗개":
                return R.drawable.d25;
            case "래브라도 리트리버":
                return R.drawable.d26;
            case "저먼 세퍼드 도그":
                return R.drawable.d27;
            case "골든 리트리버":
                return R.drawable.d28;
            case "로트와일러":
                return R.drawable.d29;
            case "그레이트 데인":
                return R.drawable.d30;
            case "시베리안 허스키":
                return R.drawable.d31;
            case "러프 콜리":
                return R.drawable.d32;
            case "스무드 콜리":
                return R.drawable.d33;
            case "알래스칸 맬러뮤트":
                return R.drawable.d34;
            case "달마시안":
                return R.drawable.d35;
            case "그레이트 피레니즈":
                return R.drawable.d36;
            case "올드 잉글리쉬 쉽독":
                return R.drawable.d37;
            default:
                return 0;
        }
    }

    public static boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    // get phone num
    public static String getPhoneNumber(Activity act) {
        TelephonyManager tm = (TelephonyManager) act.getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNum = tm.getLine1Number();
        if (StringUtil.isNull(phoneNum)) {
            return null;
        } else {
            if (phoneNum.startsWith("+82")) {
                phoneNum = phoneNum.replace("+82", "0");
            }
            return phoneNum;
        }
    }

    public static String getTelecom(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getNetworkOperatorName();
    }


    /**
     * @param str 입력 String
     * @return 한글(true) / 한글제외(false)
     */
    public static boolean checkKorean(String str) {
        String regex = "^[가-힣]+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        boolean isNormal = m.matches();
        return isNormal;
    }

    /**
     * @param str 입력 String
     * @return 영문(true) / 영문제외(false)
     */
    public static boolean checkEnglish(String str) {
        String regex = "^[a-zA-Z]+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        boolean isNormal = m.matches();
        return isNormal;
    }

    /**
     * 밀리초에서 시/분/초를 계산하여 지정된 포맷으로 출력함.
     * -> 0:00 / 1:00 / 10:00 / 1:00:00
     *
     * @param duration
     * @return
     */

    public static String getTime(String duration) {

        long milliSeconds = Long.parseLong(duration);
        int totalSeconds = (int) (milliSeconds / 1000);

        int hour = totalSeconds / 3600;
        int minute = (totalSeconds - (hour * 3600)) / 60;
        int second = (totalSeconds - ((hour * 3600) + (minute * 60)));


        return formattedTime(hour, minute, second);
    }

    /**
     * 계산된 시/분/초 를 지정한 형태의 문자열로 반환함.
     *
     * @param hour
     * @param minute
     * @param second
     * @return
     */
    private static String formattedTime(int hour, int minute, int second) {
        String result = "";

        if (hour > 0) {
            result = hour + ":";
        }

        if (minute >= 10) {
            result = result + minute + ":";
        } else {
            result = result + "0" + minute + ":";
        }

        if (second >= 10) {
            result = result + second;
        } else {
            result = result + "0" + second;
        }

        return result;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context. * @param uri The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


}
