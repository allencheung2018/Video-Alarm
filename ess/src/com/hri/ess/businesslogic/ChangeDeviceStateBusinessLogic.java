package com.hri.ess.businesslogic;

import com.hri.ess.network.SIVMClient;

import android.content.Context;
/**
 * 设置设备工作状�?(布撤�?
 * @param context
 */
public class ChangeDeviceStateBusinessLogic  {
	
	private Context mcontext;
	private SIVMClient sivmClient = null;
   
	public ChangeDeviceStateBusinessLogic(Context context){
		 mcontext = context;
	}
	
	/**
	 * 网络连接
	 * @param name
	 * @param psw
	 * @throws Exception
	 */
	public void executionChangeDeviceState(byte type) throws Exception{
		sivmClient = SIVMClient.getIntance(mcontext);	
		try {
			sivmClient.ChangeDeviceState(type);	
		} catch (Exception e) {
			throw e;
		}
	}
}
