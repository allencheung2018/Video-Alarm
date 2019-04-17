package com.hri.ess.businesslogic;

import com.hri.ess.network.SIVMClient;

import android.content.Context;
/**
 * 获取设备信息
 * @param context
 */
public class ReadDeviceInfoBusinessLogic  {
	
	private Context mcontext;
	private SIVMClient sivmClient = null;
   
	public ReadDeviceInfoBusinessLogic(Context context){
		 mcontext = context;
	}
	
	/**
	 * 网络连接
	 * @param name
	 * @param psw
	 * @throws Exception
	 */
	public String[]  executionReadDeviceInfo(String deviceCode) throws Exception{
		sivmClient = SIVMClient.getIntance(mcontext);	
		String[]  state ;
		try {
			state = sivmClient.ReadDeviceInfo(deviceCode);		
		} catch (Exception e) {
			throw e;
		}
		return state;
	}
	
	
}
