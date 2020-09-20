package kr.core.bowwow.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.common.internal.service.Common;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import kr.core.bowwow.R;
import kr.core.bowwow.databinding.LayoutWebviewBinding;
import kr.core.bowwow.dto.pref.UserPref;
import kr.core.bowwow.network.HttpResult;
import kr.core.bowwow.network.ReqBasic;


public class LayoutWebView extends AppCompatActivity implements View.OnClickListener {
    LayoutWebviewBinding binding;
    Activity act;
    CookieManager cookieManager;

    public static final String TAG = "TEST_HOME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.layout_webview);
        setLayout();
    }

    private void setLayout() {
        act = this;
        coupangInfo();

        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.getSettings().setSupportMultipleWindows(true);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(this);
        } else {
            binding.webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.setAcceptThirdPartyCookies(binding.webView, true);
        }

        binding.webView.setWebChromeClient(new HelloWebChromeClient());
        binding.webView.setWebViewClient(new WebViewClientClass());
        binding.webView.getSettings().setJavaScriptEnabled(true);


         /*
          일반적으로 실행되는 경우 말고, 푸시수신 후 해당 페이지가 실행되었을때만 시간값 갱신 프로세스 진행하면됨.
          fcheck 는 SharedPreferences 에 저장하는 시간 그룹 값.
          본인 소스에 사용하는 키값에 맞게 바꿔서 사용 할 것.
          최초등록 이후 여기 쿠팡페이지가 호출할때 마다 -1 시간값으로 서버db에저장.
         */
        if (null != getIntent().getStringExtra("type") && getIntent().getStringExtra("type").equalsIgnoreCase("push")) {
            if (!StringUtil.isNull(UserPref.getFCheck(act))) {
                String h;

                if (Integer.valueOf(UserPref.getFCheck(act)) > 0 &&
                        Integer.valueOf(UserPref.getFCheck(act)) < 24) {
//                    h = (Integer.valueOf(UserPref.getFCheck(act)) -1) + "";
                    //TODO
                    h = (Integer.valueOf(UserPref.getFCheck(act)) + 1) + "";
                } else {
                    h = "23";
                }
                doCsetting(h);
            } else {
                String h = new SimpleDateFormat("HH", Locale.getDefault()).format(new Date(System.currentTimeMillis()));
                if (h.equalsIgnoreCase("00") || h.equalsIgnoreCase("0")) {
                    h = "23";
                } else {
                    h = (Integer.valueOf(h) - 1) + "";
                }

                doCsetting(h);
            }
        }


           /*
              쿠팡 액티비티 실행후 테스크를 지우기 위하여 특정시간이후 해당 액티비티를 finish 처리
         */
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 4000);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && binding.webView.canGoBack()) {
            binding.webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    public class HelloWebChromeClient extends WebChromeClient {
        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            WebView webView = new WebView(act);
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            Dialog dialog = new Dialog(act);
            dialog.setContentView(webView);

            ViewGroup.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setAttributes((WindowManager.LayoutParams) params);
            dialog.show();
            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onCloseWindow(WebView window) {
                    super.onCloseWindow(window);
                }
            });
            ((WebView.WebViewTransport) resultMsg.obj).setWebView(webView);
            resultMsg.sendToTarget();
            return true;
        }
    }


    private class WebViewClientClass extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            if (URLUtil.isNetworkUrl(url)) {

                return false;
            }

            if (url.contains("market://details?id=com.coupang.mobile&url=coupang:")) {
                if (getInstallPackage() == true) {
                    Uri uri = Uri.parse(url);
                    String ccc = uri.getQueryParameter("url");
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(ccc));
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            }

            return true;
        }

        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError er) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(act);
            builder.setMessage("이 사이트의 보안 인증서는 신뢰할 수 없습니다.");
            builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.proceed();
                }
            });
            builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.cancel();
                }
            });
            final AlertDialog dialog = builder.create();
            dialog.show();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.d("Page Finish URL", url);
        }

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {

        }
    }

    public boolean getInstallPackage() {

        String mpackagename = "com.coupang.mobile";

        try {
            PackageManager pm = getApplication().getPackageManager();
            PackageInfo pi = pm.getPackageInfo(mpackagename.trim(), PackageManager.GET_META_DATA);
            ApplicationInfo appInfo = pi.applicationInfo;
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


    private void coupangInfo() {
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
                                        JSONObject jsonObject = new JSONObject(StringUtil.getStr(jo, "data"));
                                        binding.webView.loadUrl(StringUtil.getStr(jsonObject, "coupang_url"));
                                        //banner << 배너 주소
                                    } else {
                                        binding.webView.loadUrl("https://coupa.ng/bH0r4h");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(act, "점검중입니다 잠시후 다시접속 부탁드리겠습니다.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.webView.loadUrl("https://coupa.ng/bH0r4h");
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


    /*사용자 정보 쿠팡서버로 업데이트 통신.
       본인 통신양식에 맞게 파람값만 맞추어서 사용할 것*/
    private void doCsetting(final String checkTime) {
        ReqBasic server = new ReqBasic(act, "https://coupang.adamstore.co.kr/lib/control.siso") {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                final String res = resultData.getResult();
                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (!StringUtil.isNull(res)) {
                                JSONObject jo = new JSONObject(res);
                                if (StringUtil.getStr(jo, "result").equalsIgnoreCase("Y")) {
                                    UserPref.setFCheck(act, checkTime);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };

        server.addParams("dbControl", "setCoupangPartnersPush");
        server.addParams("grouptime", checkTime);
        server.addParams("site", "7");
        server.addParams("fcm", UserPref.getFcm(act));
        server.addParams("m_idx", MyUtil.getDeviceId(act));
        server.execute(true, false);
    }
}
