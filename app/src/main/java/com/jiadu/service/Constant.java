package com.jiadu.service;

import android.util.Log;

public class Constant {

    public static int ServerPort = 12345;
    public static String ip_bigScreen = "/192.168.1.102";
    public static String ip_ros = "/192.168.20.141";

    public static boolean isDebug = true;
    public static String TAG = "HeadControl";
    public static String filePath = "data/data/com.android.jdrd.headcontrol/cache/map.xml";
    public static String Type = "type";
    public static String Function = "function";
    public static String Data = "data";
    public static String Command = "command";
    public static String State = "state";
    public static String Param = "param";
    public static String Navigation = "navigation";
    public static String Peoplesearch = "peoplesearch";
    public static String StopSearch = "stop";
    public static String Result = "result";
    public static String Camera = "3dcamera";
    private static Constant constant;
    public static Constant getConstant(){
        if(constant != null){
            return constant;
        }else {
            constant = new Constant();
            return constant;
        }
    }

    public static void debugLog(String string){
        if(isDebug){
            Log.e(TAG,string);
        }
    }
}
