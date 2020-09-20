package kr.core.bowwow.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.common.internal.service.Common;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import kr.core.bowwow.R;
import kr.core.bowwow.activity.MainActivity;
import kr.core.bowwow.adapter.ChatAdapter;
import kr.core.bowwow.app;
import kr.core.bowwow.customWidget.VisualizerView;
import kr.core.bowwow.databinding.FragChattingBinding;
import kr.core.bowwow.detectApi.DetectorThread;
import kr.core.bowwow.detectApi.RecorderThread;
import kr.core.bowwow.dialogAct.DlgDogTrans;
import kr.core.bowwow.dialogAct.DlgPersonTrans;
import kr.core.bowwow.dto.ChatItem;
import kr.core.bowwow.dto.pref.UserPref;
import kr.core.bowwow.network.HttpResult;
import kr.core.bowwow.network.NetUrls;
import kr.core.bowwow.network.ReqBasic;
import kr.core.bowwow.service.ForegroundService;
import kr.core.bowwow.utils.DBHelper;
import kr.core.bowwow.utils.LayoutWebView;
import kr.core.bowwow.utils.MyUtil;
import kr.core.bowwow.utils.StringUtil;

public class Chatting extends Fragment implements View.OnClickListener {
    Activity act;
    FragChattingBinding binding;

    ChatAdapter adapter;
    ArrayList<ChatItem> searchList = new ArrayList<>();

    DBHelper db = new DBHelper();

    VisualizerView mVisualizerView;

    public static final int DETECT_NONE = 0;
    public static final int DETECT_WHISTLE = 1;
    public static int selectedDetection = DETECT_NONE;

    // detection parameters
    private DetectorThread detectorThread;
    private RecorderThread recorderThread;

    MediaPlayer mp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_chatting, container, false);
        act = getActivity();
        binding.title.setTypeface(app.tf_bmjua);

        setClickListener();
        setBanner();

        binding.rcvChat.setLayoutManager(new LinearLayoutManager(act));
//        binding.rcvChat.setHasFixedSize(true);
        adapter = new ChatAdapter(act,app.chatItems);
        binding.rcvChat.setAdapter(adapter);

//        getTalkList();

//        startService();
        setDetectApi();





//        YoYo.with(Techniques.FadeIn)
//                .duration(1000)
//                .repeat(YoYo.INFINITE)
//                .playOn(binding.ivSpeaker);

        mVisualizerView = new VisualizerView(getActivity());
