package com.hri.ess.command;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;

import com.hri.ess.util.Util;

/**
 * 收到报警数据包解�?
 * @author yu
 *
 */
public class AnswerMsgAlarm extends PushMessage{
	
	public String alarmId;//报警id
	
	public byte alarmType;//报警类型
	
	public byte alarmChannel;//通道�?
	
	public String alarmTime;//报警时间
	
	public byte[] alarmImg;//报警图片
	
	public String x;
	
	public String y;
	
	public String readtxt;
	
	public boolean isAlarm;
	
	public AnswerMsgAlarm(){
		
	}
	protected void ParseData(byte[] data) {
		alarmId = Util.getString(data, 0, 36);
		if((((int)(data[36]))&0x80)==0){
			//提示触发
			isAlarm = false;
		}else{
			isAlarm = true;
		}
		alarmType = (byte) (data[36]&0x7f);
		alarmChannel = data[37];
		byte[] time = new byte[]{data[38],data[39],data[40],data[41],data[42],data[43],data[44],data[45]};
		SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		alarmTime =sdf.format(Util.fileTime2Date(Util.bytesToLong(time,true)));
		if(alarmType == 109 || alarmType == 111){
			byte[] xByte=new byte[8];
			byte[] yByte=new byte[8];
			System.arraycopy(data,46, xByte, 0,8);			
			System.arraycopy(data,54, yByte, 0,8);
			x = Double.longBitsToDouble(Util.bytesToLong(xByte, true))+"";
		    y = Double.longBitsToDouble(Util.bytesToLong(yByte, true))+"";
		    byte[] readByte = new byte[data.length-62];
		    System.arraycopy(data, 62, readByte, 0, readByte.length);
		    try {
				readtxt = new String(readByte, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}else{
		alarmImg = new byte[data.length - 46];
		System.arraycopy(data, 46, alarmImg, 0, data.length-46);
		}
	}
	
	public AlarmAnswer MsgToAnswer(){
		AlarmAnswer answer = new AlarmAnswer();
		answer.cmdCode = 0;
		answer.cmdId = this.cmdId;
		answer.loginID = Util.IntToBytes(this.loginId);
		return answer;
		
	}
	
	public class AlarmAnswer extends AnswerCmd{

		@Override
		public byte[] dataToByte() {
			byte[] data = new byte[]{};
			return data;
		}
		
	}


}


