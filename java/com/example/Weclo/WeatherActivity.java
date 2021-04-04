package com.example.Weclo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherActivity extends AppCompatActivity implements LocationListener, View.OnClickListener {
    private LocationManager locationManager;
    double latitude;
    double longitude;
    private DbOpenHelper mDbOpenHelper;
    private AsyncTask<Integer, String, Integer> mProgressDlg;

    private TextView latText, lonText, cityText;
    private TextView tCurrText, tMinText, tMaxText;
    private ImageView mainView;
    private ImageView topImg, outwearImg, bottomImg, shoesImg;
    private ImageView skyImg;

    private Button btn_closet;
    private Button btn_Add;
    private Button btn_EditStyle;

    String city;
    String county;
    String village;
    String winddir;
    String windspd;
    String sinceOntime;
    String type;
    String sky;
    String tempCurrent;
    String tempMax;
    String tempMin;
    String humidity;
    String lighting;
    String timeRelease;
    String alert;
    String storm;

    String result;

    private Bitmap styleImg;
    private Bitmap styleBitmap;
    private String styleImgPath;

    // 이미지 있는 루트 경로
    private String root = Environment.getExternalStorageDirectory().toString() + "/Weclo";

    //최종 선택한 옷 이미지 주소를 저장할 리스트
    public ArrayList<String> finalPathList = new ArrayList<>();
    public ArrayList<String> finalList = new ArrayList<>();
    private File imgFile;

    //해당 옷 정보 로드하여 저장할 배열
    private String[] clothesInfo = new String[4];
    private String clothesType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        initView();

        Intent intent = getIntent();
        finalList = intent.getStringArrayListExtra("finalList");
        finalPathList = intent.getStringArrayListExtra("finalListPath");

        //새로운 코디를 완료하기 전까지 이전 코디 저장하고 불러오기
        if(finalList==null || finalPathList==null){
            finalList = getStringArrayPref(getApplicationContext(), "finalListK");
            finalPathList = getStringArrayPref(getApplicationContext(), "finalPathListK");

        }

        //부위별 위치 지정하여 옷 이미지 뿌리기
        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open();
        mDbOpenHelper.create();
        setStyleView(finalPathList, finalList, mDbOpenHelper);
        mProgressDlg = new ProgressDlg(WeatherActivity.this).execute(40);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager != null) {
            requestLocation();
        }

    }

    /**재실행시에도 이전에 결정했던 코디를 유지하기 위한 함수**/
    @Override
    protected void onStop() {
        super.onStop();
        setStringArrayPref(getApplicationContext(), "finalListK", finalList);
        setStringArrayPref(getApplicationContext(), "finalPathListK", finalPathList);
    }

    //ArrayList를 SharedPreferences에 저장하기 위한 함수
    private void setStringArrayPref(Context context, String key, ArrayList<String> values) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray a = new JSONArray();
        for (int i = 0; i < values.size(); i++) {
            a.put(values.get(i));
        }
        if (!values.isEmpty()) {
            editor.putString(key, a.toString());
        } else {
            editor.putString(key, null);
        }
        editor.apply();
    }

    private ArrayList<String> getStringArrayPref(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(key, null);
        ArrayList<String> urls = new ArrayList<String>();
        if (json != null) {
            try {
                JSONArray a = new JSONArray(json);
                for (int i = 0; i < a.length(); i++) {
                    String url = a.optString(i);
                    urls.add(url);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }

    private void initView() {
        //뷰세팅
//        latText = (TextView) findViewById(R.id.latitude);
//        lonText = (TextView) findViewById(R.id.longitude);
        cityText = findViewById(R.id.city);
        tCurrText = findViewById(R.id.tempCurr);
        tMinText = findViewById(R.id.tempMin);
        tMaxText = findViewById(R.id.tempMax);
//        mainView = findViewById(R.id.view_mainStyle);

        topImg = findViewById(R.id.view_mainTop);
        outwearImg = findViewById(R.id.view_mainOutwear);
        bottomImg = findViewById(R.id.view_mainBottom);
        shoesImg = findViewById(R.id.view_mainShoes);
        skyImg = findViewById(R.id.img_sky);

        btn_closet = (Button) findViewById(R.id.btn_closet);
        btn_Add = findViewById(R.id.btn_add);
        btn_EditStyle = findViewById(R.id.btn_editStyle);

        btn_closet.setOnClickListener(this);
        btn_Add.setOnClickListener(this);
        btn_EditStyle.setOnClickListener(this);
    }

    //코디 화면 구성하기
    private void setStyleView(ArrayList<String> finalPathList, ArrayList<String> finalList, DbOpenHelper mDbOpenHelper){
        for(int i=0; i<finalPathList.size(); i++) {
            String path = root + finalPathList.get(i);
            imgFile = new File(root + finalPathList.get(i));
            if (imgFile.exists()) {
//                styleBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                styleBitmap = decodeSampledBitmapFromUri(path, 200, 200);;
                clothesType = mDbOpenHelper.getType(finalList.get(i));
                switch (clothesType) {
                    case "top":
                    case "overall":
                        topImg.setImageBitmap(styleBitmap);
                        break;
                    case "outwear":
                        outwearImg.setImageBitmap(styleBitmap);
                        break;
                    case "bottom":
                        bottomImg.setImageBitmap(styleBitmap);
                        break;
                    case "shoes":
                        shoesImg.setImageBitmap(styleBitmap);
                        break;
                }
            }
        }
    }

    // 불러온 코디 이미지 불러와 resize 한 비트맵 형성
    public Bitmap decodeSampledBitmapFromUri(String path, int reqWidth, int reqHeight) {
        Bitmap bm = null;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        bm = BitmapFactory.decodeFile(path, options);

        return bm;
    }

    // 이미지 원래 비율대로 resize 위한 계산
    public int calculateInSampleSize(

            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float)height / (float)reqHeight);
            } else {
                inSampleSize = Math.round((float)width / (float)reqWidth);
            }
        }

        return inSampleSize;
    }

    /**날씨 관련 화면 구성하기**/

    @Override
    public void onLocationChanged(Location location) {
        /*현재 위치에서 위도경도 값을 받아온뒤 우리는 지속해서 위도 경도를 읽어올것이 아니니
        날씨 api에 위도경도 값을 넘겨주고 위치 정보 모니터링을 제거한다.*/
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        //위도 경도 텍스트뷰에 보여주기
//        latText.setText(String.valueOf(latitude));
//        lonText.setText(String.valueOf(longitude));
        //날씨 가져오기 통신
        getWeather(latitude, longitude);
        //위치정보 모니터링 제거
        locationManager.removeUpdates(WeatherActivity.this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_closet:   //옷장으로
                if(mDbOpenHelper.ifExist()) {
                    Intent intent_closet = new Intent(getApplicationContext(),ClosetActivity.class);
                    intent_closet.putExtra("mainlat", latitude);
                    intent_closet.putExtra("mainlong", longitude);
                    startActivity(intent_closet);
                }
                else
                    Toast.makeText(this, "옷장에 옷이 없습니다. 먼저 새 옷을 추가해주세요!", Toast.LENGTH_SHORT).show();

                break;

            case R.id.btn_add:  //새옷 추가
                Intent intent_add = new Intent(getApplicationContext(), AddActivity.class);
                intent_add.putExtra("mainlat", latitude);
                intent_add.putExtra("mainlong", longitude);
                startActivity(intent_add);
                break;

            case R.id.btn_editStyle:    //코디화면으로
                if(finalList.size()>0) {
                    Intent intent_style = new Intent(getApplicationContext(),FinalstyleActivity.class);
                    intent_style.putExtra("editStyle", finalList);
                    intent_style.putExtra("editStylePath", finalPathList);
                    startActivity(intent_style);
                }
                else
                    Toast.makeText(this, "이전 코디가 없습니다. 먼저 옷장에서 옷을 선택해 주세요!", Toast.LENGTH_SHORT).show();
                break;

        }
    }

    public void requestLocation() {
        //사용자로 부터 위치정보 권한체크
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 1, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 1, this);
        }
    }

    private void getWeather(double latitude, double longitude) {
        Retrofit retrofit = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
                .baseUrl(WeatherhourlyInfo.ApiService.BASEURL)
                .build();
        WeatherhourlyInfo.ApiService apiService = retrofit.create(WeatherhourlyInfo.ApiService.class);
        Call<WeatherhourlyInfo> call = apiService.getJson(WeatherhourlyInfo.ApiService.APPKEY, 2, latitude, longitude);
        call.enqueue(new Callback<WeatherhourlyInfo>() {
            @Override
            public void onResponse(@NonNull Call<WeatherhourlyInfo> call, @NonNull Response<WeatherhourlyInfo> response) {
                if (response.isSuccessful()) {
                    //날씨데이터를 받아옴
                    WeatherhourlyInfo array = response.body();
                    if (array != null) {
                        //데이터가 null 이 아니라면 날씨 데이터를 텍스트뷰로 보여주기

                        city = array.getWeather().getHourly().get(0).getGrid().getCity();
                        county = array.getWeather().getHourly().get(0).getGrid().getCounty();
                        village = array.getWeather().getHourly().get(0).getGrid().getVillage();

                        winddir = array.getWeather().getHourly().get(0).getWind().getWdir();
                        windspd = array.getWeather().getHourly().get(0).getWind().getWspd();

                        sinceOntime = array.getWeather().getHourly().get(0).getPrecipitation().getSinceOntime();

                        sky = array.getWeather().getHourly().get(0).getSky().getName();

                        tempCurrent = array.getWeather().getHourly().get(0).getTemperature().getTc();
                        tempMax = array.getWeather().getHourly().get(0).getTemperature().getTmax();
                        tempMin = array.getWeather().getHourly().get(0).getTemperature().getTmin();

                        humidity = array.getWeather().getHourly().get(0).getHumidity();
                        lighting = array.getWeather().getHourly().get(0).getLightning();
                        timeRelease = array.getWeather().getHourly().get(0).getTimeRelease();

                        alert = array.getCommon().getAlertYn();
                        storm = array.getCommon().getStormYn();

                        String test = array.getResult().getMessage();
                        Log.e("json", test);

                        setSkyImg(sky);
                        cityText.setText(city);
                        tCurrText.setText(tempCurrent);
                        tMinText.setText(tempMin);
                        tMaxText.setText(tempMax);
                    }

                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherhourlyInfo> call, @NonNull Throwable t) {
            }
        });
    }

    //    # 하늘상태코드명
//    # - SKY_A01: 맑음
//    # - SKY_A02: 구름조금
//    # - SKY_A03: 구름많음
//    # - SKY_A04: 구름많고 비
//    # - SKY_A05: 구름많고 눈
//    # - SKY_A06: 구름많고 비 또는 눈
//    # - SKY_A07: 흐림
//    # - SKY_A08: 흐리고 비
//    # - SKY_A09: 흐리고 눈
//    # - SKY_A10:  흐리고 비 또는 눈
//    # - SKY_A11: 흐리고 낙뢰
//    # - SKY_A12: 뇌우, 비
//    # - SKY_A13: 뇌우, 눈
//    # - SKY_A14: 뇌우, 비 또는 눈
    private void setSkyImg(String sky){
        switch(sky){
            case "맑음":
                skyImg.setImageResource(R.drawable.sky_01);
                break;
            case "구름조금":
                skyImg.setImageResource(R.drawable.sky_0203);
                break;
            case "구름많음":
                skyImg.setImageResource(R.drawable.sky_0203);
                break;
            case "구름많고 비":
                skyImg.setImageResource(R.drawable.sky_04);
                break;
            case "구름많고 눈":
                skyImg.setImageResource(R.drawable.sky_0509);
                break;
            case "구름많고 비 또는 눈":
                skyImg.setImageResource(R.drawable.sky_06);
                break;
            case "흐림":
                skyImg.setImageResource(R.drawable.sky_07);
                break;
            case "흐리고 비":
                skyImg.setImageResource(R.drawable.sky_08);
                break;
            case "흐리고 눈":
                skyImg.setImageResource(R.drawable.sky_0509);
                break;
            case "흐리고 비 또는 눈":
                skyImg.setImageResource(R.drawable.sky_10);
                break;
            case "흐리고 낙뢰":
                skyImg.setImageResource(R.drawable.sky_11);
                break;
            case "뇌우, 비":
                skyImg.setImageResource(R.drawable.sky_12);
                break;
            case "뇌우, 눈":
                skyImg.setImageResource(R.drawable.sky_13);
                break;
            case "뇌우, 비 또는 눈":
                skyImg.setImageResource(R.drawable.sky_14);
                break;
        }
    }
}