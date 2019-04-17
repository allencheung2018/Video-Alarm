package com.hri.ess.businesslogic;

import com.hri.ess.network.SIVMClient;
import android.content.Context;
/**
 * 设置设备工作状�?(布撤�?
 * @param context
 */
public class SendClientOnLineBussinessLogic  {
	
	private Context mcontext;
	private SIVMClient sivmClient = null;
   
	public SendClientOnLineBussinessLogic(Context context){
		 mcontext = context;
		 sivmClient = SIVMClient.getIntance(mcontext);	
	}
	
	/**
	 * 网络连接
	 * @param name
	 * @param psw
	 * @throws Exception
	 */
	public void executionSendClientOnline() throws Exception{
		try {
			 sivmClient.SendClientOnline();
			 
		} catch (Exception e) {			
			throw e;
		}
	}
}
