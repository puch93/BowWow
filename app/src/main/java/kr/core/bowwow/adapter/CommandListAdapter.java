package kr.core.bowwow.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.internal.service.Common;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import kr.core.bowwow.R;
import kr.core.bowwow.activity.MainActivity;
import kr.core.bowwow.app;
import kr.core.bowwow.customWidget.VisualizerView;
import kr.core.bowwow.dialogAct.DlgCommandPlay;
import kr.core.bowwow.dialogAct.DlgPayment;
import kr.core.bowwow.dto.CommandItem;
import kr.core.bowwow.dto.pref.UserPref;
import kr.core.bowwow.network.HttpResult;
import kr.core.bowwow.network.NetUrls;
import kr.core.bowwow.network.ReqBasic;
import kr.core.bowwow.utils.LayoutWebView;
import kr.core.bowwow.utils.MyUtil;
import kr.core.bowwow.utils.StringUtil;

public class CommandListAdapter extends RecyclerView.Adapter<CommandListAdapter.ViewHolder> {

    Activity act;
    ArrayList<CommandItem> list;

    public CommandListAdapter(Activity act, ArrayList<CommandItem> list) {
        this.act = act;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_command, parent, false);
        CommandListAdapter.ViewHolder viewHolder = new CommandListAdapter.ViewHolder(v);

        int height = (parent.getMeasuredWidth() - act.getResources().getDimensionPixelSize(R.dimen.dimen_32)) / 3;

        if (height <= 0) {
            height = act.getResources().getDimensionPixelSize(R.dimen.dimen_109);
        }

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.itemarea.getLayoutParams();
        params.height = height;

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Glide.with(act)
                .load(list.get(position).getItemimg())
                .into(holder.cimg);

        if(list.get(position).getItemname().equalsIgnoreCase("쿠팡")) {
            holder.ctext.setVisibility(View.GONE);
            holder.itemarea.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getCoupaBanner();
                }
            });
        } else {
            holder.ctext.setVisibility(View.VISIBLE);
            holder.ctext.setText(list.get(position).getItemname());
            holder.itemarea.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (UserPref.getSubscribeState(act).equalsIgnoreCase("N")) {
//                        checkPay(list.get(getAdapterPosition()));
                        getMypoint(list.get(position));
                    } else {
                        Intent play = new Intent(act, DlgCommandPlay.class);
                        play.putExtra("command", list.get(position));
                        act.startActivity(play);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        LinearLayout itemarea;
        ImageView cimg;
        TextView ctext;

        public ViewHolder(View v) {
            super(v);
            itemarea = v.findViewById(R.id.ll_itemarea);
            cimg = v.findViewById(R.id.iv_cimg);
            ctext = v.findViewById(R.id.tv_ctext);

            ctext.setTypeface(app.tf_bmjua);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
//                case R.id.ll_itemarea:
//                    if (UserPref.getSubscribeState(act).equalsIgnoreCase("N")) {
////                        checkPay(list.get(getAdapterPosition()));
//                        getMypoint(list.get(getAdapterPosition()));
//                    } else {
//                        Intent play = new Intent(act, DlgCommandPlay.class);
//                        play.putExtra("command", list.get(getAdapterPosition()));
//                        act.startActivity(play);
//                    }

//                    Intent play = new Intent(act, DlgCommandPlay.class);
//                    play.putExtra("command",list.get(getAdapterPosition()));
//                    act.startActivity(play);

//                    Toast.makeText(act, ""+getAdapterPosition(), Toast.LENGTH_SHORT).show();
//                    break;
            }
        }
    }

    private void getMypoint(final CommandItem item) {
        ReqBasic myPoint = new ReqBasic(act, NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                Log.d(MyUtil.TAG, "getMypoint: " + resultData.getResult());
//                {"result":"Y","message":"성공적으로 등록하였습니다.","url":"", "point":"11"}
                if (resultData.getResult() != null) {

                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (jo.has("point")) {

                            if (UserPref.getSubscribeState(act).equalsIgnoreCase("N")) {
                                if (MyUtil.isNull(jo.getString("point"))) {
                                    act.startActivity(new Intent(act, DlgPayment.class));
                                } else {
                                    if (jo.getInt("point") > 0) {
                                        Intent play = new Intent(act, DlgCommandPlay.class);
                                        play.putExtra("command", item);
                                        act.startActivity(play);
                                    } else {
                                        act.startActivity(new Intent(act, DlgPayment.class));
                                    }
                                }
                            } else {
                                Intent play = new Intent(act, DlgCommandPlay.class);
                                play.putExtra("command", item);
                                act.startActivity(play);
                            }
                        } else {
                            Toast.makeText(act, act.getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
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

        myPoint.addParams("CONNECTCODE", "APP");
        myPoint.addParams("siteUrl", NetUrls.MEDIADOMAIN);
        myPoint.addParams("_APP_MEM_IDX", UserPref.getIdx(act));
        myPoint.addParams("dbControl", "getMemberPoint");
        myPoint.addParams("MEMCODE", UserPref.getIdx(act));
        myPoint.addParams("m_uniq", UserPref.getDeviceId(act));
        myPoint.execute(true, false);
    }

    private void checkPay(final CommandItem item) {
        ReqBasic checkPay = new ReqBasic(act, NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
//                {"result":"N","message":"포인트가 부족합니다.","url":"","point":""}
                Log.d(MyUtil.TAG, "checkPay: " + resultData.getResult());

                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (jo.getString("result").equalsIgnoreCase("Y")) {
                            Intent play = new Intent(act, DlgCommandPlay.class);
                            play.putExtra("command", item);
                            act.startActivity(play);
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

                                        Intent intent = new Intent(act, LayoutWebView.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        act.startActivity(intent);
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
        server.execute(true, true);
    }
}
