package kr.core.bowwow.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import kr.core.bowwow.R;
import kr.core.bowwow.databinding.ActivityTermsBinding;


public class TermsAct extends Activity implements View.OnClickListener {

    ActivityTermsBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_terms);

        binding.btnBack.setOnClickListener(this);

        String type = getIntent().getStringExtra("type");

        if (type.equalsIgnoreCase("t")){
            binding.tvTermtitle.setText("이용약관");
            binding.tvContent.setText(getResources().getString(R.string.privacy));
        }else if (type.equalsIgnoreCase("p")){
            binding.tvTermtitle.setText("개인정보 처리방침");
            binding.tvContent.setText(getResources().getString(R.string.privacy));
        }

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
