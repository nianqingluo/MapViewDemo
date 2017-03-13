package com.jiaud.Manager;

import android.content.Intent;
import android.graphics.Point;

import com.jiadu.broadcast.MessageBroadcast;
import com.jiadu.fragment.MapFragment;
import com.jiadu.mapdemo.MainActivity;
import com.jiadu.mapdemo.util.LogUtil;
import com.jiadu.mapdemo.util.ToastUtils;
import com.jiadu.mapdemo.util.TurnAngleUtil;
import com.jiadu.mapdemo.view.MapView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2017/3/7.
 */
public class ReceiveBrain implements MessageBroadcast.MessageListener{

    private Brain mBrain = Brain.getInstance();

    private MainActivity mContext;

    MapFragment mMapFragment = null;

    public Timer mTimer= new Timer();
    private float mDistance;//找到人的距离


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

        LogUtil.debugLog("message:"+message);

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

            if (mBrain.CAMERA.equals(function) && mBrain.getStateMapValue(mBrain.CAMERA)!=null){//说明是在3d摄像头找人
                JSONObject data = (JSONObject) jsonObject.get("data");

                if ("body".equals(data.get("result"))){  //说明3D摄像头找到人了

                    float angle = (float) data.getDouble("degree");

                    mDistance = (float) data.getDouble("distance");

                    angle = angle<180?angle:angle-360;

                    float v = (mMapFragment.currentAngle + angle)%360;


                    if (TurnAngleUtil.isInTolerance(v,mMapFragment.currentAngle)){//表示找的人在正对准机器人
                        if (mDistance <= 1){
                            // TODO: 2017/3/9 人就在旁边，播放广告
                            mBrain.setStateMapValue(mBrain.CAMERA,mBrain.CAMERA_CHAT);
                            mBrain.sendCommand("{'type':'command','function':'stop','data':''}");
                            ToastUtils.makeToast(mContext,"已经到人面前了，播放广告");
                        }
                        else if (mDistance >1){
                            // TODO: 2017/3/9 人在前面，需要走到前面才播放广告
                            mBrain.setStateMapValue(mBrain.CAMERA,mBrain.CAMERA_FOUND);
                            mBrain.sendCommand("{'type':'command','function':'turn','data':{'degree':"+(mDistance -1)+"}}");

                        }
                    }else {//表示找的人没有对准机器人，需要转一定角度
                        // TODO: 2017/3/9  表示找的人没有对准机器人，需要转一定角度
                        float transferAngle = TurnAngleUtil.getTransferAngle(mMapFragment.currentAngle - v);

                        LogUtil.debugLog("表示找的人没有对准机器人，需要转一定角度");

                        mBrain.setStateMapValue(mBrain.CAMERA,mBrain.CAMERA_FOUND);

                        mMapFragment.setNextDirection(v);

                        mBrain.sendCommand("{'type':'command','function':'turn','data':{'degree':"+transferAngle+"}}");

                    }
                }
                else if ("nobody".equals(data.get("result"))){//说明没有找到人


                }
                else if ("away".equals(data.get("result"))){//说明人走开了
                
                    if (mBrain.CAMERA_FOUND.equals(mBrain.getStateMapValue(mBrain.CAMERA))){//表示找到人后，人走开了
                        if (mBrain.getStateMapValue(mBrain.WALK)!=null){
                            if (mBrain.WALK_FORWARD.equals(mBrain.getStateMapValue(mBrain.WALK))){//表示在往人方向走的过程中人离开了
                                LogUtil.debugLog("在往人方向走的过程中人离开了");
//                                    mBrain.sendCommand("{'type':'command','function':'stop','data':''}");
                                mBrain.setStateMapValue(mBrain.CAMERA,mBrain.CAMERA_BACK);
                                mMapFragment.gotoPoint(mMapFragment.getStartManYouPoint());
                            }
                        }
                        else if (mBrain.getStateMapValue(mBrain.TURN)!=null){
                            if (mBrain.TURNING.equals(mBrain.getStateMapValue(mBrain.TURN))){//表示在旋转过程中人走开了
                                LogUtil.debugLog("表示在旋转过程中人走开了");
                                mBrain.setStateMapValue(mBrain.CAMERA,null);
                                mBrain.sendCommand("{'type':'command','function':'stop','data':''}");
                                mMapFragment.startManYou(mMapFragment.getStartManYouPoint(),mMapFragment.getNextPoint());
                            }
                        }
                    }
                }
            }

