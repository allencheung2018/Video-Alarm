package com.hri.ess.command;

import java.io.UnsupportedEncodingException;

import com.hri.ess.network.NetCmd;

/**
 * 获取设备信息
 * @author yu
 *
 */
public class CmdReadDeviceInfo extends CMD {
	
	public String deviceCode = "";
	
	public CmdReadDeviceInfo(){
		this.cmdCode = NetCmd.cmd_ReadDeviceInfo;
	}

	@Override
	public byte[] dataToByte() {
		byte[] data = null;
		try {
			data =  deviceCode.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return data;
	}
	
	public MsgReadDeviceInfo ParseToMsg(byte[] data){
		MsgReadDeviceInfo msgReadDeviceInfo = new MsgReadDeviceInfo();
		msgReadDeviceInfo.Parse(data);
		return msgReadDeviceInfo;		
	}
	
	public class MsgReadDeviceInfo extends Message{

		public String[] deviceInfo;
		
		@Override
		protected void ParseData(byte[] data) {
			try {
				String deviceInfos = new String(data,"UTF-8");
				deviceInfo = deviceInfos.split(";");				
			} catch (UnsupportedEncodingException e) {				
				e.printStackTrace();
			}
		}
	}
		

}
