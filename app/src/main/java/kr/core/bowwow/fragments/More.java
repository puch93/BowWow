package kr.core.bowwow.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.gms.common.internal.service.Common;
import com.onestore.iap.api.IapEnum;
import com.onestore.iap.api.IapResult;
import com.onestore.iap.api.PurchaseClient;
import com.onestore.iap.api.PurchaseData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import kr.core.bowwow.R;
import kr.core.bowwow.activity.DogInfoEditAct;
import kr.core.bowwow.activity.MydogProfAct;
import kr.core.bowwow.activity.ServiceCenterAct;
import kr.core.bowwow.activity.TermsAct;
import kr.core.bowwow.app;
import kr.core.bowwow.databinding.FragMoreBinding;
import kr.core.bowwow.dialogAct.DlgCommandPlay;
import kr.core.bowwow.dialogAct.DlgPayment;
import kr.core.bowwow.dto.pref.SettingPref;
import kr.core.bowwow.dto.pref.UserPref;
import kr.core.bowwow.network.HttpResult;
import kr.core.bowwow.network.NetUrls;
import kr.core.bowwow.network.ReqBasic;
import kr.core.bowwow.service.ForegroundService;
import kr.core.bowwow.utils.LayoutWebView;
import kr.core.bowwow.utils.MyUtil;
import kr.core.bowwow.utils.StringUtil;

public class More extends Fragment implements View.OnClickListener {
    FragMoreBinding binding;
    Activity act;