            if (mBrain.WALK.equals(function) && mBrain.getStateMapValue(mBrain.WALK)!=null){//说明是行走
                JSONObject data = (JSONObject) jsonObject.get("data");
                float dis = (float) data.getDouble("distance");
                changeRobotPoint(dis);
                if ("success".equals(data.get("result"))){ //说明行走完成
                    mBrain.setStateMapValue(mBrain.WALK,null);
                    if (mBrain.getStateMapValue(mBrain.MANYOU)!=null){//漫游状态下完成行走
                        if (mBrain.MANYOU_TARGET.equals(mBrain.getStateMapValue(mBrain.MANYOU))){//在漫游目的点状态下完成行走
                            LogUtil.debugLog("到达漫游点："+dis);
                            if (mMapFragment!=null){
                                mMapFragment.setCurrentListPosition(mMapFragment.getCurrentListPosition()+1);
                            }
                            mBrain.RobotPointBeforeWalk = mMapFragment.getNextPoint();
                            mBrain.setStateMapValue(mBrain.MANYOU,null);
                            ToastUtils.makeToast(mContext,"到达了第"+(mMapFragment.getCurrentListPosition()+1)+"个漫游点");

                            // TODO: 2017/3/8 漫游达到一个点完成后需要添加的逻辑
            //                        mMapFragment.startManYou(mMapFragment.getRobotPointInMap(),mMapFragment.getNextPoint());
                            close3DCamera("远");
                            mBrain.setStateMapValue(mBrain.CAMERA,mBrain.CAMERA_FINDING);
                            mBrain.sendCommand("{'type':'command','function':'turn','data':{'degree':720}}");
                        }
                        else if (mBrain.MANYOU_TEMP.equals(mBrain.getStateMapValue(mBrain.MANYOU))){//在漫游临时点状态下完成行走
//                            changeRobotPoint(dis);

                            LogUtil.debugLog("到达中间临时点："+dis);

                            mMapFragment.startManYou(mMapFragment.tempIntermediate,mMapFragment.getNextPoint());
                        }
                    }

                    else if(mBrain.getStateMapValue(mBrain.CAMERA)!=null){//在找人状态下完成行走

                        if (mBrain.CAMERA_FOUND.equals(mBrain.getStateMapValue(mBrain.CAMERA))){//在找到人后完成行走，需要播放广告
                            mBrain.setStateMapValue(mBrain.CAMERA,mBrain.CAMERA_CHAT);
                            LogUtil.debugLog("需要播放广告");
                            // TODO: 2017/3/9 需要播放广告
                            ToastUtils.makeToast(mContext,"需要播放广告");

                            mBrain.setStateMapValue(mBrain.CAMERA,mBrain.CAMERA_BACK);
                            mMapFragment.gotoPoint(mMapFragment.getStartManYouPoint());

                        }else if (mBrain.CAMERA_BACK.equals(mBrain.getStateMapValue(mBrain.CAMERA))){//在找到人，返回漫游点完成行走。需要往下一个漫游点走

                            LogUtil.debugLog("在找到人，返回漫游点完成行走。需要往下一个漫游点走");
                            mMapFragment.setRobotPointInMap(mMapFragment.getStartManYouPoint(),false);
                            //需要往下一个漫游点行走
                            mMapFragment.startManYou(mMapFragment.getStartManYouPoint(),mMapFragment.getNextPoint());
                        }
                    }
                    else if (mBrain.getStateMapValue(mBrain.RETURN)!=null){//在返回原点中完成行走
                        LogUtil.debugLog("在返回原点中完成行走");
                        mBrain.setStateMapValue(mBrain.RETURN,mBrain.RETURNING);
                    }
                }
                else if ("fail".equals(data.get("result"))){//说明行走失败
                    mBrain.setStateMapValue(mBrain.WALK,null);
                    if (mBrain.getStateMapValue(mBrain.MANYOU)!=null) {//在漫游状态下行走失败
                        LogUtil.debugLog("在漫游状态下行走失败");
//                        changeRobotPoint(dis);
                    }

                    if (mBrain.getStateMapValue(mBrain.CAMERA)!=null){//在找人状态下行走失败
                        if (mBrain.CAMERA_FOUND.equals(mBrain.getStateMapValue(mBrain.CAMERA))){//表示人走开了
                            LogUtil.debugLog("表示人走开了");
                        }
                    }
                }
                else {//说明是在运动中

                    if (mBrain.getStateMapValue(mBrain.MANYOU)!=null){//在漫游状态下的运动中
                        LogUtil.debugLog("在漫游状态下的运动中");
//                        changeRobotPoint(dis);
                    }
                    else if (mBrain.getStateMapValue(mBrain.CAMERA)!=null){
                        LogUtil.debugLog("在找人状态下的运动中");

                    }
                }
            }
            else if (mBrain.TURN.equals(function) && mBrain.getStateMapValue(mBrain.TURN) != null){//说明是在转弯
                JSONObject data = (JSONObject) jsonObject.get("data");
                if ("success".equals(data.get("result"))){//旋转指定的角度已经完成
                    mBrain.setStateMapValue(mBrain.TURN,null);

                    if (mTimer == null){
                        mTimer = new Timer();
                    }

                    TimerTask task =new TimerTask() {
                        @Override
                        public void run() {
                            mTimer.cancel();
                            mTimer = null;
                            if (TurnAngleUtil.isInTolerance(mMapFragment.nextDirection , mMapFragment.currentAngle)){//说明转弯角度OK

                                if (mBrain.getStateMapValue(mBrain.MANYOU) != null){ // 说明是在漫游状态下完成转弯

                                    if (mBrain.MANYOU_TEMP.equals(mBrain.getStateMapValue(mBrain.MANYOU))){//说明去临时点完成转弯
                                        LogUtil.debugLog("去临时点完成转弯");
                                        mMapFragment.startManYou(mMapFragment.getRobotPointInMap(),mMapFragment.getNextPoint());
                                    }
                                    else if (mBrain.MANYOU_TARGET.equals(mBrain.getStateMapValue(mBrain.MANYOU))){//说明是去目标点完成转弯
                                        LogUtil.debugLog("去目标点完成转弯");
                                        mMapFragment.startManYou(mMapFragment.getRobotPointInMap(),mMapFragment.getNextPoint());
                                    }
                                }
                                else if (mBrain.getStateMapValue(mBrain.CAMERA)!=null){//说明在找人的状态下完成旋转

                                    if (mBrain.CAMERA_FINDING.equals(mBrain.getStateMapValue(mBrain.CAMERA))){//说明正在找人的状态下完成转弯,说明找人超时
                                        LogUtil.debugLog("正在找人的状态下完成转弯");

                                        mBrain.setStateMapValue(mBrain.CAMERA,null);
                                        //关闭3D摄像头
                                        close3DCamera("关闭");
                                        mBrain.sendCommand("{'type':'command','function':'stop','data':''}");
                                        mMapFragment.startManYou(mMapFragment.getStartManYouPoint(),mMapFragment.getNextPoint());

                                    }
                                    else if (mBrain.CAMERA_FOUND.equals(mBrain.getStateMapValue(mBrain.CAMERA))){//说明在找到人的状态下完成转弯

                                        if (mDistance <= 1){//2017/3/9 人就在前面，播放广告
                                            // TODO: 2017/3/9 人就在前面，播放广告
                                            LogUtil.debugLog("人就在前面，播放广告");

                                        }
                                        else {//人在前面，需要走到前面才播放广告
                                            // TODO: 2017/3/9 人在前面，需要走到前面才播放广告
                                            LogUtil.debugLog("人在前面，需要走到前面才播放广告");
                                            mBrain.sendCommand("{'type':'command','function':'walk','data':{'distance':"+(mDistance-1)+"}}");
                                        }
                                    }
                                    else if (mBrain.CAMERA_BACK.equals(mBrain.getStateMapValue(mBrain.CAMERA))){//说明返回漫游点转弯完成
                                        LogUtil.debugLog("返回漫游点转弯完成转弯");
                                        mMapFragment.gotoPoint(mMapFragment.getStartManYouPoint());
                                    }
                                }
                                else if (mBrain.getStateMapValue(mBrain.RETURN)!=null){//说明在返回原点中完成转弯
                                    mMapFragment.gotoPoint(mMapFragment.getCenterPoint());
                                }
                            }
                            else {//说明转弯角度不精确

                                if (mBrain.CAMERA_FINDING.equals(mBrain.getStateMapValue(mBrain.CAMERA))){//没有找到人，去下一个点
                                    mBrain.setStateMapValue(mBrain.CAMERA,null);
                                    //关闭摄像头
                                    close3DCamera("关闭");
                                    mMapFragment.startManYou(mMapFragment.getRobotPointInMap(),mMapFragment.getNextPoint());
                                    return;
                                }
                                float v = mMapFragment.currentAngle - mMapFragment.nextDirection;
                                LogUtil.debugLog("转弯角度不精确");
                                float transferAngle = TurnAngleUtil.getTransferAngle(v);
                                mBrain.sendCommand("{'type':'command','function':'turn','data':{'degree':"+transferAngle+"}}");
                            }
                        }
                    };
                    mTimer.schedule(task,2000);
                }
                else if ("fail".equals(data.get("result"))){//旋转角度失败
                    if (mBrain.getStateMapValue(mBrain.CAMERA)!=null){
                        if (mBrain.CAMERA_FINDING.equals(mBrain.getStateMapValue(mBrain.CAMERA))){//在3D摄像头找人情况下失败,说明找人超时，发了STOP命令
                            LogUtil.debugLog("说明找人超时");
                            
                        }
                        else if (mBrain.CAMERA_FOUND.equals(mBrain.getStateMapValue(mBrain.CAMERA))){//表示在找到人行走前人走开了，停止行走，往下一个漫游点走去
                            LogUtil.debugLog("找到人行走前人走开了，停止行走，往下一个漫游点走去");

                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param type："远" 表示打开3D摄像头找人
     *              "关闭" 表示关闭3D摄像头找人
     *
     */
    private void close3DCamera(String type) {
        Intent intent = new Intent();
        intent.setAction("com.jdrd.CursorSDKExample.TD_CAMERA");
        intent.putExtra("msg", type);
        mContext.sendBroadcast(intent);
    }

    /**
     * 处理命令的函数
     * @param jsonObject:json数据
     */
    private void handleReceiveCommandMessage(JSONObject jsonObject) {

    }
    private void changeRobotPoint(float distance){

        float px = transferToPx(distance);

        float dy = (float) (Math.cos(mMapFragment.nextDirection * Math.PI / 180) * px);
        float dx = (float) (Math.sin(mMapFragment.nextDirection * Math.PI / 180) * px);

        Point p = new Point(mBrain.RobotPointBeforeWalk);

        p.y = (int) (p.y - dy);

        p.x = (int) (p.x+dx);

        mMapFragment.setRobotPointInMap(p,false);


//        switch (mMapFragment.getDirection()){
//            case 0: {//上
//
//                Point p = new Point(mBrain.RobotPointBeforeWalk);
//
//                p.y = (int) (p.y - px);
//
//                mMapFragment.setRobotPointInMap(p,false);
//
//            }
//                break;
//            case 1: {//下
//                Point p = new Point(mBrain.RobotPointBeforeWalk);
//
//                p.y = (int) (p.y + px);
//
//                mMapFragment.setRobotPointInMap(p,false);
//            }
//            break;
//            case 2:{//左
//                Point p = new Point(mBrain.RobotPointBeforeWalk);
//
//                p.x = (int) (p.x - px);
//
//                mMapFragment.setRobotPointInMap(p,false);
//            }
//            break;
//            case 3:{//右
//                Point p = new Point(mBrain.RobotPointBeforeWalk);
//
//                p.x = (int) (p.x + px);
//
//                mMapFragment.setRobotPointInMap(p,false);
//            }
//            break;
//            default:
//            break;
//        }
    }


    /**
     * @param distance:距离
     * @return 返回距离对应的像素点
     */
    private float transferToPx(float distance) {
        return MapView.mGridWidth/ (mContext.getMapScaleFactor()*0.1f)*distance;
    }

}
