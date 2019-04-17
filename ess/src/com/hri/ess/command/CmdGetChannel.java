package com.hri.ess.command;

import java.util.ArrayList;
import com.hri.ess.network.NetCmd;

public class CmdGetChannel extends CMD{
	
	public CmdGetChannel(){
		this.cmdCode = NetCmd.cmd_GetChannle;
	}

	@Override
	public byte[] dataToByte() {
		byte[] data = new byte[]{};
		return data;
	}
	
	public MsgGetChannel ParseToMsg(byte[] data){
		MsgGetChannel msgGetChannel = new MsgGetChannel();
		msgGetChannel.Parse(data);
		return msgGetChannel;		
	}
	
	public class MsgGetChannel extends Message{
		
       public ArrayList<String> channelList = new ArrayList<String>();
		protected void ParseData(byte[] data) {
			String Channel = new String(data);
			String[] channelItems = Channel.split(";");
			for(int i = 0 ; i < channelItems.length  ; i ++){
				channelList.add(channelItems[i]);
			}
			
		}
	}
}
