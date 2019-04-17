package com.hri.ess.command;

import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.UUID;
import com.hri.ess.network.NetCmd;
import com.hri.ess.util.Util;

/**
 * 登陆指令封装
 * @author yu
 *
 */
public class CmdLogin extends CMD{	
	
	//用户名，密码
	public String namestr,passwordstr;
	//接受状�?事件
	public Boolean fStatusChanged=true,fRealAlarm=true,fAnyAlarm=false,fEntry=true,
			             fDebug=false,flag5=false,flag6=false,flag7=false;
	//设备码，随机�?
	public String deviceCode = "";
	
	private String ranNO ="";
	
	public CmdLogin (String name,String password,String code){	
		this.loginID = new byte[]{0,0,0,0};
		this.cmdCode = NetCmd.cmd_Login;
		namestr = name;
		passwordstr = password;
		String str= UUID.randomUUID().toString();		
		deviceCode =code;
		ranNO = str.substring(32,str.length());
	}


	public byte[] dataToByte() {		
		byte[] mData = null;
		int index = 0;
		try {
			//用户名转�?
			byte[] nameBytes = new byte[16];
			byte[] name = namestr.getBytes("UTF8");
			System.arraycopy(name, 0, nameBytes, 0, name.length);	
			//密码转换
			byte[] pswBytes = new byte[16];
			byte[] psw = passwordstr.getBytes("UTF8");
			System.arraycopy(psw, 0, pswBytes, 0, psw.length);	
			//设备码转�?
			//数据数组
			mData = new byte[69];	
			//用户名填�?
			System.arraycopy(nameBytes, 0, mData,index, nameBytes.length);
			index += nameBytes.length;
			//用户密码填充
			System.arraycopy(pswBytes, 0, mData,index, pswBytes.length);
			index +=  pswBytes.length;
			//状�?码填�?		
			byte flag = (byte)Util.toByte(new Boolean[]{flag7,flag6,flag5,fDebug,fEntry,fAnyAlarm,fRealAlarm,fStatusChanged});
			System.arraycopy(new byte[]{flag}, 0, mData, index, 1);
			index +=1;
			byte[] deviceCodeBytes = deviceCode.getBytes("UTF-8");
			System.arraycopy(deviceCodeBytes, 0, mData, index, deviceCodeBytes.length);
			//随机码填�?
			index += deviceCodeBytes.length;
			System.out.println("index="+index);
			byte[] ranNOBytes = Util.IntToBytes((new Random()).nextInt());
			System.arraycopy(ranNOBytes, 0, mData, index, ranNOBytes.length);							
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return mData;
	}
	

	
	public MsgLogin ParseToMsg(byte[] data){
		MsgLogin msgLogin = new MsgLogin();
		msgLogin.Parse(data);
		return msgLogin;		
	}
	
	public class MsgLogin extends Message{

		public byte rights;
		public byte[] userId;
		public int ranNo;
		public String deviceCode,time;		
		
		protected void ParseData(byte[] data) {
			rights =data[0];
			userId = new byte[]{data[1],data[2],data[3],data[4]};
			deviceCode = Util.getString(data, 5, 32);
			ranNo = Util.getInt(data, 37);
			time = Util.getString(data, 41, 8);
		}
	}
		
}
