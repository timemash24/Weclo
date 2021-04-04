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
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FinalstyleActivity extends AppCompatActivity implements View.OnClickListener, LocationListener {
    LocationManager locationManager;
    private DbOpenHelper mdb;
    private AsyncTask<Integer, String, Integer> fProgressDlg;
    private AsyncTask<Integer, String, Integer> fProgressDlgA;

    long nowIndex;
    double latitude;
    double longitude;

    private LinearLayout fStyleView;
    private GridView gridview;
    private RelativeLayout capture_target_Layout;

    private ImageView fTopView;
    private ImageView fBottomView;
    private ImageView fOutwearView;
    private ImageView fShoesView;

    public TextView comment_top;
    private TextView comment_outwear;
    private TextView comment_bottom;
    private TextView comment_shoes;
    private TextView comment_colors;

    private Button fSaveButton;
    private Button fMainButton;
    private Button fCancelButton;
    private Button fClosetButton;

    // 날씨 정보 저장용
    //daily
    private String[] precipitation_type = new String[4];
    private String[] precipitation_prob = new String[4];
    private String[] sky = new String[4];
    private String[] temperature = new String[4];
    private String[] humidity = new String[4];
    private int selectedIndex;
    //hourly
    String windspd;
    String type;
    String tempMax;
    String tempMin;

    //style table 저장을 위한 인스턴스
    String stylename;
    String top;
    String bottom;
    String overall;
    String outwear;
    String shoes;

    // 이미지 있는 루트 경로
    private String root = Environment.getExternalStorageDirectory().toString() + "/Weclo";

    //캡쳐한 이미지 비트맵
    private Bitmap bitmap_capture;
    private Bitmap styleBitmap;

    //선택한 옷 이미지 주소를 저장할 리스트
    private ArrayList<String> selectedPathList = new ArrayList<>();
    private ArrayList<String> selectedList = new ArrayList<>();
    private ArrayList<String> editPathList = new ArrayList<>();
    private ArrayList<String> editList = new ArrayList<>();

    List<String> styleList;
    List<String> pathList = new ArrayList<>(); //불러온 이미지 경로
    DisplayMetrics mMetrics;

    //선택한 옷 종류를 저장할 배열
    private String viewSelect= "";
//    private String[][] selectedArray;
    private File imgFile;
//    private String finalStylePath;

    public String[][] clothesLabels = new String[13][2];
    String[] commentArray = new String[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finalstyle);

        Intent intent = getIntent();

        editList = intent.getStringArrayListExtra("editStyle");
        editPathList = intent.getStringArrayListExtra("editStylePath");
        if(editList!=null) {    //main에서 전환한 경우
            selectedList = editList;
            selectedPathList = editPathList;
            precipitation_prob = getStringArrayPref(getApplicationContext(), "preProbK");
            precipitation_type = getStringArrayPref(getApplicationContext(), "preTypeK");
            sky = getStringArrayPref(getApplicationContext(), "skyK");
            temperature = getStringArrayPref(getApplicationContext(), "temperatureK");
            humidity = getStringArrayPref(getApplicationContext(), "humidityK");
            selectedIndex = getIntPref(getApplicationContext(), "selectedIndexK");
        }
        else {  //closet에서 전환한 경우
            selectedList = intent.getStringArrayListExtra("selected");
            selectedPathList = intent.getStringArrayListExtra("selectedPath");
            precipitation_prob = intent.getStringArrayExtra("preProb");
            precipitation_type = intent.getStringArrayExtra("preType");
            sky = intent.getStringArrayExtra("sky");
            temperature = intent.getStringArrayExtra("temperature");
            humidity = intent.getStringArrayExtra("humidity");
            selectedIndex = intent.getIntExtra("selectedIndex", 0);
        }

        //새로운 코디를 완료하기 전까지 이전 코디 저장하고 불러오기
        if(selectedList==null || selectedPathList==null){
            selectedList = getStringArrayListPref(getApplicationContext(), "selectedListK");
            selectedPathList = getStringArrayListPref(getApplicationContext(), "selectedPathListK");
            precipitation_prob = getStringArrayPref(getApplicationContext(), "preProbK");
            precipitation_type = getStringArrayPref(getApplicationContext(), "preTypeK");
            sky = getStringArrayPref(getApplicationContext(), "skyK");
            temperature = getStringArrayPref(getApplicationContext(), "temperatureK");
            humidity = getStringArrayPref(getApplicationContext(), "humidityK");
            selectedIndex = getIntPref(getApplicationContext(), "selectedIndexK");
        }
        Log.i("selectedListSize", Integer.toString(selectedList.size()));

        Log.e("selectedIndex", Integer.toString(selectedIndex));
        Log.e("temperature", temperature[selectedIndex]);

        //DB불러오기
        mdb = new DbOpenHelper(this);
        mdb.open();
        mdb.create();

        initView();

        setImageViews(selectedPathList);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        loadGridView();

        fProgressDlg = new ProgressDlg(this).execute(60);

        //추천 멘트 설정
        if(selectedList.size()>=2) {
            if (locationManager != null) {
                requestLocation();
            }
        }
        else
            Toast.makeText(getApplicationContext(), "옷을 2개 이상 선택해야 추천 멘트가 보입니다!", Toast.LENGTH_SHORT).show();

    }

    private void initView(){
        capture_target_Layout = (RelativeLayout) findViewById(R.id.capture_target_Layout); //캡쳐할 영역의 레이아웃

        fTopView = (ImageView) findViewById(R.id.top);
        fBottomView = (ImageView) findViewById(R.id.bottom);
        fOutwearView = (ImageView) findViewById(R.id.outwear);
        fShoesView = (ImageView) findViewById(R.id.shoes);

        comment_top = (TextView) findViewById(R.id.comment_temp);
        comment_outwear = (TextView) findViewById(R.id.comment_tempLater);
        comment_bottom = (TextView) findViewById(R.id.comment_outwear);
        comment_shoes = (TextView) findViewById(R.id.comment_tempDiff);
        comment_colors = (TextView) findViewById(R.id.comment_colors);

        fSaveButton = (Button) findViewById(R.id.savestyle_button);
        fMainButton = (Button) findViewById(R.id.complete_button);
        fCancelButton = (Button) findViewById(R.id.cancel_button);
        fClosetButton = (Button) findViewById(R.id.closet_button);
        fSaveButton.setOnClickListener(this);
        fMainButton.setOnClickListener(this);
        fCancelButton.setOnClickListener(this);
        fClosetButton.setOnClickListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        setStringArrayListPref(getApplicationContext(), "selectedListK", selectedList);
        setStringArrayListPref(getApplicationContext(), "selectedPathListK", selectedPathList);
        setStringArrayPref(getApplicationContext(), "preProbK", precipitation_prob);
        setStringArrayPref(getApplicationContext(), "preTypeK", precipitation_type);
        setStringArrayPref(getApplicationContext(), "temperatureK", temperature);
        setStringArrayPref(getApplicationContext(), "humidityK", humidity);
        setStringArrayPref(getApplicationContext(), "skyK", sky);
        setIntPref(getApplicationContext(), "selectedIndexK", selectedIndex);
    }
    //String을 SharedPreferences에 저장하기 위한 함수
    private void setIntPref(Context context, String key, int value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    //Array를 SharedPreferences에 저장하기 위한 함수
    private void setStringArrayPref(Context context, String key, String[] values) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray a = new JSONArray();
        for (int i = 0; i < values.length; i++) {
            a.put(values[i]);
        }
        if (values!=null) {
            editor.putString(key, a.toString());
        } else {
            editor.putString(key, null);
        }
        editor.apply();
    }

    //ArrayList를 SharedPreferences에 저장하기 위한 함수
    private void setStringArrayListPref(Context context, String key, ArrayList<String> values) {
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

    private int getIntPref(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int url = prefs.getInt(key, 0);
        return url;
    }

    private String[] getStringArrayPref(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(key, null);
        String[] urls = new String[4];
        if (json != null) {
            try {
                JSONArray a = new JSONArray(json);
                for (int i = 0; i < a.length(); i++) {
                    urls[i] = a.optString(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }

    private ArrayList<String> getStringArrayListPref(Context context, String key) {
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

    // 그리드뷰 로드
    private void loadGridView(){
        gridview = (GridView)findViewById(R.id.style_scroll);
        gridview.setAdapter(new ImageAdapter(this));
        gridview.setOnItemClickListener(gridviewOnItemClickListener);

        mMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }
    private GridView.OnItemClickListener gridviewOnItemClickListener
            = new GridView.OnItemClickListener() {

        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                long arg3) {
            fProgressDlgA = new ProgressDlg(FinalstyleActivity.this).execute(40);

            String sFileName = arg0.getAdapter().getItem(arg2).toString();
            setImageNull();

            top = mdb.LoadStyle(sFileName)[1];
            bottom = mdb.LoadStyle(sFileName)[2];
            overall = mdb.LoadStyle(sFileName)[3];
            outwear = mdb.LoadStyle(sFileName)[4];
            shoes = mdb.LoadStyle(sFileName)[5];

            selectedList.clear();
            selectedPathList.clear();
            for(int i=0; i<5;i++){
                for(int j=1; j<6;j++){
                    String fname = mdb.LoadStyle(sFileName)[j];
                    if(fname!=null) {
                        selectedList.add(i, fname);
                        selectedPathList.add(i, "/"+clothesCategory(fname)+"/"+fname);
                    }
                }
            }
            Log.i("testselected",selectedList.get(1));
            Log.i("testPath",selectedPathList.get(1));
            setImageViews(selectedPathList);
            //추천 멘트 설정
            setComments(mdb, selectedList, sky, windspd,
                    temperature, tempMax, tempMin,
                    humidity, precipitation_prob, precipitation_type,
                    selectedIndex);
        }
    };

    private void setImageNull(){
        fShoesView.setImageBitmap(null);
        fOutwearView.setImageBitmap(null);
        fBottomView.setImageBitmap(null);
        fTopView.setImageBitmap(null);
    }

    public String clothesCategory(String fname){
        String EngCat = "";
        String Korean = mdb.LoadCloset(fname)[1];
        switch (Korean){
            case "긴팔":
                EngCat = "longtop";
                break;
            case "반팔":
                EngCat = "shorttop";
                break;
            case "민소매":
                EngCat = "sleeveless";
                break;
            case "긴바지":
                EngCat = "pants";
                break;
            case "반바지":
                EngCat = "shorts";
                break;
            case "치마":
                EngCat = "skirts";
                break;
            case "원피스":
                EngCat = "onepiece";
                break;
            case "자켓":
                EngCat = "jacket";
                break;
            case "코트":
                EngCat = "coat";
                break;
            case "패딩":
                EngCat = "padding";
                break;
            case "운동화":
                EngCat = "sneakers";
                break;
            case "샌들":
                EngCat = "sandals";
                break;
            case "부츠":
                EngCat = "boots";
                break;
        }
        return EngCat;
    }

    public List<String> loadImages() {
        String path = root + "/styles";
        File directory = new File(path);
        if(!directory.exists()){
            directory.mkdirs();
        }
        File[] files = directory.listFiles();
        List<String> filesNameList = new ArrayList<>();
        for (int i=0; i< files.length; i++) {
            filesNameList.add(files[i].getName());
            pathList.add("/styles/" + files[i].getName());
        }
        return filesNameList;
    }

    //Adapter for GridView
    private class ImageAdapter extends BaseAdapter {
        private Context mContext;
        Bitmap bm;
        Bitmap resize;

        public ImageAdapter(Context c) {
            mContext = c;
            styleList = loadImages();
        }

        public int getCount() {
            return styleList.size();
//            return mThumbIds.length;
        }

        public Object getItem(int position) {
            return styleList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
//            loadImages();
            int rowWidth = (mMetrics.widthPixels) / 4;

            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
            } else {
                imageView = (ImageView) convertView;
            }
//            imageView.setImageURI(Uri.parse(clothesList.get(position)));
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 6;
//            bm = BitmapFactory.decodeFile(root + "/styles/" + styleList.get(position), options);
            resize = decodeSampledBitmapFromUri(root + "/styles/" + styleList.get(position), 200, 200);
            Bitmap mThumbnail = ThumbnailUtils.extractThumbnail(resize, 380, 400);
            imageView.setPadding(1, 1, 1, 1);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setLayoutParams(new GridView.LayoutParams(380, 400));
            imageView.setImageBitmap(mThumbnail);

//            if (!bm.isRecycled()) {
//                bm.recycle();
//            }
            return imageView;
        }

    }

    //선택한 코디를 STYLE 테이블에 저장
    public void saveStyle(){
        for(int i = 0; i<selectedList.size(); i++){
            String fname = selectedList.get(i);
            String type = mdb.getType(fname);
            switch (type) {
                case "top":
                    top = fname;
                    break;
                case "bottom":
                    bottom = fname;
                    break;
                case "overall":
                    overall = fname;
                    break;
                case "outwear":
                    outwear = fname;
                    break;
                case "shoes":
                    shoes = fname;
                    break;
            }
        }
        //mdb.insertStyle(top, bottom, overall, outwear, shoes);
    }

    //likes +1 해주기
    public void addLikes(){
        for(int i = 0; i<selectedList.size(); i++){
            String fname = selectedList.get(i);
            nowIndex = mdb.getID(fname);
            mdb.updateLikes(nowIndex, fname);
        }
    }

    // 이미지뷰에 상의, 하의, 신발 순으로 뿌리기
    private void setImageViews(ArrayList<String> selectedPathList) {
        for(int i=0; i<selectedPathList.size(); i++) {
            imgFile = new File(root+selectedPathList.get(i));
            if(imgFile.exists()){
//               styleBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                styleBitmap = decodeSampledBitmapFromUri(imgFile.getAbsolutePath(), 200, 200);
                String toCheck = selectedPathList.get(i);
                Log.i("selected=", toCheck);
                String fname = imgFile.getName();
                type = mdb.LoadCloset(fname)[2];

                switch(type)
                {
                    case "top":
                    case "overall":
                        fTopView.setImageBitmap(styleBitmap);
                        break;
                    case "bottom":
                        fBottomView.setImageBitmap(styleBitmap);
                        break;
                    case "outwear":
                        fOutwearView.setImageBitmap(styleBitmap);
                        break;
                    case "shoes":
                        fShoesView.setImageBitmap(styleBitmap);
                        break;
                }
            }
        }
    }

    //최종 멘트 설정
    private void setComments(DbOpenHelper mdb, ArrayList<String> selectedList,
                             String[] sky, String windspd, String[] temperature, String tempMax, String tempMin,
                             String[] humidity, String[] precipitation_prob, String[] precipitation_type, int selectedIndex) {
        FinalstyleComment fStyleComment = new FinalstyleComment();
//        getWeather(latitude, longitude);
        Log.i("FinalstyleActivity", selectedList.get(1));
        commentArray = fStyleComment.finalComment(mdb, selectedList, sky[selectedIndex], windspd,
                temperature[selectedIndex], temperature[selectedIndex+1], tempMax, tempMin,
                humidity[selectedIndex], precipitation_prob[selectedIndex], precipitation_type[selectedIndex]);
        comment_top.setText(commentArray[0]);
        comment_bottom.setText(commentArray[1]);
        comment_shoes.setText(commentArray[2]);
        comment_outwear.setText(commentArray[3]);
        comment_colors.setText((commentArray[4]));
    }

    // 선택한 옷들은 코디해 놓은 layout을 capture용으로 설정
    private void captureStyle() {
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyyMMddHHmmss"); //년,월,일,시간 포멧 설정
        Date time = new Date(); //파일명 중복 방지를 위해 사용될 현재시간
        String current_time = sdf.format(time); //String형 변수에 저장

        requestCapture(capture_target_Layout); //지정한 Layout 영역 사진첩 저장 요청
//        Toast.makeText(getApplicationContext(),"현재 코디가 저장되었습니다.",Toast.LENGTH_SHORT).show();

    }

    private void getCaptureView(View view) {
        view.buildDrawingCache(); //캐시 비트 맵 만들기
        bitmap_capture = view.getDrawingCache();
    }

    //Weclo 폴더에 캡처한 코디 저장
    public File requestCapture(View view) {
        /* 캡쳐 파일 저장 */
        getCaptureView(view);

        String root = Environment.getExternalStorageDirectory().toString() + "/Weclo/styles";
        File myDir = new File(root);

        //Generate Weclo directory
        if(!myDir.exists())     //check if file already exists
        {
            myDir.mkdirs();     //if not, create it
        }

        stylename = "style"+System.currentTimeMillis()+".png";
        File file = new File(myDir, stylename);

        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap_capture.compress(Bitmap.CompressFormat.PNG, 50, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //이미지 갤러리에 로드
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file));
        sendBroadcast(intent);

        return file;
    }

    // 저장된 코디 이미지를 Weclo에서 불러와 layout에 보여주기
    View setClothes(String path){
        Bitmap bm = decodeSampledBitmapFromUri(path, 300, 300);

        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
        layout.setGravity(Gravity.CENTER);

        ImageView imageView = new ImageView(getApplicationContext());
        imageView.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setImageBitmap(bm);

        layout.addView(imageView);
        return layout;
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

    //
    //Weather 관련 정보 로드
    //
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
        getHourlyWeather(mdb, latitude, longitude);
        //위치정보 모니터링 제거
        locationManager.removeUpdates(FinalstyleActivity.this);
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
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.savestyle_button:
                captureStyle();
                Toast.makeText(getApplicationContext(),"현재 코디가 저장되었습니다.",Toast.LENGTH_SHORT).show();
                loadGridView();
                saveStyle();
                mdb.insertStyle(stylename,top,bottom,overall,outwear,shoes);
                addLikes();
                addLikes();
                break;

            case R.id.complete_button:
                getCaptureView(capture_target_Layout);
                addLikes();
                Intent intent = new Intent(getApplicationContext(), WeatherActivity.class);
                intent.putExtra("finalList", selectedList);
                intent.putExtra("finalListPath", selectedPathList);
                Log.e("final_size", Integer.toString(selectedList.size()));
                Log.e("final_sizepath", Integer.toString(selectedPathList.size()));
                startActivity(intent);
                break;

            case R.id.cancel_button:
                Intent mainIntent = new Intent(getApplicationContext(), WeatherActivity.class);
                startActivity(mainIntent);
                break;

            case R.id.closet_button:
                Intent closetIntent = new Intent(getApplicationContext(), ClosetActivity.class);
                closetIntent.putExtra("mainlat", latitude);
                closetIntent.putExtra("mainlong", longitude);
                startActivity(closetIntent);
        }
    }

    private void getHourlyWeather(DbOpenHelper mdb, double latitude, double longitude) {
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
//                    Log.e("json", array.toString());
                    if (array != null) {
                        windspd = array.getWeather().getHourly().get(0).getWind().getWspd();

                        tempMax = array.getWeather().getHourly().get(0).getTemperature().getTmax();
                        tempMin = array.getWeather().getHourly().get(0).getTemperature().getTmin();

                        String test = array.getResult().getMessage();
                        Log.e("json", test);
                        setComments(mdb, selectedList, sky, windspd,
                                temperature, tempMax, tempMin,
                                humidity, precipitation_prob, precipitation_type,
                                selectedIndex);

                    }

                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherhourlyInfo> call, @NonNull Throwable t) {
            }
        });
    }
}

