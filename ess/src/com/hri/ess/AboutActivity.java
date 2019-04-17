package com.hri.ess;

import h264.com.H264MediaPlayer;

import com.hri.ess.app.VillaApplication;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 关于Activity
 * @author zhuqian
 *
 */
public class AboutActivity extends Activity implements OnClickListener {
	
	private TextView tv_version,version_info;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		VillaApplication.getApp().addActivity(this);
		
		setContentView(R.layout.activity_about);
		
		tv_version = (TextView) findViewById(R.id.tv_version);
		version_info = (TextView) findViewById(R.id.version_info);
		
		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(),0);
			tv_version.setText(packageInfo.versionName);
			version_info.setText("视频报警器 "+packageInfo.versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		findViewById(R.id.about_back).setOnClickListener(this);
		findViewById(R.id.about_version).setOnClickListener(this);
		findViewById(R.id.about_function).setOnClickListener(this);
		
		tv_version.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				String t = H264MediaPlayer.getDecLibVer();
				Toast.makeText(AboutActivity.this, "视频解码库版本号："+t, 1).show();
			}
		});
	}
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.about_back:
			//返回主界�?
			Intent intent =new Intent(this,MainActivity.class);
			startActivity(intent);
			break;

		default:
			break;
		}
	}
	protected void onResume() {
		//保存报警对话框需要的上下�?
		VillaApplication.mContext = this;
		super.onResume();
	}
}
