package kr.core.bowwow.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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
import kr.core.bowwow.dialogAct.DlgCommandPlay;
import kr.core.bowwow.dialogAct.DlgDogTrans;
import kr.core.bowwow.dialogAct.DlgPayment;
import kr.core.bowwow.dialogAct.DlgPersonTrans;
import kr.core.bowwow.dto.ChatItem;
import kr.core.bowwow.dto.CommandItem;
import kr.core.bowwow.dto.pref.UserPref;
import kr.core.bowwow.network.HttpResult;
import kr.core.bowwow.network.NetUrls;
import kr.core.bowwow.network.ReqBasic;
import kr.core.bowwow.service.ForegroundService;
import kr.core.bowwow.utils.AllOfDecoration;
import kr.core.bowwow.utils.DBHelper;
import kr.core.bowwow.utils.LayoutWebView;
import kr.core.bowwow.utils.MyUtil;
import kr.core.bowwow.utils.StringUtil;

import static android.app.Activity.RESULT_OK;

public class Chatting extends Fragment implements View.OnClickListener {
    public static Activity act;
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
        binding.dogName.setTypeface(app.tf_bmjua);
        binding.textListening.setTypeface(app.tf_bmjua);

        Animation startAnimation = AnimationUtils.loadAnimation(act, R.anim.blink_anim);
        binding.clickBtn.startAnimation(startAnimation);

        setClickListener();
        getCoupaBanner();

//        getMyPoint();

        binding.rcvChat.setLayoutManager(new LinearLayoutManager(act));

//        binding.rcvChat.setHasFixedSize(true);
        adapter = new ChatAdapter(act, app.chatItems);
        binding.rcvChat.setAdapter(adapter);
        binding.rcvChat.addItemDecoration(new AllOfDecoration(act, "chatting"));

//        getTalkList();

//        startService();

//        YoYo.with(Techniques.FadeIn)
//                .duration(1000)
//                .repeat(YoYo.INFINITE)
//                .playOn(binding.ivSpeaker);

        mVisualizerView = new VisualizerView(getActivity());
//        binding.drawview.addView(mVisualizerView);

        binding.dogNameBottom.setText(app.myDogKname);
//        YoYo.with(Techniques.FadeIn)
//                .duration(1000)
//                .repeat(YoYo.INFINITE)
//                .playOn(binding.areaListening);
        binding.dogName.setText(app.myDogKname);
        binding.dogName02.setText(app.myDogKname);

        Glide.with(act).load(R.raw.pettranslate_img_translate01_animation).into(binding.gifDog);

        binding.areaListening.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if(UserPref.getSubscribeState(act).equalsIgnoreCase("Y")) {
                            act.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    binding.areaListeningAll.setVisibility(View.VISIBLE);
                                }
                            });
                            binding.areaListening.setPressed(true);
                            setDetectApi();
                        } else {
                            if(Integer.parseInt(binding.boneCount.getText().toString()) > 0) {
                                Log.i(StringUtil.TAG, "onTouch: ACTION_DOWN");
                                pointMinusDog();
                                act.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        binding.areaListeningAll.setVisibility(View.VISIBLE);
                                    }
                                });
                                binding.areaListening.setPressed(true);
                                setDetectApi();
                            } else {
                                act.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(act, "뼈다귀 개수가 모자랍니다. 충전 후 이용해주세요.", Toast.LENGTH_SHORT).show();
                                        act.startActivity(new Intent(act, DlgPayment.class));
                                    }
                                });
                            }
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        Log.i(StringUtil.TAG, "onTouch: ACTION_UP");
                        releaseDetectApi();

                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                act.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        binding.areaListeningAll.setVisibility(View.GONE);
                                    }
                                });
                                binding.areaListening.setPressed(false);
                            }
                        }, 500);
                        return true;
                }
                return false;
            }
        });


        binding.areaListeningAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

