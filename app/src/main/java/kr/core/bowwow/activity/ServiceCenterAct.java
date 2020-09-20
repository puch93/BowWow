package kr.core.bowwow.activity;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import kr.core.bowwow.R;
import kr.core.bowwow.databinding.ActivityServieccenterBinding;

public class ServiceCenterAct extends Activity implements View.OnClickListener {

    ActivityServieccenterBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_servieccenter);

        binding.btnBack.setOnClickListener(this);

        binding.tvAppver.setText("v"+getAppVersion());

    }

    private String getAppVersion(){
        String version = "";
        try{
            PackageInfo i = getPackageManager().getPackageInfo(getPackageName(),0);
            version = i.versionName;
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
        return version;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_back:
                finish();
                break;
        }
    }
}
