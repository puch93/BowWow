package kr.core.bowwow;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import kr.core.bowwow.adapter.ChatAdapter;
import kr.core.bowwow.dto.ChatItem;
import kr.core.bowwow.dto.pref.UserPref;
import kr.core.bowwow.network.HttpResult;
import kr.core.bowwow.network.NetUrls;
import kr.core.bowwow.network.ReqBasic;
import kr.core.bowwow.utils.MyUtil;

public class app extends Application {

    public static final int REQUEST_TAKE_PHOTO = 2001;
    public static final int REQUEST_TAKE_ALBUM = 2002;
    public static final int REQUEST_IMAGE_CROP = 2003;

    public static String bannerState;
    public static String bannerLink;
    public static String bannerImg;

    // 임시(서버 연동 후 제거)
    public static String myDogImg;
    public static String myDogBreed;
    public static String myDogGender;
    public static String myDogBirth;
    public static String myDogKname;

    public static Typeface tf_bmjua;

    public static boolean isTrans = false;

    public static ArrayList<ChatItem> chatItems = new ArrayList<>();

//    public static ChatAdapter adapter;
    public static AdRequest adRequest = new AdRequest.Builder().build();

    public static ArrayList<byte[]> recData = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();

        tf_bmjua = Typeface.createFromAsset(getAssets(),"bmjua_ttf.ttf");

        getAdState();
//        getBanner();

    }

    private void getAdState(){
        ReqBasic adState = new ReqBasic(getApplicationContext(), NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                Log.d(MyUtil.TAG, "getAdState: "+resultData.getResult());

                if (resultData.getResult() != null){
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (jo.has("di_ad_class")) {
                            Log.d(MyUtil.TAG, "di_ad_class: " + jo.getString("di_ad_class"));
//                            String adState = jo.getString("di_ad_class");
//                            adState = MyUtil.ADMOB;
                            switch (jo.getString("di_ad_class")) {
//                            switch (adState) {
                                case "B":
                                    app.bannerState = MyUtil.BANNER;
                                    getBanner();
                                    break;
                                case "A":
                                    app.bannerState = MyUtil.ADMOB;
                                    MobileAds.initialize(getApplicationContext(), new OnInitializationCompleteListener() {
                                        @Override
                                        public void onInitializationComplete(InitializationStatus initializationStatus) {

                                        }
                                    });
                                    adRequest = new AdRequest.Builder().build();
                                    break;
                                case "N":
                                    app.bannerState = MyUtil.NONE;
                                    break;
                            }
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }

                }else{

                }
            }
        };

        adState.addParams("CONNECTCODE","APP");
        adState.addParams("siteUrl",NetUrls.MEDIADOMAIN);
        adState.addParams("dbControl","getTerm");
        adState.addParams("_APP_MEM_IDX", UserPref.getIdx(this));
        adState.addParams("m_uniq", UserPref.getDeviceId(this));
        adState.execute(true,false);

    }

    private void getBanner(){
        ReqBasic getBnInfo = new ReqBasic(getApplicationContext(),NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                Log.d(MyUtil.TAG, "getBanner: "+resultData.getResult());

                if (resultData.getResult() != null){

                    try {

                        JSONObject jo = new JSONObject(resultData.getResult());

                        JSONArray ja = jo.getJSONArray("data");

                        if (ja.length() > 0){
                            for (int i = 0; i < ja.length(); i++){
                                JSONObject banner =  ja.getJSONObject(i);

                                banner.getString("b_idx");
                                banner.getString("b_name");
                                banner.getString("b_img");
                                banner.getString("b_url");
                                banner.getString("b_use_is");
                                banner.getString("b_regdate");
                                banner.getString("b_editdate");

                                app.bannerImg = NetUrls.MEDIADOMAIN + banner.getString("b_img");
                                app.bannerLink = banner.getString("b_url");

                            }
                        }

                    }catch (JSONException e){
                        e.printStackTrace();
                    }

                }else{

                }
            }
        };


        getBnInfo.addParams("CONNECTCODE","APP");
        getBnInfo.addParams("siteUrl",NetUrls.MEDIADOMAIN);
        getBnInfo.addParams("dbControl","getBanner");
        getBnInfo.addParams("_APP_MEM_IDX", UserPref.getIdx(this));
        getBnInfo.addParams("m_uniq", UserPref.getDeviceId(this));
        getBnInfo.execute(true,false);

    }



}
