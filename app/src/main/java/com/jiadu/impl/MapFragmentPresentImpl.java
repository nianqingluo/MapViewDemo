package com.jiadu.impl;

import com.jiadu.bean.IMUDataBean;
import com.jiadu.iinterface.IPresent;
import com.jiadu.iinterface.IView;
import com.jiadu.mapdemo.util.SerialPortUtil;

import java.io.IOException;

/**
 * Created by Administrator on 2017/2/21.
 */
public class MapFragmentPresentImpl implements IPresent {

    private IView mIView;
    private SerialPortUtil spu;

    public MapFragmentPresentImpl(IView iView) {
        mIView = iView;
        spu = SerialPortUtil.getInstance();
    }

    @Override
    public IMUDataBean getIMUDataBean() {

        IMUDataBean bean = spu.getBean();

        return bean;
    }

    @Override
    public void openIMUSerialPort() throws IOException {

            spu.openSerialPort();

    }

    @Override
    public void closeIMUSerialPort() {
        spu.close();
    }
}
