package com.jiadu.mapdemo.util;

import com.jiadu.bean.IMUDataBean;

public class IMUDataUtil {

	private static final byte FIELD_BEGIN = (byte) 0X90;			 //包头
	private static final byte FIELD_LINEAR_ACCELERATE = (byte) 0XA0; //线加速度
	private static final byte FIELD_ANGLE_VELOCITY = (byte) 0XB0;    //角速度
	private static final byte FIELD_MAGNET = (byte) 0XC0;			 //磁场
	private static final byte FIELD_RPY = (byte) 0XD0;				 //姿态角
	private static final byte FIELD_4META = (byte) 0XD1;			 //4元数
	private static final byte FIELD_PRESURE = (byte) 0XF0;			 //气压

	public static IMUDataBean getData(byte[] bytes , int len){
		
		IMUDataBean bean = new IMUDataBean();
		
		int offset = 0;
		
		while(offset < len){
			
			byte cmd = bytes[offset];
			
			switch (cmd) {
			
			case 0x5A:			//帧头识别
				
				if (0xA5== bytes[offset+1]) {
					
					offset = offset +6;
					
				}
				
				break;
			case FIELD_BEGIN://包头
				
				offset = offset +2;
				
				break;
				
			case FIELD_LINEAR_ACCELERATE: //线加速度
				
				bean.linearAcc[0] = (bytes[offset+2]<<8) +bytes[offset+1];
				bean.linearAcc[1] = (bytes[offset+4]<<8) +bytes[offset+3];
				bean.linearAcc[2] = (bytes[offset+6]<<8) +bytes[offset+5];
				
				offset = offset +7;
				
				break;
			case FIELD_ANGLE_VELOCITY:   //角速度
				
				bean.angleVelocity[0] = (bytes[offset+2]<<8) +bytes[offset+1];
				bean.angleVelocity[1] = (bytes[offset+4]<<8) +bytes[offset+3];
				bean.angleVelocity[2] = (bytes[offset+6]<<8) +bytes[offset+5];
				
				offset = offset +7;
				
				break;
			case FIELD_MAGNET:           //磁场强度
				bean.magnet[0] = (bytes[offset+2]<<8) +bytes[offset+1];
				bean.magnet[1] = (bytes[offset+4]<<8) +bytes[offset+3];
				bean.magnet[2] = (bytes[offset+6]<<8) +bytes[offset+5];
				offset = offset +7;		
				
				break;
			case FIELD_RPY:				//姿态角
				bean.pose[0] = (float) (((bytes[offset+2]<<8) +bytes[offset+1])*1.0/100);
				bean.pose[1] = (float) (((bytes[offset+4]<<8) +bytes[offset+3])*1.0/100);
				bean.pose[2] = (float) (((bytes[offset+6]<<8) +bytes[offset+5])*1.0/10);
				
				offset = offset +7;		
				
				break;
			case FIELD_4META:			//4元素
				
				offset = offset+17;
				
				break;
				
			case FIELD_PRESURE:        //气压
				
				bean.pressure = bytes[offset+1] + bytes[offset+2]<<8+bytes[offset+3]<<16+bytes[offset+4]<<24;
				
				offset =offset +5;
				
				break;

			default:
				return bean;
			}
		}
		return bean;
	}
}
