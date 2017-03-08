package com.jiadu.mapdemo.util;

/**
 * Created by Administrator on 2017/3/7.
 */
public class TurnAngleUtil {

    /**
     * @param angle:需要转动的角度
     * @return 返回换算后 -180° - 180°的角度
     */
    public static float getTransferAngle(float angle){

        if (angle > 180){
            
            return 180 -360;

        }
        else if (angle < - 180){
            return angle +360;
        }

        return angle;
    }

}
