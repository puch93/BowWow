package kr.core.bowwow.fragments;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import kr.core.bowwow.R;
import kr.core.bowwow.activity.DogInfoAct;
import kr.core.bowwow.activity.MainActivity;
import kr.core.bowwow.activity.MydogProfAct;
import kr.core.bowwow.adapter.DogstatsAdapter;
import kr.core.bowwow.app;
import kr.core.bowwow.customWidget.CustomScrollView;
import kr.core.bowwow.databinding.FragMydogBinding;
import kr.core.bowwow.dto.DogstatsItem;
import kr.core.bowwow.dto.pref.UserPref;
import kr.core.bowwow.network.HttpResult;
import kr.core.bowwow.network.MultipartUtility;
import kr.core.bowwow.network.NetUrls;
import kr.core.bowwow.network.ReqBasic;
import kr.core.bowwow.utils.MyUtil;

import static android.app.Activity.RESULT_OK;

public class Mydog extends Fragment implements View.OnClickListener {
    FragMydogBinding binding;

    private Uri photoUri;
    private String mImgFilePath;

    List<String> breedInfo = new ArrayList<>();
    ArrayList<DogstatsItem> list = new ArrayList<>();
    DogstatsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.frag_mydog, container, false);
        setClickListener();

        binding.title.setTypeface(app.tf_bmjua);

        if (getActivity() != null) {
                if (!MyUtil.isNull(app.myDogKname)) {
                    Glide.with(getActivity())
                            .load(app.myDogImg)
                            .into(binding.ivPimg);

                    binding.tvEname.setText(app.myDogKname);
//                    binding.tvKname.setText(app.myDogKname);
                    binding.tvBreed.setText(app.myDogBreed);
                    if (breedInfo.size() > 0){
                        breedInfo.clear();
                    }
                    breedInfo.addAll(MyUtil.getBreedInfo(getContext(), app.myDogBreed));
//                    binding.tvBreedinfo.setText(breedInfo.get(0));
                    binding.tvGender.setText(app.myDogGender);
                    binding.tvBrithday.setText(app.myDogBirth);

                    if (app.myDogGender.equalsIgnoreCase("남")) {
                        binding.tvGender.setTextColor(getResources().getColor(R.color.c_gblue));
                        binding.ivGenderimg.setImageResource(R.drawable.pettranslate_bg_main_piocn01_200117);
                    }else{
                        binding.tvGender.setTextColor(getResources().getColor(R.color.c_gpink));
                        binding.ivGenderimg.setImageResource(R.drawable.pettranslate_bg_main_piocn03_200117);
                    }

                    getBreedInfo();

//                    binding.ivDogmainimg.setVisibility(View.VISIBLE);
//                    Glide.with(getActivity())
//                            .load(app.myDogImg)
//                            .into(binding.ivDogmainimg);
                }
        }

        binding.rcvStatsList.setLayoutManager(new LinearLayoutManager(getActivity()));

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

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        getDogInfo();
    }

    private void getDogInfo(){
        // 반려견 정보 가져오기 및 세팅
        ReqBasic dogInfo = new ReqBasic(getActivity(),NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                Log.d(MyUtil.TAG, "getDogInfo: "+resultData.getResult());
//                {"result":"Y","message":"성공적으로 수정하였습니다.","url":"","DOGCODE":"{"d_idx":"359","d_site":"1","d_user_idx":"0","d_pimg":"\/UPLOAD\/DOG_INFO\/30726684_eKARpZkE_profimg1217170040272885618.jpg","d_kname":"\uba4d\uba4d","d_ename":"mm","d_breed":"\ubd88\ub3c5","d_gender":"\ub0a8","d_birth":"2019.11.01","d_regdate":"2019-12-17 17:00:53","d_editdate":"0000-00-00 00:00:00"}"}

                if (resultData.getResult() != null){
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (jo.getString("result").equalsIgnoreCase("Y")){
//                        Log.d(MyUtil.TAG, "DOGCODE: "+jo.getJSONObject("DOGCODE"));
                            Log.d(MyUtil.TAG, "DOGCODE: "+jo.getString("DOGCODE"));

                            JSONObject dInfo = jo.getJSONObject("DOGCODE");

//                        dInfo.getString("d_idx");
//                        dInfo.getString("d_site");
//                        dInfo.getString("d_user_idx");
//                        dInfo.getString("d_pimg");
//                        NetUrls.MEDIADOMAIN + dInfo.getString("d_pimg");
//                        dInfo.getString("d_kname");
//                        dInfo.getString("d_ename");
//                        dInfo.getString("d_breed");
//                        dInfo.getString("d_gender");
//                        dInfo.getString("d_birth");
//                        dInfo.getString("d_regdate");
//                        dInfo.getString("d_editdate");


                            app.myDogImg = NetUrls.MEDIADOMAIN + dInfo.getString("d_pimg");
                            app.myDogBreed = dInfo.getString("d_breed");
                            app.myDogGender = dInfo.getString("d_gender");
                            app.myDogBirth = dInfo.getString("d_birth");
                            app.myDogKname = dInfo.getString("d_kname");


//                            Calendar c = Calendar.getInstance();
//                            c.get(Calendar.YEAR) - app.myDogBirth
                            Log.d(MyUtil.TAG, "myDogBirth: "+app.myDogBirth);
                            Log.d(MyUtil.TAG, "split: "+app.myDogBirth.split("\\."));

                            if (!MyUtil.isNull(app.myDogBirth)){
                                final String[] birth = app.myDogBirth.split("\\.");
//                                String y = app.myDogBirth.split("\\.")[0];
                                // +1 여부 확인
                                Calendar c = Calendar.getInstance();
                                int age = c.get(Calendar.YEAR) - Integer.parseInt(birth[0]);
                                Log.d(MyUtil.TAG, "age: "+age);

                                countdday(Integer.parseInt(birth[0]),Integer.parseInt(birth[1]),Integer.parseInt(birth[2]));

                                Log.d(MyUtil.TAG, "출생 후: "+countdday(Integer.parseInt(birth[0]),Integer.parseInt(birth[1]),Integer.parseInt(birth[2]))+"일");

                                Log.d(MyUtil.TAG, "TEST: "+countdday(2020,8,22));

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        binding.age.setText(MyUtil.getAge(birth[0]));
                                        binding.day.setText(MyUtil.getDday(Integer.parseInt(birth[0]), Integer.parseInt(birth[1])-1, Integer.parseInt(birth[2])));
                                    }
                                });
                            }


                            if (getActivity() != null) {
                                Glide.with(getActivity())
                                        .load(app.myDogImg)
                                        .into(binding.ivPimg);


                                binding.tvEname.setText(app.myDogKname);
//                                binding.tvKname.setText(app.myDogKname);
                                binding.tvBreed.setText(app.myDogBreed);
                                if (breedInfo.size() > 0){
                                    breedInfo.clear();
                                }
                                breedInfo.addAll(MyUtil.getBreedInfo(getContext(), app.myDogBreed));
//                                binding.tvBreedinfo.setText(breedInfo.get(0));
                                binding.tvGender.setText(app.myDogGender);
                                binding.tvBrithday.setText(app.myDogBirth);

                                if (app.myDogGender.equalsIgnoreCase("남")) {
                                    binding.tvGender.setTextColor(getResources().getColor(R.color.c_gblue));
                                    binding.ivGenderimg.setImageResource(R.drawable.pettranslate_bg_main_piocn01_200117);
                                }else{
                                    binding.tvGender.setTextColor(getResources().getColor(R.color.c_gpink));
                                    binding.ivGenderimg.setImageResource(R.drawable.pettranslate_bg_main_piocn03_200117);
                                }

//                                binding.ivDogmainimg.setVisibility(View.VISIBLE);
//                                Glide.with(getActivity())
//                                        .load(app.myDogImg)
//                                        .into(binding.ivDogmainimg);

                                getBreedInfo();

                            }

                        }else{
                            Toast.makeText(getActivity(), jo.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(getActivity(), getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                }

            }
        };

        dogInfo.addParams("CONNECTCODE","APP");
        dogInfo.addParams("siteUrl",NetUrls.MEDIADOMAIN);
        dogInfo.addParams("dbControl","setDogInfo");
        dogInfo.addParams("_APP_MEM_IDX", UserPref.getIdx(getActivity()));
        dogInfo.addParams("MEMCODE", UserPref.getIdx(getActivity()));
        dogInfo.addParams("m_uniq", UserPref.getDeviceId(getActivity()));
        dogInfo.execute(true,true);

    }

    private void setClickListener(){
        binding.mydogModify.setOnClickListener(this);

        binding.btnRegphoto.setOnClickListener(this);

        binding.btnM1.setOnClickListener(this);
        binding.btnM2.setOnClickListener(this);
        binding.btnM3.setOnClickListener(this);
        binding.btnM4.setOnClickListener(this);

        binding.btnMovetop.setOnClickListener(this);
    }

    public int countdday(int myear, int mmonth, int mday) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

            Calendar todaCal = Calendar.getInstance(); //오늘날자 가져오기
            Calendar ddayCal = Calendar.getInstance(); //오늘날자를 가져와 변경시킴

            mmonth -= 1; // 받아온날자에서 -1을 해줘야함.
            ddayCal.set(myear,mmonth,mday);// D-day의 날짜를 입력
            Log.e("테스트",simpleDateFormat.format(todaCal.getTime()) + "");
            Log.e("테스트",simpleDateFormat.format(ddayCal.getTime()) + "");

            long today = todaCal.getTimeInMillis()/86400000; //->(24 * 60 * 60 * 1000) 24시간 60분 60초 * (ms초->초 변환 1000)
            long dday = ddayCal.getTimeInMillis()/86400000;
