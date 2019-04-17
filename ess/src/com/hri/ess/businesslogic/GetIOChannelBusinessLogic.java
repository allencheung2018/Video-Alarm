package com.hri.ess.businesslogic;

import com.hri.ess.dbservice.domain.IOchannelInfo;
import com.hri.ess.network.SIVMClient;

import android.content.Context;
/**
 * 获取串口通道名称
 * @param context
 */
public class GetIOChannelBusinessLogic  {
	
	private Context mcontext;
	private SIVMClient sivmClient = null;
   
	public GetIOChannelBusinessLogic(Context context){
		 mcontext = context;
	}
	
	/**
	 * 网络连接
	 * @param name
	 * @param psw
	 * @return 
	 * @throws Exception
	 */
	public IOchannelInfo executionGetIOChannel(byte channelNum) throws Exception{
		IOchannelInfo IOchannel;
		sivmClient = SIVMClient.getIntance(mcontext);	
		try {
			IOchannel = sivmClient.GetIOChannel(channelNum);
		} catch (Exception e) {
			throw e;
		}
		return IOchannel;
	}
	
	
}
