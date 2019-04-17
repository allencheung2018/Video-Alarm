package com.hri.ess;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.baidu.autoupdatesdk.AppUpdateInfo;
import com.baidu.autoupdatesdk.AppUpdateInfoForInstall;
import com.baidu.autoupdatesdk.BDAutoUpdateSDK;
import com.baidu.autoupdatesdk.CPCheckUpdateCallback;
import com.baidu.autoupdatesdk.UICheckUpdateCallback;
import com.hri.ess.app.VillaApplication;
import com.hri.ess.businesslogic.GetChRealImgBusinessLogic;
import com.hri.ess.fragment.HomeFragment;
import com.hri.ess.fragment.LeftMenuFragment;
import com.hri.ess.service.SecurityService;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
/**
 * 主Activity
 * 
 * @author zhuqian
 */
public class MainActivity extends SlidingFragmentActivity {
	private ArrayList<String> channelList;//通道列表
	
	public ArrayList<String> getChannelList() {
		return channelList;
	}
	private SlidingMenu sm;//侧滑菜单
	
	private FragmentTransaction fm;
	private DeviceStateChangeReceiver stateChangeReceiver;
	private HaveNewAlarmReceiver haveNewAlarmReceiver;
	
	private NotificationManager manager;
	private Notification alarmNotification;
	
	private PendingIntent pendingIntent;
	
	private ProgressDialog dialog;		//百度更新对话框
	private boolean isPressed_btn_update = false;
	private Button left_menu_update;
	
	private GetChRealImgBusinessLogic getChRealImgLogic;
	private byte[] imgDataSrc;
	Bitmap bitmap = null;
	BitmapFactory.Options opts;
	private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
		//添加
		VillaApplication.getApp().addActivity(this);

		channelList = getIntent().getStringArrayListExtra("channelList");
		if(VillaApplication.channelList == null){
			VillaApplication.channelList = channelList;		//
		}
		
		initChannelImg();
		
		initSlidingMenu();
		
		//�?��心跳服务
		Intent intent = new Intent(this,SecurityService.class);
		startService(intent);
		
		//注册广播
		stateChangeReceiver = new DeviceStateChangeReceiver();
		IntentFilter stateFilter = new IntentFilter();
		stateFilter.addAction("com.hri.statuschanged");
		registerReceiver(stateChangeReceiver, stateFilter);
		
		haveNewAlarmReceiver = new HaveNewAlarmReceiver();
		IntentFilter alarmFilter = new IntentFilter();
		alarmFilter.addAction("ess.com.hri.havenewalarm");
		registerReceiver(haveNewAlarmReceiver, alarmFilter);
		
		//dialog = new ProgressDialog(this);
		//dialog.setIndeterminate(true);
		try {
			BDAutoUpdateSDK.cpUpdateCheck(MainActivity.this, new MyCPCheckUpdateCallback());	//启动APP先检测版本
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		
	}

