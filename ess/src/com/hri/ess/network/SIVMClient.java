package com.hri.ess.network;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

import com.hri.ess.app.VillaApplication;
import com.hri.ess.businesslogic.AlarmBussinessLogic;
import com.hri.ess.command.AnswerFstatusChanged;
import com.hri.ess.command.AnswerMsgAlarm;
import com.hri.ess.command.CmdAlarmDetailed;
import com.hri.ess.command.CmdAlarmDetailed.MsgAlarmDetailed;
import com.hri.ess.command.CmdChangeDeviceState;
import com.hri.ess.command.CmdChangeDeviceState.MsgChangeDeviceState;
import com.hri.ess.command.CmdChannelRealImage;
import com.hri.ess.command.CmdChannelRealImage.MsgChannelRealImage;
import com.hri.ess.command.CmdDeviceInfo;
import com.hri.ess.command.CmdDeviceInfo.MsgDeviceInfo;
import com.hri.ess.command.CmdGetChannel;
import com.hri.ess.command.CmdGetChannel.MsgGetChannel;
import com.hri.ess.command.CmdGetDeviceList;
import com.hri.ess.command.CmdGetDeviceList.MsgGetDeviceList;
import com.hri.ess.command.CmdGetIOChannelState;
import com.hri.ess.command.CmdGetIOChannelState.MsgGetIOChannelState;
import com.hri.ess.command.CmdLogin;
import com.hri.ess.command.CmdLogin.MsgLogin;
import com.hri.ess.command.CmdOnLineTest;
import com.hri.ess.command.CmdPublishMsg;
import com.hri.ess.command.CmdPublishMsg.MsgPublishMsg;
import com.hri.ess.command.CmdReadAlarmNote;
import com.hri.ess.command.CmdReadAlarmNote.MsgReadAlarmNote;
import com.hri.ess.command.CmdReadDeviceChannel;
import com.hri.ess.command.CmdReadDeviceChannel.MsgReadDeviceChannel;
import com.hri.ess.command.CmdReadDeviceInfo;
import com.hri.ess.command.CmdReadDeviceInfo.MsgReadDeviceInfo;
import com.hri.ess.command.CmdReadDeviceState;
import com.hri.ess.command.CmdReadDeviceState.MsgReadDeviceState;
import com.hri.ess.command.CmdReadEntryNote;
import com.hri.ess.command.CmdReadEntryNote.MsgReadEntryNote;
import com.hri.ess.command.CmdSetIOChannelState;
import com.hri.ess.command.CmdSetIOChannelState.MsgSetIOChannelState;
import com.hri.ess.command.GetDeviceRegisterInfoCmd;
import com.hri.ess.command.GetDeviceRegisterInfoCmd.GetDeviceRegisterInfoMsg;
import com.hri.ess.command.GetUserInfoCmd;
import com.hri.ess.command.GetUserInfoCmd.GetUserInfoMsg;
import com.hri.ess.dbservice.domain.AlarmDetailed;
import com.hri.ess.dbservice.domain.IOchannelInfo;
import com.hri.ess.util.EnumSubjectEvents;
import com.hri.ess.util.EventPublisher;
import com.hri.ess.util.IEventNotify;
import com.hri.ess.util.SharePrefUtil;
import com.hri.ess.util.Util;
import com.hri.ess.network.TcpClient;
import com.hri.ess.command.CmdPeopleStream;
import com.hri.ess.command.PeopleStreamInfo;
import com.hri.ess.command.CmdPeopleStream.MsgPeopleStream;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * 网络命令请求管理
 * 
 * @author yu
 * 
 */
public class SIVMClient implements IEventNotify {
	private static final String TAG = "SIVMClient";
	private static SIVMClient client;
	private static Context sivmContext;
	private TcpClient tcp;

	private JSONObject userinfo;
	public EventPublisher publisher;
	public byte[] loginId = new byte[] { 0, 0, 0, 0 };
	private String filename = "userInfo";
	public String userName = "";
	public String userPass = "";
	private String ipAddress = "";
	public String deviceCode = "";
	private int port;
	private AlarmBussinessLogic alarmBL;
	private String doorGUID = "";

	private SIVMClient() {
		publisher = new EventPublisher();
		publisher.subject(EnumSubjectEvents.TCPClient_DeviceAlarm, this);
		alarmBL = new AlarmBussinessLogic(sivmContext);

	}

