package com.hri.ess.command;

import com.hri.ess.network.NetCmd;


/**
 * 获取设备注册信息
 * @author zhuqian
 *
 */
public class GetDeviceRegisterInfoCmd extends CMD {

	
	public GetDeviceRegisterInfoCmd(){
		this.cmdCode = NetCmd.cmd_getDeviceRegisterInfo;
	}
	public byte[] dataToByte() {
		return null;
	}
	
	public GetDeviceRegisterInfoMsg ParseToMsg(byte[] data){
		GetDeviceRegisterInfoMsg msg = new GetDeviceRegisterInfoMsg();
		msg.Parse(data);
		return msg;
	}
	public class GetDeviceRegisterInfoMsg extends Message{
		public int videoType;
		protected void ParseData(byte[] data) {
			if(data.length>243){
				this.videoType = data[243];
				System.out.println("视频类型："+videoType);
			}
		}
	}
}
