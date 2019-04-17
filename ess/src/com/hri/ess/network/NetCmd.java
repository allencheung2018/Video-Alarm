package com.hri.ess.network;

/**
 * 指令码集�?
 * @author yu
 *
 */
public class NetCmd {
	
	/**
	 * IP及端�?
	 */
//	final public static String ip = "192.168.1.112";
//	final public static String ip = "192.168.1.108";//内网
//	final public static String ip = "113.106.89.91";//外网
//	final public static String ip = "113.106.89.125";
	final public static int port = 4015;	
	
	final public static byte cmd_Heart = 0x09;//心跳
	final public static byte cmd_Login = 0x01;//用户登陆
	final public static byte cmd_UserInfo = 0x2b;//用户信息获取
	final public static byte cmd_GPS = 0x3d;//Gps
	final public static byte cmd_UploadData = 0x3e;//上传照片
	final public static byte cmd_keepWatch = 0x4a;//巡更列表
	final public static byte cmd_SignIn = 0x3a;//巡更签到
	final public static byte cmd_CloseCase = 0x3a;//结案信息
	final public static byte cmd_PlayRvs = 0x02;//请求实时视频
	final public static byte cmd_PlayVideoTape = 0x03;//请求实时视频
	final public static byte cmd_RvsData = 0x06;//视频数据到达
	final public static byte cmd_StopRvs = 0x04;//停止视频
	final public static byte cmd_SetDuty= 0x44;//交班
	final public static byte cmd_FAlarmOrHelp= 0x21;//强制报警与求�?
	final public static byte cmd_Alarm= 0x21;//主动报警
	final public static byte cmd_GetDeviceCode= 0x43;//获取设备�?
	final public static byte cmd_GetDeviceInfo = 0x38;//获取设备信息
	final public static byte cmd_GetDeciceList = 0x39;//停止实时视频
	final public static byte cmd_AlarmACK = 0x16;//确认报警（结案）
	final public static byte cmd_VideoPlay = 0x02;//请求实时视频
	final public static byte cmd_VideoArrive = 0x06;//视频数据到达
	final public static byte cmd_VideoTapeOver = 0x07;//录像数据发�?完成
	final public static byte cmd_VideoStop = 0x04;//停止实时视频
	final public static byte cmd_PeopleStream = 0x54;//人流信息
	final public static byte cmd_OperationNote = 0x50;//操作记录
	final public static byte cmd_ReadAlarmNote = 0x23;//读取报警记录列表
	final public static byte cmd_ReadEntryNote = 0x73;//读取报警记录列表
	final public static byte cmd_ChangeDeviceState = 0x1b;//变更设备状�?
	final public static byte cmd_ReadDeviceState = 0x1c;//读取设备状�?
	final public static byte cmd_GetChannle = 0x41;//读取设备状�?
	final public static byte cmd_ReadAlarmDetailed = 0x24;//读取报警明细
	final public static byte cmd_ReadDeviceChannel = 0x19;//获取设备串口信息
	final public static byte cmd_ReadDeviceInfo = 0x38;//获取设备信息
	final public static byte cmd_GetIOchannelState = 0x10;//获取IO口状�?
	final public static byte cmd_SetIOchannelState = 0x15;//设置IO口状�?
	final public static byte cmd_PublishMsg = 0x60;//订阅消息
	final public static byte cmd_fStatusChanged= 0x17;//设备状�?变更指令
	final public static byte cmd_getDeviceRegisterInfo= 0x19;//获取设备注册信息
	final public static byte cmd_getChannelImg= 0x13;//抓取指定通道的实时图片

	
	final public static byte cmd_Expand= 0x55;//扩展指令—派�?
	final public static short cmd_Expand_Alarm= 0x01;//扩展指令—派�?
	final public static byte cmd_Expand_DutyList= 0x02;//扩展指令—�?班表
	final public static byte cmd_Expand_LinkMan= 0x03;//扩展指令—联系人
	final public static short cmd_Expand_Talk= 0x04;//扩展指令—对�?
	final public static byte cmd_Expand_Head= 0x05;//扩展指令—获取头�?
	final public static byte cmd_OnLine_Test= 0x09;//扩展指令—获取头�?
	
	final public static byte[] rcmds = new byte[]{0x09};		
	
	public static boolean isClientCmd(byte cmd){	
		boolean flag = true;
		for(int i=0;i<rcmds.length;i++){
			if(rcmds[i]==cmd){
				flag = false;
				break;
			}
		}
		return flag;
	}

}
