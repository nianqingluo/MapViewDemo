package com.jiadu.iinterface;

import android.graphics.Point;

import com.jiadu.bean.IMUDataBean;

/**
 * Created by Administrator on 2017/2/21.
 */
public interface IMapView {

    public void updata13TV(IMUDataBean bean);

    public void showChoicePathDialog();

    public void showChoiceScaleDialog();

    public void setCurrentListPosition(int position);

    public int getCurrentListPosition();

    public void setRobotPointInMap(Point point);

    public Point getRobotPointInMap();

    public Point getCenterPoint();

}
