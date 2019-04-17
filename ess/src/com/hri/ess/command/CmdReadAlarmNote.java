package com.hri.ess.command;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hri.ess.network.NetCmd;
import com.hri.ess.util.Util;
/**
 * 读取�?��门记�?
 * @author yu
 *
 */
public class CmdReadAlarmNote extends CMD{
	
	//通道�?
	public byte channelNum;
	//�?��时间
	public long startTime;
	//结束时间
	public long endTime;
	//获取数据类型
	public byte type;

	
	public CmdReadAlarmNote(){
		this.cmdCode = NetCmd.cmd_ReadAlarmNote;
	}
	public byte[] dataToByte() {
		byte[] data = new byte[18];
		int index = 0 ;
		byte[] channelNumbytes = new byte[]{channelNum};
		byte[] startTimebytes = Util.LongToBytes(startTime);
		byte[] endTimebytes = Util.LongToBytes(endTime);
		byte[] typebytes = new byte[]{type};
		
		System.arraycopy(channelNumbytes, 0,data  , index, channelNumbytes.length);
		index+= channelNumbytes.length;
		System.arraycopy(startTimebytes, 0,data  , index, startTimebytes.length);
		index+= startTimebytes.length;
		System.arraycopy(endTimebytes, 0,data  , index, endTimebytes.length);
		index+= endTimebytes.length;
		System.arraycopy(typebytes, 0,data  , index, typebytes.length);		
		return data;
	}
	
	
	public MsgReadAlarmNote ParseToMsg(byte[] data){
		MsgReadAlarmNote msgReadAlarmNote = new MsgReadAlarmNote();
		msgReadAlarmNote.Parse(data);
		return msgReadAlarmNote;		
	}
	
	public class MsgReadAlarmNote extends Message{

		public List<String> noteLists = new ArrayList<String>();
		protected void ParseData(byte[] data) {
			String noteList = new String(data);
			String[] noteItem = noteList.split(";"); 
			for(int i = 0 ; i < noteItem.length;i++){
				noteLists.add(noteItem[i]);				
			}
		}
	}

}
