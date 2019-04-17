package com.hri.ess.command;

import java.io.UnsupportedEncodingException;

import com.hri.ess.dbservice.domain.DeviceInfo;
import com.hri.ess.network.NetCmd;

/**
 * 读取设备信息
 * @author yu
 *
 */
public class CmdDeviceInfo extends CMD{
	public String deviceCode = "";
	
     public CmdDeviceInfo(){
    	 this.cmdCode = NetCmd.cmd_GetDeviceInfo;
     }
	
	@Override
	public byte[] dataToByte() {
		byte[] mData = null;
		try {
			mData = deviceCode.getBytes("UTF8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return mData;
	}
	
	public MsgDeviceInfo ParseToMsg(byte[] data){
		MsgDeviceInfo msgDeviceInfo = new MsgDeviceInfo();
		msgDeviceInfo.Parse(data);
		return msgDeviceInfo;		
	}
	
	public class MsgDeviceInfo extends Message{

		public String[] deviceInfo;	
		@Override
		protected void ParseData(byte[] data) {
			String dataStr = new String(data);
			deviceInfo = dataStr.split(";");
			DeviceInfo.deviceDate = deviceInfo[0];
			DeviceInfo.deviceIsOnline = deviceInfo[1];
			DeviceInfo.deviceVersion = deviceInfo[2];
			DeviceInfo.sellTime = deviceInfo[3];
			DeviceInfo.initAddress = deviceInfo[4];
			DeviceInfo.deviceGps = deviceInfo[5];
			DeviceInfo.deviceType = deviceInfo[6];
			DeviceInfo.deviceType = deviceInfo[7];
			DeviceInfo.deviceName = deviceInfo[8];
//			DeviceInfo.deviceTip = deviceInfo[9];
		}		
	}

}
