package com.example.Weclo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DbOpenHelper {

    private static final String DATABASE_NAME = "InnerDatabase(SQLite).db";
    private static final int DATABASE_VERSION = 1;
    public static SQLiteDatabase mDB;
    private DatabaseHelper mDBHelper;
    private Context mCtx;

    private class DatabaseHelper extends SQLiteOpenHelper{

        public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db){
            db.execSQL(DataBases.CreateDB._CREATE0);
            db.execSQL(DataBases.CreateDB._CREATE1);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS "+DataBases.CreateDB._TABLENAME0);
            db.execSQL("DROP TABLE IF EXISTS "+DataBases.CreateDB._TABLENAME1);
            onCreate(db);
        }
    }

    public DbOpenHelper(Context context){
        this.mCtx = context;
    }

    public DbOpenHelper open() throws SQLException{
        mDBHelper = new DatabaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        mDB = mDBHelper.getWritableDatabase();
        return this;
    }

    public void create(){
        mDBHelper.onCreate(mDB);
    }

    public void close(){
        mDB.close();
    }

    // Insert DB
    public long insertColumn(String filename, String category, String type, String color1, String color2, String pattern, String material, String thickness){
        ContentValues values = new ContentValues();
        values.put(DataBases.CreateDB.FILENAME, filename);
        values.put(DataBases.CreateDB.CATEGORY, category);
        values.put(DataBases.CreateDB.TYPE, type);
        values.put(DataBases.CreateDB.COLOR1, color1);
        values.put(DataBases.CreateDB.COLOR2, color2);
        values.put(DataBases.CreateDB.PATTERN, pattern);
        values.put(DataBases.CreateDB.MATERIAL, material);
        values.put(DataBases.CreateDB.THICKNESS, thickness);
        values.put(DataBases.CreateDB.LIKES, 0);
        return mDB.insert(DataBases.CreateDB._TABLENAME0, null, values);
    }

    public long insertStyle(String stylefilename,String top, String bottom, String overall, String outwear, String shoes){
        ContentValues values = new ContentValues();
        values.put(DataBases.CreateDB.STYLENAME, stylefilename);
        values.put(DataBases.CreateDB.TOP, top);
        values.put(DataBases.CreateDB.BOTTOM, bottom);
        values.put(DataBases.CreateDB.OVERALL, overall);
        values.put(DataBases.CreateDB.OUTWEAR, outwear);
        values.put(DataBases.CreateDB.SHOES, shoes);
        return mDB.insert(DataBases.CreateDB._TABLENAME1, null, values);
    }

    // Update DB
    public boolean updateColumn(long id, String filename, String category, String type, String color1, String color2, String pattern, String material, String thickness){
        ContentValues values = new ContentValues();
        values.put(DataBases.CreateDB.FILENAME, filename);
        values.put(DataBases.CreateDB.CATEGORY, category);
        values.put(DataBases.CreateDB.TYPE, type);
        values.put(DataBases.CreateDB.COLOR1, color1);
        values.put(DataBases.CreateDB.COLOR2, color2);
        values.put(DataBases.CreateDB.PATTERN, pattern);
        values.put(DataBases.CreateDB.MATERIAL, material);
        values.put(DataBases.CreateDB.THICKNESS, thickness);
        return mDB.update(DataBases.CreateDB._TABLENAME0, values, "_id=" + id, null) > 0;
    }

    public boolean updateLikes(long id, String fname){
        ContentValues values = new ContentValues();
        Cursor c = mDB.rawQuery("SELECT * FROM closet WHERE filename = '"+fname+"';", null );
        int likes = 0;
        while (c.moveToNext()){
            likes = c.getInt(9) + 1;
        }
        values.put(DataBases.CreateDB.LIKES, likes);
        return mDB.update(DataBases.CreateDB._TABLENAME0, values, "_id=" + id, null) > 0;
    }

    // Delete All
    public void deleteAllColumns() {
        mDB.delete(DataBases.CreateDB._TABLENAME0, null, null);
    }

    // Delete DB
    public boolean deleteColumn(long id){
        return mDB.delete(DataBases.CreateDB._TABLENAME0, "_id="+id, null) > 0;
    }
    // Select DB
    public String selectColumns(String result){
        String search = "";
        //Cursor c = mDB.query(DataBases.CreateDB._TABLENAME0,null,DataBases.CreateDB.FILENAME,new String[]{String.valueOf(result)},null,null,null);
        Cursor c = mDB.rawQuery("SELECT * FROM closet WHERE filename = '"+result+"';", null );
        while (c.moveToNext()){
            search += c.getString(1)
                    + ","
                    + c.getString(2)
                    + ","
                    + c.getString(3)
                    + ","
                    + c.getString(4)
                    + ","
                    + c.getString(5)
                    + ","
                    + c.getString(6)
                    + ","
                    + c.getString(7)
                    + ","
                    + c.getString(8)
                    + ","
                    + c.getInt(9)
                    + "\n";
        }
        return search;
    }

    public long getID(String fname){
        long id = 0;
        Cursor c = mDB.rawQuery("SELECT * FROM closet WHERE filename = '"+fname+"';", null );
        while (c.moveToNext()){
            id = c.getLong(0);
        }
        return id;
    }

    public String getType(String fname){
        String type = "";
        Cursor c = mDB.rawQuery("SELECT * FROM closet WHERE filename = '"+fname+"';", null );
        while (c.moveToNext()){
            type += c.getString(3);
        }
        return type;
    }

    // Load Colums data from DB
    public String[] LoadCloset(String fname){
        String[] load = new String[8];
//        String load = "";
        //Cursor c = mDB.query(DataBases.CreateDB._TABLENAME0,null,DataBases.CreateDB.FILENAME,new String[]{String.valueOf(result)},null,null,null);
        Cursor c = mDB.rawQuery("SELECT * FROM closet WHERE filename = '"+fname+"';", null );
        if(c!=null){
            if(c.moveToFirst()){
                do{
                    load[0] = c.getString(c.getColumnIndex("filename"));
                    load[1] = c.getString(c.getColumnIndex("category"));
                    load[2] = c.getString(c.getColumnIndex("type"));
                    load[3] = c.getString(c.getColumnIndex("color1"));
                    load[4] = c.getString(c.getColumnIndex("color2"));
                    load[5] = c.getString(c.getColumnIndex("pattern"));
                    load[6] = c.getString(c.getColumnIndex("material"));
                    load[7] = c.getString(c.getColumnIndex("thickness"));
                } while(c.moveToNext());
            }
        }
        return load;
    }

    public String[] LoadStyle(String stylename){
        String[] load = new String[6];
//        String load = "";
        //Cursor c = mDB.query(DataBases.CreateDB._TABLENAME0,null,DataBases.CreateDB.FILENAME,new String[]{String.valueOf(result)},null,null,null);
        Cursor c = mDB.rawQuery("SELECT * FROM style WHERE stylename = '"+stylename+"';", null );
        if(c!=null){
            if(c.moveToFirst()){
                do{
                    load[0] = c.getString(c.getColumnIndex("stylename"));
                    load[1] = c.getString(c.getColumnIndex("top"));
                    load[2] = c.getString(c.getColumnIndex("bottom"));
                    load[3] = c.getString(c.getColumnIndex("overall"));
                    load[4] = c.getString(c.getColumnIndex("outwear"));
                    load[5] = c.getString(c.getColumnIndex("shoes"));
                } while(c.moveToNext());
            }
        }
        return load;
    }

    // sort by column
    public Cursor sortColumn(String sort){
        Cursor c = mDB.rawQuery( "SELECT * FROM closet ORDER BY " + sort + ";", null);
        return c;
    }

    public String selectCategory(String category, String thick){
        String search = "";
        Cursor c = mDB.rawQuery("SELECT * FROM closet WHERE category IN ('반팔','긴팔') AND thickness IN ('보통')",null);
        switch (thick){
            case "두꺼움":
                c = mDB.rawQuery("SELECT * FROM closet WHERE category IN ("+category+") AND thickness IN ('두꺼움','보통') ORDER BY likes DESC LIMIT 1",null);
                break;
            case "얇음":
                c = mDB.rawQuery("SELECT * FROM closet WHERE category IN ("+category+") AND thickness IN ('얇음','보통') ORDER BY likes DESC LIMIT 1",null);
                break;
            case "보통":
                c = mDB.rawQuery("SELECT * FROM closet WHERE category IN ("+category+") AND thickness IN ('보통') ORDER BY likes DESC LIMIT 1",null);
                break;
        }
        if(c!=null){
            while (c.moveToNext()){
                search += c.getString(1);

            }
        }
        return search;
    }

    public Boolean ifExist(){
        Cursor mCursor = mDB.rawQuery("SELECT * FROM closet", null);
        Boolean rowExists;

        if (mCursor.moveToFirst())
        {
            // DO SOMETHING WITH CURSOR
            rowExists = true;

        } else
        {
            // I AM EMPTY
            rowExists = false;
        }
        return rowExists;
    }

}