//            long count = dday - today; // 오늘 날짜에서 dday 날짜를 빼주게 됩니다.
            long count = dday - today; // 오늘 날짜에서 dday 날짜를 빼주게 됩니다.
            return (int) count;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    // 등록 글자 짤림(확인 필요)
    private void getBreedInfo(){
        ReqBasic bInfo = new ReqBasic(getActivity(),NetUrls.DOMAIN) {
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
                            m1.setBreed(app.myDogBreed);
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

                            adapter = new DogstatsAdapter(getActivity(),list);
                            binding.rcvStatsList.setAdapter(adapter);
//                            adapter.notifyDataSetChanged();

                        }else{
                            Toast.makeText(getActivity(), jo.getString("message"), Toast.LENGTH_SHORT).show();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getActivity(), getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                }

            }
        };

        bInfo.addParams("CONNECTCODE","APP");
        bInfo.addParams("siteUrl",NetUrls.MEDIADOMAIN);
        bInfo.addParams("dbControl","getDogtypeInfo");
        bInfo.addParams("_APP_MEM_IDX", UserPref.getIdx(getActivity()));
        bInfo.addParams("MEMCODE", UserPref.getIdx(getActivity()));
        bInfo.addParams("m_uniq", UserPref.getDeviceId(getActivity()));