//        setKeyboardVisibilityListener();


        adapter.setList(app.chatItems);
        binding.rcvChat.scrollToPosition(app.chatItems.size() - 1);

        return binding.getRoot();
    }

    private void setKeyboardVisibilityListener() {
        final View parentView = ((ViewGroup) act.findViewById(android.R.id.content)).getChildAt(0);
        parentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            private boolean alreadyOpen;
            private final int defaultKeyboardHeightDP = 100;
            private final int EstimatedKeyboardDP = defaultKeyboardHeightDP + (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 48 : 0);
            private final Rect rect = new Rect();

            @Override
            public void onGlobalLayout() {
                int estimatedKeyboardHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, EstimatedKeyboardDP, parentView.getResources().getDisplayMetrics());
                parentView.getWindowVisibleDisplayFrame(rect);
                int heightDiff = parentView.getRootView().getHeight() - (rect.bottom - rect.top);
                boolean isShown = heightDiff >= estimatedKeyboardHeight;

                if (isShown == alreadyOpen) {
                    Log.i("Keyboard state", "Ignoring global layout change...");
                    return;
                }
                alreadyOpen = isShown;

                if (isShown) {
                    binding.areaListening.setVisibility(View.GONE);
                } else {
                    binding.areaListening.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getMyPoint();
//        adapter.notifyDataSetChanged();
    }

    private void setDetectApi() {
        Log.i(StringUtil.TAG, "setDetectApi: ");
        selectedDetection = DETECT_WHISTLE;
        recorderThread = new RecorderThread();
        recorderThread.start();
        detectorThread = new DetectorThread(recorderThread);
        detectorThread.setOnSignalsDetectedListener(MainActivity.mainApp);
        detectorThread.start();
    }

    public void releaseDetectApi() {
        Log.i(StringUtil.TAG, "releaseDetectApi: ");
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

    public void detectAni(final String filePath) {
        Log.i(StringUtil.TAG, "detectAni: ");
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
//            mp.start();
            app.isTrans = false;
            mVisualizer.setEnabled(false);
            mVisualizer.release();

            Intent dogTrans = new Intent(act, DlgDogTrans.class);
            dogTrans.putExtra("path", filePath);
            dogTrans.putExtra("td_run_time", String.valueOf(mp.getDuration()));
            startActivityForResult(dogTrans, 101);

            releaseDetectApi();
        }

        app.isTrans = false;

//        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mediaPlayer) {
//                app.isTrans = false;
//                mVisualizer.setEnabled(false);
//                mVisualizer.release();
//                if (act != null) {
//                    Intent dogTrans = new Intent(act, DlgDogTrans.class);
//                    dogTrans.putExtra("path", filePath);
//                    dogTrans.putExtra("td_run_time", String.valueOf(mp.getDuration()));
//                    startActivity(dogTrans);
//                }
//                mp.reset();
//            }
//        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 101) {
                adapter.setList(app.chatItems);
                binding.rcvChat.scrollToPosition(app.chatItems.size() - 1);
                adapter.startAudio(app.chatItems.size() - 1);
            } else if (requestCode == 102) {
                adapter.setList(app.chatItems);
                binding.rcvChat.scrollToPosition(app.chatItems.size() - 1);
                adapter.startAudio(app.chatItems.size() - 1);
            }
        }
    }

    private void setClickListener() {
        binding.btnSend.setOnClickListener(this);

        binding.btnSearch.setOnClickListener(this);
        binding.btnSerchclose.setOnClickListener(this);
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
                                        final String banner = StringUtil.getStr(job, "banner");

                                        Log.i("TEST_HOME", "coupang_url: " + coupang_url);
                                        Log.i("TEST_HOME", "banner: " + banner);

                                        act.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                binding.bannerCore.setVisibility(View.VISIBLE);
                                                Glide.with(act).load(banner).into(binding.bannerCore);
                                                binding.bannerCore.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        Intent intent = new Intent(act, LayoutWebView.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);
                                                    }
                                                });
                                            }
                                        });
                                    } else {
                                        act.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                binding.bannerCore.setVisibility(View.GONE);
                                            }
                                        });
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    act.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            binding.bannerCore.setVisibility(View.GONE);
                                        }
                                    });
                                }
                            }
                        });
                    } else {
                        act.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.bannerCore.setVisibility(View.GONE);
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
        switch (v.getId()) {
            case R.id.btn_serchclose:
                binding.searchArea.setVisibility(View.GONE);
                binding.title.setVisibility(View.VISIBLE);
                binding.boneArea.setVisibility(View.VISIBLE);
//                binding.llDetectmsgArea.setVisibility(View.VISIBLE);
                binding.etSearch.setText(null);
                adapter = new ChatAdapter(act, app.chatItems);
                binding.rcvChat.setAdapter(adapter);
                break;
            case R.id.btn_search:
                if (binding.searchArea.getVisibility() != View.VISIBLE) {
                    binding.searchArea.setVisibility(View.VISIBLE);
                    binding.title.setVisibility(View.GONE);
                    binding.boneArea.setVisibility(View.GONE);
//                    binding.llDetectmsgArea.setVisibility(View.GONE);
                } else {
                    // 검색
                    if (binding.etSearch.length() == 0) {
                        Toast.makeText(act, "검색어를 입력하세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Toast.makeText(act, binding.etSearch.getText() + " 검색", Toast.LENGTH_SHORT).show();

                    searchList.clear();
                    searchList.addAll(db.getChatSearchList(act, binding.etSearch.getText().toString()));

                    if (searchList.size() == 0) {
                        Toast.makeText(act, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                        adapter = new ChatAdapter(act, searchList);
                        binding.rcvChat.setAdapter(adapter);
                    } else {
                        adapter = new ChatAdapter(act, searchList);
                        binding.rcvChat.setAdapter(adapter);
                    }
                }
                break;
            case R.id.btn_send:
                Log.d(MyUtil.TAG, "btn_send");
                InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(binding.btnSend.getWindowToken(), 0);


                if (binding.etMsg.length() == 0) {
                    Toast.makeText(act, "메세지를 입력하세요.", Toast.LENGTH_SHORT).show();
                } else {
//                    if (!app.isTrans) {
                    if (UserPref.getSubscribeState(act).equalsIgnoreCase("N")) {
                        MyUtil.showAlert(act, "대화하기", "대화 요청시 뼈다귀 1개가 차감됩니다. (소진된 뼈다귀는 10분에 1개씩 자동충전됩니다.)", new MyUtil.OnAlertAfter() {
                            @Override
                            public void onAfterOk() {
//                                checkPay(binding.etMsg.getText().toString());
                                if(UserPref.getSubscribeState(act).equalsIgnoreCase("Y")) {
                                    Intent pmsg = new Intent(act, DlgPersonTrans.class);
                                    pmsg.putExtra("pmsg", binding.etMsg.getText().toString());
                                    startActivityForResult(pmsg, 102);
                                    binding.etMsg.setText(null);
                                } else {
                                    pointMinus(binding.etMsg.getText().toString());
                                }

                            }

                            @Override
                            public void onAfterCancel() {

                            }
                        });
                    } else {
                        Intent pmsg = new Intent(act, DlgPersonTrans.class);
                        pmsg.putExtra("pmsg", binding.etMsg.getText().toString());
                        startActivityForResult(pmsg, 102);
                        binding.etMsg.setText(null);
                    }
//                    }
                }

                break;
        }
    }

    private void pointMinusDog() {
        ReqBasic pointMinus = new ReqBasic(act, NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                Log.d(MyUtil.TAG, "pointMinus: " + resultData.getResult());

                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (jo.getString("result").equalsIgnoreCase("Y")) {
                            getMyPoint();
                        } else {
//                            Toast.makeText(act, jo.getString("message"), Toast.LENGTH_SHORT).show();
//                            act.startActivity(new Intent(act, DlgPayment.class));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(act, act.getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(act, act.getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                }
            }
        };

        pointMinus.addParams("CONNECTCODE", "APP");
        pointMinus.addParams("siteUrl", NetUrls.MEDIADOMAIN);
        pointMinus.addParams("_APP_MEM_IDX", UserPref.getIdx(act));
        pointMinus.addParams("dbControl", "setPointMinus");
        pointMinus.addParams("MEMCODE", UserPref.getIdx(act));
        pointMinus.addParams("m_uniq", UserPref.getDeviceId(act));
        pointMinus.addParams("MINUSP", "1");
        pointMinus.addParams("MINUS_CONTENTS", "채팅 사용");
        pointMinus.execute(true, false);
    }

    private void pointMinus(final String contents) {
        ReqBasic pointMinus = new ReqBasic(act, NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
//                {"result":"N","message":"포인트가 부족합니다.","url":"","point":""}
                Log.d(MyUtil.TAG, "pointMinus: " + resultData.getResult());

                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (jo.getString("result").equalsIgnoreCase("Y")) {
                            Intent pmsg = new Intent(act, DlgPersonTrans.class);
                            pmsg.putExtra("pmsg", contents);
                            startActivityForResult(pmsg, 102);
                            binding.etMsg.setText(null);

                            getMyPoint();
                        } else {
                            Toast.makeText(act, jo.getString("message"), Toast.LENGTH_SHORT).show();
                            act.startActivity(new Intent(act, DlgPayment.class));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(act, act.getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(act, act.getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                }
            }
        };

        pointMinus.addParams("CONNECTCODE", "APP");
        pointMinus.addParams("siteUrl", NetUrls.MEDIADOMAIN);
        pointMinus.addParams("_APP_MEM_IDX", UserPref.getIdx(act));
        pointMinus.addParams("dbControl", "setPointMinus");
        pointMinus.addParams("MEMCODE", UserPref.getIdx(act));
        pointMinus.addParams("m_uniq", UserPref.getDeviceId(act));
        pointMinus.addParams("MINUSP", "1");
        pointMinus.addParams("MINUS_CONTENTS", "채팅 사용");
        pointMinus.execute(true, true);
    }

    private void checkPay(final String contents) {
        ReqBasic checkPay = new ReqBasic(act, NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                Log.d(MyUtil.TAG, "checkPay (Chatting): " + resultData.getResult());

                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (jo.getString("result").equalsIgnoreCase("Y")) {
                            Intent pmsg = new Intent(act, DlgPersonTrans.class);
                            pmsg.putExtra("pmsg", contents);
                            startActivityForResult(pmsg, 102);
                            binding.etMsg.setText(null);

                            getMyPoint();
                        } else {
                            Toast.makeText(act, jo.getString("message"), Toast.LENGTH_SHORT).show();
                            act.startActivity(new Intent(act, DlgPayment.class));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(act, act.getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(act, act.getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                }

            }
        };

        checkPay.addParams("CONNECTCODE", "APP");
        checkPay.addParams("siteUrl", NetUrls.MEDIADOMAIN);
        checkPay.addParams("_APP_MEM_IDX", UserPref.getIdx(act));
        checkPay.addParams("dbControl", "setPaymentCk");
        checkPay.addParams("MEMCODE", UserPref.getIdx(act));
        checkPay.addParams("m_uniq", UserPref.getDeviceId(act));
        checkPay.addParams("MINUSP", "1");
        checkPay.execute(true, true);
    }


    private void getMyPoint() {
        ReqBasic myPoint = new ReqBasic(getActivity(), NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                Log.d(MyUtil.TAG, "getMypoint: " + resultData.getResult());
//                {"result":"Y","message":"성공적으로 등록하였습니다.","url":"", "point":"11"}
                if (resultData.getResult() != null) {

                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (jo.has("point")) {

                            if (UserPref.getSubscribeState(getActivity()).equalsIgnoreCase("N")) {
                                if (MyUtil.isNull(jo.getString("point"))) {
                                    binding.boneCount.setText("0");
                                } else {
                                    binding.boneCount.setText(jo.getString("point"));
                                }
                            } else {
                                binding.boneCount.setText("구독권 이용중");
                                binding.gae.setVisibility(View.GONE);
                            }
                        } else {
                            binding.boneCount.setText("0");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(getActivity(), getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                }
            }
        };

        myPoint.addParams("CONNECTCODE", "APP");
        myPoint.addParams("siteUrl", NetUrls.MEDIADOMAIN);
        myPoint.addParams("_APP_MEM_IDX", UserPref.getIdx(getActivity()));
        myPoint.addParams("dbControl", "getMemberPoint");
        myPoint.addParams("MEMCODE", UserPref.getIdx(getActivity()));
        myPoint.addParams("m_uniq", UserPref.getDeviceId(getActivity()));
        myPoint.execute(true, false);

    }
}
