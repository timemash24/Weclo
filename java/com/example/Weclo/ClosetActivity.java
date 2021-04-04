package com.example.Weclo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//옷장화면 액티비티
public class ClosetActivity extends AppCompatActivity implements View.OnClickListener {
    private GridView gridview;

    private RadioGroup cRadioTime;
    private RadioButton[] cRadioArr = new RadioButton[3];
    private RadioButton cRadioChecked;

    private Button cButtonAuto;
    private Button cButtonCombine;
    private Button cButtonRetry;
    private Button cButtonMain;

    private ImageView cImgSky;
    private ImageView[] cSelected = new ImageView[4];

    private TextView cTextCity;
    private TextView cTextTemp;
    private TextView cTextProb;
    private TextView cTextType;

    private DbOpenHelper mdbOpenHelper;

    List<String> clothesList;
    List<String> pathList = new ArrayList<>(); //불러온 모든 이미지의 경로
    DisplayMetrics mMetrics;

    // 이미지 있는 루트 경로
    private String root = Environment.getExternalStorageDirectory().toString() + "/Weclo";
    private File imgFile;

    private int label;
    public int MAX_SELECT = 4;
    private int time_code;

    // 날씨 정보 저장용
    public String city;
    public String[] precipitation_type = new String[4];
    public String[] precipitation_prob = new String[4];
    public String[] sky = new String[4];
    public String[] temperature = new String[4];
    public String[] humidity = new String[4];
    private int selectedIndex=0;
    private double latitude;
    private double longitude;

    //곧 사라질...
    private String[] clothesLabels = {
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

    String clothesClass;
    private ArrayList<String> selectedList = new ArrayList<>();  //선택한 옷 파일 이름 목록
    private ArrayList<String> selectedPathList = new ArrayList<>();  //선택한 옷 경로 목록

    //자동추천 관련
    private ArrayList<String> autoList = new ArrayList<>();  //자동추천으로 선택된 옷 파일 이름 목록
    private ArrayList<String> autoPathList = new ArrayList<>();  //자동추천으로 선택된 옷 경로 목록

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_closet);
        mdbOpenHelper = new DbOpenHelper(this);
        mdbOpenHelper.open();

        Intent intent = getIntent();
        latitude = intent.getDoubleExtra("mainlat", 0);
        longitude = intent.getDoubleExtra("mainlong", 0);

        initView(mdbOpenHelper, latitude, longitude);

