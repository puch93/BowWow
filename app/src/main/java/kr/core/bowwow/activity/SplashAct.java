package kr.core.bowwow.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.android.billingclient.api.Purchase;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.internal.service.Common;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import kr.core.bowwow.R;
import kr.core.bowwow.app;
import kr.core.bowwow.billing.BillingManager;
import kr.core.bowwow.dto.ChatItem;
import kr.core.bowwow.dto.pref.UserPref;
import kr.core.bowwow.network.HttpResult;
import kr.core.bowwow.network.MultipartUtility;
import kr.core.bowwow.network.NetUrls;
import kr.core.bowwow.network.ReqBasic;
import kr.core.bowwow.utils.CoupangReceiver;
import kr.core.bowwow.utils.DBHelper;
import kr.core.bowwow.utils.MyUtil;
import kr.core.bowwow.utils.StringUtil;

public class SplashAct extends BaseAct {
    Activity act;
    BillingManager billingManager;

    DBHelper db = new DBHelper();

    private Timer timer = new Timer();
    boolean isReady = false;
    boolean isPurchaseStateReady = false;
    String fcm_token, device_version, sub_state;

    private static final int OVERLAY = 1003;
    TextView version_text;

    String dog_code = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_splash);
        act = this;

        ImageView aniView = findViewById(R.id.iv_splashani);

        Glide.with(this)
                .load(R.raw.splash_gif01)
                .into(aniView);

        version_text = (TextView) findViewById(R.id.version_text);
        // get device version
        try {
            device_version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // get fcm token
        getFcmToken();

        billingCheck();

        checkTimer();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkVer();
            }
        }, 1500);
    }

    private void getFcmToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.d(MyUtil.TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        fcm_token = task.getResult().getToken();
                        UserPref.setFcm(act, fcm_token);
                        Log.i(MyUtil.TAG, "fcm_token: " + fcm_token);
                    }
                });
    }

    //로딩중 텍스트 애니메이션
    public void checkTimer() {
        final String device_version = getAppVersion();
        TimerTask adTask = new TimerTask() {
            @Override
            public void run() {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isReady && isPurchaseStateReady && !MyUtil.isNull(fcm_token)) {
                            isReady = false;
                            Log.i(StringUtil.TAG, "run: ");
                            setUserInfo();
                            timer.cancel();
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                version_text.setText("V" + device_version + "로딩중");
                            }
                        });
                    }
                }, 0);

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                version_text.setText("V" + device_version + "로딩중.");
                            }
                        });
                    }
                }, 250);

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                version_text.setText("V" + device_version + "로딩중..");
                            }
                        });
                    }
                }, 500);

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                version_text.setText("V" + device_version + "로딩중...");
                            }
                        });
                    }
                }, 750);

            }
        };
        timer.schedule(adTask, 0, 1000);
    }

    private void startProgram() {
        Log.i(StringUtil.TAG, "startProgram: ");
        if (isReqPermission()) {
            Intent intent = new Intent(SplashAct.this, PermissionAct.class);
            startActivity(intent);
            finish();
        } else {
            requestPermissionOverlay();
        }
    }

    private void requestPermissionOverlay() {
        Log.i(StringUtil.TAG, "requestPermissionOverlay: ");
        // Check if Android M or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Log.i("TEST_HOME", "requestPermissionOverlay: ");
                // Show alert dialog to the user saying a separate permission is needed
                // Launch the settings activity if the user prefers
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY);
            } else {
                isReady = true;
            }
        } else {
            isReady = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(StringUtil.TAG, "resultCode: " + resultCode);

        if (resultCode != RESULT_OK && resultCode != RESULT_CANCELED)
            return;

        switch (requestCode) {
            case OVERLAY:
                requestPermissionOverlay();
                break;
        }
    }

    private void preSetting() {
        Log.i(StringUtil.TAG, "preSetting: ");
        if (!StringUtil.isNull(UserPref.getFCheck(act))) {
            lastProcess();
        } else {
            String h = new SimpleDateFormat("HH", Locale.getDefault()).format(new Date(System.currentTimeMillis()));
            if (h == "23") {
                h = "00";
            } else {
                h = (Integer.valueOf(h) + 1) + "";
            }
            doCsetting(h);
        }
    }

    private void doCsetting(final String checkTime) {
        Log.i(StringUtil.TAG, "doCsetting: ");
        ReqBasic server = new ReqBasic(act, "https://coupang.adamstore.co.kr/lib/control.siso") {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (StringUtil.getStr(jo, "result").equalsIgnoreCase("Y")) {
                            UserPref.setFCheck(act, checkTime);
                        }
                        lastProcess();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                }
            }
        };

        server.addParams("dbControl", "setCoupangPartnersPush");
        server.addParams("grouptime", checkTime);
        server.addParams("site", "7");
        server.addParams("fcm", fcm_token);
        server.addParams("m_idx", MyUtil.getDeviceId(act));

        server.addParams("idx", UserPref.getIdx(act));
        server.addParams("m_uniq", MyUtil.getDeviceId(act));
        server.addParams("m_hp", MyUtil.getPhoneNumber(act));
        server.addParams("m_model", Build.MODEL);
        server.addParams("m_agent", MyUtil.getTelecom(act));
        server.execute(true, false);
    }


    private void alarmSetting() {
        Log.i("TEST_HOME", "alarmSetting: ");
        /* initialize alarm service */
        Calendar mCalendar = Calendar.getInstance();


        if (!StringUtil.isNull(UserPref.getAlarmCoupa(act))) {
            //푸시 알림 셋팅값이 있으면, 설정된 푸시시간의 12시 30분 이후에 알람등록.
            //푸시알림과 겹치지 않게 하기 위함.
            mCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(UserPref.getAlarmCoupa(act)) + 12);
            mCalendar.set(Calendar.MINUTE, 30);
            mCalendar.set(Calendar.SECOND, 0);
        } else {
            //푸시 알림 셋팅값이 없으면, 현재시간으로 부터 1시간 후  값으로 설정
            mCalendar.add(Calendar.HOUR_OF_DAY, 1);
        }

        UserPref.setAlarmCoupa(act, mCalendar.getTimeInMillis() + "");

        /* set alarm manager */
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        /* set pending intent */
        PendingIntent pendingIntent01 = PendingIntent.getBroadcast(act, 10, new Intent(act, CoupangReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);

        /* register alarm (버전별로 따로) */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), pendingIntent01);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            manager.setExact(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), pendingIntent01);
        } else {
            manager.set(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), pendingIntent01);
        }
    }


    private void checkVer() {
        ReqBasic checkVer = new ReqBasic(this, NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, final HttpResult resultData) {
                Log.d(MyUtil.TAG, "checkVer: " + resultData.getResult());

                if (resultData.getResult() != null) {
                    final String res = resultData.getResult();

                    act.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (!StringUtil.isNull(res)) {
                                    JSONObject jo = new JSONObject(res);
                                    if (StringUtil.getStr(jo, "result").equalsIgnoreCase("N")) {
                                        android.app.AlertDialog.Builder alertDialogBuilder =
                                                new android.app.AlertDialog.Builder(new ContextThemeWrapper(act, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert));
                                        alertDialogBuilder.setTitle("업데이트");
                                        alertDialogBuilder.setMessage("새로운 버전이 있습니다.")
                                                .setPositiveButton("업데이트 바로가기", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                                        intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=kr.core.bowwow"));
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                });
                                        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
                                        alertDialog.setCanceledOnTouchOutside(false);
                                        alertDialog.show();
                                    } else {
                                        startProgram();
                                    }
                                } else {
                                    startProgram();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                } else {
//                    Toast.makeText(SplashAct.this, "버전 정보를 받지 못했습니다.", Toast.LENGTH_SHORT).show();

                }
            }
        };

        checkVer.addParams("CONNECTCODE", "APP");
        checkVer.addParams("siteUrl", NetUrls.MEDIADOMAIN);
        checkVer.addParams("dbControl", "setPlaystorUpdateCheck");
        checkVer.addParams("thisVer", getAppVersion());
        checkVer.execute(true, true);
    }

    private void lastProcess() {
        if (MyUtil.isNull(dog_code)) {
            startActivity(new Intent(SplashAct.this, DogInfoAct.class));
        } else {
            if (Integer.parseInt(dog_code) > 0) {
                UserPref.setDogIdx(SplashAct.this, dog_code);
                getTalkList();
                startActivity(new Intent(SplashAct.this, MainActivity.class));
            } else {
                startActivity(new Intent(SplashAct.this, DogInfoAct.class));
            }
        }
        finish();
    }

    private void setUserInfo() {
        Log.i(StringUtil.TAG, "setUserInfo: ");
        String cellnum;
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        cellnum = tm.getLine1Number();

        ReqBasic login = new ReqBasic(this, NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                Log.d(MyUtil.TAG, "setUserInfo: " + resultData.getResult());
//                {"result":"Y","message":"성공적으로 등록하였습니다.","url":"","MEMCODE":"2","DOGCODE":"0" }
                if (resultData.getResult() != null) {

                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (jo.getString("result").equalsIgnoreCase("Y")) {
                            UserPref.setIdx(SplashAct.this, jo.getString("MEMCODE"));

                            dog_code = jo.getString("DOGCODE");

                            /* check coupa alarm */
                            if (StringUtil.isNull(UserPref.getAlarmCoupa(act))) {
                                alarmSetting();
                            }

                            preSetting();

                        } else {
                            Toast.makeText(SplashAct.this, jo.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(SplashAct.this, getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(SplashAct.this, getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                }

            }
        };

        login.addParams("CONNECTCODE", "APP");
        login.addParams("siteUrl", NetUrls.MEDIADOMAIN);
        login.addParams("dbControl", "setMemberUserRegi");
        login.addParams("_APP_MEM_IDX", UserPref.getIdx(this));
        login.addParams("m_fcm", fcm_token);
        if (MyUtil.isNull(cellnum)) {
            login.addParams("m_hp", "");
        } else {
            login.addParams("m_hp", cellnum.replaceFirst("[+]82", "0"));
        }
//        login.addParams("subscription",subState);
//        login.addParams("m_device_model",Build.MODEL);

        login.addParams("m_uniq", MyUtil.getDeviceId(this));

        TelephonyManager tManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        login.addParams("agent2", Build.MODEL + "@@@@@" + tManager.getNetworkOperatorName());

        login.execute(true, true);
    }

    private void billingCheck() {
        billingManager = new BillingManager(this, new BillingManager.AfterBilling() {
            @Override
            public void sendResult(Purchase purchase, boolean isSubcribe) {
                // 서버로 값 전송(결제 완료)
            }

            @Override
            public void getSubsriptionState(final String subscription, Purchase purchase) {
                // subscription = Y : 구독중, N : 구독X
                Log.d(MyUtil.TAG, "getSubsriptionState: " + subscription);

                UserPref.setSubscribeState(SplashAct.this, subscription);
                sub_state = subscription;
                isPurchaseStateReady = true;
            }
        });
    }

    private void getTalkList() {
        ReqBasic talkList = new ReqBasic(this, NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, final HttpResult resultData) {
                Log.d(MyUtil.TAG, "talklist splash: " + resultData.getResult());

                if (resultData.getResult() != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject jo = new JSONObject(resultData.getResult());

                                jo.getString("total");
                                jo.getString("result");
                                jo.getString("message");
                                jo.getString("data");

                                if (!MyUtil.isNull(jo.getString("data"))) {


                                    JSONArray ja = jo.getJSONArray("data");
                                    if (ja.length() > 0) {
                                        for (int i = 0; i < ja.length(); i++) {
                                            JSONObject msgData = ja.getJSONObject(i);
                                            ChatItem data = new ChatItem();

//                                msgData.getString("t_idx");
//                                msgData.getString("t_site");
//                                msgData.getString("t_user_idx");
//                                msgData.getString("t_type");
//                                msgData.getString("t_msg");
//                                msgData.getString("t_sound");
//                                msgData.getString("t_regdate");
//                                msgData.getString("t_editdate");
//                                msgData.getString("num");

                                            if (db.getLastItem(SplashAct.this) == null) {


                                                data.setT_idx(msgData.getString("t_idx"));
                                                data.setT_type(msgData.getString("t_type"));
                                                data.setT_msg(msgData.getString("t_msg"));
                                                data.setT_sound(NetUrls.MEDIADOMAIN + msgData.getString("t_sound"));
                                                data.setT_regdate(msgData.getString("t_regdate"));

                                                if (MyUtil.isNull(msgData.getString("t_sound_runtime"))) {
                                                    MediaPlayer mp = new MediaPlayer();
                                                    mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                                    try {
                                                        mp.setDataSource(data.getT_sound());
                                                        mp.prepare();
                                                        data.setDuration(String.valueOf(mp.getDuration()));
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                } else {
                                                    data.setDuration(msgData.getString("t_sound_runtime"));
                                                }

                                                db.chatItemInsert(SplashAct.this, data);

                                            } else {
                                                if (Integer.parseInt(db.getLastItem(SplashAct.this).getT_idx()) < Integer.parseInt(msgData.getString("t_idx"))) {
                                                    data.setT_idx(msgData.getString("t_idx"));
                                                    data.setT_type(msgData.getString("t_type"));
                                                    data.setT_msg(msgData.getString("t_msg"));
                                                    data.setT_sound(NetUrls.MEDIADOMAIN + msgData.getString("t_sound"));
                                                    data.setT_regdate(msgData.getString("t_regdate"));

                                                    if (MyUtil.isNull(msgData.getString("t_sound_runtime"))) {
                                                        MediaPlayer mp = new MediaPlayer();
                                                        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                                        try {
                                                            mp.setDataSource(data.getT_sound());
                                                            mp.prepare();
                                                            data.setDuration(String.valueOf(mp.getDuration()));
                                                        } catch (IOException e) {
                                                            e.printStackTrace();
                                                        }
                                                    } else {
                                                        data.setDuration(msgData.getString("t_sound_runtime"));
                                                    }

                                                    db.chatItemInsert(SplashAct.this, data);
                                                }
                                            }
                                        }

                                        if (app.chatItems.size() > 0) {
                                            app.chatItems.clear();
                                        }
                                        app.chatItems.addAll(db.getChatList(SplashAct.this));
//                            for (int i = 0; i < app.chatItems.size(); i++){
//                                Log.d(MyUtil.TAG, "item: "+app.chatItems.get(i).toString());
//                            }

                                    }
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
//                                Toast.makeText(SplashAct.this, getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                            }

                        }
                    }).start();
                } else {
//                    Toast.makeText(SplashAct.this, getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show(); // 대화내용 없거나 불러오기 실패
                }

            }
        };

        talkList.addParams("CONNECTCODE", "APP");
        talkList.addParams("siteUrl", NetUrls.MEDIADOMAIN);
//        talkList.addParams("APPCONNECTCODE","APP");
        talkList.addParams("dbControl", "setTalkDogList");
        talkList.addParams("_APP_MEM_IDX", UserPref.getIdx(this));
        talkList.addParams("MEMCODE", UserPref.getIdx(this));
        talkList.addParams("m_uniq", UserPref.getDeviceId(this));
        talkList.execute(true, false);
    }


    private String getAppVersion() {
        String version = "";
        try {
            PackageInfo i = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = i.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    private boolean isReqPermission() {
        // 필요권한 ( 전화 걸기 및 관리, 메세지 전송 및 보기, 주소록 액세스, 사진 및 미디어 파일 액세스, 위치정보)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                            checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                            checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
            ) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }


}
