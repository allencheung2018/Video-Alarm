package com.hri.ess.businesslogic;

import java.util.ArrayList;
import java.util.List;
import com.hri.ess.network.SIVMClient;
import android.content.Context;
/**
 * 获取通道名称
 * @param context
 */
public class GetChannelBusinessLogic  {
	
	private Context mcontext;
	private SIVMClient sivmClient = null;
   
	public GetChannelBusinessLogic(Context context){
		 mcontext = context;
	}
	
	/**
	 * 网络连接
	 * @param name
	 * @param psw
	 * @return 
	 * @throws Exception
	 */
	public ArrayList<String> executionGetChannel() throws Exception{
		ArrayList<String> streamList = new ArrayList<String>();
		sivmClient = SIVMClient.getIntance(mcontext);	
		try {
			streamList = sivmClient.GetChannel();
		} catch (Exception e) {
			throw e;
		}
		return streamList;
	}
	
	
}