        label = intent.getIntExtra("label",0);
        loadActivity(label);    // show saved clothing's category as main view
    }

    private void initView(DbOpenHelper mdb, double latitude, double longitude) {
        //라디오버튼
        cRadioTime = findViewById(R.id.radiogroup_time);
        cRadioArr[0] = findViewById(R.id.radio_4hour);
        cRadioArr[1] = findViewById(R.id.radio_7hour);
        cRadioArr[2] = findViewById(R.id.radio_10hour);
        cRadioTime.check(findViewById(R.id.radio_4hour).getId());


        //날씨정보
        cImgSky = findViewById(R.id.img_cSky);
        cTextCity = findViewById(R.id.text_cCity);
        cTextTemp = findViewById(R.id.text_cTemp);
        cTextProb = findViewById(R.id.text_cProb);
        cTextType = findViewById(R.id.text_cType);

        //옷종류 버튼
        Button cButtons[] = new Button[13];

        cButtons[0] = findViewById(R.id.btn_longtop);
        cButtons[1] = findViewById(R.id.btn_shorttop);
        cButtons[2] = findViewById(R.id.btn_sleeveless);
        cButtons[3] = findViewById(R.id.btn_pants);
        cButtons[4] = findViewById(R.id.btn_shorts);
        cButtons[5] = findViewById(R.id.btn_skirts);
        cButtons[6] = findViewById(R.id.btn_onepiece);
        cButtons[7] = findViewById(R.id.btn_jacket);
        cButtons[8] = findViewById(R.id.btn_coat);
        cButtons[9] = findViewById(R.id.btn_padding);
        cButtons[10] = findViewById(R.id.btn_sneakers);
        cButtons[11] = findViewById(R.id.btn_sandals);
        cButtons[12] = findViewById(R.id.btn_boots);

        //선택한 옷 목록
//        cTextSelected = findViewById(R.id.text_selected);
        cSelected[0] = findViewById(R.id.img_selectA);
        cSelected[1] = findViewById(R.id.img_selectB);
        cSelected[2] = findViewById(R.id.img_selectC);
        cSelected[3] = findViewById(R.id.img_selectD);


        //메뉴 버튼
        cButtonAuto = findViewById(R.id.btn_random);
        cButtonCombine = findViewById(R.id.btn_combine);
        cButtonRetry = findViewById(R.id.btn_retry);
        cButtonMain = findViewById(R.id.btn_main);

        //날씨 정보 불러오기
        getWeather(mdb, latitude, longitude);

        //버튼 리스너
        for(int i=0; i<cRadioArr.length; i++){
            final int index = i;
            cRadioArr[index].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setSkyImg(sky[index]);
                    cTextCity.setText(city);
                    cTextTemp.setText(temperature[index]);
                    cTextProb.setText(precipitation_prob[index]);
                    cTextType.setText(getPreTypeName(precipitation_type[index]));
                }
            });
        }

        for(int i=0; i<cButtons.length; i++) {
            cButtons[i].setOnClickListener(this);
        }
        cButtonAuto.setOnClickListener(this);
        cButtonCombine.setOnClickListener(this);
        cButtonRetry.setOnClickListener(this);
        cButtonMain.setOnClickListener(this);
    }

    //선택된 시간에 따라 하늘 상태 아이콘 보여주기
    private void setSkyImg(String sky) {
        switch(sky){
            case "맑음":
                cImgSky.setImageResource(R.drawable.sky_01);
                break;
            case "구름조금":
                cImgSky.setImageResource(R.drawable.sky_0203);
                break;
            case "구름많음":
                cImgSky.setImageResource(R.drawable.sky_0203);
                break;
            case "구름많고 비":
                cImgSky.setImageResource(R.drawable.sky_04);
                break;
            case "구름많고 눈":
                cImgSky.setImageResource(R.drawable.sky_0509);
                break;
            case "구름많고 비 또는 눈":
                cImgSky.setImageResource(R.drawable.sky_06);
                break;
            case "흐림":
                cImgSky.setImageResource(R.drawable.sky_07);
                break;
            case "흐리고 비":
                cImgSky.setImageResource(R.drawable.sky_08);
                break;
            case "흐리고 눈":
                cImgSky.setImageResource(R.drawable.sky_0509);
                break;
            case "흐리고 비 또는 눈":
                cImgSky.setImageResource(R.drawable.sky_10);
                break;
            case "흐리고 낙뢰":
                cImgSky.setImageResource(R.drawable.sky_11);
                break;
            case "뇌우, 비":
                cImgSky.setImageResource(R.drawable.sky_12);
                break;
            case "뇌우, 눈":
                cImgSky.setImageResource(R.drawable.sky_13);
                break;
            case "뇌우, 비 또는 눈":
                cImgSky.setImageResource(R.drawable.sky_14);
                break;
        }
    }

    //그리드뷰 새로 로드
    private void loadActivity(int labelIndex) {
        clothesClass = clothesLabels[labelIndex];
        gridview = (GridView)findViewById(R.id.gridView);
        gridview.setAdapter(new ImageAdapter(this));
        gridview.setOnItemClickListener(gridviewOnItemClickListener);

        mMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }


    private GridView.OnItemClickListener gridviewOnItemClickListener
            = new GridView.OnItemClickListener() {

        //옷 이미지 선택시 뒷 배경 색 바뀌고 아래에 이미지 이름 보여주기
        //두번 선택시 다시 원래 배경색으로 바꾸고 싶었는데 안되는듯,,
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                long arg3) {
            String search = arg0.getAdapter().getItem(arg2).toString();
            setSelectedList(search);
            setImgNull();
            setSelectedImg(selectedPathList);
        }
    };

    //이미지뷰에 뭔가 있을때, null로 비우기
    public void setImgNull(){
        for(int i=0; i<4; i++){
            if(cSelected[i]!=null){
                cSelected[i].setImageBitmap(null);
            }
        }
    }

    // 선택한 옷 목록 만들기
    private void setSelectedList(String selected) {
        int duplicate = 0;
        if(selectedList.size()>0 && selectedList.size()<=MAX_SELECT) {
            for(int i=0; i<selectedList.size(); i++) {
                String type1 = mdbOpenHelper.LoadCloset(selected)[2];
                String type2 = mdbOpenHelper.LoadCloset(selectedList.get(i))[2];
                Log.e("type1", type1);
                Log.e("type2", type2);
                String file = selectedList.get(i);
                if(selected.equals(selectedList.get(i))) {
                    duplicate++;
                    selectedList.remove(file);
                    selectedPathList.remove(i);
                    Log.i("duplicate",Integer.toString(duplicate));
                }
                else if (type1.equals(type2)) {
                    duplicate++;
                    selectedList.remove(file);
                    selectedPathList.remove(i);
                    selectedList.add(selected);
                    selectedPathList.add("/"+clothesClass + "/"+selected);
                }
                else if(type1.equals("overall") && type2.equals("top")) {  //현재 고른 옷이 원피스일 경우
                    duplicate++;
                    selectedList.remove(file);
                    selectedPathList.remove(i);
                    selectedList.add(selected);
                    selectedPathList.add("/"+clothesClass + "/"+selected);
                }
                else if(type1.equals("top") && type2.equals("overall")) {  //이전에 원피스를 고른 경우
                    duplicate++;
                    selectedList.remove(file);
                    selectedPathList.remove(i);
                    selectedList.add(selected);
                    selectedPathList.add("/"+clothesClass + "/"+selected);
                }
            }
            if(duplicate==0){
                selectedList.add(selected);
                selectedPathList.add("/"+clothesClass + "/"+selected);
            }
            else return;
        }
        //add for the first time
        else if(selectedList.size()==0) {
            selectedList.add(selected);
            selectedPathList.add("/"+clothesClass + "/"+selected);
        }
    }

    // 선택한 옷 이미지 설정
    private void setSelectedImg(List<String> selectedPathList) {
        for(int i=0; i<selectedPathList.size(); i++) {
            imgFile = new File(root + selectedPathList.get(i));
            if (imgFile.exists()) {
                Bitmap myBitmap = decodeSampledBitmapFromUri(imgFile.getAbsolutePath(), 200, 200);
                String fname = imgFile.getName();
                String ftype = mdbOpenHelper.LoadCloset(fname)[2];
                switch (ftype){
                    case "top":
                    case "overall":
                        cSelected[0].setImageBitmap(myBitmap);
                        break;
                    case "bottom":
                        cSelected[1].setImageBitmap(myBitmap);
                        break;
                    case "outwear":
                        cSelected[2].setImageBitmap(myBitmap);
                        break;
                    case "shoes":
                        cSelected[3].setImageBitmap(myBitmap);
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


    //선택한 옷 이미지 모두 해제
    private void clearSelectedImg(){
        for(int i =0; i<cSelected.length; i++){
            cSelected[i].setImageBitmap(null);
        }
    }

    //분류별 옷 버튼 눌렀을 때 이미지 로드하기
    public ArrayList<String> loadImages() {
        String path = root + "/" + clothesClass;
        File directory = new File(path);
        File[] files = directory.listFiles();
        ArrayList<String> filesNameList = new ArrayList<>();
        for (int i=0; i< files.length; i++) {
            filesNameList.add(files[i].getName());
            pathList.add("/" + clothesClass + "/" + files[i].getName());

        }
        return filesNameList;

    }

    // Adapter for GridView
    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        Bitmap bm;

        public ImageAdapter(Context c) {
            mContext = c;
            clothesList = loadImages();
        }

        public int getCount() {
            return clothesList.size();
//            return mThumbIds.length;
        }

        public Object getItem(int position) {
            return clothesList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {

            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
            } else {
                imageView = (ImageView) convertView;
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 6;
//            bm = BitmapFactory.decodeFile(root + "/" + clothesClass
//                    + "/" + clothesList.get(position), options);
            bm = decodeSampledBitmapFromUri(root + "/" + clothesClass
                    + "/" + clothesList.get(position), 200, 200);
            Bitmap mThumbnail = ThumbnailUtils.extractThumbnail(bm, 400, 400);
            imageView.setPadding(1, 1, 1, 1);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setLayoutParams(new GridView.LayoutParams(400, 400));
            imageView.setImageBitmap(mThumbnail);

            return imageView;
        }

    }

    //체크된 라디오버튼 구해서 해당 index selectedIndex에 저장 - intent로 finalStyle에 넘겨야함!
    private void getCheckedTime() {
        cRadioChecked = (RadioButton)findViewById(cRadioTime.getCheckedRadioButtonId());
        String checkedValue = cRadioChecked.getText().toString();
        Log.e("checkedvalue", checkedValue);
        switch (checkedValue){
            case "4시간 이내":
                selectedIndex = 0;
                break;
            case "5~7시간 후":
                selectedIndex = 1;
                break;
            case "8~10시간 후":
                selectedIndex = 2;
                break;
        }
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.btn_longtop:
                loadActivity(0);
                break;
            case R.id.btn_shorttop:
                loadActivity(1);
                break;

            case R.id.btn_sleeveless:
                loadActivity(2);
                break;

            case R.id.btn_pants:
                loadActivity(3);
                break;

            case R.id.btn_shorts:
                loadActivity(4);
                break;

            case R.id.btn_skirts:
                loadActivity(5);
                break;

            case R.id.btn_onepiece:
                loadActivity(6);
                break;

            case R.id.btn_jacket:
                loadActivity(7);
                break;

            case R.id.btn_coat:
                loadActivity(8);
                break;

            case R.id.btn_padding:
                loadActivity(9);
                break;

            case R.id.btn_sneakers:
                loadActivity(10);
                break;

            case R.id.btn_sandals:
                loadActivity(11);
                break;

            case R.id.btn_boots:
                loadActivity(12);
                break;

            case R.id.btn_random:
                selectedList.clear();
                selectedPathList.clear();
                autoList.clear();
                autoPathList.clear();
                getRandomStyle(mdbOpenHelper, autoList, autoPathList, temperature);
                copyArrayList(selectedList, selectedPathList, autoList, autoPathList);
                setSelectedImg(selectedPathList);
                Toast.makeText(this, "추천이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                break;

            case R.id.btn_combine:
                Intent finalStyleIntent = new Intent(getApplicationContext(), FinalstyleActivity.class);
                finalStyleIntent.putStringArrayListExtra("selected", (ArrayList<String>)selectedList);
                finalStyleIntent.putStringArrayListExtra("selectedPath", (ArrayList<String>)selectedPathList);
                Log.e("selected_size", Integer.toString(selectedList.size()));
                Log.e("selected_sizepath", Integer.toString(selectedPathList.size()));
                putWeatherInfo(finalStyleIntent);
                startActivity(finalStyleIntent);
                break;
            case R.id.btn_retry:
                //모든 옷 선택해제
                selectedList.clear();
                selectedPathList.clear();
                autoList.clear();
                autoPathList.clear();
                clearSelectedImg();
                Toast.makeText(this, "모든 선택을 취소하였습니다!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_main:
                Intent mainIntent = new Intent(getApplicationContext(), WeatherActivity.class);
                startActivity(mainIntent);
                break;

        }
    }

    private void copyArrayList(ArrayList<String> selectedList, ArrayList<String> selectedPathList, ArrayList<String> autoList, ArrayList<String> autoPathList){
        for(int i=0; i<autoList.size(); i++){
            selectedList.add(autoList.get(i));
            selectedPathList.add(autoPathList.get(i));
//            Log.e("copyfromauto", selectedList.get(i));
            Log.e("copyfromautopath", selectedPathList.get(i));
        }
    }

    private void getRandomStyle(DbOpenHelper mdbOpenHelper, ArrayList<String> autoList, ArrayList<String> autoPathList, String[] temperature){
        getCheckedTime();
        RandomSuggest rSuggest = new RandomSuggest();
        rSuggest.getRandomList(getApplicationContext(), mdbOpenHelper, autoList, autoPathList, temperature[selectedIndex]);
    }

    //단기예보 정보 내보내기
    private void putWeatherInfo(Intent finalStyleIntent){
        getCheckedTime();
        finalStyleIntent.putExtra("preProb", precipitation_prob);
        finalStyleIntent.putExtra("preType", precipitation_type);
        finalStyleIntent.putExtra("sky", sky);
        finalStyleIntent.putExtra("temperature", temperature);
        finalStyleIntent.putExtra("humidity", humidity);
        finalStyleIntent.putExtra("selectedIndex", selectedIndex);
    }

    public static String getPreTypeName(String preType){
        String result="";
        switch (preType){
            case "0":
                result = "강수확률";
                break;
            case "1":
                result = "비";
                break;
            case "2":
                result = "비/눈";
                break;
            case "3":
                result = "눈";
                break;
        }
        return result;
    }

    private void getWeather(DbOpenHelper mdb, double latitude, double longitude) {
//        JsonParser parser = new JsonParser();
//        weatherInfo = new WeatherhourlyInfo();
        Retrofit retrofit = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
                .baseUrl(WeatherdailyInfo.DailyApiService.BASEURL)
                .build();
        WeatherdailyInfo.DailyApiService apiService = retrofit.create(WeatherdailyInfo.DailyApiService.class);
        Call<WeatherdailyInfo> call = apiService.getJson(WeatherdailyInfo.DailyApiService.APPKEY, 2, latitude, longitude);
        call.enqueue(new Callback<WeatherdailyInfo>() {
            @Override
            public void onResponse(@NonNull Call<WeatherdailyInfo> call, @NonNull Response<WeatherdailyInfo> response) {
                if (response.isSuccessful()) {
                    //날씨데이터를 받아옴
                    WeatherdailyInfo array = response.body();
                    Log.e("json", array.toString());
                    if (array != null) {
                        //데이터가 null 이 아니라면 날씨 데이터를 텍스트뷰로 보여주기
                        city = array.getWeather().getForecast3days().get(0).getGrid().getCity();

                        precipitation_prob[0] = array.getWeather().getForecast3days().get(0).getFcst3hour().getPrecipitation().getProb4hour();
                        precipitation_prob[1] = array.getWeather().getForecast3days().get(0).getFcst3hour().getPrecipitation().getProb7hour();
                        precipitation_prob[2] = array.getWeather().getForecast3days().get(0).getFcst3hour().getPrecipitation().getProb10hour();
                        precipitation_prob[3] = array.getWeather().getForecast3days().get(0).getFcst3hour().getPrecipitation().getProb13hour();

                        precipitation_type[0] = array.getWeather().getForecast3days().get(0).getFcst3hour().getPrecipitation().getType4hour();
                        precipitation_type[1] = array.getWeather().getForecast3days().get(0).getFcst3hour().getPrecipitation().getType7hour();
                        precipitation_type[2] = array.getWeather().getForecast3days().get(0).getFcst3hour().getPrecipitation().getType10hour();
                        precipitation_type[3] = array.getWeather().getForecast3days().get(0).getFcst3hour().getPrecipitation().getType13hour();

                        sky[0] = array.getWeather().getForecast3days().get(0).getFcst3hour().getSky().getName4hour();
                        sky[1] = array.getWeather().getForecast3days().get(0).getFcst3hour().getSky().getName7hour();
                        sky[2] = array.getWeather().getForecast3days().get(0).getFcst3hour().getSky().getName10hour();
                        sky[3] = array.getWeather().getForecast3days().get(0).getFcst3hour().getSky().getName13hour();

                        temperature[0] = array.getWeather().getForecast3days().get(0).getFcst3hour().getTemperature().getTemp4hour();
                        temperature[1] = array.getWeather().getForecast3days().get(0).getFcst3hour().getTemperature().getTemp7hour();
                        temperature[2] = array.getWeather().getForecast3days().get(0).getFcst3hour().getTemperature().getTemp10hour();
                        temperature[3] = array.getWeather().getForecast3days().get(0).getFcst3hour().getTemperature().getTemp13hour();

                        humidity[0] = array.getWeather().getForecast3days().get(0).getFcst3hour().getHumidity().getRh4hour();
                        humidity[1] = array.getWeather().getForecast3days().get(0).getFcst3hour().getHumidity().getRh7hour();
                        humidity[2] = array.getWeather().getForecast3days().get(0).getFcst3hour().getHumidity().getRh10hour();
                        humidity[3] = array.getWeather().getForecast3days().get(0).getFcst3hour().getHumidity().getRh13hour();

                        String test = precipitation_type[0];
                        Log.e("json", test);

                        //시간 선택 전 초기 상태
                        setSkyImg(sky[0]);
                        cTextCity.setText(city);
                        cTextTemp.setText(temperature[0]);
                        cTextProb.setText(precipitation_prob[0]);
                        cTextType.setText(getPreTypeName(precipitation_type[0]));
                    }

                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherdailyInfo> call, @NonNull Throwable t) {
            }
        });
    }


}