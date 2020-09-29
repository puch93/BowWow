package kr.core.bowwow.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import kr.core.bowwow.R;
import kr.core.bowwow.adapter.ImageAdapter;
import kr.core.bowwow.app;
import kr.core.bowwow.customWidget.CustomSpinner;
import kr.core.bowwow.customWidget.CustomSpinnerAdapter;
import kr.core.bowwow.databinding.ActivityDoginfoBinding;
import kr.core.bowwow.dto.pref.UserPref;
import kr.core.bowwow.network.MultipartUtility;
import kr.core.bowwow.network.NetUrls;
import kr.core.bowwow.utils.AllOfDecoration;
import kr.core.bowwow.utils.MyUtil;
import kr.core.bowwow.utils.StringUtil;

public class DogInfoAct extends BaseAct implements View.OnClickListener {

    ActivityDoginfoBinding binding;

    CustomSpinnerAdapter breedAdapter;
    CustomSpinnerAdapter genderAdapter;
    CustomSpinnerAdapter nameTypeAdapter;

    private Uri photoUri;
    private String mImgFilePath;

    ImageAdapter imageAdapter;
    ArrayList<String> image_list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_doginfo);

        setClickListener();
        setSpinner();

        binding.tvBirth.setText(getString(R.string.birth_guide));


        image_list.add(null);
        imageAdapter = new ImageAdapter(this, image_list, new ImageAdapter.ButtonClickListener() {
            @Override
            public void deleteClicked() {
                image_list = imageAdapter.getList();
            }

            @Override
            public void addClicked() {
                showRegphoto();
            }
        });
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(RecyclerView.HORIZONTAL);
        binding.recyclerView.setLayoutManager(manager);
        binding.recyclerView.setItemViewCacheSize(20);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setAdapter(imageAdapter);
        binding.recyclerView.addItemDecoration(new AllOfDecoration(this, "reg"));

    }

    private void setSpinner() {
        final List<String> breedList = new ArrayList<>();
        breedList.addAll(Arrays.asList(getResources().getStringArray(R.array.breed_list)));     // 서버 통신으로 대체 or 앱 내부 데이터

        final List<String> genderList = new ArrayList<>();
        genderList.addAll(Arrays.asList(getResources().getStringArray(R.array.gender)));

        final List<String> nameType = new ArrayList<>();
        nameType.addAll(Arrays.asList(getResources().getStringArray(R.array.name_type)));

        breedAdapter = new CustomSpinnerAdapter(this, breedList);
        genderAdapter = new CustomSpinnerAdapter(this, genderList);
        nameTypeAdapter = new CustomSpinnerAdapter(this, nameType);

        binding.spnBreed.setAdapter(breedAdapter);
        binding.spnGender.setAdapter(genderAdapter);
        binding.spnNameType.setAdapter(nameTypeAdapter);

        binding.spnNameType.setSpinnerEventsListener(new CustomSpinner.OnSpinnerEventsListener() {
            @Override
            public void onSpinnerOpened(Spinner spinner) {
                nameTypeAdapter.isSelected(true);
                nameTypeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onSpinnerClosed(Spinner spinner) {
                nameTypeAdapter.isSelected(false);
                nameTypeAdapter.notifyDataSetChanged();
            }
        });


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

        binding.btnComplete.setOnClickListener(this);
        binding.llBritharea.setOnClickListener(this);
        binding.btnBack.setOnClickListener(this);

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

        TextView btn_ok = (TextView) dlgLayout.findViewById(R.id.btn_ok);
        TextView btn_cancel = (TextView) dlgLayout.findViewById(R.id.btn_cancel);

        String date = binding.tvBirth.getText().toString();

        if (!MyUtil.isNull(date)) {
            if (!date.equalsIgnoreCase(getString(R.string.birth_guide))) {
                int year = Integer.parseInt(date.split("\\.")[0]);
                int month = Integer.parseInt(date.split("\\.")[1]);
                int day = Integer.parseInt(date.split("\\.")[2]);

                Log.d(MyUtil.TAG, "year: " + year + " month: " + month + " day: " + day);

                datePicker.updateDate(year, month - 1, day);
            }
        }

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

    private void showRegphoto() {
        LayoutInflater dialog = LayoutInflater.from(this);
        View dlgLayout = dialog.inflate(R.layout.dlg_regphoto, null);
        final Dialog regphoto = new Dialog(this);
        regphoto.setContentView(dlgLayout);

        regphoto.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        regphoto.show();

        TextView btn_camera = (TextView) dlgLayout.findViewById(R.id.btn_camera);
        TextView btn_album = (TextView) dlgLayout.findViewById(R.id.btn_album);

        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
                regphoto.dismiss();
            }
        });

        btn_album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            Toast.makeText(this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
//            finish();
            e.printStackTrace();
        }

        if (photoFile != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                photoUri = FileProvider.getUriForFile(this,
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
            grantUriPermission("com.android.camera", photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(photoUri, "image/*");

        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);

        int size = list.size();
        if (size == 0) {

            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        } else {

            Toast.makeText(this, "용량이 큰 사진의 경우 시간이 오래 걸릴 수 있습니다.", Toast.LENGTH_SHORT).show();


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
                photoUri = FileProvider.getUriForFile(this,
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

            grantUriPermission(res.activityInfo.packageName, photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            startActivityForResult(i, app.REQUEST_IMAGE_CROP);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (requestCode) {
            case app.REQUEST_TAKE_PHOTO:
                MediaScannerConnection.scanFile(this, new String[]{photoUri.getPath()}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String path, Uri uri) {

                            }
                        });
                cropImage();
                break;
            case app.REQUEST_TAKE_ALBUM:
                if (data == null) {
                    Toast.makeText(this, "사진불러오기 실패", Toast.LENGTH_SHORT).show();
                    return;
                }
                photoUri = data.getData();
                cropImage();
                break;
            case app.REQUEST_IMAGE_CROP:
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

                    image_list.add(mImgFilePath);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageAdapter.setList(image_list);
                        }
                    });


//                    File img = new File(mImgFilePath);      // 전송 이미지 파일
////                    Log.d(MyUtil.TAG, "img: "+img.length());


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void regDogInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MultipartUtility mu = new MultipartUtility(NetUrls.DOMAIN, "UTF-8");

                    mu.addFormField("CONNECTCODE", "APP");
                    mu.addFormField("siteUrl", NetUrls.MEDIADOMAIN);
                    mu.addFormField("dbControl", "setDogInfoRegi");
                    mu.addFormField("_APP_MEM_IDX", UserPref.getIdx(DogInfoAct.this));
                    mu.addFormField("MEMCODE", UserPref.getIdx(DogInfoAct.this));
                    mu.addFormField("m_uniq", UserPref.getDeviceId(DogInfoAct.this));
                    mu.addFormField("d_kname", binding.etKname.getText().toString());
                    mu.addFormField("d_ename", binding.etKname.getText().toString());
                    mu.addFormField("d_breed", binding.spnBreed.getSelectedItem().toString());
                    mu.addFormField("d_gender", binding.spnGender.getSelectedItem().toString());
                    mu.addFormField("d_birth", binding.tvBirth.getText().toString());

                    for (int i = 1; i < image_list.size(); i++) {
                        File img = new File(image_list.get(i));

                        if(i == 1) {
                            mu.addFilePart("d_pimg", img);
                        } else {
                            mu.addFilePart("d_pimg" + i, img);
                        }
                    }

                    Log.d(MyUtil.TAG, "input idx: " + UserPref.getIdx(DogInfoAct.this));

                    String res = mu.finish();
                    Log.d(MyUtil.TAG, "result: " + res);
//                    {"result":"Y","message":"성공적으로 등록하였습니다.","url":"","MEMCODE":"359"}
                    if (MyUtil.isNull(res)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DogInfoAct.this, getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        try {
                            JSONObject jo = new JSONObject(res);

                            if (jo.getString("result").equalsIgnoreCase("Y")) {
                                UserPref.setDogIdx(DogInfoAct.this, jo.getString("MEMCODE"));
                                startActivity(new Intent(DogInfoAct.this, MainActivity.class));
                                finish();
                            } else {
                                final String msg = jo.getString("message");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(DogInfoAct.this, msg, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(DogInfoAct.this, getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                        }

                    }

                } catch (IOException e) {
                    e.printStackTrace();
//                    Toast.makeText(DogInfoAct.this, getString(R.string.net_errmsg), Toast.LENGTH_SHORT).show();
                }

            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;

            case R.id.ll_britharea:
                showDatePicker();
                break;
            case R.id.btn_complete:
                // 입력 체크
                if (binding.etKname.length() == 0) {
                    Toast.makeText(this, "이름을 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(binding.spnNameType.getSelectedItem().toString().equalsIgnoreCase("한글이름")) {
                    if (!MyUtil.checkKorean(binding.etKname.getText().toString())) {
                        Toast.makeText(this, "한글 이름 확인", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    if (!MyUtil.checkEnglish(binding.etKname.getText().toString())) {
                        Toast.makeText(this, "영어 이름 확인", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

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

                if(image_list.size() <= 1) {
                    Toast.makeText(this, "반려견 이미지를 확인해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                regDogInfo();

                break;
        }
    }
}
