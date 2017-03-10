package com.jiadu.mapdemo.util;

import com.jiadu.bean.IMUDataBean;
import com.jiadu.serialport.SerialPort;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2017/2/20.
 */
public class SerialPortUtil {
    /**
     * Called when the activity is first created.
     */

    private static String SERIALPORTNAME = "/dev/ttyUSB0";

    FileInputStream mInputStream;
    SerialPort mSp;
    private boolean mFlag =true;

    private IMUDataBean mBean = null;
    private static SerialPortUtil spu;

    /**
     * @return使用前需要调用openSerialPort(),否则返回null
     */
    public IMUDataBean getBean() {

        IMUDataBean temp = mBean;
        mBean =null;
        return temp;
    }

    public static SerialPortUtil getInstance(){

        if (spu == null){
            synchronized (SerialPortUtil.class){
                if (spu == null){
                    spu = new SerialPortUtil();
                }
            }
        }
        return spu;
    }


    private SerialPortUtil(){

    }


    /**
     * 不使用,需要close(),释放资源
     * @throws IOException
     */
    public void openSerialPort() throws IOException,SecurityException {

        if (mInputStream!=null){
            return;
        }
        mSp = new SerialPort(new File(SERIALPORTNAME),115200);

        mInputStream=(FileInputStream) mSp.getInputStream();

        new Thread(){

            @Override
            public void run() {

                if (mInputStream !=null){
                    mFlag = true;
                    try {
                        while (mFlag){

                            int read1 = mInputStream.read();

                            if (read1 == 0x5A && mFlag){

                                int read2 = mInputStream.read();

                                if (read2 == 0xA5 && mFlag){

                                    int read3 = mInputStream.read();
                                    int read4 = mInputStream.read();

                                    //丢弃两个字节,这两个字节为校验码
                                    for (int i = 0; i < 2; i++) {
                                        mInputStream.read();
                                    }

                                    int total = (read4<<8)+read3;
                                    byte[] bts = new byte[total];
                                    for (int i = 0; i<total;i++){

                                        int read = mInputStream.read();
                                        bts[i] = (byte) read;
                                    }
                                    mBean = IMUDataUtil.getData(bts, total);
                                }
                            }
                        }
                    } catch (IOException e) {

                        e.printStackTrace();
                    }
                    finally {
                        stopReceive();
                    }
                }
            }
        }.start();
    }

    public void close(){
        mFlag = false;
    }
    
    private void stopReceive(){

        mFlag =false;

        mBean = null;

        if (mInputStream != null){
            try {
                mInputStream.close();

                mInputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (mSp !=null){
            mSp.close();
        }
    }
}
