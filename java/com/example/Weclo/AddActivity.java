package com.example.Weclo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.palette.graphics.Palette;

import com.github.gabrielbb.cutout.CutOut;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AddActivity extends AppCompatActivity implements View.OnClickListener {
    public ImageClassifier classifier;

    private FragmentClothesView frgView;
    private FragmentClothesText frgText;
    private FragmentColorBox frgCheckBox;
    FragmentManager manager;

    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;

    //배경제거 완료한 이미지 비트맵
    public Bitmap photo;
    public Uri imageUri;
    public int label=0;
    private Uri mImageCaptureUri;

    private Button mAddButton;
    private Button mSaveClothesButton;

    TextView text_Category;
    TextView text_Material;
    TextView text_Thickness;
    Spinner spinner_Category;
    Spinner spinner_Material;
    Spinner spinner_Thickness;

    String filename;
    String category;
    String type;
    String color1;
    String color2;
    String pattern;
    String material;
    String thickness;

    String[] material_data;
    String[] category_data;
    String[] thickness_data;
    String[] clothesLabels = {
            "longtop",
            "shorttop",
            "sleeveless",
            "pants",
            "shorts",
            "skirts",
            "onepiece",
            "jacket",
            "coat",
            "padding",
            "sneakers",
            "sandals",
            "boots"
    };

    ArrayAdapter<String> arrayAdapter_material;
    ArrayAdapter<String> arrayAdapter_category;

    private DbOpenHelper mDbOpenHelper;
    private double latitude;
    private double longitude;

    String fname = "clothes"+System.currentTimeMillis()+".png";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        Intent intent = getIntent();
        latitude = intent.getDoubleExtra("mainlat", 0);
        longitude = intent.getDoubleExtra("mainlong", 0);

        // 6.0 마쉬멜로우 이상일 경우에는 권한 체크 후 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d("권한", "권한 설정 완료");
            } else {
                Log.d("권한", "권한 설정 요청");
                ActivityCompat.requestPermissions(AddActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        //fragment 사용 위한 준비
        manager = getSupportFragmentManager();
        frgView = (FragmentClothesView)manager.findFragmentById(R.id.viewFragment);
        frgText = (FragmentClothesText)manager.findFragmentById(R.id.textFragment);
        frgCheckBox = (FragmentColorBox)manager.findFragmentById(R.id.checkboxFragment);
        FragmentTransaction transaction = manager.beginTransaction().hide(frgText);
        transaction.commit();
        getSupportFragmentManager().executePendingTransactions();

        text_Category = findViewById(R.id.text_category);
        spinner_Category = findViewById(R.id.spinner_category);
        text_Material = findViewById(R.id.text_material);
        spinner_Material = findViewById(R.id.spinner_material);
        text_Thickness = findViewById(R.id.text_thickness);
        spinner_Thickness = findViewById(R.id.spinner_thickness);

        category_data = getResources().getStringArray(R.array.category);
        arrayAdapter_category = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, category_data);
        spinner_Category.setAdapter(arrayAdapter_category);

        material_data = getResources().getStringArray(R.array.materials);
        arrayAdapter_material = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, material_data);
        spinner_Material.setAdapter(arrayAdapter_material);

        thickness_data = getResources().getStringArray(R.array.thickness);
        arrayAdapter_material = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, material_data);
        spinner_Material.setAdapter(arrayAdapter_material);

        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open();
        mDbOpenHelper.create();

        spinner_Category.setSelection(0);
        spinner_Material.setSelection(0);
        spinner_Thickness.setSelection(0);

        mAddButton = findViewById(R.id.btn_add);
        mSaveClothesButton = findViewById(R.id.toCloset);

        mAddButton.setOnClickListener(this);
        mSaveClothesButton.setOnClickListener(this);
    }

    private void getPhotoFromAlbum()
    {
        // 앨범 호출
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    private void getPhotoFromCamera()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, PICK_FROM_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        //배경제거 완료한 이미지 비트맵
//        Bitmap photo;
        //캐시로 저장할 파일 이름 - 옷분류 관련 정보가 들어있어야 함!!
        String filename;
//        Uri tempUri = null;
//        File finalFile = null;
        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (requestCode == CutOut.CUTOUT_ACTIVITY_REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    imageUri = CutOut.getUri(data);
                    try {
                        photo = MediaStore.Images.Media.getBitmap(getContentResolver(),imageUri);
                        //비트맵을 통해 옷의 주요 색 추출
                        Palette.Builder pb = Palette.from(photo);
                        Palette palette = pb.generate();

                        if(photo != null)
                        {
                            // 색 추출 결과
                            frgCheckBox.setColorBox(palette);
                            // 배경제거 완료한 옷 결과
                            frgView.setImageChange(photo);
                            // 옷 이미지 분류한 결과
                            frgText.setTextChange(photo);

                            setCategorySpinner(frgText.classifyLabel(photo));
                        }
                        else
                            Toast.makeText(this, "저장한 옷이 없습니다!", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case CutOut.CUTOUT_ACTIVITY_RESULT_ERROR_CODE:
                    Exception ex = CutOut.getError(data);
                    break;
                default:
                    System.out.print("User cancelled the CutOut screen");
            }
        }
        switch(requestCode) {
            case PICK_FROM_ALBUM: {
                // 이후의 처리가 카메라와 같으므로 일단  break없이 진행합니다.
                // 실제 코드에서는 좀더 합리적인 방법을 선택하시기 바랍니다.
                mImageCaptureUri = data.getData();
                if(Uri.EMPTY.equals(mImageCaptureUri))
                {
                    //기기의 뒤로가기 버튼과 같은 기능
                    onBackPressed();
                    break;
                }
                Log.i("NR", mImageCaptureUri.getPath().toString());
                CutOut.activity()
                        .src(mImageCaptureUri)
                        .bordered()
                        .noCrop()
                        .intro()
                        .start(this);

                break;
            }
            case PICK_FROM_CAMERA: {

                photo = (Bitmap) data.getExtras().get("data");
                // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
                Uri tempUri = getImageUri(getApplicationContext(), photo);

                mImageCaptureUri = tempUri;

                CutOut.activity()
                        .src(mImageCaptureUri)
                        .bordered()
                        .noCrop()
                        .intro()
                        .start(this);

                break;
            }
        }
    }

    //카메라로 찍은 이미지 가져오기
    public Uri getImageUri(Context inContext, Bitmap inImage)
    {
        Bitmap OutImage = Bitmap.createScaledBitmap(inImage, 400, 600,true);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), OutImage, "Title", null);
        return Uri.parse(path);
    }

    //Weclo 폴더에 이미지 저장
    public File storeBitmap(Bitmap photo) {
        String root = Environment.getExternalStorageDirectory().toString() + "/Weclo";
        File myDir = new File(root);
        File mySubDir[] = new File[clothesLabels.length];
        //Generate Weclo directory
        if(!myDir.exists())     //check if file already exists
        {
            myDir.mkdirs();     //if not, create it
        }

        //Generate sub directories as clothes labels in Weclo directory
        for(int i=0; i<clothesLabels.length; i++) {
            mySubDir[i] = new File(root + "/" + clothesLabels[i]);
            if(!mySubDir[i].exists())     //check if file already exists
            {
                mySubDir[i].mkdirs();     //if not, create it
            }
        }

        for(int j=0; j<clothesLabels.length; j++) {
            if(getCategory(category).contains(clothesLabels[j])) {
                label = j;
            }
        }

        File file = new File(mySubDir[label], fname);
//        File file = new File(myDir, fname);
//        Log.i(TAG, "" + file);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            photo.compress(Bitmap.CompressFormat.PNG, 20, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    //이미지 갤러리에 저장
    public void saveImgToGallery(Bitmap photo, Uri imageUri){
        //이미지 Weclo 폴더에 저장
        File file = storeBitmap(photo);
        //이미지 갤러리에 로드
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file));
        sendBroadcast(intent);
    }

    //옷종류 결과값에 따라 스피너 값 설정
    private void setCategorySpinner(String classify) {
        for(int i=0; i<clothesLabels.length; i++) {
            if(classify.equals(clothesLabels[i])) {
                spinner_Category.setSelection(i);
            }
        }
    }

    //옷 타입 분류
    public String getClothesType(){
        category = spinner_Category.getSelectedItem().toString();
        switch (category) {
            case "긴팔":
            case "반팔":
            case "민소매":
                type = "top";
                break;
            case "긴바지":
            case "반바지":
            case "치마":
                type = "bottom";
                break;
            case "원피스":
                type = "overall";
                break;
            case "자켓":
            case "코트":
            case "패딩":
                type = "outwear";
                break;
            case "운동화":
            case "샌들":
            case "부츠":
                type = "shoes";
                break;
        }
        return type;
    }

    private String getCategory(String category){
        String result="";
        switch(category){
            case "긴팔":
                result = "longtop";
                break;
            case "반팔":
                result = "shorttop";
                break;
            case "민소매":
                result = "sleeveless";
                break;
            case "긴바지":
                result = "pants";
                break;
            case "반바지":
                result = "shorts";
                break;
            case "치마":
                result = "skirts";
                break;
            case "원피스":
                result = "onepiece";
                break;
            case "자켓":
                result = "jacket";
                break;
            case "코트":
                result = "coat";
                break;
            case "패딩":
                result = "padding";
                break;
            case "운동화":
                result = "sneakers";
                break;
            case "부츠":
                result = "boots";
                break;
            case "샌들":
                result = "sandals";
                break;
        }
        return result;
    }

    private int getLabel(String category){
        int result=0;
        for(int i=0;i<clothesLabels.length; i++){
            if(category.equals(clothesLabels[i]))
                result = i;
        }
        return result;
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.btn_add:
                DialogInterface.OnClickListener cameraListener = (dialog, which) -> getPhotoFromCamera();

                DialogInterface.OnClickListener albumListener = (dialog, which) -> getPhotoFromAlbum();

                DialogInterface.OnClickListener cancelListener = (dialog, which) -> dialog.dismiss();

                new AlertDialog.Builder(this)
                        .setTitle("업로드할 이미지 선택")
                        .setPositiveButton("사진촬영", cameraListener)
                        .setNeutralButton("앨범선택", albumListener)
                        .setNegativeButton("취소", cancelListener)
                        .show();
                break;
            case R.id.toCloset:
                if(photo != null){
                    category = spinner_Category.getSelectedItem().toString();
                    label = getLabel(getCategory(category));
                    saveImgToGallery(photo, imageUri);
                    Toast.makeText(getApplicationContext(),"갤러리에 저장되었습니다.",Toast.LENGTH_SHORT).show();
                    filename = fname;
                    type = getClothesType();
                    frgCheckBox.getSelectedColor();
                    color1 = frgCheckBox.getColor1();
                    color2 = frgCheckBox.getColor2();
                    pattern = frgCheckBox.getPattern();
                    material = spinner_Material.getSelectedItem().toString();
                    thickness = spinner_Thickness.getSelectedItem().toString();
                    mDbOpenHelper.insertColumn( filename,category, type, color1, color2, pattern, material, thickness);
                    Intent closetIntent = new Intent(getApplicationContext(), ClosetActivity.class);
                    closetIntent.putExtra("label", label);
                    closetIntent.putExtra("mainlat", latitude);
                    closetIntent.putExtra("mainlong", longitude);
                    startActivity(closetIntent);
                    break;
                }
                else
                    Toast.makeText(getApplicationContext(),"옷을 먼저 불러와야 합니다!",Toast.LENGTH_SHORT).show();
        }
    }
}
