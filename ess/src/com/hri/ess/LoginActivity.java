package com.hri.ess;

import java.util.ArrayList;
import java.util.List;

import com.baidu.autoupdatesdk.AppUpdateInfo;
import com.baidu.autoupdatesdk.AppUpdateInfoForInstall;
import com.baidu.autoupdatesdk.BDAutoUpdateSDK;
import com.baidu.autoupdatesdk.CPCheckUpdateCallback;
import com.baidu.autoupdatesdk.UICheckUpdateCallback;
import com.hri.ess.app.VillaApplication;
import com.hri.ess.businesslogic.GetChannelBusinessLogic;
import com.hri.ess.businesslogic.GetDeviceInfoBusinessLogic;
import com.hri.ess.businesslogic.LoginBusinessLogic;
import com.hri.ess.dbservice.control.LoginUserControl;
import com.hri.ess.dbservice.domain.LoginUser;
import com.hri.ess.util.SharePrefUtil;
import com.hri.ess.util.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 登录Activity
 * 
 * @author zhuqian
 */
public class LoginActivity extends Activity implements OnClickListener {
	public static final String TAG = "LoginActivity";
	private EditText name_actxt;
	private EditText psw_etxt;
	private TextView setting_Ip;

	private Button login_btn;
	private LoginUserControl loginHelper;
	private ImageView iv_login_logo, selectId;

	private CheckBox login_cb_rbpwd, login_cb_autologin;

	private PopupWindow ipDialog;// 配置ip对话�?

	private AlertDialog loadingDialog;

	private EditText ip_et;

	private GetDeviceInfoBusinessLogic getDeviceInfoBl;
	private LoginBusinessLogic loginBl;
	private GetChannelBusinessLogic getChannelBl;

	private String deviceCode;// 设备�?
	private ArrayList<String> channelList;// 通道列表

	private String username;// 用户�?
	private String pwd;// 密码
	private ListView listView;

	private PopupWindow userPop;
	private LinearLayout loginRoot;
   
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		// 添加
		VillaApplication.getApp().addActivity(this);

