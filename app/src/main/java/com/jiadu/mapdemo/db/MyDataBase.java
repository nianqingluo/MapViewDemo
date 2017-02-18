package com.jiadu.mapdemo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2017/2/16.
 */
public class MyDataBase extends SQLiteOpenHelper {

    private final Context mContext;

    public static final String CREATE_POINT = "create table point ("
            + "id integer primary key autoincrement, "
            + "pointx integer, "
            + "pointy integer, "
            + "type integer)";

    public MyDataBase(Context context, String name) {
        super(context, name, null, 1);

        mContext = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_POINT);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
