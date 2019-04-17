package com.hri.ess.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.hri.ess.command.AnswerMsgAlarm;
import com.hri.ess.dbservice.domain.AlarmDetailed;
import com.hri.ess.network.SIVMClient;
import com.hri.ess.service.SecurityService;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

public class VillaApplication extends Application {

	private static VillaApplication app;
	
	public static AnswerMsgAlarm alarmMsg;
	public static ArrayList<String> channelList;//通道列表
	
	public static List<AnswerMsgAlarm> alarmList = new ArrayList<AnswerMsgAlarm>();//存放未读报警
	
	
	//保存报警对话框的上下文
	public static Context mContext;
	
	public VillaApplication() {

	}
	public static AlarmDetailed alarm;
	public static VillaApplication getApp() {
		return app;
	}

	private List<Activity> linkedActivitys = new LinkedList<Activity>();

	public void addActivity(Activity activity) {
		linkedActivitys.add(activity);
	}

	public void exit(Context context) {
		// 断了Socket
		SIVMClient.getIntance(context).closeSocket();
		// 停止发送心跳
		Intent intent = new Intent(context, SecurityService.class);
		context.stopService(intent);
		// 销毁Activity
		for (Activity activity : linkedActivitys) {
			activity.finish();
		}
		// 退出应用
		System.exit(0);
	}
	public static Map<Integer,String> typeMap;
	public static String getAlarmType(int type){
		return typeMap.get(type);
	}
	public void onCreate() {
		this.app = this;
		typeMap = new HashMap<Integer,String>();
		typeMap.put(1, "警戒区检测(室外)");
		typeMap.put(2, "徘徊");
		typeMap.put(3, "面部检测");
		typeMap.put(4, "偷窃");
		typeMap.put(5, "滞留");
		typeMap.put(6, "脱岗");
		typeMap.put(7, "人群聚集)");
		typeMap.put(8, "火灾烟雾混合检测");
		typeMap.put(9, "速度异常");
		typeMap.put(10, "出入检测");
		typeMap.put(11, "尾随");
		typeMap.put(12, "ATM面板分析");
		typeMap.put(13, "蒙面识别");
		typeMap.put(14, "打架斗殴");
		typeMap.put(101, "体温探测");
		typeMap.put(102, "E面通人脸识别");
		typeMap.put(104, "利普车牌识别");
		typeMap.put(105, "周界报警主机触发");
		typeMap.put(106, "DVR报警输入触发");
		typeMap.put(107, "联动卡输入触发");
		typeMap.put(108, "视频源故障");
		typeMap.put(109, "用户远程强制触发报警");
		typeMap.put(110, "紧急求援");
		typeMap.put(111, "现场确认(模糊报警)");
		typeMap.put(112, "开门");
		typeMap.put(113, "关门");
		typeMap.put(114, "未按时开关门");
		typeMap.put(115, "本地用户强制触发报警");
	}
}
