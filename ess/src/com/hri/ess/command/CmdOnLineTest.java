package com.hri.ess.command;

import com.hri.ess.network.NetCmd;


public class CmdOnLineTest extends CMD {
	
	public CmdOnLineTest(){
		this.cmdCode = NetCmd.cmd_OnLine_Test;
	}
	public byte[] dataToByte() {
		byte[] data = new byte[]{0};
		return data;
	}
	
	public MsgOnLineTest ParseToMsg(byte[] data){
		MsgOnLineTest msgOnLineTest = new MsgOnLineTest();
		msgOnLineTest.Parse(data);
		return msgOnLineTest;		
	}
	
	public class MsgOnLineTest extends Message{		
		protected void ParseData(byte[] data) {		
		}
	}

}
