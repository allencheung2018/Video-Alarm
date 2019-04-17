package com.hri.ess.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.hri.ess.AboutActivity;
import com.hri.ess.AlarmVideoActivity;
import com.hri.ess.MainActivity;
import com.hri.ess.PlayVideoActivity;
import com.hri.ess.R;
import com.hri.ess.app.VillaApplication;
import com.hri.ess.businesslogic.PublishBusinessLogic;
import com.hri.ess.businesslogic.SendClientOnLineBussinessLogic;
import com.hri.ess.command.AnswerMsgAlarm;
import com.hri.ess.dbservice.domain.AlarmDetailed;
import com.hri.ess.util.SharePrefUtil;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 心跳服务
 * 
 * @author zhuqian
 * 
 */
public class SecurityService extends Service {
	private SendClientOnLineBussinessLogic sendOnlineHelper;
	private PublishBusinessLogic publishBusinessLogic;
	private static final String TAG = "SecurityService";

	private AlarmManager am;
	private HeartReceiver heartReceiver;
	private AlarmReceiver alarmReceiver;

	private AlertDialog alarmDialog;

	private SoundPool soundPool;
	
	private VideoStopListener videoStopListener;

	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onCreate() {

		sendOnlineHelper = new SendClientOnLineBussinessLogic(this);
		publishBusinessLogic = new PublishBusinessLogic(this);

		am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		// 初始化心跳设置
		setAlarmManager();

		if (heartReceiver == null) {
			heartReceiver = new HeartReceiver();
		}
		IntentFilter heartFilte = new IntentFilter();
		heartFilte.addAction("com.hri.ess.sendheart");
		registerReceiver(heartReceiver, heartFilte);

		if (alarmReceiver == null) {
			alarmReceiver = new AlarmReceiver();
		}
		IntentFilter alarmFilte = new IntentFilter();
		alarmFilte.addAction("ess.com.hri.alarm");
		registerReceiver(alarmReceiver, alarmFilte);
		
		if(videoStopListener==null){
			videoStopListener = new VideoStopListener();
		}
		IntentFilter videoStopFilte = new IntentFilter();
		videoStopFilte.addAction("com.hri.ess.videostop");
		registerReceiver(videoStopListener, videoStopFilte);

		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		alarmId = soundPool.load(this, R.raw.alarm, 1);

		// 先发送一个心跳
		//sendOnline();
	}

	private int alarmId;
	private PendingIntent operation;
	private Intent intent;

	private void setAlarmManager() {
		intent = new Intent("com.hri.ess.sendheart");
		operation = PendingIntent.getBroadcast(this, 0, intent, 0);
		am.setRepeating(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis() + 5 * 1000, 25 * 1000, operation);
	}

	public void onDestroy() {
		Log.i(TAG, "停止发送心跳");

		if (am != null) {
			am.cancel(operation);
			am = null;
		}
		if (heartReceiver != null) {
			unregisterReceiver(heartReceiver);
		}
		if (alarmReceiver != null) {
			unregisterReceiver(alarmReceiver);
		}
		if(videoStopListener!=null){
			unregisterReceiver(videoStopListener);
		}
	}

