package com.hri.ess.command;

import com.hri.ess.network.NetCmd;


/**
 * 设置联动口状�?
 * @author yu
 *
 */
public class CmdSetIOChannelState extends CMD{
	
	public byte IOchannelNum;
	
	public byte type;
	
	public CmdSetIOChannelState(){
	   this.cmdCode = NetCmd.cmd_SetIOchannelState;
		
	}

	@Override
	public byte[] dataToByte() {
		byte[] data = new byte[]{IOchannelNum,type};
		return data;
	}
	
	public MsgSetIOChannelState ParseToMsg(byte[] data){
		MsgSetIOChannelState msgSetIOChannelState = new MsgSetIOChannelState();
		msgSetIOChannelState.Parse(data);
		return msgSetIOChannelState;		
	}
	
	public class MsgSetIOChannelState extends Message{

		
		@Override
		protected void ParseData(byte[] data) {
			
		}
	}

}