//        binding.drawview.addView(mVisualizerView);

        binding.dogNameBottom.setText(app.myDogKname);
        YoYo.with(Techniques.FadeIn)
                .duration(1000)
                .repeat(YoYo.INFINITE)
                .playOn(binding.areaListening);

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.rcvChat.scrollToPosition(app.chatItems.size()-1);
        adapter.notifyDataSetChanged();
    }

    private void setDetectApi(){
        selectedDetection = DETECT_WHISTLE;
        recorderThread = new RecorderThread();
        recorderThread.start();
        detectorThread = new DetectorThread(recorderThread);
        detectorThread.setOnSignalsDetectedListener(MainActivity.mainApp);
        detectorThread.start();
    }

    private void releaseDetectApi(){
        if (recorderThread != null) {
            recorderThread.stopRecording();
            recorderThread = null;
        }
        if (detectorThread != null) {
            detectorThread.stopDetection();
            detectorThread = null;
        }
        selectedDetection = DETECT_NONE;
    }

    public void detectAni(final String filePath){
        app.isTrans = true;
        mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mp.setDataSource(filePath);
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Visualizer mVisualizer = new Visualizer(mp.getAudioSessionId());
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

        mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int i) {
                mVisualizerView.updateVisualizer(bytes);
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int i) {
            }
        }, Visualizer.getMaxCaptureRate() / 2, true, false);

        mVisualizer.setEnabled(true);
        if (mp != null) {
            mp.start();
        }

        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                app.isTrans = false;
                mVisualizer.setEnabled(false);
                mVisualizer.release();
                if (act != null) {
                    Intent dogTrans = new Intent(act, DlgDogTrans.class);
                    dogTrans.putExtra("path", filePath);
                    dogTrans.putExtra("td_run_time", String.valueOf(mp.getDuration()));
                    startActivity(dogTrans);
                }
                mp.reset();
            }
        });
    }

    private void setClickListener(){
        binding.btnSend.setOnClickListener(this);

        binding.btnSearch.setOnClickListener(this);
        binding.btnSerchclose.setOnClickListener(this);
    }

    private void setBanner(){
        if (MyUtil.isNull(app.bannerState)){
            binding.bannerArea.getRoot().setVisibility(View.VISIBLE);
            binding.bannerArea.bannerAdmob.setVisibility(View.VISIBLE);
            // admob 설정

            binding.bannerArea.bannerAdmob.loadAd(app.adRequest);
//            binding.bannerArea.getRoot().setVisibility(View.GONE);
        }else{
            switch (app.bannerState){
                case MyUtil.BANNER:
                    binding.bannerArea.getRoot().setVisibility(View.VISIBLE);
                    binding.bannerArea.bannerAdmob.setVisibility(View.GONE);
                    binding.bannerArea.bannerCore.setVisibility(View.VISIBLE);

                    // 이미지 세팅
//                    Glide.with(this)
//                            .load(app.bannerImg)
//                            .into(binding.bannerArea.bannerCore);
//                    binding.bannerArea.bannerCore.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            if (MyUtil.isNull(app.bannerLink)) {
//                                Toast.makeText(getContext(), "연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
//                            }else{
//                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(app.bannerLink)));
//                            }
//                        }
//                    });
                    getCoupaBanner();
                    break;
                case MyUtil.ADMOB:
                    binding.bannerArea.getRoot().setVisibility(View.VISIBLE);
                    binding.bannerArea.bannerAdmob.setVisibility(View.VISIBLE);
                    // admob 설정
                    binding.bannerArea.bannerAdmob.loadAd(app.adRequest);
                    break;
                case MyUtil.NONE:
                    binding.bannerArea.getRoot().setVisibility(View.GONE);
                    break;
            }
        }
    }

    private void getCoupaBanner() {
        ReqBasic server = new ReqBasic(act, "https://coupang.adamstore.co.kr/lib/control.siso") {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    final String res = resultData.getResult();

                    if (!StringUtil.isNull(res)) {
                        act.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    //Log.d(HoUtils.TAG, "결과 : " + result);
                                    final JSONObject jo = new JSONObject(res);
                                    if (StringUtil.getStr(jo, "result").equalsIgnoreCase("Y")) {
                                        JSONObject job = jo.getJSONObject("data");

                                        String coupang_url = StringUtil.getStr(job, "coupang_url");
                                        String banner = StringUtil.getStr(job, "banner");

                                        Log.i("TEST_HOME", "coupang_url: " + coupang_url);
                                        Log.i("TEST_HOME", "banner: " + banner);


                                        Glide.with(act).load(banner).into(binding.bannerArea.bannerCore);
                                        binding.bannerArea.bannerCore.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(act, LayoutWebView.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                            }
                                        });
                                    } else {

                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } else {
                }
            }
        };

        server.addParams("dbControl", "getCoupangPartnersInfo");
        server.addParams("si_idx", "7");
        server.execute(true, false);
    }

    @Override
    public void onPause() {
        super.onPause();
        adapter.stopMediaplayer();

    }

    @Override
    public void onStop() {
        super.onStop();
        releaseDetectApi();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(MyUtil.TAG, "onDestroy: ");
//        stopService();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_serchclose:
                binding.searchArea.setVisibility(View.GONE);
                binding.title.setVisibility(View.VISIBLE);
//                binding.llDetectmsgArea.setVisibility(View.VISIBLE);
                binding.etSearch.setText(null);
                adapter = new ChatAdapter(act,app.chatItems);
                binding.rcvChat.setAdapter(adapter);
                break;
            case R.id.btn_search:
                if (binding.searchArea.getVisibility() != View.VISIBLE) {
                    binding.searchArea.setVisibility(View.VISIBLE);
                    binding.title.setVisibility(View.GONE);
//                    binding.llDetectmsgArea.setVisibility(View.GONE);
                }else{
                    // 검색
                    if (binding.etSearch.length() == 0){
                        Toast.makeText(act, "검색어를 입력하세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Toast.makeText(act, binding.etSearch.getText()+" 검색", Toast.LENGTH_SHORT).show();

                    searchList.clear();
                    searchList.addAll(db.getChatSearchList(act,binding.etSearch.getText().toString()));

                    if (searchList.size() == 0){
                        Toast.makeText(act, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                        adapter = new ChatAdapter(act, searchList);
                        binding.rcvChat.setAdapter(adapter);
                    }else {
                        adapter = new ChatAdapter(act, searchList);
                        binding.rcvChat.setAdapter(adapter);
                    }
                }
                break;
            case R.id.btn_send:
                Log.d(MyUtil.TAG, "btn_send");
                if (binding.etMsg.length() == 0){
                    Toast.makeText(act, "메세지를 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(binding.btnSend.getWindowToken(), 0);


                if (!app.isTrans) {
                    Intent pmsg = new Intent(act, DlgPersonTrans.class);
                    pmsg.putExtra("pmsg",binding.etMsg.getText().toString());
                    startActivity(pmsg);
                    binding.etMsg.setText(null);
                }
                break;
        }
    }
}
