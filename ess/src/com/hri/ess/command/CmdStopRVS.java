package com.hri.ess.command;

import com.hri.ess.network.NetCmd;


public class CmdStopRVS extends CMD{
	
	public CmdStopRVS(){
		this.cmdCode = NetCmd.cmd_StopRvs;
	}

	@Override
	public byte[] dataToByte() {
		byte[] data = new byte[]{};
		return data;
	}
	
	public MsgStopRVS ParseToMsg(byte[] data){
		MsgStopRVS msgStop = new MsgStopRVS();
		msgStop.Parse(data);
		return msgStop;
		
	}
	
	
	public class MsgStopRVS extends Message{
		protected void ParseData(byte[] data) {
			
		}
		
	}

}
