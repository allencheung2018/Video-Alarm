package com.hri.ess.receiver;

import com.hri.ess.util.SharePrefUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
/**
 * 开机自启广播
 * @author zhuqian
 *
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = "BootBroadcastReceiver";

	public void onReceive(Context context, Intent intent) {
		//判断是否需要开机自启
		boolean is_autostart = SharePrefUtil.getBoolean(context, "is_autostart", false);
		if(is_autostart){
			Log.i(TAG, "开机自启中.....");
			
			Intent startIntent = context.getPackageManager().getLaunchIntentForPackage("com.hri.skyeyesvillasecurity");
			context.startActivity(startIntent);
			
			//防止程序崩溃，取消自动登录
			SharePrefUtil.saveBoolean(context, "is_autologin", false);
		}else{
			Log.i(TAG, "用户不需要开机自启");
		}
	}
}