	/**
	 * SIVMClient实例入口
	 * 
	 * @return
	 */
	public static SIVMClient getIntance(Context context) {
		sivmContext = context;
		if (client == null) {
			client = new SIVMClient();
		}
		return client;
	}

	/**
	 * 获取tcp实例
	 * 
	 * @return
	 * @throws Exception
	 */
	private TcpClient getTcpClient() {
		if (this.tcp == null) {
			this.tcp = new TcpClient(publisher, sivmContext);
		}
		return this.tcp;
	}

	/**
	 * 登录
	 * 
	 * @param username
	 * @param pwd
	 * @return
	 * @throws Exception
	 */
	public synchronized byte[] login(String username, String pwd)
			throws Exception {
		System.out.println("开始登录");
		byte[] answer = null;
		CmdLogin loginCmd = new CmdLogin(username, pwd,SharePrefUtil.getString(sivmContext, "deviceCode", ""));
		// 获取设备码
		this.deviceCode = SharePrefUtil
				.getString(sivmContext, "deviceCode", "");
		loginCmd.deviceCode = deviceCode;
		try {
			TcpClient tcpClient = getTcpClient();
			answer = tcpClient.sendWait(loginCmd, 20 * 1000);
		} catch (Exception e) {
			e.printStackTrace();
			closeTcpClient();
		}
		if (answer != null) {
			MsgLogin msgLogin = loginCmd.ParseToMsg(answer);
			if (!msgLogin.success) {
				closeTcpClient();
				throw new Exception(msgLogin.error);
			} else {
				//开始获取用户信息
				loginId = msgLogin.userId;
				//获取设备注册信息
				//getDeviceRegisterInfo();
				//获取用户信息
				getUserInfoFromNet();
				String publishId = getPublishId();
				if (onLoginSuccessListener != null) {
					onLoginSuccessListener.onLoginSuccess(true);
				}
//				this.PublishMsg(publishId, userName, userPass, "33",
//						deviceCode, "0");
			}
		} else {
			throw new Exception();
		}
		return loginId;
	}
	private void getDeviceRegisterInfo() throws Exception{
		byte[] answer = null;
		byte[] loginId = getLoginId();
		GetDeviceRegisterInfoCmd cmd = new GetDeviceRegisterInfoCmd();
		cmd.loginID = loginId;
		TcpClient tcpClient = getTcpClient();
		try {
			answer = tcpClient.sendWait(cmd, 20 * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(answer!=null){
			GetDeviceRegisterInfoMsg infoMsg = cmd.ParseToMsg(answer);
			if(!infoMsg.success){
				Log.i(TAG, "获取设备注册信息失败："+infoMsg.error);
				throw new Exception(infoMsg.error);
			}else{
				Log.i(TAG, "设备类型："+infoMsg.videoType+"，保存");
				SharePrefUtil.saveInt(sivmContext, "videoType", infoMsg.videoType);
			}
		}else{
			throw new Exception();
		}
		
	}
	private void getUserInfoFromNet() throws Exception{
		byte[] answer = null;
		GetUserInfoCmd user = new GetUserInfoCmd();
		user.loginID = this.loginId;
		TcpClient tcpClient = getTcpClient();
		try {
			answer = tcpClient.sendWait(user, 20 * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(answer!=null){
			GetUserInfoMsg userInfoMsg = user.ParseToMsg(answer);
			if(!userInfoMsg.success){
				Log.i(TAG, "获取用户信息失败："+userInfoMsg.error);
				throw new Exception(userInfoMsg.error);
			}else{
				String nick = userInfoMsg.user_nick;
				//保存用户别称
				SharePrefUtil.saveString(sivmContext, "nick", nick);
				Log.i(TAG, "获取用户信息成功");
			}
		}else{
			throw new Exception();
		}
	}

	/**
	 * 获取人流数量
	 * 
	 * @param channel
	 * @return
	 * @throws Exception
	 */
	public List<com.hri.ess.command.PeopleStreamInfo> GetPeopleStream(byte channel, String time,
			byte type, int num, int cycle, byte unit) throws Exception {
		byte[] loginId = getLoginId();
		com.hri.ess.command.CmdPeopleStream cmdPeopleStream = new com.hri.ess.command.CmdPeopleStream();
		DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		cmdPeopleStream.loginID = loginId;
		cmdPeopleStream.channelNum = channel;
		cmdPeopleStream.startTime = Util.date2FileTime(fmt.parse(time));
		cmdPeopleStream.queryType = type;
		cmdPeopleStream.number = num;
		cmdPeopleStream.cycle = cycle;
		cmdPeopleStream.unit = unit;

		TcpClient tcpClient = getTcpClient();
		byte[] data = tcpClient.sendWait(cmdPeopleStream, 20 * 1000);
		com.hri.ess.command.CmdPeopleStream.MsgPeopleStream msgPeopleStream = cmdPeopleStream.ParseToMsg(data);
		if (!msgPeopleStream.success) {
			throw new Exception("获取人流数量失败");
		}

		return msgPeopleStream.streamList;
	}
	/**
	 * 订阅指令
	 * 
	 * @param publishId
	 * @param userName
	 * @param userPass
	 * @param msgType
	 * @param range
	 * @param event
	 * @throws Exception
	 */
	public void PublishMsg(String publishId, String userName, String userPass,
			String msgType, String range, String event) throws Exception {
		byte[] loginId = getLoginId();
		CmdPublishMsg cmdPublishMsg = new CmdPublishMsg();
		cmdPublishMsg.loginID = loginId;
		cmdPublishMsg.publishId = publishId;
		cmdPublishMsg.userName = userName;
		cmdPublishMsg.userPass = userPass;
		cmdPublishMsg.msgType = msgType;
		cmdPublishMsg.Range = range;
		cmdPublishMsg.Event = event;
		TcpClient tcpClient = getTcpClient();
		byte[] data = tcpClient.sendWait(cmdPublishMsg, 20 * 1000);
		MsgPublishMsg msgPublishMsg = cmdPublishMsg.ParseToMsg(data);
		if (!msgPublishMsg.success) {
			throw new Exception("订阅失败");
		} else {
			Log.i(TAG, "订阅成功");
		}
	}

	private String getPublishId() {
		String imei = "";
		TelephonyManager telephonyManager = (TelephonyManager) sivmContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		imei = telephonyManager.getDeviceId();
		return imei;
	}

	// 获取设备列表
	public List<String> getDeviceList() throws Exception {
		// byte[] loginId = getLoginId();
		getUserInfo();
		CmdGetDeviceList getdeviceList = new CmdGetDeviceList();
		getdeviceList.loginID = new byte[] { 0, 0, 0, 0 };
		getdeviceList.userName = this.userName;
		getdeviceList.userPass = this.userPass;
		this.closeTcpClient();
		TcpClient tcpClient = getTcpClient();
		byte[] data = tcpClient.sendWait(getdeviceList, 15 * 1000);
		MsgGetDeviceList msgDeviceList = getdeviceList.ParseToMsg(data);
		if (!msgDeviceList.success) {
			throw new Exception(msgDeviceList.error);
		} else {
			Log.i(TAG, "获取设备列表成功");
		}
		List<String> deviceList = msgDeviceList.deviceList;

		return deviceList;
	}

	/**
	 * 获取设备信息
	 * 
	 * @throws Exception
	 */
	public String[] GetDeviceInof(String DCode) throws Exception {
		CmdDeviceInfo cmdDeviceInfo = new CmdDeviceInfo();
		cmdDeviceInfo.deviceCode = DCode;
		cmdDeviceInfo.loginID = this.loginId;
		TcpClient tcpClient = getTcpClient();
		byte[] data = tcpClient.sendWait(cmdDeviceInfo, 10 * 1000);
		MsgDeviceInfo msgDeviceInfo = cmdDeviceInfo.ParseToMsg(data);
		if (!msgDeviceInfo.success) {
			throw new Exception("获取设备信息失败");
		} else {
			Log.i(TAG, "获取设备信息成功");
		}
		return msgDeviceInfo.deviceInfo;
	}

	/**
	 * 获取通道名称
	 * 
	 * @return
	 * @throws Exception
	 */
	public ArrayList<String> GetChannel() throws Exception {
		byte[] loginId = getLoginId();
		CmdGetChannel cmdGetChannel = new CmdGetChannel();
		cmdGetChannel.loginID = loginId;
		TcpClient tcpClient = getTcpClient();
		byte[] data = tcpClient.sendWait(cmdGetChannel, 20 * 1000);
		MsgGetChannel msgGetChannel = cmdGetChannel.ParseToMsg(data);
		if (!msgGetChannel.success) {
			throw new Exception("获取通道名称失败");
		} else {
			Log.i(TAG, "获取通道列表成功");
		}
		return msgGetChannel.channelList;
	}

	// 获取用户名和密码
	private void getUserInfo() {
		this.userName = SharePrefUtil.getString(sivmContext, "username", "");
		this.userPass = SharePrefUtil.getString(sivmContext, "pwd", "");
	}

	/**
	 * 关闭TcpClient
	 */
	public void closeTcpClient() {
		if (this.tcp != null) {
			this.tcp.closeSocket();
			this.tcp.Threadswitch = false;
			this.tcp = null;
			if (onLoginSuccessListener != null) {
				onLoginSuccessListener.onLoginSuccess(false);
			}
		}
	}

	// 发送客户端在线
	public void SendClientOnline() throws Exception {
		byte[] loginId = getLoginId();
		CmdOnLineTest cmdOnLineTest = new CmdOnLineTest();
		cmdOnLineTest.loginID = loginId;
		TcpClient tcpClient = getTcpClient();
		try {
			//tcpClient.send(cmdOnLineTest);
			tcpClient.sendWait(cmdOnLineTest, 10*1000);
		} catch (Exception e) {
			e.printStackTrace();
			tcpClient.closeSocket();
		}
	}

	/**
	 * 获取设备串口数目
	 * 
	 * @return
	 * @throws Exception
	 */
	public byte ReadDeviceChannel() throws Exception {
		byte[] loginId = getLoginId();
		CmdReadDeviceChannel cmdReadDeviceChannel = new CmdReadDeviceChannel();
		cmdReadDeviceChannel.loginID = loginId;

		TcpClient tcpClient = getTcpClient();
		byte[] data = tcpClient.sendWait(cmdReadDeviceChannel, 15 * 1000);
		MsgReadDeviceChannel msgReadDeviceChannel = cmdReadDeviceChannel
				.ParseToMsg(data);
		if (!msgReadDeviceChannel.success) {
			throw new Exception(msgReadDeviceChannel.error);
		}
		return msgReadDeviceChannel.IOchannelNum;
	}

	/**
	 * 获取设备串口信息
	 * 
	 * @param channelNum串口号
	 * @return
	 * @throws Exception
	 */
	public IOchannelInfo GetIOChannel(byte channelNum) throws Exception {
		byte[] loginId = getLoginId();
		CmdGetIOChannelState cmdGetIOChannelState = new CmdGetIOChannelState();
		cmdGetIOChannelState.loginID = loginId;
		cmdGetIOChannelState.IOchannelNum = channelNum;
		TcpClient tcpClient = getTcpClient();
		byte[] data = tcpClient.sendWait(cmdGetIOChannelState, 15 * 1000);
		MsgGetIOChannelState msgGetIOChannelStatel = cmdGetIOChannelState
				.ParseToMsg(data);
		if (!msgGetIOChannelStatel.success) {
			throw new Exception(msgGetIOChannelStatel.error);
		}
		return msgGetIOChannelStatel.IOchannel;
	}

	/**
	 * 设置设备串口状态
	 * 
	 * @param channelNum
	 *            串口号
	 * @param type
	 *            串口状态 0关闭 1打开
	 * @return
	 * @throws Exception
	 */
	public void SetIOChannel(byte channelNum, byte type) throws Exception {
		byte[] loginId = getLoginId();

		CmdSetIOChannelState cmdSetIOChannelState = new CmdSetIOChannelState();
		cmdSetIOChannelState.loginID = loginId;
		cmdSetIOChannelState.IOchannelNum = channelNum;
		cmdSetIOChannelState.type = type;
		TcpClient tcpClient = getTcpClient();
		byte[] data = tcpClient.sendWait(cmdSetIOChannelState, 15 * 1000);
		MsgSetIOChannelState msgSetIOChannelStatel = cmdSetIOChannelState
				.ParseToMsg(data);
		if (!msgSetIOChannelStatel.success) {
			throw new Exception(msgSetIOChannelStatel.error);
		}

	}

	/**
	 * 获取开关门记录
	 * 
	 * @param channel
	 * @return
	 * @throws Exception
	 */
	public List<String> ReadAlarmNote(byte channel, byte type,
			String startTime, String endTime) throws Exception {
		System.out.println("开始时间："+startTime);
		System.out.println("结束时间："+endTime);
		byte[] loginId = getLoginId();
		DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		CmdReadAlarmNote cmdReadAlarmNote = new CmdReadAlarmNote();
		cmdReadAlarmNote.loginID = loginId;
		cmdReadAlarmNote.channelNum = channel;
		cmdReadAlarmNote.startTime = Util.date2FileTime(fmt.parse(startTime));
		cmdReadAlarmNote.endTime = Util.date2FileTime(fmt.parse(endTime));
		cmdReadAlarmNote.type = type;

		TcpClient tcpClient = getTcpClient();
		byte[] data = tcpClient.sendWait(cmdReadAlarmNote, 20 * 1000);
		MsgReadAlarmNote msgReadAlarmNote = cmdReadAlarmNote.ParseToMsg(data);
		if (!msgReadAlarmNote.success) {
			throw new Exception(msgReadAlarmNote.error);
		}
		return msgReadAlarmNote.noteLists;
	}

	/**
	 * 获取开关门记录
	 * 
	 * @param channel
	 * @return
	 * @throws Exception
	 */
	public List<String> ReadEntryNote(byte channel, byte type,
			String startTime, String endTime) throws Exception {
		System.out.println("开始时间："+startTime + " 结束时间："+endTime);
		byte[] loginId = getLoginId();
		DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		CmdReadEntryNote cmdReadAlarmNote = new CmdReadEntryNote();
		cmdReadAlarmNote.loginID = loginId;
		cmdReadAlarmNote.channelNum = channel;
		cmdReadAlarmNote.startTime = Util.date2FileTime(fmt.parse(startTime));
		cmdReadAlarmNote.endTime = Util.date2FileTime(fmt.parse(endTime));
		cmdReadAlarmNote.type = type;

		TcpClient tcpClient = getTcpClient();
		byte[] data = tcpClient.sendWait(cmdReadAlarmNote, 20 * 1000);
		//test
		int len = data.length;
		Log.i("SIVMClient.ReadEntryNote", "data.length="+len);
		if(len == 11){
			Log.i("SIVMClient.ReadEntryNote", "data:"+data);
		}
		MsgReadEntryNote msgReadAlarmNote = cmdReadAlarmNote.ParseToMsg(data);
		if (!msgReadAlarmNote.success) {
			System.out.println(msgReadAlarmNote.error);
			throw new Exception(msgReadAlarmNote.error);
		}
		return msgReadAlarmNote.noteLists;
	}
	/**
	 * 获取报警明细
	 * 
	 * @param alarmId
	 * @return
	 * @throws Exception
	 */
	public AlarmDetailed ReadAlarmDetailed(String alarmId) throws Exception {
		byte[] loginId = getLoginId();
		CmdAlarmDetailed cmdAlarmDetailed = new CmdAlarmDetailed();
		cmdAlarmDetailed.loginID = loginId;
		cmdAlarmDetailed.alarmId = alarmId;
		cmdAlarmDetailed.imgWidth = "176";
		cmdAlarmDetailed.imgHeight = "144";
		Log.i(TAG, "ReadAlarmDetailed-imageWidth="+cmdAlarmDetailed.imgWidth 
				+ " imgaeHeight:"+cmdAlarmDetailed.imgHeight);
		TcpClient tcpClient = getTcpClient();
		byte[] data = tcpClient.sendWait(cmdAlarmDetailed, 20 * 1000);
		MsgAlarmDetailed msgAlarmDetailed = cmdAlarmDetailed.ParseToMsg(data);
		if (!msgAlarmDetailed.success) {
			System.out.println(msgAlarmDetailed.error);
			throw new Exception(msgAlarmDetailed.error);
		}
		return msgAlarmDetailed.alarmDetailed;
	}
	
	public byte[] getChRealImg(byte ch) throws Exception
	{
		byte[] loginId = getLoginId();
		CmdChannelRealImage cmdChRealImg = new CmdChannelRealImage();
		cmdChRealImg.loginID = loginId;
		cmdChRealImg.chNum = ch;
		TcpClient tcpClient = getTcpClient();
		byte[] data = tcpClient.sendWait(cmdChRealImg, 20 * 1000);
		MsgChannelRealImage msgChRealImg = cmdChRealImg.ParseToMsg(data); 
		if (!msgChRealImg.success) {
			throw new Exception("getChRealImg:"+msgChRealImg.error);
		}
		return msgChRealImg.realImg;
	}
	/**
	 * 修改设备工作状态
	 * 
	 * @throws Exception
	 */
	public void ChangeDeviceState(byte type) throws Exception {
		byte[] loginId = getLoginId();
		CmdChangeDeviceState cmdChangeDeviceState = new CmdChangeDeviceState();
		cmdChangeDeviceState.loginID = loginId;
		cmdChangeDeviceState.deviceState = type;

		TcpClient tcpClient = getTcpClient();
		byte[] data = tcpClient.sendWait(cmdChangeDeviceState, 10 * 1000);
		MsgChangeDeviceState msgDeviceState = cmdChangeDeviceState
				.ParseToMsg(data);
		if (!msgDeviceState.success) {
			throw new Exception("设置设备工作状态失败");
		}
	}
	/**
	 * 获取设备信息
	 * 
	 * @throws Exception
	 */
	public String[] ReadDeviceInfo(String deviceCode) throws Exception {
		byte[] loginId = getLoginId();
		CmdReadDeviceInfo cmdReadDeviceInfo = new CmdReadDeviceInfo();
		cmdReadDeviceInfo.loginID = loginId;
		cmdReadDeviceInfo.deviceCode = deviceCode;
		TcpClient tcpClient = getTcpClient();
		byte[] data = tcpClient.sendWait(cmdReadDeviceInfo, 15 * 1000);
		MsgReadDeviceInfo msgReadDeviceInfo = cmdReadDeviceInfo
				.ParseToMsg(data);
		if (!msgReadDeviceInfo.success) {
			throw new Exception(msgReadDeviceInfo.error);
		}
		return msgReadDeviceInfo.deviceInfo;
	}
	/**
	 * 获取设备工作状态
	 * 
	 * @throws Exception
	 */
	public byte ReadDeviceState() throws Exception {
		byte[] loginId = getLoginId();
		CmdReadDeviceState cmdReadDeviceState = new CmdReadDeviceState();
		cmdReadDeviceState.loginID = loginId;

		TcpClient tcpClient = getTcpClient();
		byte[] data = tcpClient.sendWait(cmdReadDeviceState, 15 * 1000);
		MsgReadDeviceState msgDeviceState = cmdReadDeviceState.ParseToMsg(data);
		if (!msgDeviceState.success) {
			throw new Exception(msgDeviceState.error);
		}
		return msgDeviceState.state;
	}
	/**
	 * 获取登录ID
	 * 
	 * @return
	 * @throws Exception
	 */
	private byte[] getLoginId() throws Exception {
		byte[] a = new byte[] { 0, 0, 0, 0 };
		if (Arrays.equals(loginId, a)) {
			System.out.println("登录id失效");
			return login(SharePrefUtil.getString(sivmContext, "username", ""),
					SharePrefUtil.getString(sivmContext, "pwd", ""));
		}
		return loginId;
	}


	public void Notify(Object sender, EnumSubjectEvents event, Object eArgs,
			byte cmdId) {
		if (event == EnumSubjectEvents.TCPClient_DeviceAlarm) {
			switch (cmdId) {
			case NetCmd.cmd_Alarm:
				AnswerMsgAlarm answer = (AnswerMsgAlarm) eArgs;
				if(!answer.isAlarm && !SharePrefUtil.getBoolean(sivmContext, "is_alertdoor", true)){
					//不需要开关门提醒，返回
					Log.i(TAG, "用户不需要开关门记录提醒");
					return;
				}
				TcpClient tcpClient = getTcpClient();
				System.out.println("报警ID:"+answer.alarmId);
				try {
					tcpClient.answer(alarmBL.AnswerAlaem(eArgs));
				} catch (IOException e) {
					e.printStackTrace();
				}
				//保存在全局变量
				VillaApplication.alarmMsg = answer;
				//提示界面
				Intent alarm = new Intent("ess.com.hri.alarm");
				sivmContext.sendBroadcast(alarm);
				break;
			case NetCmd.cmd_fStatusChanged:
				// 设备状态变更
				AnswerFstatusChanged fstatusChanged = (AnswerFstatusChanged) eArgs;
				Intent statusIntent = new Intent("com.hri.statuschanged");
				int state = fstatusChanged.status;
				statusIntent.putExtra("deviceState",state);
				sivmContext.sendBroadcast(statusIntent);
				break;
			}
		}
	}

	public void setOnLoginSuccessListener(
			OnLoginSuccessListener onLoginSuccessListener) {
		this.onLoginSuccessListener = onLoginSuccessListener;
	}

	private OnLoginSuccessListener onLoginSuccessListener;

	// 登录成功回调
	public interface OnLoginSuccessListener {
		void onLoginSuccess(boolean isLoginSuccess);
	}

	public void closeSocket() {
		if(tcp!=null){
			tcp.closeSocket();
		}
	}
}
//开始播放实时视频 获取IO口总数 改变设备状态 home_pagers
