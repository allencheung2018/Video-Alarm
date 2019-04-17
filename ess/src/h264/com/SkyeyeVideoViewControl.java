package h264.com;

import java.util.Arrays;

import com.hri.ess.command.CmdLogin;
import com.hri.ess.command.CmdLogin.MsgLogin;
import com.hri.ess.command.CmdPlayRVS;
import com.hri.ess.command.CmdPlayRVS.MsgPlayRVS;
import com.hri.ess.command.CmdPlayVideoTape;
import com.hri.ess.command.CmdPlayVideoTape.MsgPlayVideoTape;
import com.hri.ess.command.CmdStopRVS;
import com.hri.ess.network.NetCmd;
import com.hri.ess.network.VideoTcpClient;
import com.hri.ess.util.EventPublisher;
import com.hri.ess.util.SharePrefUtil;

import android.content.Context;
import android.util.Log;

public class SkyeyeVideoViewControl {

	private static final String TAG = "SkyeyeVideoViewControl";
	private Context mcontext;
	private static SkyeyeVideoViewControl videoControlclient;
	private VideoTcpClient videoTcp;
	public EventPublisher mypublisher;
	public byte[] loginId = new byte[] { 0, 0, 0, 0 };
	public String deviceCode;
	private String filename = "userInfo";

	public byte channelNum, videoSources, resolution;
	public long filelength;
	public byte[] Head;
	public short videoWidth, videoHeight;
	public byte frameRate;

