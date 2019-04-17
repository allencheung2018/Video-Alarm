package com.hri.ess.command;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

import com.hri.ess.network.NetCmd;
import com.hri.ess.util.Util;
/**
 * 读取�?��门记�?
 * @author yu
 *
 */
public class CmdReadEntryNote extends CMD{
	
	//通道号
	public byte channelNum;
	//开始时间
	public long startTime;
	//结束时间
	public long endTime;
	//页码(从1开始)
	static public int pageNum=1;
	//每页条数
	public int itemPerPage=10;
	//获取数据类型
	public int type;
	//4Byte 过滤标志
	public int flagFilter=0;
	//由返回值确定
	static public int itemTotal = 4;
	static public int pageTotal = 1;
	public int pageNumact = 1;

	
	public CmdReadEntryNote(){
		this.cmdCode = NetCmd.cmd_ReadEntryNote;
	}
	public byte[] dataToByte() {
		byte[] data = new byte[33];
		int index = 0 ;
		byte[] channelNumbytes = new byte[]{channelNum};
		byte[] startTimebytes = Util.LongToBytes(startTime);
		byte[] endTimebytes = Util.LongToBytes(endTime);
		int pageRead;
		if(pageNum > pageTotal && pageTotal > 0)
			pageRead = pageTotal;
		else
			pageRead = pageNum;
		Log.i("CmdReadEntryNote.dataToByte", "pageRead = "+pageRead + " pageTotal = "+pageTotal);
		byte[] pageNumbytes = Util.IntToBytes(pageRead);
		byte[] itemPerPagebytes = Util.IntToBytes(itemPerPage);
		byte[] flagFilterbytes = Util.IntToBytes(flagFilter);
		byte[] typebytes = Util.IntToBytes(type);
		
		System.arraycopy(channelNumbytes, 0,data  , index, channelNumbytes.length);
		index+= channelNumbytes.length;
		System.arraycopy(startTimebytes, 0,data  , index, startTimebytes.length);
		index+= startTimebytes.length;
		System.arraycopy(endTimebytes, 0,data  , index, endTimebytes.length);
		index+= endTimebytes.length;
		System.arraycopy(pageNumbytes, 0,data  , index, pageNumbytes.length);
		index+= pageNumbytes.length;
		System.arraycopy(itemPerPagebytes, 0,data  , index, itemPerPagebytes.length);
		index+= itemPerPagebytes.length;
		System.arraycopy(typebytes, 0,data  , index, typebytes.length);	
		index+= typebytes.length;
		System.arraycopy(flagFilterbytes, 0,data  , index, flagFilterbytes.length);
		index+= flagFilterbytes.length;
		return data;
	}
	
	
	public MsgReadEntryNote ParseToMsg(byte[] data){
		MsgReadEntryNote msgReadAlarmNote = new MsgReadEntryNote();
		msgReadAlarmNote.Parse(data);
		return msgReadAlarmNote;		
	}
	
	public class MsgReadEntryNote extends Message{

		public List<String> noteLists = new ArrayList<String>();
		protected void ParseData(byte[] data) {
			itemTotal = Util.getInt(data, 0);
			pageNumact = Util.getInt(data, 4);
			String noteList = Util.getString(data, 8, data.length - 8);
			String[] noteItem = noteList.split(";"); 
			for(int i = 0 ; i < noteItem.length;i++){
				noteLists.add(noteItem[i]);				
			}
			//读一次后加一页
			//读一次后加一页
			pageTotal = itemTotal/itemPerPage;
			if(itemTotal%itemPerPage != 0)
				pageTotal = pageTotal + 1;
			pageNum = pageNumact + 1;
		}
	}

}
