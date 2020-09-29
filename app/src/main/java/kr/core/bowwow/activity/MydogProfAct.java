package kr.core.bowwow.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kr.core.bowwow.R;
import kr.core.bowwow.app;
import kr.core.bowwow.customWidget.CustomSpinner;
import kr.core.bowwow.customWidget.CustomSpinnerAdapter;
import kr.core.bowwow.databinding.ActivityMydogprofBinding;
import kr.core.bowwow.dto.pref.UserPref;
import kr.core.bowwow.network.HttpResult;
import kr.core.bowwow.network.MultipartUtility;
import kr.core.bowwow.network.NetUrls;
import kr.core.bowwow.network.ReqBasic;
import kr.core.bowwow.utils.MyUtil;

public class MydogProfAct extends BaseAct implements View.OnClickListener {

    ActivityMydogprofBinding binding;

    CustomSpinnerAdapter breedAdapter;
    CustomSpinnerAdapter genderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_mydogprof);

        setClickListener();

        if (MyUtil.isNull(app.myDogBirth)) {
            binding.tvBirth.setText(getString(R.string.birth_guide));
        } else {
            binding.tvBirth.setText(app.myDogBirth);
        }

        final List<String> breedList = new ArrayList<>();
        breedList.addAll(Arrays.asList(getResources().getStringArray(R.array.breed_list)));     // 서버 통신으로 대체 or 앱 내부 데이터

        final List<String> genderList = new ArrayList<>();
        genderList.addAll(Arrays.asList(getResources().getStringArray(R.array.gender)));

        breedAdapter = new CustomSpinnerAdapter(this, breedList);
        genderAdapter = new CustomSpinnerAdapter(this, genderList);

        binding.spnBreed.setAdapter(breedAdapter);
        binding.spnGender.setAdapter(genderAdapter);

        binding.spnBreed.setSelection((int) breedList.indexOf(app.myDogBreed));
        binding.spnGender.setSelection((int) genderList.indexOf(app.myDogGender));

        binding.spnBreed.setSpinnerEventsListener(new CustomSpinner.OnSpinnerEventsListener() {
            @Override
            public void onSpinnerOpened(Spinner spinner) {
                breedAdapter.isSelected(true);
                breedAdapter.notifyDataSetChanged();
            }

            @Override
            public void onSpinnerClosed(Spinner spinner) {
                breedAdapter.isSelected(false);
                breedAdapter.notifyDataSetChanged();
            }
        });

        binding.spnGender.setSpinnerEventsListener(new CustomSpinner.OnSpinnerEventsListener() {
            @Override
            public void onSpinnerOpened(Spinner spinner) {
                genderAdapter.isSelected(true);
                genderAdapter.notifyDataSetChanged();
            }

            @Override
            public void onSpinnerClosed(Spinner spinner) {
                genderAdapter.isSelected(false);
                genderAdapter.notifyDataSetChanged();
            }
        });

    }

    private void setClickListener() {

        binding.btnBack.setOnClickListener(this);
        binding.btnComplete.setOnClickListener(this);

        binding.llBritharea.setOnClickListener(this);

    }

    private void editDogInfo() {
        // 따로 적용 되는지 확인
        ReqBasic editInfo = new ReqBasic(this, NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                Log.d(MyUtil.TAG, "editDogInfo: " + resultData.getResult());
//                {"result":"Y","message":"성공적으로 수정하였습니다.","url":"","MEMCODE":""}
                if (resultData.getResult() != null) {

                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (jo.getString("result").equalsIgnoreCase("Y")) {
                            Toast.makeText(MydogProfAct.this, jo.getString("message"), Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(MydogProfAct.this, jo.getString("message"), Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MydogProfAct.this, getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(MydogProfAct.this, getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                }
            }
        };

        editInfo.addParams("CONNECTCODE", "APP");
        editInfo.addParams("siteUrl", NetUrls.MEDIADOMAIN);
        editInfo.addParams("dbControl", "setDogInfoEdit");
        editInfo.addParams("_APP_MEM_IDX", UserPref.getIdx(MydogProfAct.this));
        editInfo.addParams("MEMCODE", UserPref.getIdx(MydogProfAct.this));
        editInfo.addParams("m_uniq", UserPref.getDeviceId(this));
        editInfo.addParams("d_kname", app.myDogKname);
        editInfo.addParams("d_ename", app.myDogKname);
        editInfo.addParams("d_breed", binding.spnBreed.getSelectedItem().toString());
        editInfo.addParams("d_gender", binding.spnGender.getSelectedItem().toString());
        editInfo.addParams("d_birth", binding.tvBirth.getText().toString());
        editInfo.execute(true, true);
    }


    private void showDatePicker() {
        LayoutInflater dialog = LayoutInflater.from(this);
        View dlgLayout = dialog.inflate(R.layout.dlg_datepicker, null);
        final Dialog dlgdatepicker = new Dialog(this);
        dlgdatepicker.setContentView(dlgLayout);

        dlgdatepicker.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dlgdatepicker.show();

        final DatePicker datePicker = (DatePicker) dlgLayout.findViewById(R.id.dp_selectdate);

        datePicker.setMaxDate(System.currentTimeMillis());

        String date = binding.tvBirth.getText().toString();

        if (!MyUtil.isNull(date)) {
            int year = Integer.parseInt(date.split("\\.")[0]);
            int month = Integer.parseInt(date.split("\\.")[1]);
            int day = Integer.parseInt(date.split("\\.")[2]);

            Log.d(MyUtil.TAG, "year: " + year + " month: " + month + " day: " + day);

            datePicker.updateDate(year, month - 1, day);
        }

        TextView btn_ok = (TextView) dlgLayout.findViewById(R.id.btn_ok);
        TextView btn_cancel = (TextView) dlgLayout.findViewById(R.id.btn_cancel);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sdate = datePicker.getYear() + "." + String.format("%02d", (datePicker.getMonth() + 1)) + "." + String.format("%02d", datePicker.getDayOfMonth());
                Log.d("bowwow", "date: " + sdate);
                binding.tvBirth.setText(sdate);
                dlgdatepicker.dismiss();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dlgdatepicker.dismiss();
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_complete:

                if (binding.spnBreed.getSelectedItemPosition() == 0) {
                    Toast.makeText(this, "견종을 선택하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (binding.spnGender.getSelectedItemPosition() == 0) {
                    Toast.makeText(this, "성별을 선택하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (binding.tvBirth.getText().toString().equalsIgnoreCase(getString(R.string.birth_guide))) {
                    Toast.makeText(this, getString(R.string.birth_guide), Toast.LENGTH_SHORT).show();
                    return;
                }

                editDogInfo();
                break;
            case R.id.ll_britharea:
                showDatePicker();
                break;
        }
    }
}
