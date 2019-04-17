package com.hri.ess.businesslogic;

import com.hri.ess.network.SIVMClient;

import android.content.Context;

/**
 * 获取设备工作状�?(布撤�?
 * @param context
 */
public class ReadDeviceStateBusinessLogic  {
	
	private Context mcontext;
	private SIVMClient sivmClient = null;
   
	public ReadDeviceStateBusinessLogic(Context context){
		 mcontext = context;
	}
	
	/**
	 * 网络连接
	 * @param name
	 * @param psw
	 * @throws Exception
	 */
	public byte executionReadDeviceState() throws Exception{
		sivmClient = SIVMClient.getIntance(mcontext);	
		byte state = 5;
		try {
			state = sivmClient.ReadDeviceState();		
		} catch (Exception e) {
			throw e;
		}
		return state;
	}
}
