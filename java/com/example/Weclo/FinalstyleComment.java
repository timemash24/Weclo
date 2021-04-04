package com.example.Weclo;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;


//추천에 사용할 날씨정보 : 온도, 습도, 하늘상태, 풍속
//추천에 사용할 옷정보 : 옷종류, 소재, 두께
//옷 색상은 선택한 옷들의 색상 조합에 대한 추천하기. 날씨에 따른 색상 추천은 무의미할듯.
public class FinalstyleComment extends AppCompatActivity {

    float wind;
    float temp_avg;
    float temp_curr;
    float temp_later;
    float temp_min;
    float temp_max;
    float humid;
    float pre_prob;

    //선택한 옷 이미지 주소를 저장할 리스트 {상의, 하의, 신발, 겉옷} 순서
//    private ArrayList<String> loadSelected = new ArrayList<>();

    //온도별 옷차림 정보를 저장할 배열 {옷종류, 소재, 두께}
    //이 배열의 문자열과 contains로 포함하는지 확인
    private String[] temp_compare = {};

    public String[] load_info = {};

    String[] colors = {"RED", "ORANGE", "YELLOW", "GREEN", "CYAN", "BLUE", "PURPLE", "PINK"};

    public ArrayList<String> avoid_colors = new ArrayList<>();

    int[] tempState = new int[5];
    int[] matState = new int[5];
    int[] thickState = new int[5];
    int[] patternState = new int[5];
    String[][] colorState = new String[5][2];

    private String[] commentList = new String[5];  //멘트 저장용
    private int tempComment = 0;
    private int isOutwear = 0;

    public String[] finalComment(DbOpenHelper mdb, ArrayList<String> loadSelected,
                                 String sky, String windspd, String tempCurrent, String tempLater, String tempMax, String tempMin,
                                 String humidity, String preProb, String preType) {

        //선택한 옷의 filename을 저장한 리스트 불러오기
        Log.i("testing", loadSelected.get(0));
        Log.i("testing", mdb.selectColumns(loadSelected.get(0)));

        init(windspd, tempCurrent, tempLater, tempMax, tempMin, humidity, preProb,
                tempState, matState, thickState, patternState, colorState, commentList);

        //1.온도 구간에 맞는 옷을 선택했는지 확인후 멘트 설정
        for(int i=0; i<loadSelected.size(); i++){
            //선택한 옷의 정보 load_info에 저장(옷 하나의 정보)
            load_info = mdb.LoadCloset(loadSelected.get(i));

            //load_info = {파일명,종류,부위,색1,색2,패턴,소재,두께,선호도}
            getTempState(load_info, tempState, matState, thickState, patternState, colorState, temp_curr, humid);

            //비,눈 오면 가죽 비추천
            if(!commentList[0].contains("가죽"))
                commentList[0] += checkSky(load_info, sky);

            //바람 세면 치마 비추천
            commentList[0] += checkWind(load_info, wind);

            //겉옷 확인
            if(load_info[2].equals("outwear"))
                isOutwear++;
        }

        for(int j=0; j<tempState.length; j++){
            tempComment += tempState[j]+matState[j]+thickState[j];
            Log.e("tempComment", Integer.toString(tempComment));
            Log.e("tempState", Integer.toString(tempState[j]));
            Log.e("matState", Integer.toString(matState[j]));
            Log.e("thickState", Integer.toString(thickState[j]));
        }
        String change = getTypeToChange(tempComment, tempState, matState, thickState);
        if(tempComment>0){
            commentList[0] = commentList[0] + " 현재 옷차림으로는 더울 수 있습니다."
                    + change;

        }
        else if(tempComment<0){
            commentList[0] = commentList[0] + " 현재 옷차림으로는 추울 수 있습니다."
                    + change;
        }
        else{
            //덥다 춥다가 상쇄외어서 적절한 옷차림으로...?
            commentList[0] = commentList[0] + " 기온에 적절한 옷차림입니다!";
        }

        //2.예보에 따른 주의사항 멘트 설정
        checkTempLater(commentList, temp_curr, temp_later, pre_prob, preType);

        //3.겉옷 멘트 설정
        checkOutwear(commentList, temp_curr);

        //4.일교차 멘트 설정
        checkTempDiff(commentList, temp_max, temp_min);

        //5.패턴, 색깔에 대한 멘트 설정
        checkPattern(commentList, patternState);
        commentList[4] += checkColor(colorState, tempState);

        return commentList;
    }

