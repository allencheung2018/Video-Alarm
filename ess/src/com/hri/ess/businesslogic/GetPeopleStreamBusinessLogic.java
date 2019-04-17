package com.hri.ess.businesslogic;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.hri.ess.command.PeopleStreamInfo;
import com.hri.ess.network.SIVMClient;
/**
 * ��ȡ��������ͳ��
 * @param context
 */
public class GetPeopleStreamBusinessLogic  {
	
	private Context mcontext;
	private SIVMClient sivmClient = null;
   
	public GetPeopleStreamBusinessLogic(Context context){
		 mcontext = context;
	}
	
	/**
	 * ��������
	 * @param name
	 * @param psw
	 * @return 
	 * @throws Exception
	 */
	public List<PeopleStreamInfo> executionGetPeopleStream(byte channel,String Time,byte type,int num,int cycle,byte unit) throws Exception{
		List<PeopleStreamInfo> streamList = new ArrayList<PeopleStreamInfo>();
		sivmClient = SIVMClient.getIntance(mcontext);	
		try {
			streamList = sivmClient.GetPeopleStream(channel, Time, type, num, cycle, unit);
		} catch (Exception e) {
			throw e;
		}
		return streamList;
	}
	
	
}
