package com.example.vipul.spyapp10;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLdb {
    private static final String DATABASE_NAME = "db11";
    private static final String DATABASE_TABLE = "tnw";
    private static final int DATABASE_VERSION = 1;

    private HelperSQL ourHelper;
    private final Context ourContext;
    private SQLiteDatabase ourDatabase;

    public SQLdb(Context ourContext) {
        this.ourContext = ourContext;
    }

    public SQLdb open(){
        ourHelper=new HelperSQL(ourContext);
        ourDatabase=ourHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        ourHelper.close();
    }

    public void insertEntry(String ticker, String text,String time,String pack) {
        Log.e("db", ticker);
        ourDatabase.execSQL("INSERT INTO tnw VALUES('" + ticker + "','" + text + "','" + time + "','" + pack + "');");
    }

    public int getCount(){
        Cursor resultSet = ourDatabase.rawQuery("Select * from " + DATABASE_TABLE, null);
        return resultSet.getCount();
    }
    public String[] getList(int x) {
        int i=0;
        Cursor resultSet = ourDatabase.rawQuery("Select * from " + DATABASE_TABLE, null);
        String info[]=new String[resultSet.getCount()];
        resultSet.moveToFirst();
        do{
            info[i]=resultSet.getString(x);
            resultSet.moveToNext();
            i++;
        }while(resultSet.isLast());
        return info;
    }

    public void onDelete(){
        ourDatabase.execSQL("DROP TABLE tnw;");
    }

    private static class HelperSQL extends SQLiteOpenHelper {

        public HelperSQL(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS tnw(ticker TEXT NOT NULL,text TEXT NOT NULL,time TEXT NOT NULL,pack TEXT NOT NULL );");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE);
            onCreate(db);
        }
    }
}