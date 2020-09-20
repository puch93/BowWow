package kr.core.bowwow.dialogAct;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import kr.core.bowwow.R;
import kr.core.bowwow.activity.MainActivity;
import kr.core.bowwow.app;
import kr.core.bowwow.customWidget.VisualizerView;
import kr.core.bowwow.databinding.DlgTransdogBinding;
import kr.core.bowwow.dto.ChatItem;
import kr.core.bowwow.dto.pref.UserPref;
import kr.core.bowwow.network.HttpResult;
import kr.core.bowwow.network.MultipartUtility;
import kr.core.bowwow.network.NetUrls;
import kr.core.bowwow.network.ReqBasic;
import kr.core.bowwow.utils.DBHelper;
import kr.core.bowwow.utils.MyUtil;

public class DlgDogTrans extends Activity {

    DlgTransdogBinding binding;

    DBHelper db = new DBHelper();

    String filePath,td_run_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.dlg_transdog);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);

        AudioManager mAudioManger = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        if (mAudioManger.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
            Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vib.vibrate(1000);
        }

        if (mAudioManger.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            Uri nofitication = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), nofitication);
            ringtone.play();
        }

        binding.dogName.setText(app.myDogKname);

        filePath = getIntent().getStringExtra("path");
        td_run_time = getIntent().getStringExtra("td_run_time");

        app.isTrans = true;

        Log.d(MyUtil.TAG, "filePath: "+filePath);
        if (MyUtil.isNull(filePath)) {
            Toast.makeText(DlgDogTrans.this, "파일 경로를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
            app.isTrans = false;
            finish();
        } else {
            regDogSay();
        }

        Glide.with(this).load(app.myDogImg).transform(new CircleCrop()).into(binding.dogImage);

        binding.btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void regDogSay() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    MultipartUtility mu = new MultipartUtility(NetUrls.DOMAIN, "UTF-8");

                    mu.addFormField("CONNECTCODE", "APP");
                    mu.addFormField("siteUrl", NetUrls.MEDIADOMAIN);
                    mu.addFormField("dbControl", "setTalkDogRegi");
                    mu.addFormField("_APP_MEM_IDX", UserPref.getIdx(DlgDogTrans.this));
                    mu.addFormField("MEMCODE", UserPref.getIdx(DlgDogTrans.this));
                    mu.addFormField("m_uniq", UserPref.getDeviceId(DlgDogTrans.this));
                    mu.addFormField("td_run_time", MyUtil.getTime(td_run_time));
                    File sound = new File(filePath);
                    mu.addFilePart("t_sound", sound);

                    String res = mu.finish();
                    Log.d(MyUtil.TAG, "dogtalk reg: " + res);

                    if (MyUtil.isNull(res)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DlgDogTrans.this, getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        try {

                            JSONObject jo = new JSONObject(res);

                            if (jo.getString("result").equalsIgnoreCase("Y")) {
                                getTalkList();
                            } else {
                                final String msg = jo.getString("message");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(DlgDogTrans.this, msg, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private void getTalkList() {
        ReqBasic talkList = new ReqBasic(this, NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                Log.d(MyUtil.TAG, "getTalkList: " + resultData.getResult());

                if (resultData.getResult() != null) {

                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        jo.getString("total");
                        jo.getString("result");
                        jo.getString("message");
                        jo.getString("data");
                        if (jo.getString("result").equalsIgnoreCase("Y")) {
                            JSONArray ja = jo.getJSONArray("data");
                            if (ja.length() > 0) {

                                if (db.getChatList(DlgDogTrans.this) != null) {
                                    if (db.getChatList(DlgDogTrans.this).size() < ja.length()) {
                                        for (int i = 0; i < ja.length(); i++) {
                                            JSONObject msgData = ja.getJSONObject(i);
                                            ChatItem data = new ChatItem();

                                            if (db.getLastItem(DlgDogTrans.this) == null) {

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

                                                db.chatItemInsert(DlgDogTrans.this, data);

                                            } else {
                                                if (Integer.parseInt(db.getLastItem(DlgDogTrans.this).getT_idx()) < Integer.parseInt(msgData.getString("t_idx"))) {
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

                                                    db.chatItemInsert(DlgDogTrans.this, data);
                                                }
                                            }
                                        }

                                        if (app.chatItems.size() > 0) {
                                            app.chatItems.clear();
                                        }
                                        app.chatItems.addAll(db.getChatList(DlgDogTrans.this));
                                    }
                                } else {
                                    for (int i = 0; i < ja.length(); i++) {
                                        JSONObject msgData = ja.getJSONObject(i);
                                        ChatItem data = new ChatItem();

                                        if (db.getLastItem(DlgDogTrans.this) == null) {

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

                                            db.chatItemInsert(DlgDogTrans.this, data);

                                        } else {
                                            if (Integer.parseInt(db.getLastItem(DlgDogTrans.this).getT_idx()) < Integer.parseInt(msgData.getString("t_idx"))) {
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

                                                db.chatItemInsert(DlgDogTrans.this, data);
                                            }
                                        }
                                    }

                                    if (app.chatItems.size() > 0) {
                                        app.chatItems.clear();
                                    }
                                    app.chatItems.addAll(db.getChatList(DlgDogTrans.this));
                                }

//                                Intent movetab = new Intent(DlgDogTrans.this, MainActivity.class);
//                                movetab.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                movetab.putExtra("detect", "y");
//                                startActivity(movetab);
                                app.isTrans = false;
                                finish();

                            }

                        } else {
                            Toast.makeText(DlgDogTrans.this, jo.getString("message"), Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(DlgDogTrans.this, getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                        if (app.chatItems.size() > 0) {
                            app.chatItems.clear();
                        }
                        app.chatItems.addAll(db.getChatList(DlgDogTrans.this));
                        app.isTrans = false;
                        finish();
                    }

                } else {
                    Toast.makeText(DlgDogTrans.this, getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                    if (app.chatItems.size() > 0) {
                        app.chatItems.clear();
                    }
                    app.chatItems.addAll(db.getChatList(DlgDogTrans.this));
                    app.isTrans = false;
                    finish();
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

}
