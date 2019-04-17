package com.hri.ess.command;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import com.hri.ess.dbservice.domain.AlarmDetailed;
import com.hri.ess.network.NetCmd;
import com.hri.ess.util.Util;

/**
 * 获取报警明细
 * @author yu
 *
 */
public class CmdAlarmDetailed extends CMD{

	public String alarmId;
	
	public String imgWidth;
	
	public String imgHeight;
	
	public CmdAlarmDetailed(){
		this.cmdCode = NetCmd.cmd_ReadAlarmDetailed;
	}
	
	public byte[] dataToByte() {
		byte[] data = null;
		String info = alarmId+","+imgWidth+","+imgHeight;
		try {
			data = info.getBytes("UTF8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return data;
	}
	
	public MsgAlarmDetailed ParseToMsg(byte[] data){
		MsgAlarmDetailed msgAlarmDetailed = new MsgAlarmDetailed();
		msgAlarmDetailed.Parse(data);
		return msgAlarmDetailed;		
	}
	
	public class MsgAlarmDetailed extends Message{

		private String alarmId;
		
		private String channelName;
		
		private byte state;
		
		private byte alarmType;
		
		private byte channelNum;
		
		private byte isAlarm;
		
		private long alarmTime;
		
		private byte[] alarmImg;
		/**
		 * 明细信息
		 */
		public AlarmDetailed alarmDetailed;
		
		@Override
		protected void ParseData(byte[] data) {
			byte[] alarmIdbytes = Util.getBytes(data, 0, 36);
			alarmId = new String(alarmIdbytes);
			byte[] channelNamebytes = Util.getBytes(data, 36, 16);
			channelName = new String(channelNamebytes);
			state = data[52];
			alarmType = data[53];
			channelNum = data[54];
			isAlarm = data[55];
			byte[] alarmTimebytes = Util.getBytes(data, 56, 8);
			alarmTime = Util.bytesToLong(alarmTimebytes, true);
			alarmImg = Util.getBytes(data, 64, data.length-64);
			alarmDetailed = new AlarmDetailed();
			alarmDetailed.setAlarmId(alarmId);
			alarmDetailed.setChannelName(channelName);
			alarmDetailed.setState(state) ;
			alarmDetailed.setAlarmType(alarmType);
			alarmDetailed.setChannelNum(channelNum);
			alarmDetailed.setIsAlarm(isAlarm);
			SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
			
			alarmDetailed.setAlarmtime(formatter.format(Util.fileTime2Date(alarmTime)));
			alarmDetailed.setAlarmImg(alarmImg);		
		}
	}

}