	private void initChannelImg() {
		getChRealImgLogic = new GetChRealImgBusinessLogic(this);
		opts = new BitmapFactory.Options();;
		opts.outWidth = 88;
		opts.outHeight = 72;
		final int sizeChList = channelList.size();
		PlayVideoActivity.InitVideoList(sizeChList);
		for(int i=0;i<sizeChList;i++){
			
			final byte ch = (byte) i;
			singleThreadExecutor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						imgDataSrc = getChRealImgLogic.executionGetChRealImg(ch);
						if(imgDataSrc != null){
							bitmap = BitmapFactory.decodeByteArray(imgDataSrc, 0, 
									imgDataSrc.length, opts);
							PlayVideoActivity.setVideoImg(bitmap, ch);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
		
	}
	private static final String LEFT_MENU_TAG = "left_menu_fragment";
	private static final String HOME_TAG = "home_fragment";

	private static final String TAG = "MainActivity";
	/**
	 * 初始化SlidingMenu
	 */
	private void initSlidingMenu() {
		setBehindContentView(R.layout.layout_left_menu);
		
		sm = getSlidingMenu();
		sm.setMode(SlidingMenu.LEFT);//设置SlidingMenu只能从左边划出
		sm.setShadowDrawable(R.drawable.shadow);//设置分割阴影图片
		sm.setShadowWidthRes(R.dimen.shadow_width);//设置分割线宽�?		
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);//设置主屏幕剩余宽�?		
		sm.setFadeDegree(0.25f);//设置手势模式
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);//设置SlidingMenu全屏可以划出
		
		//初始化HomeFragment和LeftFragment
		fm = getSupportFragmentManager().beginTransaction();
		fm.replace(R.id.sliding_left_menu, new LeftMenuFragment(), LEFT_MENU_TAG);
		fm.replace(R.id.content, new HomeFragment(), HOME_TAG);
		fm.commit();
	}
	private long nowTime = 0;
	/**
	 * 按返回键回到桌面
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			if ((System.currentTimeMillis() - nowTime) > 2000) {
				Toast.makeText(this, "再按一次返回桌面", 0).show();
				nowTime = System.currentTimeMillis();
			} else {
				Intent home = new Intent(Intent.ACTION_MAIN);
				home.addCategory(Intent.CATEGORY_HOME);
				startActivity(home);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	private AlertDialog alarmDialog;
	protected void onResume() {
		//保存报警对话框需要的上下�?		
		VillaApplication.mContext = this;
		super.onResume();
	}
	protected void onDestroy() {
		Log.i(TAG, "�?��Mainctivity");
		//取消注册广播
		if(stateChangeReceiver!=null){
			unregisterReceiver(stateChangeReceiver);
		}
		if(haveNewAlarmReceiver!=null){
			unregisterReceiver(haveNewAlarmReceiver);
		}
		//�?��心跳
		Intent intent = new Intent(this,SecurityService.class);
		stopService(intent);

		super.onDestroy();
	}
	//singleTask回调
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}
	public void openMenu() {
		sm.toggle();
	}
	//设备状�?变更接收
	private class DeviceStateChangeReceiver extends BroadcastReceiver{
		public void onReceive(Context context, Intent intent) {
			//通知界面
			int state = intent.getIntExtra("deviceState", 0);
			((HomeFragment) getSupportFragmentManager().findFragmentByTag(HOME_TAG)).changeDeviceState(state);
		}
	}
	private class HaveNewAlarmReceiver extends BroadcastReceiver{
		public void onReceive(Context context, Intent intent) {
			
			String alarmId = intent.getStringExtra("alarmId");
			boolean isAlarm = intent.getBooleanExtra("isAlarm", false);
			//弹出通知			
			alarmNotify(isAlarm);
			//新的警情接收
			Log.i(TAG, "通知界面更新警情或出入提示列表");
			((HomeFragment)getSupportFragmentManager().findFragmentByTag(HOME_TAG)).notifyHaveNewAlarm(alarmId,isAlarm);
		}
	}
	public void alarmNotify(boolean isAlarm) {
		alarmNotification =new Notification(R.drawable.villasecurity_icon,  "通知",0);
		alarmNotification.flags |= Notification.FLAG_ONGOING_EVENT;
		alarmNotification.flags |= Notification.FLAG_AUTO_CANCEL;
		alarmNotification.flags |= Notification.FLAG_INSISTENT;
		
		
		Intent notificationIntent = new Intent(this, MainActivity.class);
		pendingIntent = PendingIntent.getActivity(this, 0,notificationIntent, 0);
		if(isAlarm){
			alarmNotification.setLatestEventInfo(this, "警报", "有新的报警，请抓紧处理",pendingIntent);
			manager.notify(1, alarmNotification);
		}else{
			alarmNotification.setLatestEventInfo(this, "提示", "有新的出入记录，请查看",pendingIntent);
			manager.notify(0, alarmNotification);
		}
		
	}
	public void clearNotify() {
		manager.cancelAll();
	}
	
	private class MyCPCheckUpdateCallback implements CPCheckUpdateCallback {

		@Override
		public void onCheckUpdateCallback(AppUpdateInfo info, AppUpdateInfoForInstall infoForInstall) {
			if(info != null) {
				//tUT.start();
				//BDAutoUpdateSDK.asUpdateAction(MainActivity.this, new MyUICheckUpdateCallback()); //百度助手升级
				BDAutoUpdateSDK.uiUpdateAction(MainActivity.this, new MyUICheckUpdateCallback());

			}else if(info == null && infoForInstall == null && isPressed_btn_update) {
				Toast.makeText(MainActivity.this, "已是最新版本！", 0).show();
			}else {
				//txt_log.setText(txt_log.getText() + "\n no update.");
			}
			//dialog.dismiss();
		}
	}
	
	private class MyUICheckUpdateCallback implements UICheckUpdateCallback {

		@Override
		public void onCheckComplete() {
			//dialog.dismiss();
		}
	}
}
