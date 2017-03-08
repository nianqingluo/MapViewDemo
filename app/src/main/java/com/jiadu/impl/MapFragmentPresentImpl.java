package com.jiadu.impl;

import com.jiadu.bean.IMUDataBean;
import com.jiadu.iinterface.IMapFragmentPresent;
import com.jiadu.iinterface.IMapView;
import com.jiadu.mapdemo.util.SerialPortUtil;

import java.io.IOException;

/**
 * Created by Administrator on 2017/2/21.
 */
public class MapFragmentPresentImpl implements IMapFragmentPresent {

    private IMapView mIMapView;
    private SerialPortUtil spu;

    public MapFragmentPresentImpl(IMapView iMapView) {
        mIMapView = iMapView;
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
