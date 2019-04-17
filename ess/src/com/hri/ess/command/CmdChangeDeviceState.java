package com.hri.ess.command;

import com.hri.ess.network.NetCmd;


/**
 * 变更设备状�?（布防或撤防�?
 * @author yu
 *
 */
public class CmdChangeDeviceState extends CMD{
	
	//设备状�?
	public byte deviceState;
	
	public CmdChangeDeviceState(){
		this.cmdCode = NetCmd.cmd_ChangeDeviceState;
	}

	@Override
	public byte[] dataToByte() {
		byte[] data = new byte[]{deviceState};
		return data;
	}
	
	public MsgChangeDeviceState ParseToMsg(byte[] data){
		MsgChangeDeviceState msgChangeDeviceState = new MsgChangeDeviceState();
		msgChangeDeviceState.Parse(data);
		return msgChangeDeviceState;		
	}
	
	public class MsgChangeDeviceState extends Message{

		@Override
		protected void ParseData(byte[] data) {
			
		}
	}

}
