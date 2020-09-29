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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import kr.core.bowwow.R;
import kr.core.bowwow.activity.MydogProfAct;
import kr.core.bowwow.activity.ServiceCenterAct;
import kr.core.bowwow.adapter.ChatAdapter;
import kr.core.bowwow.adapter.DictionaryAdapter;
import kr.core.bowwow.app;
import kr.core.bowwow.databinding.FragDictionaryBinding;
import kr.core.bowwow.databinding.FragMoreBinding;
import kr.core.bowwow.dialogAct.DlgPayment;
import kr.core.bowwow.dto.DictionaryData;
import kr.core.bowwow.dto.pref.SettingPref;
import kr.core.bowwow.dto.pref.UserPref;
import kr.core.bowwow.network.HttpResult;
import kr.core.bowwow.network.NetUrls;
import kr.core.bowwow.network.ReqBasic;
import kr.core.bowwow.utils.LayoutWebView;
import kr.core.bowwow.utils.MyUtil;
import kr.core.bowwow.utils.StringUtil;

public class Dictionary extends Fragment implements View.OnClickListener {
    FragDictionaryBinding binding;
    Activity act;

    ArrayList<DictionaryData> list = new ArrayList<>();
    DictionaryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_dictionary, container, false);
        act = getActivity();

        binding.btnSearch.setOnClickListener(this);
        binding.btnSerchclose.setOnClickListener(this);

        binding.title.setTypeface(app.tf_bmjua);

        String[] arrays = getResources().getStringArray(R.array.breed_list);
        for (int i = 1; i < arrays.length; i++) {
            list.add(new DictionaryData(MyUtil.getDogImage(arrays[i]), arrays[i]));
        }

        adapter = new DictionaryAdapter(act, list);
        binding.recyclerView.setLayoutManager(new GridLayoutManager(act, 2));
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setItemViewCacheSize(20);
        binding.recyclerView.setAdapter(adapter);

        getMyPoint();

        return binding.getRoot();
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
    private ArrayList<DictionaryData> getSearchList(String keyword) {
        ArrayList<DictionaryData> searchList = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            if(list.get(i).getName().contains(keyword)) {
                searchList.add(list.get(i));
            }
        }

        return searchList;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_search:
                if (binding.searchArea.getVisibility() != View.VISIBLE) {
                    binding.searchArea.setVisibility(View.VISIBLE);
                    binding.title.setVisibility(View.GONE);
                    binding.boneArea.setVisibility(View.GONE);
//                    binding.llDetectmsgArea.setVisibility(View.GONE);
                }else{
                    // 검색
                    if (binding.etSearch.length() == 0){
                        Toast.makeText(act, "검색어를 입력하세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Toast.makeText(act, binding.etSearch.getText()+" 검색", Toast.LENGTH_SHORT).show();

                    if(getSearchList(binding.etSearch.getText().toString()).size() == 0) {
                        Toast.makeText(act, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        act.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.setList(getSearchList(binding.etSearch.getText().toString()));
                            }
                        });
                    }
                }
                break;

            case R.id.btn_serchclose:
                binding.searchArea.setVisibility(View.GONE);
                binding.title.setVisibility(View.VISIBLE);
                binding.boneArea.setVisibility(View.VISIBLE);
                binding.etSearch.setText(null);

                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.setList(list);
                    }
                });
                break;
        }
    }
}
