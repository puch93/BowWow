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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_dictionary, container, false);
        act = getActivity();

        binding.title.setTypeface(app.tf_bmjua);

        String[] arrays = getResources().getStringArray(R.array.breed_list);
        for (int i = 1; i < arrays.length; i++) {
            list.add(new DictionaryData(null, arrays[i]));
        }
        binding.recyclerView.setLayoutManager(new GridLayoutManager(act, 2));
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setItemViewCacheSize(20);
        binding.recyclerView.setAdapter(new DictionaryAdapter(act, list));

        return binding.getRoot();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
    }
}
