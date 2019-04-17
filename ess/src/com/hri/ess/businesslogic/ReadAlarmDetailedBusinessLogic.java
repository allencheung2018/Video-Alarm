package com.hri.ess.businesslogic;

import com.hri.ess.dbservice.domain.AlarmDetailed;
import com.hri.ess.network.SIVMClient;

import android.content.Context;
/**
 * 获取报警明细
 * @param context
 */
public class ReadAlarmDetailedBusinessLogic  {
	
	private Context mcontext;
	private SIVMClient sivmClient = null;
   
	public ReadAlarmDetailedBusinessLogic(Context context){
		 mcontext = context;
	}
	
	/**
	 * 网络连接
	 * @param name
	 * @param psw
	 * @throws Exception
	 */
	public AlarmDetailed executionReadAlarmDetailed(String alarmId) throws Exception{
		sivmClient = SIVMClient.getIntance(mcontext);	
		AlarmDetailed	 alarmInfo;
		try {
			alarmInfo = sivmClient.ReadAlarmDetailed(alarmId);		
		} catch (Exception e) {
			throw e;
		}
		return alarmInfo;
	}
	
	
}
