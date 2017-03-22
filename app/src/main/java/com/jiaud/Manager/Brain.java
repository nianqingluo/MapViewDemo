package com.jiaud.Manager;

import android.graphics.Point;

import com.jiadu.fragment.MapFragment;
import com.jiadu.mapdemo.util.LogUtil;
import com.jiadu.service.ServerSocketUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/3/7.
 */
public class Brain {

    private Map<String,String>  stateMap = new HashMap<String,String>();
    public final String MANYOU = "manyou";
    public final String MANYOU_TEMP = "temp";
    public final String MANYOU_TARGET = "target";
    public final String WALK = "walk";//forward back
    public final String WALK_FORWARD = "forward";//forward
    public final String WALK_BACK = "back";//back
    public final String TURN = "turn";//right left
    public final String TURNING = "turning";//right left
    public final String ZHAOREN = "manyou";
    public final String CAMERA = "3dcamera";
    public final String CAMERA_FINDING = "finding";//正在找人
    public final String CAMERA_FOUND = "found";//找到了人
    public final String CAMERA_BACK = "back";//在返回中
    public final String CAMERA_CHAT = "chat";//在与客人交流中
    public final String RETURNING = "returning";//在返回原点
    public final String RETURN = "return";//在返回原点

    public final String STOP = "stop";
    public final String INTERMEDIATER = "intermediater";
    public Point RobotPointBeforeWalk = null;

    public void setMapFragment(MapFragment mapFragment) {
        mMapFragment = mapFragment;
    }

    public MapFragment mMapFragment =null;

    public static Brain instance = null;

    public static Brain getInstance(){
        
        if (instance == null){
            synchronized (Brain.class){
                if (instance == null){
                    instance = new Brain();
                }
            }
        }
        return instance;
    }

    private Brain(){

    }

    public String getStateMapValue(String key){
        return stateMap.get(key);
    }

    public void setStateMapValue(String key, String value){
        
        if (value == null){
            stateMap.remove(key);
            return;
        }
        
        stateMap.put(key,value);
    }

    public void sendCommand(String string){
        try {

            LogUtil.debugLog("发送的数据："+string);

            JSONObject json = new JSONObject(string);
            String type = (String) json.get("type");

            if ("command".equals(type)){//说明处理的是命令
                handleSendCommand(json);
            }
            else if ("state".equals(type)){//说明处理的是状态

            }
            else if ("param".equals(type)){//说明处理的是参数
                        
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleSendCommand(JSONObject json) throws JSONException {

        String function = (String) json.get("function");

        if (CAMERA.equals(function)){//是要发送打开3D摄像头

        }
        else if (WALK.equals(function)){//是要发送行走命令
            try {
                stopAll();
                ServerSocketUtil.sendDateToClient(json.toString());
                RobotPointBeforeWalk = mMapFragment.getRobotPointInMap();
                stateMap.put(WALK,WALK_FORWARD);
            } catch (IOException e) {
                e.printStackTrace();
        }
            }
        else if (TURN.equals(function)){//是要发送转弯命令
            try {
                stopAll();
                ServerSocketUtil.sendDateToClient(json.toString());
                stateMap.put(TURN,TURNING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (STOP.equals(function)){//是要发送停止命令
            try {
                stopAll();
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    }

    private void stopAll() throws IOException {

        ServerSocketUtil.sendDateToClient("{'type':'command','function':'stop','data':''}");
        stateMap.remove(WALK);
        stateMap.remove(TURN);
    }
}