    /* one store billing */
    private static final String SUBS_ID = "subitem";
    private static final int PURCHASE_REQUEST = 9500;
    String productType = "auto";
    PurchaseClient mPurchaseClient;
    boolean isListenerCalled = false;
    PurchaseData purchaseData;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_more, container, false);
        act = getActivity();

        binding.title.setTypeface(app.tf_bmjua);
        binding.text.setTypeface(app.tf_bmjua);

        setClickListener();

        Glide.with(act).load(R.raw.gif_mor_dog).into(binding.gifMorDog);

        if (SettingPref.isPushReceive(getActivity())) {
            binding.swChat.setChecked(true);
        } else {
            binding.swChat.setChecked(false);
        }
        binding.swChat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingPref.setPushReceive(act, isChecked);
            }
        });

        getMypoint();

        getCoupaBanner();

        mPurchaseClient = new PurchaseClient(act, StringUtil.KEY);
        mPurchaseClient.connect(mServiceConnectionListener);

        return binding.getRoot();
    }

    PurchaseClient.ServiceConnectionListener mServiceConnectionListener = new PurchaseClient.ServiceConnectionListener() {
        @Override
        public void onConnected() {
//            mPurchaseClient.isBillingSupportedAsync(StringUtil.IAP_API_VERSION, mBillingSupportedListener);
            mPurchaseClient.queryPurchasesAsync(StringUtil.IAP_API_VERSION, "auto", mQueryPurchaseListener);
            Log.d("ONE", "Service connected");
            //2. mBillingSupportedListener < 나도모름 / mQueryPurchaseListener << 구매 내역 들고오기
        }

        @Override
        public void onDisconnected() {
            Log.d("ONE", "Service disconnected");
        }

        @Override
        public void onErrorNeedUpdateException() {
            Log.e("ONE", "connect onError, 원스토어 서비스앱의 업데이트가 필요합니다");
            PurchaseClient.launchUpdateOrInstallFlow(act);
        }
    };

    PurchaseClient.BillingSupportedListener mBillingSupportedListener = new PurchaseClient.BillingSupportedListener() {

        @Override
        public void onSuccess() {
            Log.d("ONE", "isBillingSupportedAsync onSuccess");
        }

        @Override
        public void onError(IapResult result) {
            Log.e("ONE", "isBillingSupportedAsync onError, " + result.toString());
        }

        @Override
        public void onErrorRemoteException() {
            Log.e("ONE", "isBillingSupportedAsync onError, 원스토어 서비스와 연결을 할 수 없습니다");
        }

        @Override
        public void onErrorSecurityException() {
            Log.e("ONE", "isBillingSupportedAsync onError, 비정상 앱에서 결제가 요청되었습니다");
        }

        @Override
        public void onErrorNeedUpdateException() {
            Log.e("ONE", "isBillingSupportedAsync onError, 원스토어 서비스앱의 업데이트가 필요합니다");
        }
    };

    PurchaseClient.QueryPurchaseListener mQueryPurchaseListener = new PurchaseClient.QueryPurchaseListener() {
        @Override
        public void onSuccess(List<PurchaseData> purchaseDataList, String productType) {
            isListenerCalled = true;
            purchaseData = null;

            Log.d("one", "queryPurchasesAsync onSuccess, " + purchaseDataList.toString());
            //구독 판별
            if (IapEnum.ProductType.AUTO.getType().equalsIgnoreCase(productType)) {
                //구독
                if (purchaseDataList.size() > 0) {
                    for (int i = 0; i < purchaseDataList.size(); i++) {
                        purchaseData = purchaseDataList.get(i);
                        Log.i(StringUtil.TAG, "purchaseDataList.get(" + i + "): " + purchaseDataList.get(i).toString());
                        if (purchaseDataList.get(i).getRecurringState() == 0) {
                            //  구독중
                            UserPref.setSubscribeState(act, "Y");
                            binding.tvSubs.setText("월정액 해지");
                        } else if (purchaseDataList.get(i).getRecurringState() == 1) {
                            //  구독 해지중
                            UserPref.setSubscribeState(act, "Y");
                            binding.tvSubs.setText("월정액 해지취소");
                        } else if (purchaseDataList.get(i).getRecurringState() == -1) {
                            //  구독 X
                            UserPref.setSubscribeState(act, "N");
                            binding.tvSubs.setText("월정액 해지");
                        }
                    }
                } else {
                    //  구독 X
                    UserPref.setSubscribeState(act, "N");
                    binding.tvSubs.setText("월정액 해지");
                }
            }
        }

        @Override
        public void onErrorRemoteException() {
            Log.e("one", "queryPurchasesAsync onError, 원스토어 서비스와 연결을 할 수 없습니다");
        }

        @Override
        public void onErrorSecurityException() {
            Log.e("one", "queryPurchasesAsync onError, 비정상 앱에서 결제가 요청되었습니다");
        }

        @Override
        public void onErrorNeedUpdateException() {
            Log.e("one", "queryPurchasesAsync onError, 원스토어 서비스앱의 업데이트가 필요합니다");
        }

        @Override
        public void onError(IapResult result) {
            Log.e("one", "queryPurchasesAsync onError, " + result.toString());
        }
    };


    private void setClickListener() {

        binding.mydogModify.setOnClickListener(this);
        binding.btnPayment.setOnClickListener(this);
        binding.btnPushstate.setOnClickListener(this);
        binding.btnScenter.setOnClickListener(this);
        binding.btnQna.setOnClickListener(this);
        binding.btnSubs.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(StringUtil.TAG, "onResume:호출됨 ");
        getMypoint();

        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Glide.with(getActivity())
                        .load(app.myDogImg)
                        .transform(new CircleCrop())
                        .into(binding.ivProfimg);

                binding.tvKname.setText(app.myDogKname);
                binding.tvBreed.setText(app.myDogBreed);
            }
        });

        mPurchaseClient.queryPurchasesAsync(StringUtil.IAP_API_VERSION, "auto", mQueryPurchaseListener);
    }

    private void getMypoint() {
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
                                    binding.tvBonecnt.setText("0");
                                    binding.boneCount.setText("0");
                                } else {
                                    binding.tvBonecnt.setText(jo.getString("point"));
                                    binding.boneCount.setText(jo.getString("point"));
                                }
                            } else {
                                binding.boneCount.setText("구독권 이용중");
                                binding.gae.setVisibility(View.GONE);
                            }
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                            binding.tvBonecnt.setText("0");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
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


                                        Glide.with(act).load(banner).into(binding.coupaBanner);
                                        binding.coupaBanner.setOnClickListener(new View.OnClickListener() {
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


    /*
     * PurchaseClient의 manageRecurringProductAsync API (월정액상품 상태변경) 콜백 리스너
     */
    PurchaseClient.ManageRecurringProductListener mManageRecurringProductListener = new PurchaseClient.ManageRecurringProductListener() {
        @Override
        public void onSuccess(PurchaseData purchaseData, String manageAction) {
            mPurchaseClient.queryPurchasesAsync(StringUtil.IAP_API_VERSION, productType, mQueryPurchaseListener);
        }

        @Override
        public void onErrorRemoteException() {
            Log.e(StringUtil.TAG, "manageRecurringProductAsync onError, 원스토어 서비스와 연결을 할 수 없습니다");
        }

        @Override
        public void onErrorSecurityException() {
            Log.e(StringUtil.TAG, "manageRecurringProductAsync onError, 비정상 앱에서 결제가 요청되었습니다");
        }

        @Override
        public void onErrorNeedUpdateException() {
            Log.e(StringUtil.TAG, "manageRecurringProductAsync onError, 원스토어 서비스앱의 업데이트가 필요합니다");
        }

        @Override
        public void onError(IapResult result) {
            Log.e(StringUtil.TAG, "manageRecurringProductAsync onError, " + result.toString());
        }
    };


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_subs:
                if (isListenerCalled) {
                    if (!StringUtil.isNull(UserPref.getSubscribeState(act)) && UserPref.getSubscribeState(act).equalsIgnoreCase("Y")) {
                        if (purchaseData == null) {
                            Toast.makeText(act, "이용중인 상품이 없습니다", Toast.LENGTH_SHORT).show();
                        } else {
                            if (purchaseData.getRecurringState() == 0) {
                                //구독취소
                                mPurchaseClient.manageRecurringProductAsync(5, purchaseData, "cancel", mManageRecurringProductListener);
                            } else if (purchaseData.getRecurringState() == 1) {
                                //구독취소해제
                                mPurchaseClient.manageRecurringProductAsync(5, purchaseData, "reactivate", mManageRecurringProductListener);
                            }
                        }
                    } else {
                        Toast.makeText(act, "이용중인 상품이 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(act, "결제정보를 불러오고 있습니다. 잠시후에 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.mydog_modify:
                startActivity(new Intent(getActivity(), DogInfoEditAct.class));
                break;
            case R.id.btn_payment:
                startActivityForResult(new Intent(getActivity(), DlgPayment.class), 101);
                break;
            case R.id.btn_scenter:
                startActivity(new Intent(getActivity(), ServiceCenterAct.class));
                break;
            case R.id.btn_qna:
                Intent email = new Intent(Intent.ACTION_SEND);
                email.setType("text/html");
                email.setPackage("com.google.android.gm");
                // email setting 배열로 해놔서 복수 발송 가능
                String[] address = {"neosdisc1234@gmail.com"};
                email.putExtra(Intent.EXTRA_EMAIL, address);
                email.putExtra(Intent.EXTRA_SUBJECT, "바우와우 문의하기");
                email.putExtra(Intent.EXTRA_TEXT, "");
                startActivity(email);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == 101) {
                mPurchaseClient.queryPurchasesAsync(StringUtil.IAP_API_VERSION, "auto", mQueryPurchaseListener);
            }
        }
    }
}