//        binding.tvBreed.getText().toString()  견종 text
//        bInfo.addParams("_NAME","푸들");
//        bInfo.addParams("_NAME","닥스훈트");
        bInfo.addParams("_NAME",app.myDogBreed);
        bInfo.execute(true,true);
    }

    private void editDogInfo(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    MultipartUtility mu = new MultipartUtility(NetUrls.DOMAIN,"UTF-8");

                    mu.addFormField("CONNECTCODE","APP");
                    mu.addFormField("siteUrl",NetUrls.MEDIADOMAIN);
                    mu.addFormField("dbControl","setDogInfoEdit");
                    mu.addFormField("_APP_MEM_IDX", UserPref.getIdx(getActivity()));
                    mu.addFormField("MEMCODE", UserPref.getIdx(getActivity()));
                    mu.addFormField("m_uniq", UserPref.getDeviceId(getActivity()));
                    mu.addFormField("d_kname",app.myDogKname);
                    mu.addFormField("d_ename",app.myDogKname);
                    mu.addFormField("d_breed",app.myDogBreed);
                    mu.addFormField("d_gender",app.myDogGender);
                    mu.addFormField("d_birth",app.myDogBirth);

                    File img = new File(mImgFilePath);
                    mu.addFilePart("d_pimg",img);

                    String res = mu.finish();
                    Log.d(MyUtil.TAG, "mydog editDogInfo: "+res);
//                    {"result":"Y","message":"성공적으로 등록하였습니다.","url":"","MEMCODE":"359"}
                    if (MyUtil.isNull(res)){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else{
                        try {
                            JSONObject jo = new JSONObject(res);

                            if (jo.getString("result").equalsIgnoreCase("Y")){
//                            UserPref.setDogIdx(DogInfoAct.this,jo.getString("MEMCODE"));
                                getDogInfo();
                            }else{
                                final String msg = jo.getString("message");
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                }catch (IOException e){
                    e.printStackTrace();
                }

            }
        }).start();

    }

    private void showRegphoto() {
        LayoutInflater dialog = LayoutInflater.from(getActivity());
        View dlgLayout = dialog.inflate(R.layout.dlg_regphoto, null);
        final Dialog regphoto = new Dialog(getActivity());
        regphoto.setContentView(dlgLayout);

        regphoto.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        regphoto.show();

        TextView btn_camera = (TextView) dlgLayout.findViewById(R.id.btn_camera);
        TextView btn_album = (TextView) dlgLayout.findViewById(R.id.btn_album);

        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(DogInfoAct.this, "사진촬영", Toast.LENGTH_SHORT).show();
                takePhoto();
                regphoto.dismiss();
            }
        });

        btn_album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(DogInfoAct.this, "앨범선택", Toast.LENGTH_SHORT).show();
                getAlbum();
                regphoto.dismiss();
            }
        });

    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("MMddHHmmss").format(new Date());
        String imageFileName = "profimg" + timeStamp;
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/BOWWOW/");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        return image;
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(getActivity(), "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
//            finish();
            e.printStackTrace();
        }

        if (photoFile != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                photoUri = FileProvider.getUriForFile(getActivity(),
                        "kr.core.bowwow.provider", photoFile);
            } else {
                photoUri = Uri.fromFile(photoFile);
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(intent, app.REQUEST_TAKE_PHOTO);
        }
    }

    private void getAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, app.REQUEST_TAKE_ALBUM);
    }

    private void cropImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getContext().grantUriPermission("com.android.camera", photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(photoUri, "image/*");

        List<ResolveInfo> list = getContext().getPackageManager().queryIntentActivities(intent, 0);

        int size = list.size();
        if (size == 0) {

            Toast.makeText(getActivity(), "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        } else {

            Toast.makeText(getActivity(), "용량이 큰 사진의 경우 시간이 오래 걸릴 수 있습니다.", Toast.LENGTH_SHORT).show();


            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            File croppedFileName = null;
            try {
                croppedFileName = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            File folder = new File(Environment.getExternalStorageDirectory() + "/BOWWOW/");
            File tempFile = new File(folder.toString(), croppedFileName.getName());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                photoUri = FileProvider.getUriForFile( getActivity(),
                        "kr.core.bowwow.provider", tempFile);
            } else {
                photoUri = Uri.fromFile(tempFile);
            }

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("return-data", false);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

            Intent i = new Intent(intent);
            ResolveInfo res = list.get(0);

            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            getContext().grantUriPermission(res.activityInfo.packageName, photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            startActivityForResult(i, app.REQUEST_IMAGE_CROP);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            Toast.makeText(getActivity(), "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (requestCode) {
            case app.REQUEST_TAKE_PHOTO:
                MediaScannerConnection.scanFile(getActivity(), new String[]{photoUri.getPath()}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String path, Uri uri) {

                            }
                        });

                cropImage();
                break;
            case app.REQUEST_TAKE_ALBUM:
                if (data == null) {
                    Toast.makeText(getActivity(), "사진불러오기 실패", Toast.LENGTH_SHORT).show();
                    return;
                }
                photoUri = data.getData();
                cropImage();
                break;
            case app.REQUEST_IMAGE_CROP:
                // crop 결과 처리
                mImgFilePath = photoUri.getPath();

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                Bitmap bm = BitmapFactory.decodeFile(mImgFilePath, options);

                Bitmap resize = null;
                try {
                    File resize_file = new File(mImgFilePath);
                    FileOutputStream out = new FileOutputStream(resize_file);

                    int width = bm.getWidth();
                    int height = bm.getHeight();

                    resize = Bitmap.createScaledBitmap(bm, width, height, true);
                    resize.compress(Bitmap.CompressFormat.JPEG, 100, out);

                    editDogInfo();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.mydog_modify:
                startActivity(new Intent(getActivity(), MydogProfAct.class));
//                Toast.makeText(getContext(), "프로필수정", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_movetop:
                binding.nscArea.scrollTo(0,0);
                break;
            case R.id.btn_regphoto:
                if (isReqPermission()){
                    reqPermission();
                }else {
                    showRegphoto();
                }
//                Toast.makeText(getContext(), "사진등록", Toast.LENGTH_SHORT).show();
                break;
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
        }
    }

    private void reqPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            },0);
        }
    }

    private boolean isReqPermission() {
        // 필요권한 ( 전화 걸기 및 관리, 메세지 전송 및 보기, 주소록 액세스, 사진 및 미디어 파일 액세스, 위치정보)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (
                    getContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                            getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
