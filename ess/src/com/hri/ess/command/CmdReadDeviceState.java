package com.hri.ess.command;

import com.hri.ess.network.NetCmd;


/**
 * 读取设备当前布防状�?
 * @author yu
 *
 */
public class CmdReadDeviceState extends CMD{
	
	public CmdReadDeviceState(){
		this.cmdCode = NetCmd.cmd_ReadDeviceState;
	}

	@Override
	public byte[] dataToByte() {
		byte[] data = new byte[]{};
		return data;
	}
	
	public MsgReadDeviceState ParseToMsg(byte[] data){
		MsgReadDeviceState msgReadDeviceState = new MsgReadDeviceState();
		msgReadDeviceState.Parse(data);
		return msgReadDeviceState;		
	}
	
	public class MsgReadDeviceState extends Message{

		public byte state;
		@Override
		protected void ParseData(byte[] data) {
			state = data[0];
		}
	}


}
