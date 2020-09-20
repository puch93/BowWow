package kr.core.bowwow.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import kr.core.bowwow.R;
import kr.core.bowwow.app;
import kr.core.bowwow.databinding.ActivityMainBinding;
import kr.core.bowwow.detectApi.DetectorThread;
import kr.core.bowwow.detectApi.OnSignalsDetectedListener;
import kr.core.bowwow.detectApi.RecorderThread;
import kr.core.bowwow.dialogAct.DlgDogTrans;
import kr.core.bowwow.dialogAct.DlgPayment;
import kr.core.bowwow.dto.ChatItem;
import kr.core.bowwow.dto.pref.UserPref;
import kr.core.bowwow.fragments.Chatting;
import kr.core.bowwow.fragments.Command;
import kr.core.bowwow.fragments.More;
import kr.core.bowwow.fragments.Mydog;
import kr.core.bowwow.network.HttpResult;
import kr.core.bowwow.network.NetUrls;
import kr.core.bowwow.network.ReqBasic;
import kr.core.bowwow.service.ForegroundService;
import kr.core.bowwow.utils.DBHelper;
import kr.core.bowwow.utils.MyUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnSignalsDetectedListener {

    ActivityMainBinding binding;

    public static MainActivity mainApp;

    public static final int DETECT_NONE = 0;
    public static final int DETECT_WHISTLE = 1;
    public static int selectedDetection = DETECT_NONE;

    // detection parameters
    private DetectorThread detectorThread;
    private RecorderThread recorderThread;

    private int numWhistleDetected = 0;

    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;

    int timeCount = 0;
    final int RESETTIME = 3600;

    public int currPos = -1;

    DBHelper db = new DBHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler.sendEmptyMessage(0);

        binding = DataBindingUtil.setContentView(this,R.layout.activity_main);
        mainApp = this;

        Log.d(MyUtil.TAG, "dir: "+ Environment.getDataDirectory());

//        if (isReqPermission()){
//            reqPermission();
//        }else{
//
//        }

//        selectedDetection = DETECT_WHISTLE;
//        recorderThread = new RecorderThread();
//        recorderThread.start();
//        detectorThread = new DetectorThread(recorderThread);
//        detectorThread.setOnSignalsDetectedListener(MainActivity.mainApp);
//        detectorThread.start();

        getDogInfo();

        binding.btnMydog.setOnClickListener(this);
        binding.btnChat.setOnClickListener(this);
        binding.btnCommand.setOnClickListener(this);
        binding.btnMore.setOnClickListener(this);

        String detect = getIntent().getStringExtra("detect");

        if (MyUtil.isNull(detect)) {
            setFragment(0);

            if (isReqPermission()){
                Toast.makeText(mainApp, "마이크 권한을 허용해주세요.", Toast.LENGTH_SHORT).show();
                reqPermission();
            }else{
//                startService();
            }

//            if (!isWhitelist()){
//                regtWhiteList();
//            }

        }else if (detect.equalsIgnoreCase("y")){
            if (currPos != 1) {
                setFragment(1);
            }
        }

        // db 세팅
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                getTalkList();
//            }
//        }).start();

//        if (UserPref.isDetected(this)) {
//        }
    }

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (currPos == 1){
            setFragment(0);
        }else{
            if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
                finishAffinity();
            } else {
                backPressedTime = tempTime;
                Toast.makeText(this, "뒤로 버튼을 한번 더 누르시면 종료됩니다", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isWhitelist(){
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        boolean isWhiteListing = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            isWhiteListing = pm.isIgnoringBatteryOptimizations(getPackageName());
        }
        return isWhiteListing;
    }

    private void regtWhiteList(){
        AlertDialog.Builder setdialog = new AlertDialog.Builder(MainActivity.this);
        setdialog.setTitle("추가 설정이 필요합니다.")
                .setMessage("어플을 문제없이 사용하기 위해서는 해당 어플을 \"배터리 사용량 최적화\" 목록에서 \"제외\"해야 합니다. 설정하시겠습니까?")
                .setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent  = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        intent.setData(Uri.parse("package:"+ getPackageName()));
                        startActivity(intent);
//                        startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
                    }
                })
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "설정을 취소했습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .create()
                .show();
    }

    private void getDogInfo(){
        // 반려견 정보 가져오기 및 세팅
        ReqBasic dogInfo = new ReqBasic(this,NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                Log.d(MyUtil.TAG, "getDogInfo: "+resultData.getResult());
//                {"result":"Y","message":"성공적으로 수정하였습니다.","url":"","DOGCODE":"{"d_idx":"359","d_site":"1","d_user_idx":"0","d_pimg":"\/UPLOAD\/DOG_INFO\/30726684_eKARpZkE_profimg1217170040272885618.jpg","d_kname":"\uba4d\uba4d","d_ename":"mm","d_breed":"\ubd88\ub3c5","d_gender":"\ub0a8","d_birth":"2019.11.01","d_regdate":"2019-12-17 17:00:53","d_editdate":"0000-00-00 00:00:00"}"}

                if (resultData.getResult() != null){
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (jo.getString("result").equalsIgnoreCase("Y")){
//                        Log.d(MyUtil.TAG, "DOGCODE: "+jo.getJSONObject("DOGCODE"));
                            Log.d(MyUtil.TAG, "DOGCODE: "+jo.getString("DOGCODE"));

                            JSONObject dInfo = jo.getJSONObject("DOGCODE");

                            app.myDogImg = NetUrls.MEDIADOMAIN + dInfo.getString("d_pimg");
                            app.myDogBreed = dInfo.getString("d_breed");
                            app.myDogGender = dInfo.getString("d_gender");
                            app.myDogBirth = dInfo.getString("d_birth");
                            app.myDogKname = dInfo.getString("d_kname");

                        }else{
                            Toast.makeText(MainActivity.this, jo.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(MainActivity.this, getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                }

            }
        };

        dogInfo.addParams("CONNECTCODE","APP");
        dogInfo.addParams("siteUrl",NetUrls.MEDIADOMAIN);
        dogInfo.addParams("dbControl","setDogInfo");
        dogInfo.addParams("_APP_MEM_IDX", UserPref.getIdx(this));
        dogInfo.addParams("MEMCODE", UserPref.getIdx(this));
        dogInfo.addParams("m_uniq", UserPref.getDeviceId(this));
        dogInfo.execute(true,true);

    }

    private void getTalkList(){
        ReqBasic talkList = new ReqBasic(this, NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, final HttpResult resultData) {
                Log.d(MyUtil.TAG, "getTalkList: "+resultData.getResult());

                if (resultData.getResult() != null){
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

                                    if (db.getLastItem(MainActivity.this) == null) {

                                        data.setT_idx(msgData.getString("t_idx"));
                                        data.setT_type(msgData.getString("t_type"));
                                        data.setT_msg(msgData.getString("t_msg"));
                                        data.setT_sound(NetUrls.MEDIADOMAIN + msgData.getString("t_sound"));
                                        data.setT_regdate(msgData.getString("t_regdate"));

                                        if (MyUtil.isNull(msgData.getString("t_sound_runtime"))){
                                            MediaPlayer mp = new MediaPlayer();
                                            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                            try {
                                                mp.setDataSource(data.getT_sound());
                                                mp.prepare();
                                                data.setDuration(String.valueOf(mp.getDuration()));
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }else{
                                            data.setDuration(msgData.getString("t_sound_runtime"));
                                        }

                                        db.chatItemInsert(MainActivity.this, data);

                                    } else {
                                        if (Integer.parseInt(db.getLastItem(MainActivity.this).getT_idx()) < Integer.parseInt(msgData.getString("t_idx"))) {
                                            data.setT_idx(msgData.getString("t_idx"));
                                            data.setT_type(msgData.getString("t_type"));
                                            data.setT_msg(msgData.getString("t_msg"));
                                            data.setT_sound(NetUrls.MEDIADOMAIN + msgData.getString("t_sound"));
                                            data.setT_regdate(msgData.getString("t_regdate"));

                                            if (MyUtil.isNull(msgData.getString("t_sound_runtime"))){
                                                MediaPlayer mp = new MediaPlayer();
                                                mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                                try {
                                                    mp.setDataSource(data.getT_sound());
                                                    mp.prepare();
                                                    data.setDuration(String.valueOf(mp.getDuration()));
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }else{
                                                data.setDuration(msgData.getString("t_sound_runtime"));
                                            }

                                            db.chatItemInsert(MainActivity.this, data);
                                        }
                                    }
                                }

                                if (app.chatItems.size() > 0) {
                                    app.chatItems.clear();
                                }
                                app.chatItems.addAll(db.getChatList(MainActivity.this));
//                            for (int i = 0; i < app.chatItems.size(); i++){
//                                Log.d(MyUtil.TAG, "item: "+app.chatItems.get(i).toString());
//                            }

                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                    }

                        }
                    }).start();
                }else{
                    Toast.makeText(MainActivity.this, getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show(); // 대화내용 없거나 불러오기 실패
                }

            }
        };

        talkList.addParams("CONNECTCODE","APP");
        talkList.addParams("siteUrl",NetUrls.MEDIADOMAIN);