		getDeviceInfoBl = new GetDeviceInfoBusinessLogic(this);
		loginBl = new LoginBusinessLogic(this);
		getChannelBl = new GetChannelBusinessLogic(this);
		loginHelper = new LoginUserControl(this);
		initViews();
		initData();
		initListener();
		checkNewVersion();
	}

	public void checkNewVersion() {
		BDAutoUpdateSDK.cpUpdateCheck(this, new MyCPCheckUpdateCallback());
	}
	
	private class MyCPCheckUpdateCallback implements CPCheckUpdateCallback {

		@Override
		public void onCheckUpdateCallback(AppUpdateInfo info, AppUpdateInfoForInstall infoForInstall) {
			Log.i(TAG, "info:"+info.getAppVersionName() + " infoForInstall:"+infoForInstall);
			if(info != null) {
				//tUT.start();
				//BDAutoUpdateSDK.asUpdateAction(MainActivity.this, new MyUICheckUpdateCallback()); //百度助手升级
				BDAutoUpdateSDK.uiUpdateAction(LoginActivity.this, new MyUICheckUpdateCallback());

			}else if(info == null && infoForInstall == null) {
				Toast.makeText(LoginActivity.this, "已是最新版本！", 0).show();
			}else {
				//txt_log.setText(txt_log.getText() + "\n no update.");
			}
		}
	}
	
	private class MyUICheckUpdateCallback implements UICheckUpdateCallback {

		@Override
		public void onCheckComplete() {
			Log.i(TAG, "onCheckComplete");
		}
	}

	private void initData() {
		listView = new ListView(this);
		listView.setFocusable(true);
		listView.setFocusableInTouchMode(true);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				LoginUser user = (LoginUser) listView
						.getItemAtPosition(position);
				name_actxt.setText(user.getName());
				psw_etxt.setText(user.getPwd());
				name_actxt.setSelection(user.getName().length());
				hidePop();
			}
		});
		users = loginHelper.queryAll();
	}

	private static final int LOGIN_SUCCESS = 200;
	private static final int LOGIN_ERROR = 202;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case LOGIN_SUCCESS:
				if (loadingDialog.isShowing()) {
					loadingDialog.dismiss();
				}
				// 添加用户
				LoginUser login = new LoginUser();
				login.setName(username);
				login.setPwd(pwd);
				loginHelper.add(login);
				// 登录成功
				Intent intent = new Intent(LoginActivity.this,
						MainActivity.class);
				intent.putStringArrayListExtra("channelList", channelList);
				if(channelList.size() >= 0)
					PlayVideoActivity.InitVideoList(channelList.size());
				startActivity(intent);
				LoginActivity.this.finish();
				break;
			case LOGIN_ERROR:
				// 登录失败
				if (loadingDialog.isShowing()) {
					loadingDialog.dismiss();
				}
				Toast.makeText(LoginActivity.this, (String) msg.obj, 0).show();
				break;
			default:
				break;
			}
		};
	};

	// 初始化监�?
	private void initListener() {
		login_btn.setOnClickListener(this);
	}

	// 初始化View
	private void initViews() {
		name_actxt = (EditText) findViewById(R.id.et_user);
		psw_etxt = (EditText) findViewById(R.id.et_pwd);
		login_btn = (Button) findViewById(R.id.login_btn);
		setting_Ip = (TextView) findViewById(R.id.tv_adsettings);
		login_cb_rbpwd = (CheckBox) findViewById(R.id.login_cb_rbpwd);
		login_cb_autologin = (CheckBox) findViewById(R.id.login_cb_autologin);
		iv_login_logo = (ImageView) findViewById(R.id.login_iv_topicon);
		selectId = (ImageView) findViewById(R.id.selectId);
		loginRoot = (LinearLayout) findViewById(R.id.login_root);

		//初始化ip地址
		String ip_addr = SharePrefUtil.getString(this, "ip_config", "");
		//if (TextUtils.isEmpty(SharePrefUtil.getString(this, "ip_config", "")+ "")) 
		if(ip_addr.equals(""))
		{
			SharePrefUtil.saveString(this, "ip_config", "113.106.89.125");
		}
		// 填充用户�?
		name_actxt.setText(SharePrefUtil.getString(this, "username", ""));

		name_actxt.addTextChangedListener(new TextWatcher() {
			private int oldLen;

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				oldLen = s.length();
			}

			public void afterTextChanged(Editable s) {
				String text = s.toString();
				if (oldLen - s.length() == 1) {
					psw_etxt.setText("");
				}
			}
		});

		// 填充密码
		if (SharePrefUtil.getBoolean(this, "is_rbpwd", false)) {
			psw_etxt.setText(SharePrefUtil.getString(this, "pwd", ""));
		}

		setting_Ip.setOnClickListener(this);
		selectId.setOnClickListener(this);
		login_cb_rbpwd.setChecked(SharePrefUtil.getBoolean(this, "is_rbpwd",
				false));
		login_cb_autologin.setChecked(SharePrefUtil.getBoolean(this,
				"is_autologin", false));

		login_cb_autologin
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// 自动登录监听
						SharePrefUtil.saveBoolean(LoginActivity.this,
								"is_autologin", isChecked);
					}
				});
		login_cb_rbpwd
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// 记住密码监听
						SharePrefUtil.saveBoolean(LoginActivity.this,
								"is_rbpwd", isChecked);
					}
				});

		// 是否�?��自动登录
		if (SharePrefUtil.getBoolean(this, "is_autologin", false)) {

			psw_etxt.setText(SharePrefUtil.getString(this, "pwd", ""));
			username = name_actxt.getText().toString();
			pwd = psw_etxt.getText().toString();
			// 防止没有填充也登�?
			if (TextUtils.isEmpty(username) || TextUtils.isEmpty(pwd)) {
				return;
			}
			startLogin();
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.login_btn:
			// 登录

			if (TextUtils.isEmpty(name_actxt.getText().toString())
					|| TextUtils.isEmpty(psw_etxt.getText().toString())) {
				Toast.makeText(this, "请输入用户名或密码", 0).show();
				return;
			}
			if (TextUtils.isEmpty(SharePrefUtil
					.getString(this, "ip_config", ""))) {
				Toast.makeText(this, "请输入IP", 0).show();
				return;
			}
			// �?��登陆
			startLogin();
			break;
		case R.id.tv_adsettings:
			// 显示配置ip对话�?
			if (ipDialog == null) {
				View view = LayoutInflater.from(this).inflate(
						R.layout.dialog_fav, null);
				ipDialog = new PopupWindow(view, LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT);
				ipDialog.setFocusable(true);
				ipDialog.setOutsideTouchable(true);
				ipDialog.setBackgroundDrawable(new ColorDrawable(0xFFE1E6F6));
			}

			ipDialog.showAtLocation(loginRoot, Gravity.CENTER, 0, 0);

			// 确定和取消按钮监�?
			ipDialog.getContentView().findViewById(R.id.btn_ok)
					.setOnClickListener(this);
			ipDialog.getContentView().findViewById(R.id.btn_can)
					.setOnClickListener(this);

			ip_et = (EditText) ipDialog.getContentView().findViewById(
					R.id.et_fav);
			String ip_addr = SharePrefUtil.getString(this, "ip_config", "");
			if(!ip_addr.isEmpty()){
				ip_et.setText(ip_addr);
			}
			break;
		case R.id.btn_can:
			if (ipDialog.isShowing()) {
				ipDialog.dismiss();
			}
			break;
		case R.id.btn_ok:
			String ip_address = ip_et.getText().toString();
			if (TextUtils.isEmpty(ip_address)) {
				// 输入为空
				Toast.makeText(LoginActivity.this, "请输入IP", 0).show();
				return;
			} else if (!Util.isIPAddress(ip_address)) {
				Toast.makeText(LoginActivity.this, "请输入正确IP", 0).show();
				// 不是正确Ip格式
				return;
			}
			SharePrefUtil.saveString(LoginActivity.this, "ip_config",
					ip_address);
			if (ipDialog.isShowing()) {
				ipDialog.dismiss();
			}
			Toast.makeText(LoginActivity.this, "保存成功", 0).show();
			break;
		case R.id.selectId:
			// 选择用户监听
			if (userPop != null && userPop.isShowing()) {
				// 隐藏Pop
				hidePop();
			} else {
				// 显示pop
				showPop();
			}
			break;
		default:
			break;
		}
	}

	private UsernameAdapter mAdapter;
	private List<LoginUser> users;

	private void showPop() {
		if (userPop == null) {
			userPop = new PopupWindow(listView, this.name_actxt.getWidth(),
					AbsListView.LayoutParams.WRAP_CONTENT);
			userPop.setFocusable(true);
			userPop.setOutsideTouchable(true);
			userPop.setBackgroundDrawable(new BitmapDrawable());
			userPop.setOnDismissListener(new OnDismissListener() {
				public void onDismiss() {
					selectId.setImageResource(R.drawable.qz_icon_navbar_drop_down);
				}
			});
			mAdapter = new UsernameAdapter(users);
			listView.setAdapter(mAdapter);
		}
		mAdapter.notifyDataSetChanged();
		userPop.showAsDropDown(name_actxt);
		selectId.setImageResource(R.drawable.qz_icon_navbar_drop_up);
	}

	private void hidePop() {
		if (userPop != null && userPop.isShowing()) {
			userPop.dismiss();
		}
	}

	// 执行登录
	private void startLogin() {
		if (loadingDialog == null) {
			loadingDialog = new AlertDialog.Builder(this).create();
		}
		loadingDialog.show();
		loadingDialog.getWindow().setContentView(
				R.layout.cs_dialog_comm_login_loading);

		username = name_actxt.getText().toString();
		pwd = psw_etxt.getText().toString();

		SharePrefUtil.saveString(this, "username", username);
		SharePrefUtil.saveString(this, "pwd", pwd);
		new Thread() {

			public void run() {
				try {
					// 获取设备�?
					deviceCode = getDeviceInfoBl.executionGetDevice();
					// 保存设备�?
					SharePrefUtil.saveString(LoginActivity.this, "deviceCode",
							deviceCode);
					// 执行登录
					loginBl.executionLogin(username, pwd);
					channelList = getChannelBl.executionGetChannel();
					// 保存到app�?
					VillaApplication.channelList = channelList;

					mHandler.sendEmptyMessage(LOGIN_SUCCESS);
				} catch (Exception e) {
					e.printStackTrace();
					if (e.getMessage() != null
							&& e.getMessage().equals("请检查当前网络状..")) {
						Message msg = mHandler.obtainMessage();
						msg.what = LOGIN_ERROR;
						msg.obj = "请检查当前网络状态";
						mHandler.sendMessage(msg);
					} else if (e.getMessage() != null) {
						Message msg = mHandler.obtainMessage();
						msg.what = LOGIN_ERROR;
						//msg.obj = "登录失败";
						msg.obj = e.getMessage();
						mHandler.sendMessage(msg);
					}
				}
			};
		}.start();
	}

	private class UsernameAdapter extends BaseAdapter {
		private List<LoginUser> users;

		public UsernameAdapter(List<LoginUser> users) {
			this.users = users;
		}

		public int getCount() {
			return users.size();
		}

		public Object getItem(int position) {
			return users.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final LoginUser user = users.get(position);
			if (convertView == null) {
				convertView = View.inflate(LoginActivity.this,
						R.layout.cs_item_login_popup, null);
			}
			TextView popUser = (TextView) convertView.findViewById(R.id.popQQ);
			ImageView popDelete = (ImageView) convertView
					.findViewById(R.id.popQQDelete);
			popDelete.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					users.remove(user);
					loginHelper.delete(user);
					users = loginHelper.queryAll();
					mAdapter.notifyDataSetChanged();
					name_actxt.setText("");
					psw_etxt.setText("");
				}
			});
			popUser.setText(user.getName());
			return convertView;
		}
	}

	protected void onNewIntent(Intent intent) {
		System.out.println("复用LoginActivity");
		name_actxt.setText("");
		psw_etxt.setText("");
	}

	protected void onDestroy() {
		System.out.println("Loginactivity");
		super.onDestroy();
	}
}
