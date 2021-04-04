package com.example.Weclo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public class WeatherdailyInfo {
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
        @SerializedName("forecast3days")
        @Expose
        public List<forecast3days> forecast3days = new ArrayList<>();
        public List<forecast3days> getForecast3days() {return forecast3days;}

        public class forecast3days {
            @SerializedName("grid") @Expose Grid grid;
            @SerializedName("fcstdaily") @Expose Fcstdaily fcstdaily;
            @SerializedName("fcst6hour") @Expose Fcst6hour fcst6hour;
            @SerializedName("fcst3hour") @Expose Fcst3hour fcst3hour;

            @SerializedName("timeRelease") @Expose String timeRelease;

            public class Grid{
                @SerializedName("city") @Expose String city;
                @SerializedName("county") @Expose String county;
                @SerializedName("village") @Expose String village;

                public String getCity() {return city;}
                public String getCounty() {return county;}
                public String getVillage() {return village;}
            }

            public class Fcstdaily{

            }

            public class Fcst6hour{

            }

            public class Fcst3hour{
                @SerializedName("precipitation") @Expose precipitation precipitation;
                @SerializedName("sky") @Expose Sky sky;
                @SerializedName("temperature") @Expose temperature temperature;
                @SerializedName("humidity") @Expose humidity humidity;

                public class precipitation { // 강수 정보
                    //0:현상없음,  1:비,  2:비/눈,  3:눈
                    @SerializedName("type4hour") @Expose String type4hour;
                    @SerializedName("prob4hour") @Expose String prob4hour;
                    @SerializedName("type7hour") @Expose String type7hour;
                    @SerializedName("prob7hour") @Expose String prob7hour;
                    @SerializedName("type10hour") @Expose String type10hour;
                    @SerializedName("prob10hour") @Expose String prob10hour;
                    @SerializedName("type13hour") @Expose String type13hour;
                    @SerializedName("prob13hour") @Expose String prob13hour;

                    public String getType4hour() {return type4hour;}
                    public String getProb4hour() {return prob4hour;}
                    public String getType7hour() {return type7hour;}
                    public String getProb7hour() {return prob7hour;}
                    public String getType10hour() {return type10hour;}
                    public String getProb10hour() {return prob10hour;}
                    public String getType13hour() {return type13hour;}
                    public String getProb13hour() {return prob13hour;}
                }

                public class Sky {
                    @SerializedName("name4hour") @Expose String name4hour;
                    @SerializedName("code4hour") @Expose String code4hour;
                    @SerializedName("name7hour") @Expose String name7hour;
                    @SerializedName("code7hour") @Expose String code7hour;
                    @SerializedName("name10hour") @Expose String name10hour;
                    @SerializedName("code10hour") @Expose String code10hour;
                    @SerializedName("name13hour") @Expose String name13hour;
                    @SerializedName("code13hour") @Expose String code13hour;

                    public String getName4hour() {return name4hour;}
                    public String getCode4hour() {return code4hour;}
                    public String getName7hour() {return name7hour;}
                    public String getCode7hour() {return code7hour;}
                    public String getName10hour() {return name10hour;}
                    public String getCode10hour() {return code10hour;}
                    public String getName13hour() {return name13hour;}
                    public String getCode13hour() {return code13hour;}
                }

                public class temperature{
                    @SerializedName("temp4hour") @Expose String temp4hour;
                    @SerializedName("temp7hour") @Expose String temp7hour;
                    @SerializedName("temp10hour") @Expose String temp10hour;
                    @SerializedName("temp13hour") @Expose String temp13hour;

                    public String getTemp4hour() {return temp4hour;}
                    public String getTemp7hour() {return temp7hour;}
                    public String getTemp10hour() {return temp10hour;}
                    public String getTemp13hour() {return temp13hour;}
                }

                public class humidity{
                    @SerializedName("rh4hour") @Expose String rh4hour;
                    @SerializedName("rh7hour") @Expose String rh7hour;
                    @SerializedName("rh10hour") @Expose String rh10hour;
                    @SerializedName("rh13hour") @Expose String rh13hour;

                    public String getRh4hour(){return rh4hour;}
                    public String getRh7hour(){return rh7hour;}
                    public String getRh10hour(){return rh10hour;}
                    public String getRh13hour(){return rh13hour;}
                }

                public precipitation getPrecipitation() {return precipitation;}
                public Sky getSky() {return sky;}
                public temperature getTemperature() {return temperature;}
                public humidity getHumidity() {return humidity;}
            }

            public Grid getGrid() {return grid;}
            public Fcst3hour getFcst3hour() {return fcst3hour;}
            public String getTimeRelease() {return timeRelease;}
        }
    }

    public Result getResult() {return result;}
    public Common getCommon() {return common;}
    public weather getWeather() {return weather;}

    public interface DailyApiService {
        static final String BASEURL = "https://apis.openapi.sk.com/";
        static final String APPKEY ="57512dd0-77b7-436d-aa80-1cd037b76ccb";
        //get 메소드를 통한 http rest api 통신
        @GET("weather/forecast/3days")
        Call<WeatherdailyInfo> getJson (@Header("appKey")String appKey , @Query("version") int version,
                                         @Query("lat") double lat, @Query("lon") double lon);
    }
}
