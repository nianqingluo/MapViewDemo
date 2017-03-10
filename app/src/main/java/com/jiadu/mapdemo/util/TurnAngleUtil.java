package com.jiadu.mapdemo.util;

/**
 * Created by Administrator on 2017/3/7.
 */
public class TurnAngleUtil {


    public static boolean isInTolerance(float a,float b){

        if (Math.abs(a-b)%360 < Constant.TOLERANCEANGLE){
            //说明在误差范围之内
            return true;
        }

        if ((Math.abs(a-b)%360) > (360 - Constant.TOLERANCEANGLE)){
            //说明在误差范围之内
            return true;
        }

        return false;
    }


    /**
     * @param angle:需要转动的角度
     * @return 返回换算后 -180° - 180°的角度
     */
    public static float getTransferAngle(float angle){

        angle = angle%360;

        if (angle > 180){
            
            return angle -360;

        }
        else if (angle < - 180){
            return angle +360;
        }

        return angle;
    }
}