	private class HeartReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			// 发送心跳
			sendOnline();
		}
	}

	// 接收报警广播
	private class AlarmReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			// 收到新的报警
			Log.i(TAG, "收到新的报警，报警类型：" + VillaApplication.alarmMsg.alarmType);
			mHandle.sendEmptyMessage(NEWALARM);
		}
	}

	private static final int NEWALARM = 200;
	private String oldAlarmId;
	private int alarmIndex = 0;
	private Handler mHandle = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case NEWALARM:
				final AnswerMsgAlarm alarm = VillaApplication.alarmMsg;
				if (oldAlarmId == null) {
					oldAlarmId = alarm.alarmId;
				} else {
					if (oldAlarmId.equals(alarm.alarmId)) {
						Log.i(TAG, "相同报警ID，过滤");
						return;
					} else {
						oldAlarmId = alarm.alarmId;
					}
				}
				// 发送通知广播
				Intent alarmIntent = new Intent("ess.com.hri.havenewalarm");
				alarmIntent.putExtra("isAlarm", alarm.isAlarm);
				alarmIntent.putExtra("alarmId", alarm.alarmId);
				sendBroadcast(alarmIntent);
				// 播放报警音
				if (alarm.isAlarm) {
					soundPool.play(alarmId, 1, 1, 0, 0, 1);
				} else {
					// 播放提示音
					Uri notification = RingtoneManager
							.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
					Ringtone r = RingtoneManager.getRingtone(
							SecurityService.this, notification);
					r.play();
				}
				if (VillaApplication.mContext instanceof MainActivity
						|| VillaApplication.mContext instanceof AboutActivity) {
					notifyAlarm(alarm);
				} else {
					// 在播放视频
					Toast.makeText(VillaApplication.mContext, "您有新的警情，请注意查收", 0)
							.show();
					// 添加未读报警
					alarmIndex = alarmIndex == 255 ? 0 : alarmIndex + 1;
					hasAlarm.put(alarmIndex, VillaApplication.alarmMsg);
				}
				break;
			default:
				break;
			}
		}
	};

	private void notifyAlarm(final AnswerMsgAlarm alarm) {
		alarmDialog = new AlertDialog.Builder(VillaApplication.mContext)
					.create();
		alarmDialog.show();
		alarmDialog.setContentView(R.layout.layout_alarminfo_dialog);
		if (alarm.isAlarm) {
			// 播放报警音乐
			((TextView) alarmDialog.getWindow().findViewById(
					R.id.dialog_alarm_type)).setText("报警类型："
					+ VillaApplication.getAlarmType(alarm.alarmType));
		} else {
			((TextView) alarmDialog.getWindow().findViewById(
					R.id.dialog_alarm_alert)).setText("您有新的出入记录，请查收");
			((TextView) alarmDialog.getWindow().findViewById(
					R.id.dialog_alarm_type)).setText("报警类型：出入提示");
		}
		// 跳出报警对话框
		((TextView) alarmDialog.getWindow()
				.findViewById(R.id.dialog_alarm_time)).setText("报警时间："
				+ alarm.alarmTime);
		Bitmap alarmImage = BitmapFactory.decodeByteArray(alarm.alarmImg, 0,
				alarm.alarmImg.length);
		((ImageView) alarmDialog.getWindow()
				.findViewById(R.id.dialog_alarm_img))
				.setImageBitmap(alarmImage);
		alarmDialog.getWindow().findViewById(R.id.dialog_ok_btn)
				.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						// 播放实时视频
						Intent intent = new Intent(SecurityService.this,
								PlayVideoActivity.class);
						intent.putStringArrayListExtra("channelList",
								VillaApplication.channelList);
						intent.putExtra("channelnum", (int) alarm.alarmChannel);
						intent.putExtra("onlineVideo", 1);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
					}
				});
		alarmDialog.getWindow().findViewById(R.id.dialog_cancel_btn)
				.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						// 播放录像
						String channelName = VillaApplication.channelList
								.get(alarm.alarmChannel);
						AlarmDetailed alarmDetailed = new AlarmDetailed();
						alarmDetailed.setAlarmtime(alarm.alarmTime);
						alarmDetailed.setChannelName(channelName);
						alarmDetailed.setChannelNum(alarm.alarmChannel);
						alarmDetailed.setAlarmId(alarm.alarmId);
						VillaApplication.alarm = alarmDetailed;
						Intent intent = new Intent(SecurityService.this,
								AlarmVideoActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
					}
				});
	};

	// 发送心跳和订阅指令
	private void sendOnline() {
		new Thread() {
			public void run() {
				try {
					sendOnlineHelper.executionSendClientOnline();
					publishBusinessLogic.executionPublish(SharePrefUtil
							.getString(SecurityService.this, "deviceCode", ""));
					Log.i(TAG, "发送心跳和订阅成功");
				} catch (Exception e) {
					e.printStackTrace();
					Log.i(TAG, "发送心跳和订阅失败");
				}
			};
		}.start();
	}

	private Map<Integer, AnswerMsgAlarm> hasAlarm = new HashMap<Integer, AnswerMsgAlarm>();// 还没有显示的报警或开关门记录

	private class VideoStopListener extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			if (hasAlarm.size() > 0) {
				for (Entry<Integer, AnswerMsgAlarm> item : hasAlarm.entrySet()) {
					AnswerMsgAlarm alarm = item.getValue();
					notifyAlarm(alarm);
				}
				// 通知完记得清空
				hasAlarm.clear();
			}
		}
	}
}
