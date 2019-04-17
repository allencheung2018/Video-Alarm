package com.hri.ess.command;

import java.io.UnsupportedEncodingException;

import com.hri.ess.network.NetCmd;


public class CmdPublishMsg extends CMD{
	
	//订阅ID
	public String publishId = "";
	//用户�?
	public String userName = "";
	//用户密码
	public String userPass = "";
	//消息类型
	public String msgType = "";
	//范围
	public String Range = "";
	//事件
	public String Event = "";
	
	
	public CmdPublishMsg(){
		this.cmdCode = NetCmd.cmd_PublishMsg;	
	}
	public byte[] dataToByte() {
		byte[] data = null;
		try{
		String publishMsgHead = publishId + "|"+userName+"|"+userPass+"|";
		String publishMsg = msgType + ";" + Range +";"+Event;
		String dataStr = publishMsgHead + publishMsg;
		data = dataStr.getBytes("UTF8");
		}catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return data;
	}
	
	public MsgPublishMsg ParseToMsg(byte[] data){
		MsgPublishMsg msgPublishMsg= new MsgPublishMsg();
		msgPublishMsg.Parse(data);
		return msgPublishMsg;		
	}
	
	public class MsgPublishMsg extends Message{
		protected void ParseData(byte[] data) {
		
		}
	}

	

}
