package com.jiadu.mapdemo.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;

import com.jiadu.mapdemo.db.MyDataBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/2/16.
 */
public class MyDataBaseUtil {


    private Context mContext;
    private final MyDataBase mDataBase;

    public MyDataBaseUtil(Context context) {
        mContext = context;

        mDataBase = new MyDataBase(context,"database.db");

    }

    public void addPoint(int x, int y, int type){

        SQLiteDatabase db = mDataBase.getWritableDatabase();

        db.execSQL("insert into point values(null,"+x+","+y+","+type+")");

        db.close();

    }


    public void deleteAll(){

        SQLiteDatabase db = mDataBase.getWritableDatabase();

        db.execSQL("delete from point");

        db.close();

    }

    public void deletePoint(int x,int y,int type){
        SQLiteDatabase db = mDataBase.getWritableDatabase();

        db.execSQL("delete from point where pointx = "+x+" and pointy = "+y+" and type ="+type+";");

        db.close();

    }

    public void updataBasePoint(int x,int y){

        SQLiteDatabase db = mDataBase.getWritableDatabase();

        db.execSQL("update point set pointx = "+x+", pointy = "+y+" where type =0;");

        db.close();
    }

    public Point queryCenterPoint(int type){

        Point point = null;

        SQLiteDatabase db = mDataBase.getReadableDatabase();

        Cursor cursor = db.rawQuery("select * from point where type = ?", new String[]{type + ""});

        while (cursor.moveToNext()){

            int x = cursor.getInt(1);
            int y = cursor.getInt(2);

            LogUtil.debugLog("x:"+x+",y:"+y);

            point = new Point(x,y);
        }
        cursor.close();

        db.close();

        return point;
    }


    public List<Point> queryPathPoint(int type){

        if (type==0){
            return null;
        }

        List<Point> list = new ArrayList<>();

        SQLiteDatabase db = mDataBase.getReadableDatabase();

        Cursor cursor = db.rawQuery("select * from point where type = ?", new String[]{type + ""});

        while (cursor.moveToNext()){

            int x = cursor.getInt(1);
            int y = cursor.getInt(2);
            list.add(new Point(x,y));
        }
        cursor.close();
        db.close();
        return list;
    }
}