	public SkyeyeVideoViewControl(Context context, EventPublisher publisher) {
		mcontext = context;
		mypublisher = publisher;
		try {
			videoTcp = new VideoTcpClient(getUserIp(), NetCmd.port, mypublisher,this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 鑾峰彇鐢ㄦ埛璧勬枡
	 * 
	 * @throws Exception
	 */
	private String getUserIp() throws Exception {
		String ipAddress = SharePrefUtil.getString(mcontext, "ip_config", "");
		return ipAddress;
	}

	/**
	 * 璁惧鐧婚檰
	 * 
	 * @throws Exception
	 */
	public void login() throws Exception {
		String userName = SharePrefUtil.getString(mcontext, "username", "");
		String password = SharePrefUtil.getString(mcontext, "pwd", "");
		CmdLogin loginCmd = new CmdLogin(userName, password,SharePrefUtil.getString(mcontext, "deviceCode", ""));
		loginCmd.fStatusChanged = false;
		loginCmd.fRealAlarm = false;
		loginCmd.fRealAlarm = false;
		loginCmd.fEntry = false;
		loginCmd.deviceCode = deviceCode;
		byte[] answer =null;
		try
		{
			answer=videoTcp.sendWait(loginCmd, 15 * 1000);
		}catch(Exception e){
			loginId=new byte[]{0,0,0,0};
			throw new Exception("获取登录ID超时,请重试...");
		}
		MsgLogin msgLogin = loginCmd.ParseToMsg(answer);
		if (!msgLogin.success) {
			throw new Exception("登录失败:" + msgLogin.error);
		} else {
			loginId = msgLogin.userId;
		}

	}
   
	/**
	 * 鑾峰彇鐧诲綍ID,鑻ユ病鏈夌櫥褰曡澶囷紝灏辩櫥褰曡澶�
	 * @return
	 * @throws Exception
	 */
	private byte[] getLoginId() throws Exception {
		byte[] a = new byte[] { 0, 0, 0, 0 };
		if (Arrays.equals(loginId, a)) {
			this.login();
		}
		Log.i("getLoginId", "loginId : "+loginId[0]+loginId[1]+loginId[2]+loginId[3]);
		return loginId;
	}
	public void clearLogId()
	{
		loginId[0]=0;
		loginId[1]=0;
		loginId[2]=0;
		loginId[3]=0;
	}

	/**
	 * 瑙嗛寮�鎸囦护
	 * 
	 * @param channelnum
	 *            骞抽亾鍙�
	 * @param videosources
	 *            鍥惧儚鏉ユ簮
	 * @throws Exception
	 */
	public void startVideo(byte channelnum, byte videosources, byte resolution)
			throws Exception {
		videoTcp.StartReceiveData();				//启动接收线程
		getLoginId();
		CmdPlayRVS cmdPlayRVS = new CmdPlayRVS();
		cmdPlayRVS.channelNum = new byte[] { channelnum };
		cmdPlayRVS.videoSources = new byte[] { videosources };
		cmdPlayRVS.resolution = new byte[] { resolution };
		cmdPlayRVS.loginID = loginId;
		byte[] data;
		try{
		data = videoTcp.sendWait(cmdPlayRVS, 15 * 1000);
		}catch(Exception e)
		{
			System.out.println("视频播放失败 ");
			loginId=new byte[]{0,0,0,0};
			throw e;
		}
		MsgPlayRVS msgPlayRVS = cmdPlayRVS.ParseToMsg(data);
		if (!msgPlayRVS.success) {
			throw new Exception("视频播放失败 " + msgPlayRVS.error);
		} else {
			channelNum = msgPlayRVS.channelNum[0];
			videoSources = msgPlayRVS.videoSources[0];
			resolution = msgPlayRVS.resolution[0];
			Head = msgPlayRVS.playHear;
			videoWidth = msgPlayRVS.width;
			videoHeight = msgPlayRVS.hight;
			frameRate = msgPlayRVS.frameRate;
		}

	}

	/**
	 * 录像播发指令
	 * @param channelnum 通道号
	 * @param videosources 图像来源
	 * @param startTime 录像开始时间
	 * @param playTime 播放时长
	 * @param alarmId 报警ID
	 * @throws Exception
	 */
	public void startVideoTape(byte channelnum, byte videosources,
			byte resolutions, long startTime, short playTime, String alarmId)
			throws Exception {
		videoTcp.StartReceiveData();				//启动接收线程
	   getLoginId();
		CmdPlayVideoTape cmdPlayVideoTape = new CmdPlayVideoTape();
		cmdPlayVideoTape.loginID = loginId;
		cmdPlayVideoTape.channelNum = new byte[] { channelnum };
		cmdPlayVideoTape.videoSources = new byte[] { videosources };
		cmdPlayVideoTape.resolution = new byte[] { resolutions };
		cmdPlayVideoTape.startTime = startTime;
		cmdPlayVideoTape.playTime = playTime;
		cmdPlayVideoTape.alarmId = alarmId;
		byte[] data = null;
		try
		{
			data=videoTcp.sendWait(cmdPlayVideoTape, 15 * 1000);
		}
		catch(Exception ex){
		   throw ex;	
		}
		MsgPlayVideoTape msgPlayVideoTape = cmdPlayVideoTape.ParseToMsg(data);
		if (!msgPlayVideoTape.success) {
			videoTcp.closeSocket();
			this.loginId=new byte[]{0,0,0,0};
			throw new Exception("sent failed : " + msgPlayVideoTape.error);
		} else {
			channelNum = msgPlayVideoTape.channelNum[0];
			videoSources = msgPlayVideoTape.videoSources[0];
			resolution = msgPlayVideoTape.resolution[0];
			Head = msgPlayVideoTape.playHear;
			filelength = msgPlayVideoTape.fileLength;
			frameRate = msgPlayVideoTape.frameRate;
			videoWidth = msgPlayVideoTape.width;
			videoHeight = msgPlayVideoTape.hight;
		}
	}

	/**
	 *  鍙戦� 鍋滄鎾斁瑙嗛鍛戒护
	 * 
	 * @throws Exception
	 */
	public void stopVideo() throws Exception {
		//byte[] loginId = getLoginId();
		CmdStopRVS cmdStopRVS = new CmdStopRVS();
		cmdStopRVS.loginID = loginId;
		this.videoTcp.send(cmdStopRVS, 15 * 1000);
		this.videoTcp.closeSocket();
		this.loginId=new byte[]{0,0,0,0};
	}

	/**
	 * 鍙戦� 鍋滄褰曞儚鎾斁鍛戒护 
	 * 
	 * @throws Exception
	 */
	public void stopTapVideo() throws Exception {
		//byte[] loginId = getLoginId();
		CmdStopRVS cmdStopRVS = new CmdStopRVS();
		cmdStopRVS.loginID = loginId;
		//this.videoTcp.send(cmdStopRVS, 15 * 1000);
		this.videoTcp.closeSocket();
		this.loginId=new byte[]{0,0,0,0};
	}
	/**
	 * 閿�瘉VideoTcpClient
	 */
    public void destroyTcpClient()
    {
    	videoTcp.closeSocketAndDespoyThread();
    }
}
