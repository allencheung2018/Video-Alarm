package com.hri.ess.fragment;

import com.baidu.autoupdatesdk.AppUpdateInfo;
import com.baidu.autoupdatesdk.AppUpdateInfoForInstall;
import com.baidu.autoupdatesdk.BDAutoUpdateSDK;
import com.baidu.autoupdatesdk.CPCheckUpdateCallback;
import com.baidu.autoupdatesdk.UICheckUpdateCallback;
import com.hri.ess.AboutActivity;
import com.hri.ess.LoginActivity;
import com.hri.ess.MainActivity;
import com.hri.ess.R;
import com.hri.ess.app.VillaApplication;
import com.hri.ess.util.SharePrefUtil;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * 左侧菜单页面
 * 
 * @author zhuqian
 * 
 */
public class LeftMenuFragment extends BaseFragment implements
		OnCheckedChangeListener, OnClickListener {

	public static final String TAG = "LeftMenuFragment";
	private Button left_menu_changeuser, left_menu_about,
			left_menu_exit;
	public Button menu_update;
	private CheckBox cb_auto_start, cb_alert_door, cb_auto_login;
	
	private TextView left_menu_usertext,left_menu_nick;
	
	private ProgressDialog dialog;		//百度更新对话框

	public View initView(LayoutInflater inflater) {
		view = inflater.inflate(R.layout.layout_fragment_left_menu, null);
		
		initViews();
		return view;
	}

	private void initViews() {
		left_menu_usertext = (TextView) view.findViewById(R.id.left_menu_usertext);
		left_menu_nick = (TextView) view.findViewById(R.id.left_menu_nick);
		left_menu_changeuser = (Button) view.findViewById(R.id.left_menu_changeuser);
		left_menu_about = (Button) view.findViewById(R.id.left_menu_about);
		left_menu_exit = (Button) view.findViewById(R.id.left_menu_exit);
		menu_update = (Button) view.findViewById(R.id.left_menu_update);
		
		cb_auto_start = (CheckBox) view.findViewById(R.id.cb_auto_start);
		cb_alert_door = (CheckBox) view.findViewById(R.id.cb_alert_door);
		cb_auto_login = (CheckBox) view.findViewById(R.id.cb_auto_login);
		
		left_menu_usertext.setText(SharePrefUtil.getString(context, "username", ""));
		left_menu_nick.setText(SharePrefUtil.getString(context, "nick", "未知.."));
	}

	public void initData(Bundle savedInstanceState) {

		cb_auto_login.setChecked(SharePrefUtil.getBoolean(context,
				"is_autologin", false));
		cb_alert_door.setChecked(SharePrefUtil.getBoolean(context,
				"is_alertdoor", true));
		cb_auto_start.setChecked(SharePrefUtil.getBoolean(context, "is_autostart", false));

		cb_auto_start.setOnCheckedChangeListener(this);
		cb_alert_door.setOnCheckedChangeListener(this);
		cb_auto_login.setOnCheckedChangeListener(this);

		left_menu_changeuser.setOnClickListener(this);
		left_menu_about.setOnClickListener(this);
		left_menu_exit.setOnClickListener(this);
		menu_update.setOnClickListener(this);
	}

	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.cb_auto_start:
			// 保存是否�?��自启
			SharePrefUtil.saveBoolean(context, "is_autostart", isChecked);
			break;
		case R.id.cb_alert_door:
			// 保存是否�?���?��门提�?
			SharePrefUtil.saveBoolean(context, "is_alertdoor", isChecked);
			break;
		case R.id.cb_auto_login:
			// 保存是否自动登录
			SharePrefUtil.saveBoolean(context, "is_autologin", isChecked);
			break;
		default:
			break;
		}
	}

	private AlertDialog exitDialog;

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.left_menu_changeuser:
			// 切换账号
			((MainActivity)context).finish();
			SharePrefUtil.saveBoolean(context, "is_autologin", false);		//取消自动登陆
			Intent loginIntent = new Intent(context,LoginActivity.class);
			context.startActivity(loginIntent);
			//�?��MainActivity
			break;
		case R.id.left_menu_about:
			// 关于
			Intent intent = new Intent(context,AboutActivity.class);
			context.startActivity(intent);
			break;
		case R.id.left_menu_update:
			dialog = new ProgressDialog(context);
			dialog.setIndeterminate(true);
			dialog.setMessage("检查更新!");
			dialog.show();
			BDAutoUpdateSDK.cpUpdateCheck(context, new MyCPCheckUpdateCallback());
			break;
		case R.id.left_menu_exit:
			// �?��
			if (exitDialog == null) {
				exitDialog = new AlertDialog.Builder(context).create();
			}
			exitDialog.show();
			exitDialog.getWindow().setContentView(
					R.layout.layout_custom_alertdialog);
			exitDialog.getWindow().findViewById(R.id.negativeButton)
					.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							exitDialog.dismiss();
						}
					});
			exitDialog.getWindow().findViewById(R.id.positiveButton)
					.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							((MainActivity)context).clearNotify();
							exitDialog.dismiss();
							//�?��
							VillaApplication.getApp().exit(context);
						}
					});
			break;
		default:
			break;
		}
	}
	
	private class MyCPCheckUpdateCallback implements CPCheckUpdateCallback {

		@Override
		public void onCheckUpdateCallback(AppUpdateInfo info, AppUpdateInfoForInstall infoForInstall) {
			Log.i(TAG, "info:"+info + " infoForInstall:"+infoForInstall);
			if(info != null) {
				//tUT.start();
				//BDAutoUpdateSDK.asUpdateAction(MainActivity.this, new MyUICheckUpdateCallback()); //百度助手升级
				BDAutoUpdateSDK.uiUpdateAction(context, new MyUICheckUpdateCallback());

			}else if(info == null && infoForInstall == null) {
				Toast.makeText(context, "已是最新版本！", 0).show();
			}else {
				//txt_log.setText(txt_log.getText() + "\n no update.");
			}
			dialog.dismiss();
		}
	}
	
	private class MyUICheckUpdateCallback implements UICheckUpdateCallback {

		@Override
		public void onCheckComplete() {
			dialog.dismiss();
		}
	}
}
