package com.hri.ess.command;

import java.io.UnsupportedEncodingException;
import com.hri.ess.network.NetCmd;

/**
 * 获取用户信息
 * @author zhuqian
 *
 */
public class GetUserInfoCmd extends CMD {
	
	public GetUserInfoCmd(){
		this.cmdCode = NetCmd.cmd_UserInfo;
	}
	public byte[] dataToByte() {
		return null;
	}
	public GetUserInfoMsg ParseToMsg(byte[] data){
		GetUserInfoMsg msg = new GetUserInfoMsg();
		msg.Parse(data);
		return msg;
	}
	
	public class GetUserInfoMsg extends Message{
		public String user_nick;//用户别称
		protected void ParseData(byte[] data) {
			try {
				String result = new String(data, "UTF-8");
				String[] userInfos = result.split(";");
				user_nick = userInfos[2]+userInfos[18];
				System.out.println(user_nick);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}
}
