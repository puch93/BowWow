package kr.core.bowwow.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import kr.core.bowwow.R;
import kr.core.bowwow.app;
import kr.core.bowwow.databinding.ActivityPermissionBinding;
import kr.core.bowwow.dto.ChatItem;
import kr.core.bowwow.dto.pref.UserPref;
import kr.core.bowwow.network.HttpResult;
import kr.core.bowwow.network.NetUrls;
import kr.core.bowwow.network.ReqBasic;
import kr.core.bowwow.utils.DBHelper;
import kr.core.bowwow.utils.MyUtil;

public class PermissionAct extends BaseAct implements View.OnClickListener {

    ActivityPermissionBinding binding;
    DBHelper db = new DBHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_permission);

        binding.title.setTypeface(app.tf_bmjua);

        binding.btnSubmit.setOnClickListener(this);

        binding.tvPersonal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://bowwow.alrigo.co.kr/term.php"));
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
            ) {
                // 권한 허용안됨
                finish();

            } else {
                // 권한 허용됨
//                FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
//                        if (!task.isSuccessful()){
//                            Log.d(MyUtil.TAG, "getInstanceId failed",task.getException());
//                            return;
//                        }
//                        setUserInfo(task.getResult().getToken());
//                        Log.d(MyUtil.TAG, "getToken: "+task.getResult().getToken());
//                    }
//
//                });

                startActivity(new Intent(this, SplashAct.class));
                finish();

            }
        }

    }

    private void setUserInfo(String fcm){

        String cellnum;
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        cellnum = tm.getLine1Number();

        ReqBasic login = new ReqBasic(this, NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                Log.d(MyUtil.TAG, "permi setUserInfo: "+resultData.getResult());

                if (resultData.getResult() != null){

                    try{
                        JSONObject jo = new JSONObject(resultData.getResult());

                        // 멤버 인덱스 저장
                        if (jo.getString("result").equalsIgnoreCase("Y")){
                            UserPref.setIdx(PermissionAct.this,jo.getString("MEMCODE"));

                            if (MyUtil.isNull(jo.getString("DOGCODE"))){
                                startActivity(new Intent(PermissionAct.this,DogInfoAct.class));
                            }else{
                                if (Integer.parseInt(jo.getString("DOGCODE")) > 0) {
//                                UserPref.setDogIdx(SplashAct.this,jo.getString("DOGCODE"));
                                    getTalkList();
                                    startActivity(new Intent(PermissionAct.this, MainActivity.class));
                                }else{
                                    startActivity(new Intent(PermissionAct.this,DogInfoAct.class));
                                }
                            }

                        }else{
                            Toast.makeText(PermissionAct.this, jo.getString("message"), Toast.LENGTH_SHORT).show();
                        }

                        finish();

                    }catch (JSONException e){
                        e.printStackTrace();
                        Toast.makeText(PermissionAct.this, getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                        finish();
                    }

                }else{
                    Toast.makeText(PermissionAct.this, getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                    finish();
                }

            }
        };

        login.addParams("CONNECTCODE","APP");
        login.addParams("siteUrl",NetUrls.MEDIADOMAIN);
        login.addParams("dbControl","setMemberUserRegi");
        login.addParams("_APP_MEM_IDX",UserPref.getIdx(this));
        login.addParams("m_fcm",fcm);
        if (MyUtil.isNull(cellnum)){
            login.addParams("m_hp", "");
        }else {
            login.addParams("m_hp", cellnum.replaceFirst("[+]82", "0"));
        }
//        login.addParams("m_device_model",Build.MODEL);
        login.addParams("m_uniq", MyUtil.getDeviceId(this));
        login.execute(true,true);
    }

    private void getTalkList(){
        ReqBasic talkList = new ReqBasic(this, NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, final HttpResult resultData) {
                Log.d(MyUtil.TAG, "talklist permission: "+resultData.getResult());

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

                                            if (db.getLastItem(PermissionAct.this) == null) {

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

                                                db.chatItemInsert(PermissionAct.this, data);

                                            } else {
                                                if (Integer.parseInt(db.getLastItem(PermissionAct.this).getT_idx()) < Integer.parseInt(msgData.getString("t_idx"))) {
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

                                                    db.chatItemInsert(PermissionAct.this, data);
                                                }
                                            }
                                        }

                                        if (app.chatItems.size() > 0) {
                                            app.chatItems.clear();
                                        }
                                        app.chatItems.addAll(db.getChatList(PermissionAct.this));
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
                }else{
//                    Toast.makeText(SplashAct.this, getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show(); // 대화내용 없거나 불러오기 실패
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


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_submit:
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.RECORD_AUDIO
                    }, 0);
                }
                break;
        }
    }
}