    //문자열 숫자로 이용하기 위한 함수
    private void init(String windspd, String tempCurrent, String tempLater, String tempMax, String tempMin, String humidity, String preProb,
                      int[] tempState, int[] matState, int[] thickState, int[] patternState, String[][] colorState, String[] commentList){
        wind = Float.parseFloat(windspd);
        temp_curr = Float.parseFloat(tempCurrent);
        temp_later = Float.parseFloat(tempLater);
        temp_min = Float.parseFloat(tempMin);
        temp_max = Float.parseFloat(tempMax);
        temp_avg = (temp_min+temp_max)/2;
        humid = Float.parseFloat(humidity);
        pre_prob = Float.parseFloat(preProb);

        for(int i=0; i<5; i++){
            tempState[i]=0;
            matState[i]=0;
            thickState[i]=0;
            patternState[i]=0;
            commentList[i]="";
            for(int j=0; j<2; j++){
                colorState[i][j]="";
            }
        }

    }

    //온도에 따라 추운지 더운지 확인하여 코멘트에 이용
    private void getTempState(String[] load_info,
                              int[] tempState, int[] matState, int[] thickState, int[] patternState, String[][] colorState,
                              float temp_curr, float humid) {
        //tempState = {상의, 하의, 신발, 겉옷, 한벌옷}
        checkHumidity(humid, temp_curr);
        switch (load_info[1]) {
            case "민소매":
                if(temp_curr<28) tempState[0]--; //춥다
                getStates(load_info, temp_curr, 0, matState, thickState, patternState, colorState);
                break;
            case "반팔":
                if(temp_curr<17) tempState[0]--;
                getStates(load_info, temp_curr, 0, matState, thickState, patternState, colorState);
                break;
            case "긴팔":
                if(temp_curr>22) tempState[0]++;   //덥다
                getStates(load_info, temp_curr, 0, matState, thickState, patternState, colorState);
                break;
            case "반바지":
                if(temp_curr<23) tempState[1]--;
                getStates(load_info, temp_curr, 1, matState, thickState, patternState, colorState);
                break;
            case "긴바지":
                if(temp_curr>27) tempState[1]++;
                getStates(load_info, temp_curr, 1, matState, thickState, patternState, colorState);
                break;
            case "치마":
                if(temp_curr<9) tempState[1]--;
                getStates(load_info, temp_curr, 1, matState, thickState, patternState, colorState);
                break;
            case "원피스":
                if(temp_curr<9) tempState[4]--;
                getStates(load_info, temp_curr, 4, matState, thickState, patternState, colorState);
                break;
            case "자켓":
                if(temp_curr>16) tempState[3]++;
                else if(temp_curr<5) tempState[3]--;
                getStates(load_info, temp_curr, 3, matState, thickState, patternState, colorState);
                break;
            case "코트":
                if(temp_curr>11) tempState[3]++;
                getStates(load_info, temp_curr, 3, matState, thickState, patternState, colorState);
                break;
            case "패딩":
                if(temp_curr>8) tempState[3]++;
                getStates(load_info, temp_curr, 3, matState, thickState, patternState, colorState);
                break;
            case "샌들":
                if(temp_curr<17) tempState[2]--;
                getStates(load_info, temp_curr, 2, matState, thickState, patternState, colorState);
                break;
            case "부츠":
                if(temp_curr>17) tempState[2]++;
                getStates(load_info, temp_curr, 2, matState, thickState, patternState, colorState);
                break;
        }
    }