//        talkList.addParams("APPCONNECTCODE","APP");
        talkList.addParams("dbControl","setTalkDogList");
        talkList.addParams("_APP_MEM_IDX", UserPref.getIdx(this));
        talkList.addParams("MEMCODE", UserPref.getIdx(this));
        talkList.addParams("m_uniq", UserPref.getDeviceId(this));
        talkList.execute(true,false);

    }

    public Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
            if(timeCount == RESETTIME){
                timeCount = 0;
//                addLips();   // 1시간 후 뼈다귀 추가 (5개까지 가능)
            }

            if(MyUtil.isAppOnForeground(MainActivity.this)) {
                timeCount++;
            }else{
                // 화면이 꺼졌거나 앱이 백그라운드로 들어갔을 때
            }

            mHandler.sendEmptyMessageDelayed(0,1000);
        }
    };


    public void startService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
//        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            startForegroundService(serviceIntent);
        }else{
            startService(serviceIntent);
        }

    }

    public void stopService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        stopService();
    }

    private void setFragment(int idx){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        Fragment frag = null;
        switch (idx){
            case 0:
                currPos = 0;
                frag = new Mydog();
                binding.btnMydog.setSelected(true);
                binding.btnChat.setSelected(false);
                binding.btnCommand.setSelected(false);
                binding.btnMore.setSelected(false);
                break;
            case 1:
                currPos = 1;
                frag = new Chatting();
                binding.btnMydog.setSelected(false);
                binding.btnChat.setSelected(true);
                binding.btnCommand.setSelected(false);
                binding.btnMore.setSelected(false);
                break;
            case 2:
                currPos = 2;
                frag = new Command();
                binding.btnMydog.setSelected(false);
                binding.btnChat.setSelected(false);
                binding.btnCommand.setSelected(true);
                binding.btnMore.setSelected(false);
                break;
            case 3:
                currPos = 3;
                frag = new More();
                binding.btnMydog.setSelected(false);
                binding.btnChat.setSelected(false);
                binding.btnCommand.setSelected(false);
                binding.btnMore.setSelected(true);
                break;
        }

        ft.replace(R.id.fragment_area,frag);
        ft.commit();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_mydog:
                setFragment(0);
                break;
            case R.id.btn_chat:
                setFragment(1);
                break;
            case R.id.btn_command:
                setFragment(2);
                break;
            case R.id.btn_more:
                setFragment(3);
                break;
        }
    }

    private void reqPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(new String[]{
                    Manifest.permission.RECORD_AUDIO
            },0);
        }
    }

    private boolean isReqPermission() {
        // 필요권한 ( 전화 걸기 및 관리, 메세지 전송 및 보기, 주소록 액세스, 사진 및 미디어 파일 액세스, 위치정보)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (
                    checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public void onBarkDetected(final String filePath) {
        final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_area);
        if (fragment instanceof Chatting){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ((Chatting)fragment).detectAni(filePath);
                }
            }).start();
        }

    }

}
