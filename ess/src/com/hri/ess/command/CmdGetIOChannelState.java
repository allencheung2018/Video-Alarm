package com.hri.ess.command;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import com.hri.ess.dbservice.domain.IOchannelInfo;
import com.hri.ess.network.NetCmd;
import com.hri.ess.util.Util;

/**
 * 获取联动口状�?
 * @author yu
 *
 */
public class CmdGetIOChannelState extends CMD{
	
	public byte IOchannelNum;
	
	public CmdGetIOChannelState(){
	   this.cmdCode = NetCmd.cmd_GetIOchannelState;
		
	}

	@Override
	public byte[] dataToByte() {
		byte[] data = new byte[]{IOchannelNum};
		return data;
	}
	
	public MsgGetIOChannelState ParseToMsg(byte[] data){
		MsgGetIOChannelState msgGetIOChannelState = new MsgGetIOChannelState();
		msgGetIOChannelState.Parse(data);
		return msgGetIOChannelState;		
	}
	
	public class MsgGetIOChannelState extends Message{

		//当前状�?
		private byte IOchannelState;
		//是否允许手动操作
		private byte isCanDo;
		//联动口名�?
		private String IOchannelName;
		
		public IOchannelInfo IOchannel;
		
		protected void ParseData(byte[] data) {
			IOchannelState = data[0];
			isCanDo = data[1];
			byte[] namebytes = Util.getBytes(data, 2, data.length-2);
			for(int i=0;i<namebytes.length;i++){
				if(namebytes[i]==(byte)0){
					//结束�?
					namebytes = Util.getBytes(namebytes, 0, i);
				}
			}
			try {
				IOchannelName = new String(namebytes,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			IOchannel = new IOchannelInfo();
			IOchannel.IOchannelState = IOchannelState;
			IOchannel.isCanDo = isCanDo;
			IOchannel.IOchannelName = IOchannelName;
		}
	}

}
