package com.hri.ess.command;

import java.util.ArrayList;
import java.util.List;

import com.hri.ess.network.NetCmd;

/**
 * 获取设备列表
 * @author yu
 *
 */
public class CmdGetDeviceList extends CMD{
	
	public String userName;
	public String userPass;
	
	public CmdGetDeviceList(){
		this.cmdCode = NetCmd.cmd_GetDeciceList;
		
	}

	@Override
	public byte[] dataToByte() {
		byte[] mData = new byte[32];
		int index = 0;
		try{
		byte[] nameBytes = new byte[16];
		byte[] name = userName.getBytes("UTF8");
		System.arraycopy(name, 0, nameBytes, 0, name.length);	
		//密码转换
		byte[] pswBytes = new byte[16];
		byte[] psw = userPass.getBytes("UTF8");
		System.arraycopy(psw, 0, pswBytes, 0, psw.length);
		//用户名填�?
		System.arraycopy(nameBytes, 0, mData,index, nameBytes.length);
		index += nameBytes.length;
		//用户密码填充
		System.arraycopy(pswBytes, 0, mData,index, pswBytes.length);
		index +=  pswBytes.length;
		}catch(Exception e){
			e.printStackTrace();
		}		
		return mData;
	}
	
	public MsgGetDeviceList ParseToMsg(byte[] data){
		MsgGetDeviceList msgDeviceList = new MsgGetDeviceList();
		msgDeviceList.Parse(data);
		return msgDeviceList;		
	}
	

	public class MsgGetDeviceList extends Message{

		public List<String> deviceList = new ArrayList<String>();
		
		protected void ParseData(byte[] data) {
			String[] deviceStrs = new String(data).split(";");
			for(int i = 0 ; i<deviceStrs.length;i++ ){
				deviceList.add(deviceStrs[i]);
			}
		}
	}
		

}
