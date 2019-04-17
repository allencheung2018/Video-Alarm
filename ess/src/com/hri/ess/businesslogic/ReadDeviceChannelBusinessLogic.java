package com.hri.ess.businesslogic;

import com.hri.ess.network.SIVMClient;

import android.content.Context;
/**
 * 获取设备联动�?
 * @param context
 */
public class ReadDeviceChannelBusinessLogic  {
	
	private Context mcontext;
	private SIVMClient sivmClient = null;
   
	public ReadDeviceChannelBusinessLogic(Context context){
		 mcontext = context;
	}
	
	/**
	 * 网络连接
	 * @param name
	 * @param psw
	 * @throws Exception
	 */
	public byte  executionReadDeviceChannel() throws Exception{
		sivmClient = SIVMClient.getIntance(mcontext);	
		byte  state ;
		try {
			state = sivmClient.ReadDeviceChannel();		
		} catch (Exception e) {
			throw e;
		}
		return state;
	}
	
	
}
