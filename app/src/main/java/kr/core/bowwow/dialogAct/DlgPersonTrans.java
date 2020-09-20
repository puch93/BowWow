package kr.core.bowwow.dialogAct;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import kr.core.bowwow.R;
import kr.core.bowwow.activity.MainActivity;
import kr.core.bowwow.activity.SplashAct;
import kr.core.bowwow.app;
import kr.core.bowwow.databinding.DlgTranspersonBinding;
import kr.core.bowwow.dto.ChatItem;
import kr.core.bowwow.dto.pref.UserPref;
import kr.core.bowwow.network.HttpResult;
import kr.core.bowwow.network.MultipartUtility;
import kr.core.bowwow.network.NetUrls;
import kr.core.bowwow.network.ReqBasic;
import kr.core.bowwow.utils.DBHelper;
import kr.core.bowwow.utils.MyUtil;

public class DlgPersonTrans extends Activity {

    DlgTranspersonBinding binding;

    ChatItem data;
    String msg;

    DBHelper db = new DBHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.dlg_transperson);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);


        binding.dogName.setText(app.myDogKname);

        Glide.with(this).load(app.myDogImg).transform(new CircleCrop()).into(binding.dogImage);
        Glide.with(this).load(R.raw.ptodog).transform(new CircleCrop()).into(binding.dogImageGif);

        if (!MyUtil.isNull(getIntent().getStringExtra("pmsg"))){
            msg = getIntent().getStringExtra("pmsg");
        }

        app.isTrans = true;

        regPersonSay();

        binding.btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 글 전송 -> 대화값 추가 -> 사운드 파일 리턴

    }

    private void regPersonSay(){
        ReqBasic personSay = new ReqBasic(this,NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                Log.d(MyUtil.TAG, "regPersonSay: "+resultData.getResult());
//                {"result":"Y","message":"성공적으로 등록하였습니다.","url":"","MEMCODE":"1"}

                if (resultData.getResult() != null){

                    try{
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (jo.getString("result").equalsIgnoreCase("Y")){
                            getTalkList();
                        }else{
                            Toast.makeText(DlgPersonTrans.this, jo.getString("message"), Toast.LENGTH_SHORT).show();
                            app.isTrans = false;
                            finish();
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                        Toast.makeText(DlgPersonTrans.this, getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                        app.isTrans = false;
                        finish();
                    }

                }else{
                    Toast.makeText(DlgPersonTrans.this, getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                    app.isTrans = false;
                    finish();
                }
            }
        };

        personSay.addParams("CONNECTCODE","APP");
        personSay.addParams("siteUrl",NetUrls.MEDIADOMAIN);
        personSay.addParams("dbControl","setTalkPeopleRegi");
        personSay.addParams("_APP_MEM_IDX", UserPref.getIdx(this));
        personSay.addParams("MEMCODE", UserPref.getIdx(this));
        personSay.addParams("m_uniq", UserPref.getDeviceId(this));
        personSay.addParams("t_msg",msg);
        personSay.execute(true,false);

    }

    private void getTalkList(){
        ReqBasic talkList = new ReqBasic(this, NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                Log.d(MyUtil.TAG, "getTalkList: "+resultData.getResult());

                if (resultData.getResult() != null){

                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        jo.getString("total");
                        jo.getString("result");
                        jo.getString("message");
                        jo.getString("data");

                        if (jo.getString("result").equalsIgnoreCase("Y")){

                            JSONArray ja = jo.getJSONArray("data");

                            if (ja.length() > 0){

                                if (db.getChatList(DlgPersonTrans.this) != null){
                                    if (db.getChatList(DlgPersonTrans.this).size() < ja.length()){
                                        for (int i = 0; i < ja.length(); i++){
                                            JSONObject msgData = ja.getJSONObject(i);
                                            ChatItem data = new ChatItem();

                                            if (db.getLastItem(DlgPersonTrans.this) == null){

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

                                                db.chatItemInsert(DlgPersonTrans.this,data);

                                            }else{
                                                if (Integer.parseInt(db.getLastItem(DlgPersonTrans.this).getT_idx()) < Integer.parseInt(msgData.getString("t_idx"))){
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

                                                    db.chatItemInsert(DlgPersonTrans.this,data);
                                                }
                                            }
                                        }

                                        if(app.chatItems.size() > 0){
                                            app.chatItems.clear();
                                        }
                                        app.chatItems.addAll(db.getChatList(DlgPersonTrans.this));
                                    }
                                }else{
                                    for (int i = 0; i < ja.length(); i++){
                                        JSONObject msgData = ja.getJSONObject(i);
                                        ChatItem data = new ChatItem();

                                        if (db.getLastItem(DlgPersonTrans.this) == null){

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

                                            db.chatItemInsert(DlgPersonTrans.this,data);

                                        }else{
                                            if (Integer.parseInt(db.getLastItem(DlgPersonTrans.this).getT_idx()) < Integer.parseInt(msgData.getString("t_idx"))){
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

                                                db.chatItemInsert(DlgPersonTrans.this,data);
                                            }
                                        }
                                    }

                                    if(app.chatItems.size() > 0){
                                        app.chatItems.clear();
                                    }
                                    app.chatItems.addAll(db.getChatList(DlgPersonTrans.this));
                                }

                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                app.isTrans = false;
                                finish();
                            }

                        }else{
                            Toast.makeText(DlgPersonTrans.this, jo.getString("message") , Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(DlgPersonTrans.this, getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                        if(app.chatItems.size() > 0){
                            app.chatItems.clear();
                        }
                        app.chatItems.addAll(db.getChatList(DlgPersonTrans.this));
                        app.isTrans = false;
                        finish();
                    }

                }else{
                    Toast.makeText(DlgPersonTrans.this, getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                    if(app.chatItems.size() > 0){
                        app.chatItems.clear();
                    }
                    app.chatItems.addAll(db.getChatList(DlgPersonTrans.this));
                    app.isTrans = false;
                    finish();
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
}
