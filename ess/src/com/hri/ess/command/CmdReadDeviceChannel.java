package com.hri.ess.command;

import com.hri.ess.network.NetCmd;
import com.hri.ess.util.Util;


/**
 * 获取串口设备信息
 * @author yu
 *
 */
public class CmdReadDeviceChannel extends CMD {
	
	public CmdReadDeviceChannel(){
		this.cmdCode = NetCmd.cmd_ReadDeviceChannel;
	}

	@Override
	public byte[] dataToByte() {
		byte[] data = new byte[]{};
		return data;
	}
	
	public MsgReadDeviceChannel ParseToMsg(byte[] data){
		MsgReadDeviceChannel msgReadDeviceInfo = new MsgReadDeviceChannel();
		msgReadDeviceInfo.Parse(data);
		return msgReadDeviceInfo;		
	}
	
	public class MsgReadDeviceChannel extends Message{	
		//设备�?
		public String deviceCode;
		//联动输入口数
		public byte IOchannelNum;
		@Override
		protected void ParseData(byte[] data) {
		byte[] devicebytes = Util.getBytes(data, 0, 32);
		deviceCode = new String(devicebytes);
		IOchannelNum = data[42];
		}
	}
		

}
