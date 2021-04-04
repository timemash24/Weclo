package com.example.Weclo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

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

public class WeatherhourlyInfo {
    @SerializedName("result")
    @Expose
    Result result;
    @SerializedName("common")
    @Expose
    Common common;
    @SerializedName("weather")
    @Expose
    weather weather;

    public class Result {
        @SerializedName("message") @Expose String message;
        @SerializedName("code") @Expose String code;

        public String getMessage() {return message;}
        public String getCode() {return code;}
    }

    public class Common {
        @SerializedName("alertYn") @Expose String alertYn;
        @SerializedName("stormYn") @Expose String stormYn;

        public String getAlertYn() {return alertYn;}
        public String getStormYn() {return stormYn;}
    }

    public class weather {
        @SerializedName("hourly")
        @Expose
        public List<hourly> hourly = new ArrayList<>();
        public List<hourly> getHourly() {return hourly;}

        public class hourly {
            @SerializedName("sky") @Expose Sky sky;
            @SerializedName("precipitation") @Expose precipitation precipitation;
            @SerializedName("temperature") @Expose temperature temperature;
            @SerializedName("wind") @Expose wind wind;
            @SerializedName("grid") @Expose Grid grid;

            @SerializedName("humidity") @Expose String humidity;
            @SerializedName("lightning") @Expose String lightning;
            @SerializedName("timeRelease") @Expose String timeRelease;

            public class Grid{
                @SerializedName("city") @Expose String city;
                @SerializedName("county") @Expose String county;
                @SerializedName("village") @Expose String village;

                public String getCity() {return city;}
                public String getCounty() {return county;}
                public String getVillage() {return village;}
            }

            public class Sky{
                @SerializedName("name") @Expose String name;
                @SerializedName("code") @Expose String code;

                public String getName() {return name;}
                public String getCode() {return code;}
            }

            public class precipitation{ // 강수 정보
                @SerializedName("sinceOntime") @Expose String sinceOntime; // 강우
                @SerializedName("type") @Expose String type; //0 :없음 1:비 2: 비/눈 3: 눈

                public String getSinceOntime() {return sinceOntime;}
                public String getType() {return type;}
            }

            public class temperature{
                @SerializedName("tc") @Expose String tc; // 현재 기온
                @SerializedName("tmax") @Expose String tmax; // 최고 기온
                @SerializedName("tmin") @Expose String tmin; // 최저 기온

                public String getTc() {return tc;}
                public String getTmax() {return tmax;}
                public String getTmin() {return tmin;}
            }

            public class wind{ // 바람
                @SerializedName("wdir") @Expose String wdir;
                @SerializedName("wspd") @Expose String wspd;

                public String getWdir() {return wdir;}
                public String getWspd() {return wspd;}
            }

//            public class Humidity{ //습도
//                public String getHumidity() {return humidity;}
//            }

            public Grid getGrid() {return grid;}
            public Sky getSky() {return sky;}
            public hourly.precipitation getPrecipitation() {return precipitation;}
            public hourly.temperature getTemperature() {return temperature;}
            public hourly.wind getWind() {return wind;}

            public String getHumidity() {return humidity;}
            public String getLightning() {return lightning;}
            public String getTimeRelease() {return timeRelease;}
        }
    }
    public Result getResult() {return result;}
    public Common getCommon() {return common;}
    public weather getWeather() {return weather;}

    public interface ApiService {
        static final String BASEURL = "https://apis.openapi.sk.com/";
        static final String APPKEY ="57512dd0-77b7-436d-aa80-1cd037b76ccb";
        //get 메소드를 통한 http rest api 통신
        @GET("weather/current/hourly")
        Call<WeatherhourlyInfo> getJson (@Header("appKey") String appKey , @Query("version") int version,
                                         @Query("lat") double lat, @Query("lon") double lon);
    }


}
