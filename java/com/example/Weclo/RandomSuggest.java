package com.example.Weclo;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class RandomSuggest {

    private float wind;
    private float temp_curr;

    //임의로 선택된 옷 파일이름과 종류
    private String fname;
    private String category="";

    //type_state={상의, 하의(한벌옷), 신발, 겉옷}
    private int[] type_state = new int[4];
    private String[] type_compare = new String[4];
    private String[] temp_compare = new String[4];
    private String thick_compare="보통";

    String[][] clothesLabels = {
            {"긴팔", "top"},
            {"반팔", "top"},
            {"민소매", "top"},
            {"긴바지", "bottom"},
            {"반바지", "bottom"},
            {"치마", "bottom"},
            {"원피스", "overall"},
            {"자켓", "outwear"},
            {"코트", "outwear"},
            {"패딩", "outwear"},
            {"운동화", "shoes"},
            {"샌들", "shoes"},
            {"부츠", "shoes"}
    };

    //선택한 옷 종류중 임의의 옷을 선택
    public void getRandomList(Context context, DbOpenHelper mdb, ArrayList<String> autoList, ArrayList<String> autoPathList, String tempCurrent) {
        init(type_compare, type_state, tempCurrent);
        sortByTemp(temp_curr, temp_compare);
        sortByThick(thick_compare, temp_curr);

        //type_state={상의, 하의(한벌옷), 신발, 겉옷}
        int type_count=0;

        Log.e("temp_comparebottom", temp_compare[0]);
        Log.e("temp_comparebottom", temp_compare[1]);
        Log.e("temp_comparebottom", temp_compare[2]);
        Log.e("temp_comparebottom", temp_compare[3]);

        for(int i=0; i<temp_compare.length; i++){
            if(!temp_compare[i].equals("")){
                try{
                    Log.i("temp_compare", temp_compare[i]);
                    type_state[i]++;
                    fname = mdb.selectCategory(temp_compare[i], thick_compare);
                    Log.e("randomfname", fname);
                    if(!fname.equals("")){
                        autoList.add(fname);
                        setAutoPathList(mdb, autoPathList, fname);
                    }
                    //원피스 선택시 상의 버리기
                }catch(Exception e){
                    e.printStackTrace();
                    Toast.makeText(context, "추천에 필요한 옷이 부족합니다!", Toast.LENGTH_SHORT).show();
                    throw e;
                }
            }
        }

        if(autoPathList.size()==0){
            Toast.makeText(context, "추천에 필요한 옷이 부족합니다!", Toast.LENGTH_SHORT).show();
        }

        else{
            if(type_state[0]>0)
                checkOverall(autoList, autoPathList);
            Log.e("autoPath", autoPathList.get(0));
            Log.e("autoPathsize", Integer.toString(autoPathList.size()));
        }
    }

    private void init(String[] type_compare, int[] type_state, String tempCurrent) {
        temp_curr = Float.parseFloat(tempCurrent);
        for(int i=0; i<type_compare.length; i++){
            type_compare[i] = "";
            type_state[i] = 0;
        }
    }

    private void checkOverall(ArrayList<String> autoList, ArrayList<String> autoPathList){
        for(int i=0; i<autoPathList.size(); i++){
            if(autoPathList.get(i).contains("onepiece")){
                for(int j=0; j<autoPathList.size(); j++){
                    if(autoPathList.get(j).contains("shorttop") || autoPathList.get(j).contains("longtop")
                            || autoPathList.get(j).contains("sleeveless")){
                        autoPathList.remove(j);
                        autoList.remove(j);
                    }
                }
            }
        }
    }

    //선택된 옷의 경로 가져오기 - autoPathList에 저장!
    private void setAutoPathList(DbOpenHelper mdb, List<String> autoPathList, String fname) {
        String[] load_info = mdb.LoadCloset(fname);
        Log.e("autoloadinfo",load_info[1]);
        String dir = "";
        switch (load_info[1]){
            case "긴팔":
                dir = "longtop";
                break;
            case "반팔":
                dir = "shortop";
                break;
            case "민소매":
                dir = "sleeveless";
                break;
            case "긴바지":
                dir = "pants";
                break;
            case "반바지":
                dir = "shorts";
                break;
            case "치마":
                dir = "skirts";
                break;
            case "원피스":
                dir = "onepiece";
                break;
            case "자켓":
                dir = "jacket";
                break;
            case "코트":
                dir = "coat";
                break;
            case "패딩":
                dir = "padding";
                break;
            case "운동화":
                dir = "sneakers";
                break;
            case "샌들":
                dir = "sandals";
                break;
            case "부츠":
                dir = "boots";
                break;
        }
        String path = "/" + dir + "/" + fname;
        autoPathList.add(path);
    }

    //온도 구간에 따라 추천할 옷종류 분류
    //temp_compare={상의, 하의(한벌옷), 신발, 겉옷}
    private void sortByTemp(float temp_curr, String[] temp_compare) {
        String[] result;
        if(temp_curr>=28){
            result = new String[]{"'민소매','반팔'","'반바지','원피스','치마'","'샌들','운동화'",""};
        }
        else if(temp_curr>=23){
            result = new String[]{"'반팔'", "'반바지','원피스','치마','긴바지'", "'샌들','운동화'", ""};
        }
        else if(temp_curr>=20){
            result = new String[]{"'반팔','긴팔'", "'반바지','긴바지','원피스','치마'", "'샌들','운동화'", ""};
        }
        else if(temp_curr>=17){
            result = new String[]{"'반팔','긴팔'", "'치마','원피스','긴바지'", "'샌들','운동화'", "'자켓'"};
        }
        else if(temp_curr>=12){
            result = new String[]{"'긴팔'", "'긴바지'", "'운동화','부츠'", "'자켓'"};
        }
        else if(temp_curr>=9){
            result = new String[]{"'긴팔'", "'긴바지'", "'운동화','부츠'", "'자켓','코트'"};
        }
        else if(temp_curr>=5){
            result = new String[]{"'긴팔'", "'긴바지'", "'운동화','부츠'", "'코트','자켓','패딩'"};
        }
        else
            result = new String[]{"'긴팔'", "'긴바지'", "'운동화','부츠'", "'자켓','코트','패딩'"};

        for(int i=0; i<result.length; i++){
            temp_compare[i] = result[i];
        }
    }

    //온도 구간에 따라 추천할 옷두께 분류
    private void sortByThick(String thick_compare, float temp_curr) {
        if(temp_curr>=17){
            thick_compare = "얇음";
        }
        else if(temp_curr>=9){
            thick_compare = "보통";
        }
        else{
            thick_compare = "두꺼움";
        }
    }

    //기준에 맞는 임의의 옷종류 선택
    private void checkSelectedCategory(int[] type_state, String[] type_compare, int type_count, String rCategory, float temp_curr){
        for(int i=0; i<type_state.length; i++) {
            type_count += type_state[i];
        }
        if(type_count==0){  //첫 선택시
            for(int j=0; j<clothesLabels.length; j++){
                if(rCategory.equals(clothesLabels[j][0])){
                    type_state[getTypeIndex(clothesLabels[j][1])]++; //선택한 상태 업데이트
                    type_compare[getTypeIndex(clothesLabels[j][1])] = rCategory;
                }
            }
        }
        else if(type_count<3){   //이미 선택한 옷이 있을 떄 중복 선택 금지(3벌까지)
            for(int k=0; k<clothesLabels.length; k++){
                if(rCategory.equals(clothesLabels[k][0])){
                    if(type_state[getTypeIndex(clothesLabels[k][1])]==0) {
                        type_state[getTypeIndex(clothesLabels[k][1])]++; //선택한 상태 업데이트
                        type_compare[getTypeIndex(clothesLabels[k][1])] = rCategory;
                    }
                    else return;
                }
            }
        }
        else{   //4벌 이상 선택시 겉옷이 선택되지 않았을 때 기온이 높으면 선택 안함
            if(type_state[3] == 0){
                if(temp_curr>17){
                    type_state[3]++;
                    return;
                }
                else{
                    for(int l=0; l<clothesLabels.length; l++){
                        if(rCategory.equals(clothesLabels[l][0])){
                            if(type_state[getTypeIndex(clothesLabels[l][1])]==0){
                                type_state[getTypeIndex(clothesLabels[l][1])]++; //선택한 상태 업데이트
                                type_compare[getTypeIndex(clothesLabels[l][1])] = rCategory;
                            }
                        }
                    }
                }

            }
        }
    }

    //type_state에 저장할 위치 index 구하기
    //type_state={상의, 하의(한벌옷), 신발, 겉옷}
    private int getTypeIndex(String type){
        int index=0;
        switch (type){
            case "top":
                index = 0;
                break;
            case "bottom":
            case "overall":
                index = 1;
                break;
            case "shoes":
                index = 2;
                break;
            case "outwear":
                index = 3;
                break;
        }
        return index;
    }

}
