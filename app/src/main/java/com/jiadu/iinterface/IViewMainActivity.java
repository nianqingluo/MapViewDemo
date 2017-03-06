package com.jiadu.iinterface;

/**
 * Created by Administrator on 2017/2/28.
 */
public interface IViewMainActivity {

    public void OpenPower();
    public void OpenClean();
    public void OpenMap();
    public void OpenSetting();

    /**
     * 显示机器人的位置坐标
     * @param position:位置坐标
     */
    public void setPosition(String position);
}
