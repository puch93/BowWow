package kr.core.bowwow.dialogAct;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.android.billingclient.api.Purchase;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.internal.service.Common;
import com.onestore.iap.api.IapEnum;
import com.onestore.iap.api.IapResult;
import com.onestore.iap.api.PurchaseClient;
import com.onestore.iap.api.PurchaseData;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import kr.core.bowwow.R;
import kr.core.bowwow.activity.BaseAct;
import kr.core.bowwow.activity.MainActivity;
import kr.core.bowwow.app;
import kr.core.bowwow.billing.BillingManager;
import kr.core.bowwow.databinding.DlgPaymentBinding;
import kr.core.bowwow.dto.pref.UserPref;
import kr.core.bowwow.network.HttpResult;
import kr.core.bowwow.network.NetUrls;
import kr.core.bowwow.network.ReqBasic;
import kr.core.bowwow.utils.MyUtil;
import kr.core.bowwow.utils.StringUtil;

public class DlgPayment extends BaseAct implements View.OnClickListener {
    DlgPaymentBinding binding;
    Activity act;

    String itemname, price;

    /* one store billing */
    private static final String SUBS_ID = "subitem";
    private static final int PURCHASE_REQUEST = 9500;
    String productType = "auto";
    PurchaseClient mPurchaseClient;
    boolean isPurchaseStateReadySubs = false;
    boolean isPurchaseStateReadyItem = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = DataBindingUtil.setContentView(this, R.layout.dlg_payment);
        act = this;

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);

        setClickListener();

        mPurchaseClient = new PurchaseClient(act, StringUtil.KEY);
        mPurchaseClient.connect(mServiceConnectionListener);


        Glide.with(this)
                .load(R.raw.order_gif)
                .into(binding.gifRight);

        binding.btnClose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }


    PurchaseClient.ServiceConnectionListener mServiceConnectionListener = new PurchaseClient.ServiceConnectionListener() {
        @Override
        public void onConnected() {
            mPurchaseClient.isBillingSupportedAsync(StringUtil.IAP_API_VERSION, mBillingSupportedListener);
            mPurchaseClient.queryPurchasesAsync(StringUtil.IAP_API_VERSION, "auto", mQueryPurchaseListenerSubs);
            mPurchaseClient.queryPurchasesAsync(StringUtil.IAP_API_VERSION, "inapp", mQueryPurchaseListenerItem);
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


    PurchaseClient.QueryPurchaseListener mQueryPurchaseListenerSubs = new PurchaseClient.QueryPurchaseListener() {
        @Override
        public void onSuccess(List<PurchaseData> purchaseDataList, String productType) {
            isPurchaseStateReadySubs = true;

            Log.d("one", "queryPurchasesAsync onSuccess, " + purchaseDataList.toString());
            //구독 판별
            if (IapEnum.ProductType.AUTO.getType().equalsIgnoreCase(productType)) {
                //구독
                if (purchaseDataList.size() > 0) {
                    for (int i = 0; i < purchaseDataList.size(); i++) {
                        Log.i(StringUtil.TAG, "purchaseDataList.get(" + i + "): " + purchaseDataList.get(i).toString());
                        if (purchaseDataList.get(i).getRecurringState() == 0) {
                            //  구독중
                            UserPref.setSubscribeState(getApplicationContext(), "Y");
                        } else if (purchaseDataList.get(i).getRecurringState() == 1) {
                            //  구독 해지중
                            UserPref.setSubscribeState(getApplicationContext(), "Y");
                        } else if (purchaseDataList.get(i).getRecurringState() == -1) {
                            //  구독 X
                            UserPref.setSubscribeState(getApplicationContext(), "N");
                        }
                    }
                }  else {
                    //  구독 X
                    UserPref.setSubscribeState(act, "N");
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


    //4. 결제 완료 리스너
    PurchaseClient.PurchaseFlowListener mPurchaseFlowListener = new PurchaseClient.PurchaseFlowListener() {
        @Override
        public void onSuccess(com.onestore.iap.api.PurchaseData purchaseData) {
            Log.d("ONE", "launchPurchaseFlowAsync onSuccess, " + purchaseData.toString());
            // 구매완료 후 developer payload 검증을 수해한다.
            if (!purchaseData.getDeveloperPayload().equalsIgnoreCase(StringUtil.DEVELOPERPAYLOAD)) {
                Log.d("ONE", "launchPurchaseFlowAsync onSuccess, Payload is not valid.");
                return;
            }

            if (purchaseData.getProductId().equals(SUBS_ID)) {
                // 구독 구매완료
                Log.i(StringUtil.TAG, "구독 구매완료: ");

                UserPref.setSubscribeState(getApplicationContext(), "Y");
                //TODO 여기 결제완료되었으니 서버 업뎃 할것
//                    sendPurchaseResult(purchaseData, true);
            } else {
                // 인앱아이템 구매완료
                Log.i(StringUtil.TAG, "인앱아이템 구매완료: ");
                // 관리형상품(inapp)은 구매 완료 후 소비를 수행한다.
                mPurchaseClient.consumeAsync(StringUtil.IAP_API_VERSION, purchaseData, mConsumeListener);
                return;
            }
        }

        @Override
        public void onError(IapResult result) {
            Log.e("ONE", "launchPurchaseFlowAsync onError, " + result.toString());
        }

        @Override
        public void onErrorRemoteException() {
            Log.e("ONE", "launchPurchaseFlowAsync onError, 원스토어 서비스와 연결을 할 수 없습니다");
        }

        @Override
        public void onErrorSecurityException() {
            Log.e("ONE", "launchPurchaseFlowAsync onError, 비정상 앱에서 결제가 요청되었습니다");
        }

        @Override
        public void onErrorNeedUpdateException() {
            Log.e("ONE", "launchPurchaseFlowAsync onError, 원스토어 서비스앱의 업데이트가 필요합니다");
        }
    };


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case PURCHASE_REQUEST:
                    /*
                     * launchPurchaseFlowAsync API 호출 시 전달받은 intent 데이터를 handlePurchaseData를 통하여 응답값을 파싱합니다.
                     * 파싱 이후 응답 결과를 launchPurchaseFlowAsync 호출 시 넘겨준 PurchaseFlowListener 를 통하여 전달합니다.
                     */
                    if (resultCode == Activity.RESULT_OK) {
                        if (mPurchaseClient.handlePurchaseData(data) == false) {
                            Log.e("ONE", "onActivityResult handlePurchaseData false ");
                            // listener is null
                        }
                    } else {
                        Log.e("ONE", "onActivityResult user canceled");
                        // user canceled , do nothing..
                    }
                    break;
            }
        }
    }


    PurchaseClient.QueryPurchaseListener mQueryPurchaseListenerItem = new PurchaseClient.QueryPurchaseListener() {
        @Override
        public void onSuccess(List<PurchaseData> purchaseDataList, String productType) {

            Log.d("one", "queryPurchasesAsync onSuccess, " + purchaseDataList.toString());
            if (IapEnum.ProductType.IN_APP.getType().equalsIgnoreCase(productType)) {
//구독 판별
                if (IapEnum.ProductType.IN_APP.getType().equalsIgnoreCase(productType)) {
                    //아이템
                    if (purchaseDataList.size() > 0) {
                        for (int i = 0; i < purchaseDataList.size(); i++) {
                            PurchaseData purchaseData = purchaseDataList.get(i); // 구매내역조회 및 구매요청 후 전달받은 PurchaseData
                            mPurchaseClient.consumeAsync(StringUtil.IAP_API_VERSION, purchaseData, mConsumeListener);
                        }
                    }
                }
            }

            isPurchaseStateReadyItem = true;
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


    PurchaseClient.ConsumeListener mConsumeListener = new PurchaseClient.ConsumeListener() {
        @Override
        public void onSuccess(com.onestore.iap.api.PurchaseData purchaseData) {
            Log.d("ONE", "consumeAsync onSuccess, " + purchaseData.toString());
            // 상품소비 성공, 이후 시나리오는 각 개발사의 구매완료 시나리오를 진행합니다.

            //TODO 여기 결제완료되었으니 서버 업뎃 할것
//                    sendPurchaseResult(purchaseData, true);
        }

        @Override
        public void onErrorRemoteException() {
            Log.e("ONE", "consumeAsync onError, 원스토어 서비스와 연결을 할 수 없습니다");
        }

        @Override
        public void onErrorSecurityException() {
            Log.e("ONE", "consumeAsync onError, 비정상 앱에서 결제가 요청되었습니다");
        }

        @Override
        public void onErrorNeedUpdateException() {
            Log.e("ONE", "consumeAsync onError, 원스토어 서비스앱의 업데이트가 필요합니다");
        }

        @Override
        public void onError(IapResult result) {
            Log.e("ONE", "consumeAsync onError, " + result.toString());
        }
    };


//
//    private void sendPurchaseResultTest() {
//        ReqBasic pResult = new ReqBasic(this, NetUrls.DOMAIN) {
//            @Override
//            public void onAfter(int resultCode, HttpResult resultData) {
////                {"result":"Y","message":"성공적으로 등록 완료되었습니다.","url":""}
//                Log.d(MyUtil.TAG, "sendPurchaseResult: " + resultData.getResult());
//                if (resultData.getResult() != null) {
//
//                    try {
//
//                        JSONObject jo = new JSONObject(resultData.getResult());
//                        Toast.makeText(DlgPayment.this, jo.getString("message"), Toast.LENGTH_SHORT).show();
//                        if (jo.getString("result").equalsIgnoreCase("Y")) {
//
//                        } else {
//
//                        }
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                        Toast.makeText(DlgPayment.this, getString(R.string.net_errmsg) + "\n문제 : 데이터 형태", Toast.LENGTH_SHORT).show();
//                    }
//
//                    finish();
//
//                } else {
//                    Toast.makeText(DlgPayment.this, getString(R.string.net_errmsg) + "\n문제 : 값이 없음", Toast.LENGTH_SHORT).show();
//                }
//            }
//        };
//
//        pResult.addParams("CONNECTCODE", "APP");
//        pResult.addParams("siteUrl", NetUrls.MEDIADOMAIN);
//        pResult.addParams("_APP_MEM_IDX", UserPref.getIdx(this));
//        pResult.addParams("MEMCODE", UserPref.getIdx(this));
//        pResult.addParams("dbControl", "setPointINAPPPayment");
//        pResult.addParams("m_uniq", UserPref.getDeviceId(this));
//        pResult.addParams("p_market", "PlayStore");
//        pResult.addParams("p_class", "point");
//        pResult.addParams("p_point", "10");
//        pResult.addParams("price", "3300");
//        pResult.addParams("p_purchase_item_name", "bone10");
//        pResult.addParams("p_purchasetime", String.valueOf(System.currentTimeMillis()));
//        pResult.addParams("p_orderid", "1234");
//        pResult.addParams("p_token", "1234");
//        pResult.addParams("productId", "1234");
//        pResult.addParams("p_pay_data_info", "1234");
//        pResult.execute(true, true);
//    }

    private void setClickListener() {
        binding.llSubitem.setOnClickListener(this);
        binding.llBone10.setOnClickListener(this);
        binding.llBone30.setOnClickListener(this);
        binding.llBone50.setOnClickListener(this);
        binding.llBone100.setOnClickListener(this);

        binding.llSubitem.setTag(0);
        binding.llBone10.setTag(1);
        binding.llBone30.setTag(2);
        binding.llBone50.setTag(3);
        binding.llBone100.setTag(4);
    }

    private void sendPurchaseResult(final com.onestore.iap.api.PurchaseData p, final boolean isSubscribe) {
        ReqBasic pResult = new ReqBasic(this, NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                Log.d(MyUtil.TAG, "sendPurchaseResult: " + resultData.getResult());
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());
                        Toast.makeText(DlgPayment.this, jo.getString("message"), Toast.LENGTH_SHORT).show();
                        if (jo.getString("result").equalsIgnoreCase("Y")) {
                            if (isSubscribe) {
                                UserPref.setSubscribeState(DlgPayment.this, "Y");
                            }
                        } else {

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(DlgPayment.this, getString(R.string.net_errmsg) + "\n문제 : 데이터 형태", Toast.LENGTH_SHORT).show();
                    }

                    finish();

                } else {
                    Toast.makeText(DlgPayment.this, getString(R.string.net_errmsg) + "\n문제 : 값이 없음", Toast.LENGTH_SHORT).show();
                }
            }
        };

        pResult.addParams("CONNECTCODE", "APP");
        pResult.addParams("siteUrl", NetUrls.MEDIADOMAIN);
        pResult.addParams("_APP_MEM_IDX", UserPref.getIdx(this));
        pResult.addParams("MEMCODE", UserPref.getIdx(this));
        pResult.addParams("dbControl", "setPointINAPPPayment");
        pResult.addParams("m_uniq", UserPref.getDeviceId(this));
        pResult.addParams("p_market", "One");

        if (isSubscribe) {
            pResult.addParams("p_class", "month");
            Date d = new Date(p.getPurchaseTime());
            Calendar c = Calendar.getInstance();
            c.setTime(d);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            c.add(Calendar.MONTH, 1);
            pResult.addParams("expire_datetime", sdf.format(c.getTime()));
        } else {
            pResult.addParams("p_class", "point");

            String point = "0";
            switch (itemname) {
                case "bone10":
                    point = "10";
                    break;
                case "bone30":
                    point = "30";
                    break;
                case "bone50":
                    point = "50";
                    break;
                case "bone100":
                    point = "100";
                    break;
            }
            pResult.addParams("p_point", point);
        }
        pResult.addParams("price", price);
        pResult.addParams("p_purchase_item_name", p.getProductId());
        pResult.addParams("p_purchasetime", String.valueOf(p.getPurchaseTime()));
        pResult.addParams("p_orderid", p.getOrderId());
        pResult.addParams("p_token", p.getSignature());
        pResult.addParams("productId", p.getProductId());

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("p_orderid", p.getOrderId());
            jsonObject.put("p_store_type", "OneStore");
            jsonObject.put("p_purchasetime", String.valueOf(p.getPurchaseTime()));
            jsonObject.put("p_purchasePrice", price);
            jsonObject.put("p_signature", p.getSignature());
            jsonObject.put("p_itemtype", p.getProductId());
            jsonObject.put("p_itemcount", "1");
            jsonObject.put("p_purchage_item_name", p.getProductId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        pResult.addParams("p_pay_data_info", jsonObject.toString());
        pResult.execute(true, true);
    }

    private void setItemState(int item) {
        switch (item) {
            case 0:
                itemname = BillingManager.SUBITEM;
                price = "19900";
                break;
            case 1:
                itemname = BillingManager.BONE10;
                price = "3300";
                break;
            case 2:
                itemname = BillingManager.BONE30;
                price = "7700";
                break;
            case 3:
                itemname = BillingManager.BONE50;
                price = "14300";
                break;
            case 4:
                itemname = BillingManager.BONE100;
                price = "22000";
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_subitem:
            case R.id.ll_bone10:
            case R.id.ll_bone30:
            case R.id.ll_bone50:
            case R.id.ll_bone100:
                setItemState((int) v.getTag());
                if (MyUtil.isNull(itemname)) {
                    Toast.makeText(this, "결제 항목을 선택해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d(MyUtil.TAG, "itemname: " + itemname);


                if (itemname.equalsIgnoreCase(BillingManager.SUBITEM)) {
                    if (isPurchaseStateReadySubs) {
                        if (!StringUtil.isNull(UserPref.getSubscribeState(act)) && UserPref.getSubscribeState(act).equalsIgnoreCase("N")) {
                            String productName = ""; // "" 일때는 개발자센터에 등록된 상품명 노출
                            String productType = IapEnum.ProductType.AUTO.getType(); // "
                            String devPayload = StringUtil.DEVELOPERPAYLOAD;
                            String gameUserId = ""; // 디폴트 ""
                            boolean promotionApplicable = false;
                            mPurchaseClient.launchPurchaseFlowAsync(StringUtil.IAP_API_VERSION, act, PURCHASE_REQUEST, SUBS_ID, productName, productType, devPayload, gameUserId, promotionApplicable, mPurchaseFlowListener);
                        } else {
                            Toast.makeText(act, "이미 구독중입니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(act, "결제정보를 불러오고 있습니다. 잠시후에 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String productName = ""; // "" 일때는 개발자센터에 등록된 상품명 노출
                    String productType = IapEnum.ProductType.IN_APP.getType(); // "inapp"
                    String devPayload = StringUtil.DEVELOPERPAYLOAD;
                    String gameUserId = ""; // 디폴트 ""
                    mPurchaseClient.launchPurchaseFlowAsync(
                            StringUtil.IAP_API_VERSION,
                            act,
                            PURCHASE_REQUEST,
                            itemname,
                            productName,
                            productType,
                            devPayload,
                            gameUserId,
                            false,
                            mPurchaseFlowListener);
                }
                break;

            case R.id.btn_cancel:
                finish();
                break;
        }
    }
}
