package kr.core.bowwow.dialogAct;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.android.billingclient.api.Purchase;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import kr.core.bowwow.R;
import kr.core.bowwow.activity.BaseAct;
import kr.core.bowwow.app;
import kr.core.bowwow.billing.BillingManager;
import kr.core.bowwow.databinding.DlgPaymentBinding;
import kr.core.bowwow.dto.pref.UserPref;
import kr.core.bowwow.network.HttpResult;
import kr.core.bowwow.network.NetUrls;
import kr.core.bowwow.network.ReqBasic;
import kr.core.bowwow.utils.MyUtil;

public class DlgPayment extends BaseAct implements View.OnClickListener {

    DlgPaymentBinding binding;

    String itemname, price;

    BillingManager billingManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = DataBindingUtil.setContentView(this, R.layout.dlg_payment);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);

        setClickListener();
        setBilling();

        Glide.with(this)
                .load(R.raw.order_gif)
                .into(binding.gifRight);

        binding.btnClose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //TODO 제거해야함
//                sendPurchaseResultTest();
                finish();
            }
        });

    }


    private void sendPurchaseResultTest() {
        ReqBasic pResult = new ReqBasic(this, NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
//                {"result":"Y","message":"성공적으로 등록 완료되었습니다.","url":""}
                Log.d(MyUtil.TAG, "sendPurchaseResult: " + resultData.getResult());
                if (resultData.getResult() != null) {

                    try {

                        JSONObject jo = new JSONObject(resultData.getResult());
                        Toast.makeText(DlgPayment.this, jo.getString("message"), Toast.LENGTH_SHORT).show();
                        if (jo.getString("result").equalsIgnoreCase("Y")) {

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
        pResult.addParams("p_market", "PlayStore");
        pResult.addParams("p_class", "point");
        pResult.addParams("p_point", "10");
        pResult.addParams("price", "3300");
        pResult.addParams("p_purchase_item_name", "bone10");
        pResult.addParams("p_purchasetime", String.valueOf(System.currentTimeMillis()));
        pResult.addParams("p_orderid", "1234");
        pResult.addParams("p_token", "1234");
        pResult.addParams("productId", "1234");
        pResult.addParams("p_pay_data_info", "1234");
        pResult.execute(true, true);
    }

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

    private void setBilling() {
        billingManager = new BillingManager(this, new BillingManager.AfterBilling() {
            @Override
            public void sendResult(Purchase purchase, boolean isSubscribe) {
                // 구독
//                getSku: subitem
//                getPurchaseToken: fgbcjedjabinkfnhmohdemhp.AO-J1Oy38ZkQXz94eNAJWJJGNUsW01mqj-JErpX61sSIa9dJWvR1_WpNK8hmjii5Win34z9mNTboYb1w1I6zRxc_ZOlH4gXM3PaoFvksflkNdAcwTKSk2QE
//                getOrderId: GPA.3373-1236-2126-62189
//                getPurchaseTime: 1577667498792
//                getOriginalJson: {"orderId":"GPA.3373-1236-2126-62189","packageName":"kr.core.bowwow","productId":"subitem","purchaseTime":1577667498792,"purchaseState":0,"purchaseToken":"fgbcjedjabinkfnhmohdemhp.AO-J1Oy38ZkQXz94eNAJWJJGNUsW01mqj-JErpX61sSIa9dJWvR1_WpNK8hmjii5Win34z9mNTboYb1w1I6zRxc_ZOlH4gXM3PaoFvksflkNdAcwTKSk2QE","autoRenewing":true,"acknowledged":false}

                // 소모성
//                getSku: bone10
//                getPurchaseToken: nchdokkhbcmlpepekmfmgnao.AO-J1OwWkXZ234qt8D3HN78jdTk8JE1Un5EgcJ8To6Zno6sTJg1VqlBPNGHTPfCEvhC0s0_UYmT1HRHOVJF5LhIpE-req9jr2HG-6uVpoLTvEHVk0oaDq_M
//                getOrderId: GPA.3303-9864-0457-90862
//                getPurchaseTime: 1577667304962
//                getOriginalJson: {"orderId":"GPA.3303-9864-0457-90862","packageName":"kr.core.bowwow","productId":"bone10","purchaseTime":1577667304962,"purchaseState":0,"purchaseToken":"nchdokkhbcmlpepekmfmgnao.AO-J1OwWkXZ234qt8D3HN78jdTk8JE1Un5EgcJ8To6Zno6sTJg1VqlBPNGHTPfCEvhC0s0_UYmT1HRHOVJF5LhIpE-req9jr2HG-6uVpoLTvEHVk0oaDq_M","acknowledged":false}

                // 서버로 값 전송(결제 완료)
                Log.d(MyUtil.TAG, "getSku: " + purchase.getSku());
                Log.d(MyUtil.TAG, "getPurchaseToken: " + purchase.getPurchaseToken());
                Log.d(MyUtil.TAG, "getOrderId: " + purchase.getOrderId());
                Log.d(MyUtil.TAG, "getPurchaseTime: " + purchase.getPurchaseTime());
                Log.d(MyUtil.TAG, "getOriginalJson: " + purchase.getOriginalJson());
                sendPurchaseResult(purchase, isSubscribe);
            }

            @Override
            public void getSubsriptionState(String subscription, Purchase purchase) {

            }
        });
    }

    private void sendPurchaseResult(Purchase p, final boolean isSubscribe) {
        ReqBasic pResult = new ReqBasic(this, NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
//                {"result":"Y","message":"성공적으로 등록 완료되었습니다.","url":""}
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
//        pResult.addParams("MEMCODE", UserPref.getIdx(this));
        pResult.addParams("p_market", "PlayStore");

        if (isSubscribe) {
            pResult.addParams("p_class", "month");
            Date d = new Date(p.getPurchaseTime());
            Calendar c = Calendar.getInstance();
            c.setTime(d);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            Log.d(MyUtil.TAG, "current: "+sdf.format(c.getTime()));
            c.add(Calendar.MONTH, 1);
//            Log.d(MyUtil.TAG, "plus: "+sdf.format(c.getTime()));

//            pResult.addParams("expire_datetime","");
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
        pResult.addParams("p_purchase_item_name", p.getSku());
        pResult.addParams("p_purchasetime", String.valueOf(p.getPurchaseTime()));
        pResult.addParams("p_orderid", p.getOrderId());
        pResult.addParams("p_token", p.getPurchaseToken());
        pResult.addParams("productId", p.getSku());
        pResult.addParams("p_pay_data_info", p.getOriginalJson());
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
                    billingManager.purchase(itemname, true);
                } else {
                    billingManager.purchase(itemname, false);
                }
                break;

            case R.id.btn_cancel:
                finish();
                break;
        }
    }
}
