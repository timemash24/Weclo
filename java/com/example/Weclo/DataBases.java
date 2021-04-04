package com.example.Weclo;

import android.provider.BaseColumns;

public final class DataBases {

    public static final class CreateDB implements BaseColumns{
        //        public static final String NAME = "name";
        public static final String FILENAME = "filename";
        public static final String CATEGORY = "category";
        public static final String TYPE = "type";
        public static final String COLOR1 = "color1";
        public static final String COLOR2 = "color2";
        public static final String PATTERN = "pattern";
        public static final String MATERIAL = "material";
        public static final String THICKNESS = "thickness";
        public static final String LIKES = "likes";
        public static final String _TABLENAME0 = "closet";
        public static final String _CREATE0 = "create table if not exists "+_TABLENAME0+"("
                +_ID+" integer primary key autoincrement, "
//                +NAME+" text not null , "
                +FILENAME+ " text , "
                +CATEGORY+" text not null , "
                +TYPE+ " text , "
                +COLOR1+" text , "
                +COLOR2+" text , "
                +PATTERN+" text ,"
                +MATERIAL+" text ,"
                +THICKNESS+" text ,"
                +LIKES+" integer );";

        public static final String STYLENAME = "stylename";
        public static final String TOP = "top";
        public static final String BOTTOM = "bottom";
        public static final String OVERALL = "overall";
        public static final String OUTWEAR = "outwear";
        public static final String SHOES = "shoes";
        public static final String _TABLENAME1 = "style";
        public static final String _CREATE1 = "create table if not exists "+_TABLENAME1+"("
                +_ID+" integer primary key autoincrement, "
                +STYLENAME+ " text , "
                +TOP+ " text , "
                +BOTTOM+ " text , "
                +OVERALL+ " text , "
                +OUTWEAR+ " text , "
                +SHOES+ " text );";
    }
}
