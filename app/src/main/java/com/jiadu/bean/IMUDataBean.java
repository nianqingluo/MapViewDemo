package com.jiadu.bean;

public class IMUDataBean {

	public int[] linearAcc= new int[3];   //线加速度

	public int[] angleVelocity = new int[3];   //角速度

	public int[] magnet = new int[3];     //磁场

	public float[] pose = new float[3];   //姿态角

	public int pressure; 				  //气压
}
