package com.hri.ess.businesslogic;

import java.util.ArrayList;
import java.util.List;
import com.hri.ess.network.SIVMClient;

import android.content.Context;
/**
 * 获取设备工作状�?(布撤�?
 * @param context
 */
public class ReadAlarmNoteBusinessLogic  {
	
	private Context mcontext;
	private SIVMClient sivmClient = null;
   
	public ReadAlarmNoteBusinessLogic(Context context){
		 mcontext = context;
	}
	
	/**
	 * 网络连接
	 * @param name
	 * @param psw
	 * @throws Exception
	 */
	public List<String> executionReadAlarmNote(byte channel,byte type
			,String startTime,String endTime) throws Exception{
		sivmClient = SIVMClient.getIntance(mcontext);	
		List<String> alarmNoteList = new ArrayList<String>();
		try {
			 alarmNoteList = sivmClient.ReadAlarmNote(channel,type,startTime,endTime);		
		} catch (Exception e) {
			throw e;
		}
		return alarmNoteList;
	}
	
	/**
	 * 网络连接
	 * @param name
	 * @param psw
	 * @throws Exception
	 */
	public List<String> executionReadEntryNote(byte channel,byte type
			,String startTime,String endTime) throws Exception{
		sivmClient = SIVMClient.getIntance(mcontext);	
		List<String> entryNoteList = new ArrayList<String>();
		try {
			entryNoteList = sivmClient.ReadEntryNote(channel,type,startTime,endTime);		
		} catch (Exception e) {
			throw e;
		}
		return entryNoteList;
	}
	
}
