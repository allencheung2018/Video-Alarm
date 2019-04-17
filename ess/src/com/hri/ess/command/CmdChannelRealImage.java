package com.hri.ess.command;

import com.hri.ess.command.CmdAlarmDetailed.MsgAlarmDetailed;
import com.hri.ess.network.NetCmd;

public class CmdChannelRealImage extends CMD{

	public byte chNum;
	
	public CmdChannelRealImage(){
		this.cmdCode = NetCmd.cmd_getChannelImg;
	}
	
	@Override
	public byte[] dataToByte() {
		byte[] data = new byte[]{chNum};
		return data;
	}
	
	public MsgChannelRealImage ParseToMsg(byte[] data){
		MsgChannelRealImage msgChRealImg = new MsgChannelRealImage();
		msgChRealImg.Parse(data);
		return msgChRealImg;		
	}

	public class MsgChannelRealImage extends Message{

		public byte[] realImg;
		@Override
		protected void ParseData(byte[] data) {
			realImg = data;
			
		}
	}
}
