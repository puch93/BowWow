package kr.core.bowwow.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import kr.core.bowwow.R;
import kr.core.bowwow.activity.DogInfoEditAct;
import kr.core.bowwow.activity.MydogProfAct;
import kr.core.bowwow.adapter.CommandListAdapter;
import kr.core.bowwow.app;
import kr.core.bowwow.databinding.FragCommandBinding;
import kr.core.bowwow.dto.CommandItem;
import kr.core.bowwow.dto.pref.UserPref;
import kr.core.bowwow.network.HttpResult;
import kr.core.bowwow.network.NetUrls;
import kr.core.bowwow.network.ReqBasic;
import kr.core.bowwow.utils.AllOfDecoration;
import kr.core.bowwow.utils.LayoutWebView;
import kr.core.bowwow.utils.MyUtil;
import kr.core.bowwow.utils.StringUtil;

public class Command extends Fragment {
    FragCommandBinding binding;

    CommandListAdapter adapter;
    ArrayList<CommandItem> list = new ArrayList<>();

    GridLayoutManager mManager;
    public boolean isScroll = false;
    public int page = 1;
    int lastPage = 0;
    public static Activity act;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_command, container, false);
        act = getActivity();

        mManager = new GridLayoutManager(getContext(), 3);
        binding.rcvCmdlist.setLayoutManager(mManager);
        adapter = new CommandListAdapter(getActivity(), list, new CommandListAdapter.Clicked() {
            @Override
            public void clicked() {
                getMyPoint();
            }
        });
        binding.rcvCmdlist.setAdapter(adapter);

        binding.title.setTypeface(app.tf_bmjua);

        binding.rcvCmdlist.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalCount = mManager.getItemCount();
                int lastComplete = mManager.findLastCompletelyVisibleItemPosition();
                if (!isScroll) {
                    if (totalCount - 1 == lastComplete) {
                        if (page < lastPage) {
                            isScroll = true;
                            page++;
                            setCommandList();
                        }
                    }
                }
            }
        });

        binding.rcvCmdlist.addItemDecoration(new AllOfDecoration(act, "command"));

        setCommandList();
        getCoupaBanner();

        binding.mydogModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), DogInfoEditAct.class));
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        getMyPoint();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK) {
            getMyPoint();
        }
    }

    public void getMyPoint() {
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

    private void setCommandList() {
        isScroll = true;
        ReqBasic commandList = new ReqBasic(getActivity(), NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                Log.d(MyUtil.TAG, "etCommandList: " + resultData.getResult());
//                {"total":"1","maxpageno":1,"result":"Y","message":"","data":[{"do_idx":"2","do_site":"1","do_user_idx":"0","do_name":"\ub2e5\uccd0","do_img":"\/UPLOAD\/DOGORDER\/30726684_DHVQnkyg_default-user.png","do_sound":"\/UPLOAD\/DOGORDER\/30726684_eVMBN0ag_Aggressive-dogs-barking.mp3","do_regdate":"2019-12-18 12:29:02","do_editdate":"0000-00-00 00:00:00","num":1}]}

                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        jo.getString("total");
                        jo.getString("maxpageno");
                        jo.getString("result");

                        lastPage = jo.getInt("maxpageno");

                        JSONArray ja = jo.getJSONArray("data");
                        ArrayList<CommandItem> tmplist = new ArrayList<>();
                        if (ja.length() > 0) {
                            for (int i = 0; i < ja.length(); i++) {
                                JSONObject comm = ja.getJSONObject(i);
                                CommandItem data = new CommandItem();

                                comm.getString("do_idx");
                                comm.getString("do_site");
                                comm.getString("do_user_idx");
                                comm.getString("do_name");
                                comm.getString("do_img");
                                comm.getString("do_sound");
                                comm.getString("do_regdate");
                                comm.getString("do_editdate");
                                comm.getString("num");

                                data.setCidx(comm.getString("do_idx"));
                                data.setItemimg(NetUrls.MEDIADOMAIN + comm.getString("do_img"));
                                data.setItemname(comm.getString("do_name"));
                                data.setCsound(NetUrls.MEDIADOMAIN + comm.getString("do_sound"));

                                tmplist.add(data);
                            }

                            if (list.size() <= Integer.parseInt(jo.getString("total"))) {
                                list.addAll(tmplist);
                                adapter.notifyDataSetChanged();
                            }
                        }

                        isScroll = false;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        isScroll = false;
                        if (list.size() > 0) {
                            list.clear();
                        }
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getActivity(), getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    isScroll = false;
                    if (list.size() > 0) {
                        list.clear();
                    }
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getActivity(), getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                }
            }
        };

        commandList.addParams("CONNECTCODE", "APP");
        commandList.addParams("siteUrl", NetUrls.MEDIADOMAIN);
//        commandList.addParams("APPCONNECTCODE","APP");
        commandList.addParams("dbControl", "getDogorderList");
        commandList.addParams("_APP_MEM_IDX", UserPref.getIdx(getActivity()));
        commandList.addParams("m_uniq", UserPref.getDeviceId(getActivity()));
        commandList.addParams("pagenum", String.valueOf(page));
        commandList.execute(true, true);
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
                    }
                } else {
                    act.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.bannerCore.setVisibility(View.GONE);
                        }
                    });
                }
            }
        };

        server.addParams("dbControl", "getCoupangPartnersInfo");
        server.addParams("si_idx", "7");
        server.execute(true, false);
    }

}
