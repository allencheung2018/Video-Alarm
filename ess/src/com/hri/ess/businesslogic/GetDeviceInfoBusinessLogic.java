package com.hri.ess.businesslogic;


import java.util.List;

import com.hri.ess.network.SIVMClient;
import android.content.Context;
/**
 * 获取主设备信�?
 * @param context
 */
public class GetDeviceInfoBusinessLogic  {
	
	private Context mcontext;
	private SIVMClient sivmClient = null;
	private String filename = "deviceCode";
   
	public GetDeviceInfoBusinessLogic(Context context){
		 mcontext = context;
	}
	
	/**
	 * 获取主设备码
	 * @param name
	 * @param psw
	 * @throws Exception
	 */
	public String executionGetDevice() throws Exception{
		String deviceCode = "";
		sivmClient = SIVMClient.getIntance(mcontext);	
		try {
			List<String> deviceList = sivmClient.getDeviceList();
			if(deviceList !=null && deviceList.size()>0){
				deviceCode = deviceList.get(0);
				System.out.println("设备码："+deviceCode);
			}
		} catch (Exception e) {
			throw e;
		}
		return deviceCode;
	}
}
