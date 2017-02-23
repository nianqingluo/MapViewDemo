package com.jiadu.iinterface;

import com.jiadu.bean.IMUDataBean;

import java.io.IOException;

/**
 * Created by Administrator on 2017/2/21.
 */
public interface IPresent {

    public IMUDataBean getIMUDataBean();

    public void openIMUSerialPort() throws IOException;

    public void closeIMUSerialPort();
}
