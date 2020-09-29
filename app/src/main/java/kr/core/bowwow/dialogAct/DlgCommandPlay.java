package kr.core.bowwow.dialogAct;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import kr.core.bowwow.R;
import kr.core.bowwow.activity.BaseAct;
import kr.core.bowwow.app;
import kr.core.bowwow.customWidget.VisualizerView;
import kr.core.bowwow.databinding.DlgCommandBinding;
import kr.core.bowwow.dto.CommandItem;
import kr.core.bowwow.dto.pref.UserPref;
import kr.core.bowwow.network.HttpResult;
import kr.core.bowwow.network.NetUrls;
import kr.core.bowwow.network.ReqBasic;
import kr.core.bowwow.utils.MyUtil;

public class DlgCommandPlay extends BaseAct implements View.OnClickListener {

    DlgCommandBinding binding;

    MediaPlayer mediaPlayer;

    private Visualizer mVisualizer;
    private VisualizerView mVisualizerView;

    CommandItem data;

    boolean isUseState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.dlg_command);
        app.isTrans = true;

        binding.tvCommandName.setTypeface(app.tf_bmjua);

        data = (CommandItem)getIntent().getSerializableExtra("command");

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(data.getCsound());
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mVisualizerView = new VisualizerView(this);
        binding.drawview.addView(mVisualizerView);

        binding.tvPlaytime.setText(MyUtil.getTime(String.valueOf(mediaPlayer.getDuration())));

        mVisualizer = new Visualizer(mediaPlayer.getAudioSessionId());
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

        mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int i) {
                mVisualizerView.updateVisualizer(bytes);
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int i) {
            }
        },Visualizer.getMaxCaptureRate() / 2, true, false);

        binding.btnPlayBack.setOnClickListener(this);

        binding.tvCommandName.setText(data.getItemname());

    }

    private void pointMinus(){
        ReqBasic pointMinus = new ReqBasic(this, NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
//                {"result":"N","message":"포인트가 부족합니다.","url":"","point":""}
                Log.d(MyUtil.TAG, "pointMinus: "+resultData.getResult());

                if (resultData.getResult() != null){
                    try{
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (jo.getString("result").equalsIgnoreCase("Y")){
                            // 재생가능
                            isUseState = true;
                            playControl();
                        }else{
                            Toast.makeText(DlgCommandPlay.this, jo.getString("message"), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(DlgCommandPlay.this, DlgPayment.class));
                        }

                    }catch (JSONException e){
                        e.printStackTrace();
                        Toast.makeText(DlgCommandPlay.this, getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(DlgCommandPlay.this, getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                }

            }
        };

        pointMinus.addParams("CONNECTCODE","APP");
        pointMinus.addParams("siteUrl",NetUrls.MEDIADOMAIN);
        pointMinus.addParams("_APP_MEM_IDX", UserPref.getIdx(this));
        pointMinus.addParams("dbControl","setPointMinus");
        pointMinus.addParams("MEMCODE", UserPref.getIdx(this));
        pointMinus.addParams("m_uniq", UserPref.getDeviceId(this));
        pointMinus.addParams("MINUSP", "1");
        pointMinus.addParams("MINUS_CONTENTS", "명령하기 뼈다귀 사용");
        pointMinus.execute(true,true);
    }

    private void playControl(){
        binding.btnPlaystop.setSelected(!binding.btnPlaystop.isSelected());

        if (binding.btnPlaystop.isSelected()){
            app.isTrans = true;
            mVisualizer.setEnabled(true);
            mediaPlayer.start();

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mVisualizer.setEnabled(false);
                    binding.btnPlaystop.setSelected(false);
                    binding.tvPlaytime.setText(MyUtil.getTime(String.valueOf(mediaPlayer.getDuration())));
                }
            });

            new Thread(new Runnable() {
                @Override
                public void run() {
//                            while (mediaPlayer.getDuration() >= mediaPlayer.getCurrentPosition()){
                    while (!Thread.currentThread().isInterrupted()){
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (mediaPlayer != null) {
                                    if (mediaPlayer.isPlaying()) {
                                        binding.tvPlaytime.setText(MyUtil.getTime(String.valueOf(mediaPlayer.getCurrentPosition())));
                                    }
                                }

                            }
                        });
                    }

                }
            }).start();


        }else{
            mediaPlayer.stop();
            mVisualizer.setEnabled(false);
            // 서버 경로
//                    mediaPlayer = new MediaPlayer();
//                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.reset();

            try {
                mediaPlayer.setDataSource(data.getCsound());
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mVisualizer = new Visualizer(mediaPlayer.getAudioSessionId());
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
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_play_back:
                // 결제 제거
                playControl();

                // 결제 확인 (기본)
//                if (UserPref.getSubscribeState(this).equalsIgnoreCase("N")){
//                    if (!isUseState) {
//                        pointMinus();
//                    }else{
//                        playControl();
//                    }
////                    if (mediaPlayer.isPlaying()){
////                        playControl();
////                    }else {
////                        if (!isUseState) {
////                            pointMinus();
////                        }else{
////                            playControl();
////                        }
////                    }
//                    return;
//                }else{
//                    playControl();
//                }
//                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
            Thread.interrupted();
        }
        app.isTrans = false;
    }

}