    private void getStates(String[] load_info, float temp_curr, int index,
    int[] matState, int[] thickState, int[] patternState, String[][] colorState) {
        getMatState(load_info, matState, temp_curr, index);
        getThickState(load_info, thickState, temp_curr, index);
        getPatternState(load_info, patternState, index);
        getColorState(load_info, colorState, index);
    }

    //소재에 따라 더운지 추운지 확인
    private void getMatState(String[] load_info, int[] matState, float temp_curr, int index) {
        //matState = {상의, 하의, 신발, 겉옷, 한벌옷}
        switch (load_info[2]){
            case "린넨":
                if(temp_curr<17) matState[index]--;
                break;
            case "가죽":
            case "니트":
                if(temp_curr>16) matState[index]++;
                break;
            case "기모":
            case "울":
                if(temp_curr>11) matState[index]++;
                break;
        }
    }

    //두께에 따라 더운지 추운지 확인
    private void getThickState(String[] load_info, int[] thickState, float temp_curr, int index) {
        switch(load_info[7]){
            case "얇음":
                if(temp_curr<17) thickState[index]--;
                break;
            case "보통":
                if(temp_curr>28) thickState[index]++;
                break;
            case "두꺼움":
                if(temp_curr>8) thickState[index]++;
                break;
        }
    }

    //패턴을 포함한 옷 확인
    private void getPatternState(String[] load_info, int[] patternState, int index) {
        switch(load_info[5]){
            case "true":
                patternState[index]++;
        }
    }

    private String getTypeToChange(int tempComment, int[] tempState, int[] matState, int[] thickState) {
        String result="";
        int index = -1;
        for(int i=0; i<tempState.length; i++){
            if(tempComment>0){
                if(tempState[i]>0){
                    index = i;
                    if(thickState[i]>0){
                        result += " 조금 더 얇은 ";
                        result += getTypeName(index) +"은(는) 어떠신가요?" ;
//                        return result;
                    }
                    else if(matState[i]>0){
                        result += " 다른 소재의 ";
                        result += getTypeName(index)+"은(는) 어떠신가요?";
//                        return result;
                    }
                    else{
                        result += " "+getTypeName(index)+" 이(가) 기온에 맞지 않습니다.";
                    }
                }
            }
            else if(tempComment<0){
                if(tempState[i]<0){
                    index = i;
                    if(thickState[i]<0){
                        result += " 조금 더 두꺼운 ";
                        result += getTypeName(index)+"은(는) 어떠신가요?";
//                        return result;
                    }
                    else if(matState[i]<0){
                        result += " 다른 소재의 ";
                        result += getTypeName(index)+"은(는) 어떠신가요?";
//                        return result;
                    }
                    else{
                        result += " "+getTypeName(index)+" 이(가) 기온에 맞지 않습니다.";
                    }

                }
            }
            else{
                if(thickState[i]>0){
                    index = i;
                    result += " 조금 더 얇은 ";
                    result += getTypeName(index)+"은(는) 어떠신가요?";
//                    return result;
                }
                else if(thickState[i]<0){
                    index = i;
                    result += " 조금 더 두꺼운 ";
                    result += getTypeName(index)+"은(는) 어떠신가요?";
//                    return result;
                }

                if(matState[i]<0){
                    index = i;
                    result += " 다른 소재의 ";
                    result += getTypeName(index)+"은(는) 어떠신가요?";
//                    return result;
                }
                else if(matState[i]>0){
                    index = i;
                    result += " 다른 소재의 ";
                    result += getTypeName(index)+"은(는) 어떠신가요?";
//                    return result;
                }
            }
        }
        return result;
    }

    private String getTypeName(int index) {
        String result="옷";
        switch(index){
            case 0:
                result = "상의";
                break;
            case 1:
                result = "하의";
                break;
            case 2:
                result = "신발";
                break;
            case 3:
                result = "겉옷";
                break;
            case 4:
                result = "한벌옷";
                break;
        }
        return result;
    }

