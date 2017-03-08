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

    /**
     * @param point:robot在地图的point点
     *
     * @param flag:是否需要匹配最近点
     */
    public void setRobotPointInMap(Point point,boolean flag);

    public Point getRobotPointInMap();

    public Point getCenterPoint();

    public Point getNextPoint();

    public Point getStartManYouPoint();

}
