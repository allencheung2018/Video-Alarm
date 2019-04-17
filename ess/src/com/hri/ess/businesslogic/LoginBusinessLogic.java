package com.hri.ess.businesslogic;

import com.hri.ess.network.SIVMClient;
import android.content.Context;

/**
 * 登陆业务逻辑
 * @param context
 */
public class LoginBusinessLogic  {
	
	private Context mcontext;
	private SIVMClient sivmClient = null;
   
	public LoginBusinessLogic(Context context){
		 mcontext = context;
	}
	
	/**
	 * 登陆网络连接
	 * @param name
	 * @param psw
	 * @throws Exception
	 */
	public void executionLogin(String username,String pwd) throws Exception{
		sivmClient = SIVMClient.getIntance(mcontext);	
		try {
			sivmClient.login(username,pwd);			
		} catch (Exception e) {
			throw e;
		}
	}
	
	
}