    private void getColorName(ArrayList<String> avoid_colors, String[] final_comment){
       String color_name="";
        String[] colors = {"RED", "ORANGE", "YELLOW", "GREEN", "CYAN", "BLUE", "PURPLE", "PINK"};
        String[] name_list = {"빨간색", "주황색", "노란색", "초록색", "하늘색", "파란색", "보라색", "분홍색"};
        for(int i=0; i<avoid_colors.size(); i++){
           for(int j=0; j<colors.length; j++){
               if(avoid_colors.get(i).equals(colors[j])){
                   if(!color_name.contains(name_list[j]))
                       color_name += name_list[j] + " ";
               }
           }
       }
        final_comment[3] += color_name;
    }

    //패턴 개수에 따른 멘트 설정
    private void checkPattern(String[] commentList, int[] patternState) {
        //patternState = {상의, 하의, 신발, 겉옷, 한벌옷}
        int patterns =0;
        for(int i=0; i<patternState.length; i++){
            patterns += patternState[i];
        }
        if(patterns>1){
            if((patternState[0]+patternState[1])==2){   //상의+하의 패턴 겹침
                commentList[4] = " 상의에 따라 단색의 하의를 추천합니다.";
            }
            else if((patternState[0]+patternState[3])==2){   //상의+겉옷 패턴 겹침
                commentList[4] = " 상의에 따라 단색의 겉옷을 추천합니다.";
            }
        }
    }

    //예보에 따른 주의사항 멘트 설정
    private void checkTempLater(String[] commentList, float temp_curr, float temp_later, float pre_prob, String preType) {
        if(temp_curr<23 && temp_curr>=20){
            if(temp_later<temp_curr)
                commentList[1] = " 외출 이후에 기온이 낮아질 예정이니 겉옷을 챙기는 것이 좋겠습니다.";
        }
        if(temp_curr<5 && temp_later<temp_curr)
            commentList[1] = " 외출 이후에 기온이 더 낮아질 예정이니 모자나 목도리를 챙기는 것이 좋겠습니다.";

        if(pre_prob>50){
            commentList[1] = " 외출 이후에 " + ClosetActivity.getPreTypeName(preType) + " 올 확률이 높으니 우산을 챙기시기 바랍니다!";
        }
    }

    //온도에 따라 겉옷 유무 확인 후 멘트 설정
    private void checkOutwear(String[] commentList, float temp_curr) {
        if(temp_curr>28){
            commentList[2] = " 냉방이 강한 실내에서는 겉옷을 챙기는 것이 좋겠습니다.";
        }
        else if(temp_curr>22){
            if(isOutwear>0)
                commentList[2] = " 실외에서 겉옷은 더울 것으로 예상되어 추천드리지 않습니다.";
        }
        else if(temp_curr<17){
            if(isOutwear==0)
                commentList[2] = " 기온이 낮으므로 겉옷을 챙기셔야 합니다!";
        }
    }

    //일교차 확인 후 멘트 설정
    private void checkTempDiff(String[] commentList, float temp_max, float temp_min) {
        if(temp_max-temp_min >= 10)
            commentList[3] = " 오늘 일교차가 큰 것에 유의하세요.";
    }

