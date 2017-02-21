package com.jiadu.iinterface;

import com.jiadu.bean.IMUDataBean;

/**
 * Created by Administrator on 2017/2/21.
 */
public interface IPresent {

    public IMUDataBean getIMUDataBean();

    public void openIMUSerialPort();

    public void closeIMUSerialPort();
}
