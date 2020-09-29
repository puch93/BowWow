package kr.core.bowwow.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import kr.core.bowwow.R;
import kr.core.bowwow.adapter.ChatAdapter;
import kr.core.bowwow.adapter.DogstatsAdapter;
import kr.core.bowwow.app;
import kr.core.bowwow.databinding.ActivityDictionaryDetailBinding;
import kr.core.bowwow.dto.DictionaryData;
import kr.core.bowwow.dto.DogstatsItem;
import kr.core.bowwow.dto.pref.UserPref;
import kr.core.bowwow.network.HttpResult;
import kr.core.bowwow.network.NetUrls;
import kr.core.bowwow.network.ReqBasic;
import kr.core.bowwow.utils.MyUtil;

public class DictionaryDetailAct extends BaseAct implements View.OnClickListener {
    ActivityDictionaryDetailBinding binding;
    Activity act;

    List<String> breedInfo = new ArrayList<>();
    ArrayList<DogstatsItem> list = new ArrayList<>();
    DogstatsAdapter adapter;

    DictionaryData received_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dictionary_detail, null);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        act = this;

        received_data = (DictionaryData) getIntent().getSerializableExtra("data");
        binding.title.setText(received_data.getName());
        Glide.with(act).load(received_data.getImage()).into(binding.image);


        binding.btnM1.setOnClickListener(this);
        binding.btnM2.setOnClickListener(this);
        binding.btnM3.setOnClickListener(this);
        binding.btnM4.setOnClickListener(this);
        binding.btnBack.setOnClickListener(this);
        binding.btnBack02.setOnClickListener(this);


        breedInfo.addAll(MyUtil.getBreedInfo(act, received_data.getName()));

        binding.rcvStatsList.setLayoutManager(new LinearLayoutManager(act));

        binding.nscArea.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (binding.rcvStatsList.getChildAt(1).getTop() < scrollY && scrollY <
                        binding.rcvStatsList.getChildAt(1).getBottom()){
                    Log.d(MyUtil.TAG, "성격 위치");
                    adapter.progAni(list.get(1));
                }
            }
        });

        getBreedInfo();
    }


    private void getBreedInfo(){
        ReqBasic bInfo = new ReqBasic(act,NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                Log.d(MyUtil.TAG, "mydog getBreedInfo: "+resultData.getResult());
//                {"result":"Y","message":"","data":{"dt_idx":"5","dt_site":"1","dt_user_idx":"0","dt_name":"\ud478\ub4e4","dt_yu":"","dt_tek":"","dt_sung":"","dt_jil":"","dt_regdate":"2020-01-17 13:20:45","dt_editdate":"2020-01-17 18:33:58","dt_type1":"81","dt_type2":"42","dt_type3":"50","dt_type4":"74","dt_type5":"90"}}
//                {"result":"Y","message":"","data":{"dt_idx":"4","dt_site":"1","dt_user_idx":"0","dt_name":"\ub2e5\uc2a4\ud6c8\ud2b8","dt_yu":"\uc720\ub798","dt_tek":"\ud2b9\uc9d5","dt_sung":"\uc131\uaca9","dt_jil":"\uc9c8\ubcd1","dt_regdate":"2019-12-18 13:39:48","dt_editdate":"0000-00-00 00:00:00"}}
//                dt_yu/dt_tek/dt_sung/dt_jil
                if (resultData.getResult() != null){
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        jo.getString("result");
                        jo.getString("message");
                        jo.getString("data");

                        if (jo.getString("result").equalsIgnoreCase("Y")){

                            JSONObject data = jo.getJSONObject("data");

                            // 견종(유래,특징,성격,질병) 서버 저장값
//                            if (breedInfo.size() > 0){
//                                breedInfo.clear();
//                            }
//                            breedInfo.add(data.getString("dt_yu"));
//                            breedInfo.add(data.getString("dt_tek"));
//                            breedInfo.add(data.getString("dt_sung"));
//                            breedInfo.add(data.getString("dt_jil"));

                            data.getString("dt_yu");
                            data.getString("dt_tek");
                            data.getString("dt_sung");
                            data.getString("dt_jil");

                            data.getString("dt_type1");
                            data.getString("dt_type2");
                            data.getString("dt_type3");
                            data.getString("dt_type4");
                            data.getString("dt_type5");

                            DogstatsItem m1 = new DogstatsItem();

                            m1.setItemtype("m1");
                            m1.setBreed(received_data.getName());
//                            m1.setExplanation(data.getString("dt_yu"));
                            m1.setExplanation(breedInfo.get(0));
                            m1.setMTitle("유래");

                            DogstatsItem m2 = new DogstatsItem();

                            m2.setItemtype("m2");
//                            m2.setExplanation(data.getString("dt_sung"));
                            m2.setExplanation(breedInfo.get(2));
                            m2.setMTitle("성격");
                            m2.setStats01(data.getString("dt_type1"));
//                            m2.setStats02(data.getString("dt_type2"));
                            m2.setStats02(data.getString("dt_type3"));
                            m2.setStats03(data.getString("dt_type4"));
                            m2.setStats04(data.getString("dt_type5"));

                            DogstatsItem m3 = new DogstatsItem();

                            m3.setItemtype("m3");
//                            m3.setExplanation(data.getString("dt_jil"));
                            m3.setExplanation(breedInfo.get(3));
                            m3.setMTitle("질병");

                            DogstatsItem m4 = new DogstatsItem();

                            m4.setItemtype("m4");
                            m4.setExplanation(breedInfo.get(1));
                            m4.setMTitle("특징");

                            if (list.size() > 0){
                                list.clear();
                            }

                            list.add(m1);
                            list.add(m2);
                            list.add(m3);
                            list.add(m4);

                            adapter = new DogstatsAdapter(act,list);
                            binding.rcvStatsList.setAdapter(adapter);
//                            adapter.notifyDataSetChanged();

                        }else{
                            Toast.makeText(act, jo.getString("message"), Toast.LENGTH_SHORT).show();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(act, getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(act, getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                }

            }
        };

        bInfo.addParams("CONNECTCODE","APP");
        bInfo.addParams("siteUrl", NetUrls.MEDIADOMAIN);
        bInfo.addParams("dbControl","getDogtypeInfo");
        bInfo.addParams("_APP_MEM_IDX", UserPref.getIdx(act));
        bInfo.addParams("MEMCODE", UserPref.getIdx(act));
        bInfo.addParams("m_uniq", UserPref.getDeviceId(act));
        bInfo.addParams("_NAME", received_data.getName());
        bInfo.execute(true,true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_m1:
                binding.nscArea.smoothScrollTo(0,(int)binding.rcvStatsList.getChildAt(1).getY());
                break;
            case R.id.btn_m2:
                binding.nscArea.smoothScrollTo(0,(int)binding.rcvStatsList.getChildAt(2).getY());
                break;
            case R.id.btn_m3:
                binding.nscArea.smoothScrollTo(0,(int)binding.rcvStatsList.getChildAt(3).getY());
                break;
            case R.id.btn_m4:
                binding.nscArea.smoothScrollTo(0,(int)binding.rcvStatsList.getChildAt(3).getBottom());
                break;
            case R.id.btn_back:
            case R.id.btn_back_02:
                finish();
                break;
        }
    }
}