    //온도 구간에 따라 추천할 옷 종류
    private String classifyByTemp(String sky, float wind, float humid, float temp_min, float temp_curr){
        String suggest="";

        if(temp_curr>=28){
            temp_compare = new String[]{"sleeveless shortop shorts onepiece sandals sneakers", "데님 면 린넨 합성섬유 기타", "얇음 보통"};
        }
        else if(temp_curr>=23){
            temp_compare = new String[]{"shortop shorts onepiece pants sandals sneakers", "데님 면 린넨 합성섬유 기타", "얇음 보통"};
        }
        else if(temp_curr>=20){
            temp_compare = new String[]{"shortop longtop shorts pants sandals sneakers", "데님 면 린넨 기타", "얇음 보통"};
        }
        else if(temp_curr>=17){
            temp_compare = new String[]{"shortop longtop jacket pants sandals sneakers", "데님 면 린넨 합성섬유 기타", "얇음 보통"};
        }
        else if(temp_curr>=12){
            temp_compare = new String[]{"longtop jacket pants sneakers boots", "데님 면 합성섬유 기타 가죽 니트", "보통"};
        }
        else if(temp_curr>=9){
            temp_compare = new String[]{"longtop jacket coat jacket pants sneakers boots", "데님 면 합성섬유 기타 가죽 기모 니트 울", "보통"};
        }
        else if(temp_curr>=5){
            temp_compare = new String[]{"longtop jacket coat jacket padding pants sneakers boots", "데님 면 합성섬유 기타 가죽 기모 니트 울", "보통 두꺼움"};
        }
        else
            temp_compare = new String[]{"longtop jacket coat jacket padding pants sneakers boots", "데님 면 합성섬유 기타 가죽 기모 니트 울", "두꺼움"};


        for(int i=0; i<3; i++){
            suggest += temp_compare[i];
        }
        return suggest;
    }

    //습도에 따른 옷 추천
    private void checkHumidity(float humid, float temp_curr){
        if(humid>=60 && temp_curr>=23){
            //습하고 온도가 23도 이상의 더운 날에는 기준 온도보다 시원한 옷으로.
            temp_curr = 28;
        }
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

    //비,눈 상태에 따른 옷 추천
    private String checkSky(String[] load_info, String sky){
        String result = "";
        if(sky.contains("비") || sky.contains("눈")){
            //비나 눈이오면 흰색 제외. 가죽 제외. 샌들 추천, 운동화와 부츠는 소재 고려 추천.
            //흰색은 MONO에 포함되어서 따로 빼낼 수 없을 것 같음.
            if(load_info[6].equals("가죽"))
                result = " 가죽 제품은 젖으면 망가질 수 있어 추천하지 않습니다.";
        }
        return result;
    }

    //풍속에 따른 옷 추천
    private String checkWind(String[] load_info, float wind){
        String result = "";
        if(wind>=4){
            //바람이 약간 강할때부터 치마와 원피스 추천제외
            if(load_info[1].equals("치마") || load_info[1].equals("원피스")){
                result = " 바람이 강하여 치마 종류는 피하는 것이 좋겠습니다.";
            }
        }
        return result;
    }

    //색 조합 확인
    private String checkColor(String[][] colorState, int[] tempState){
        //colorState = {상의, 하의, 신발, 겉옷, 한벌옷}
        String result = "";
        Log.e("colorbottom", colorState[1][0]);
        Log.e("colorbottom", colorState[1][1]);
        for(int i=0; i<2; i++){
            if(!colorState[0][i].equals("none") || !colorState[0][i].equals("MONO")){
                if(colorState[0][i].equals(colorState[1][0]) || colorState[0][i].equals(colorState[1][1])){
                    Log.e("colorState", colorState[0][i]);
                    //상의 하의 색 조합
                    if(tempState[4]>0 && tempState[1]==0) {  //원피스 확인
                        result =" 서로 다른 색 계열의 상의와 하의를 코디하는건 어떨까요?";
                    }
                }
                else if(colorState[0][i].equals(colorState[3][0]) || colorState[0][i].equals(colorState[3][1])){
                    Log.e("colorState", colorState[0][i]);
                    //상의 겉옷 색 조합
                    result = " 서로 다른 색 계열의 상의와 겉옷을 코디하는건 어떨까요?";
                }
            }
        }
        return result;
    }

    private void getColorState(String[] load_info, String[][] colorState, int index) {
        String colorA = load_info[3];
        String colorB = load_info[4];
        Log.e("colorA", colorA);
        Log.e("colorB", colorB);
        Log.e("index", Integer.toString(index));
        colorState[index][0] = colorA;
        colorState[index][1] = colorB;
    }

}
