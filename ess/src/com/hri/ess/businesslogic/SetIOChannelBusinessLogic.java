package com.hri.ess.businesslogic;

import com.hri.ess.network.SIVMClient;
import android.content.Context;
/**
 * 获取串口通道名称
 * @param context
 */
public class SetIOChannelBusinessLogic  {
	
	private Context mcontext;
	private SIVMClient sivmClient = null;
   
	public SetIOChannelBusinessLogic(Context context){
		 mcontext = context;
	}
	
	/**
	 * 网络连接
	 * @param name
	 * @param psw
	 * @return 
	 * @throws Exception
	 */
	public void executionSetIOChannel(byte channelNum,byte type) throws Exception{		
		sivmClient = SIVMClient.getIntance(mcontext);	
		try {
			sivmClient.SetIOChannel(channelNum,type);
		} catch (Exception e) {
			throw e;
		}
	}
	
	
}
