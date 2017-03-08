package com.jiaud.Manager;

import android.graphics.Point;

import com.jiadu.broadcast.MessageBroadcast;
import com.jiadu.fragment.MapFragment;
import com.jiadu.mapdemo.MainActivity;
import com.jiadu.mapdemo.util.TurnAngleUtil;
import com.jiadu.mapdemo.view.MapView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/3/7.
 */
public class ReceiveBrain implements MessageBroadcast.MessageListener{

    private Brain mBrain = Brain.getInstance();

    private MainActivity mContext;

    MapFragment mMapFragment = null;


    public void setContext(MainActivity context) {
        mContext = context;
        mMapFragment = (MapFragment) mContext.getFragmentManager().findFragmentByTag(MainActivity.TAG_FRAGMENT_MAP);
    }

    public static ReceiveBrain instance = null;

    public static ReceiveBrain getInstance(){

        if (instance == null){
            synchronized (Brain.class){
                if (instance == null){
                    instance = new ReceiveBrain();
                }
            }
        }
        return instance;
    }
    private ReceiveBrain(){}


    @Override
    public void receiveMessage(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);

            String type = (String) jsonObject.get("type");

            if ("command".equals(type)){//接收到的是命令

                handleReceiveCommandMessage(jsonObject);

            }else if ("state".equals(type)){//接收到的是状态

                handleReceiveStateMessage(jsonObject);

            }else if ("param".equals(type)){//接收到的是参数

                handleReceiveParamMessage(jsonObject);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    /**
     * 处理参数的函数
     * @param jsonObject:json数据
     */
    private void handleReceiveParamMessage(JSONObject jsonObject) {

    }

    /**
     * 处理状态的函数
     * @param jsonObject:json数据
     */
    private void handleReceiveStateMessage(JSONObject jsonObject) {

        try {
            String function = (String) jsonObject.get("function");


            if (mBrain.WALK.equals(function)){//说明是行走
                JSONObject data = (JSONObject) jsonObject.get("data");
                float dis = (float) data.getDouble("distance");
                if ("success".equals(data.get("result"))){ //说明行走完成s
                    mBrain.setStateMapValue(mBrain.WALK,null);
                    if ("target".equals(mBrain.getStateMapValue(mBrain.MANYOU))){//在漫游目的点状态下完成行走
                       if (mMapFragment!=null){
                           mMapFragment.setCurrentListPosition(mMapFragment.getCurrentListPosition()+1);
                       }

                       changeRobotPoint(dis);
                    }
                    else if ("temp".equals(mBrain.getStateMapValue(mBrain.MANYOU))){//在漫游临时点状态下完成行走

                       changeRobotPoint(dis);
                    }


                    else {//在其他状态下完成行走
                        // TODO: 2017/3/7 在其他状态下完成行走
                    }
                }
                else if ("fail".equals(data.get("result"))){//说明行走失败
                    mBrain.setStateMapValue(mBrain.WALK,null);
                    if ("yes".equals(mBrain.getStateMapValue(mBrain.MANYOU))) {//在漫游状态下行走失败
                        changeRobotPoint(dis);
                    }
                }
                else {//说明是在运动中

                    if ("yes".equals(mBrain.getStateMapValue(mBrain.MANYOU))){//在漫游状态下的运动中
                       changeRobotPoint(dis);
                    }
                }
            }
            else if (mBrain.TURN.equals(function)){//说明是在转弯
                JSONObject data = (JSONObject) jsonObject.get("data");
                if ("sucess".equals(data.get("result"))){//旋转指定的角度已经完成
                    mBrain.setStateMapValue(mBrain.TURN,null);
                    if (Math.abs(mMapFragment.nextDirection - mMapFragment.currentAngle)<5){//说明转弯角度OK
                    }
                    else {//说明转弯角度不精确
                        float v = mMapFragment.nextDirection - mMapFragment.currentAngle;

                        float transferAngle = TurnAngleUtil.getTransferAngle(v);

                        mBrain.sendCommand("{'type':'command','function':'turn','data':{'degree':"+transferAngle+"}");
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理命令的函数
     * @param jsonObject:json数据
     */
    private void handleReceiveCommandMessage(JSONObject jsonObject) {

    }
    private void changeRobotPoint(float distance){

        float px = transferToPx(distance);

        switch (mMapFragment.getDirection()){
            case 0: {//上

                Point robotPointInMap = mMapFragment.getRobotPointInMap();

                Point p = new Point(robotPointInMap);

                p.y = (int) (p.y - px);

                mMapFragment.setRobotPointInMap(p);

            }
                break;
            case 1: {//下
                Point robotPointInMap = mMapFragment.getRobotPointInMap();

                Point p = new Point(robotPointInMap);

                p.y = (int) (p.y + px);

                mMapFragment.setRobotPointInMap(p);
            }
            break;
            case 2:{//左
                Point robotPointInMap = mMapFragment.getRobotPointInMap();

                Point p = new Point(robotPointInMap);

                p.x = (int) (p.x - px);

                mMapFragment.setRobotPointInMap(p);
            }
            break;
            case 3:{//右
                Point robotPointInMap = mMapFragment.getRobotPointInMap();

                Point p = new Point(robotPointInMap);

                p.x = (int) (p.x + px);

                mMapFragment.setRobotPointInMap(p);
            }
            break;
            default:
            break;
        }
    }

    /**
     * @param distance:距离
     * @return 返回距离相距的像素点
     */
    private float transferToPx(float distance) {
        return MapView.mGridWidth/ (mContext.getMapScaleFactor()*0.1f)*distance;
    }
}
