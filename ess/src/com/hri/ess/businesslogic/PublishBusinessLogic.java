package com.hri.ess.businesslogic;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import org.json.JSONObject;

import com.hri.ess.network.SIVMClient;
import com.hri.ess.util.SharePrefUtil;

import android.content.Context;
import android.telephony.TelephonyManager;
/**
 * 确认出警业务逻辑
 * @author yu
 *
 */
public class PublishBusinessLogic {
	
	private Context mcontext;
	private SIVMClient sivmClient = null;
	private String filename = "userInfo";
	//订阅ID
	private String publishId = "";
	//用户�?
	private String userName = "";
	//用户密码
	private String userPass = "";
	//消息类型
	private String msgType = "33";
	//范围
	private String Range = "";
	//事件
	private String Event = "0";
		
    /**
     *订阅消息业务逻辑
     * @param context
     */
	public PublishBusinessLogic(Context context){
		 mcontext = context;
		 sivmClient = SIVMClient.getIntance(mcontext);
		 getUserInfo();
		
	}
	
	private String getPublishId() {
		String imei = "";
		TelephonyManager telephonyManager= (TelephonyManager) mcontext.getSystemService(Context.TELEPHONY_SERVICE);
		imei=telephonyManager.getDeviceId();	
		return imei;
	}

	/**
	 * 获取用户资料
	 */
	private void getUserInfo() {
		this.userName = SharePrefUtil.getString(mcontext, "username", "");
		this.userPass = SharePrefUtil.getString(mcontext, "pwd", "");
	}
	
	/**
	 * 发�?订阅消息
	 * @param deviceCode 
	 * @param name
	 * @param psw
	 * @throws Exception
	 */
	public void executionPublish(String deviceCode) throws Exception{	
		try {
			publishId = getPublishId();
			sivmClient.PublishMsg(publishId,userName,userPass,msgType,deviceCode,Event);
		} catch (Exception e) {
			throw e;
		}
	}


}
