package com.hri.ess;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.hri.ess.PlayVideoActivity.OnLineVideoListener;
import com.hri.ess.PlayVideoActivity.OnVideoDecordListener;
import com.hri.ess.app.VillaApplication;
import com.hri.ess.dbservice.domain.AlarmDetailed;
import com.hri.ess.util.SharePrefUtil;
import com.hri.ess.util.Util;

import h264.com.H264MediaPlayer;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
/**
 * 播放报警视频Activity
 * @author zhuqian
 *
 */
public class AlarmVideoActivity extends Activity implements OnClickListener{
	private H264MediaPlayer videoPlay;
	private RelativeLayout video_nostream_ad;//没有视频流的时�?显示
	private ProgressBar video_error_progress;//播放出错
	private RelativeLayout video_error_view;//播放出错显示
	private RelativeLayout video_loading_view;//正在加载显示
	
	private TextView video_back;//顶部操作栏返�?
	private TextView video_channelName;//通道名称
	private AlarmDetailed alarm;
	private TextView video_loading_info, video_error_info;
	private boolean isRepeatPlay = false;
	
	
	private RelativeLayout video_top_operation;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm_video);
		
		
		alarm = VillaApplication.alarm;
		initViews();
		
		startVideoTap();
	}
	private static final int VIDEODECORD = 200;
	private static final int VIDEOSTART = 202;
	
	private static final int DISSMENU = 300;
	private static final int VIDEOERROR = 303;
	
	private static final int VIDEOBUFFERING = 501;	//缓冲
	private static final int VIDEOBUFFERED = 502;	//缓冲完成
	private static final int VIDEOTAPFINISH = 503;	//录像播放结束
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case VIDEODECORD:
				video_loading_info.setText("视频正在解码中...请稍候...");
				break;
			case VIDEOSTART:
				video_error_view.setVisibility(View.GONE);
				video_loading_view.setVisibility(View.GONE);
				video_nostream_ad.setVisibility(View.GONE);
//				video_top_operation.setVisibility(View.GONE);
				break;
			case VIDEOERROR:
				//播放出错
				video_error_view.setVisibility(View.VISIBLE);
				video_loading_view.setVisibility(View.GONE);
				video_nostream_ad.setVisibility(View.VISIBLE);
				video_error_info.setText("播放失败");
				video_top_operation.setVisibility(View.GONE);
				break;
			case DISSMENU:
				//隐藏顶部菜单
				video_top_operation.setVisibility(View.GONE);
				break;
			case VIDEOBUFFERING:
				//缓冲字样
				if(video_loading_view.isShown()){
					break;
				}
				video_loading_view.setVisibility(View.VISIBLE);
				video_loading_info.setText("视频缓冲…");
				break;
			case VIDEOBUFFERED:
				if(video_loading_view.isShown()){
					video_loading_view.setVisibility(View.GONE);
				}
				break;
			case VIDEOTAPFINISH:
				video_error_view.setVisibility(View.VISIBLE);
				video_error_info.setText("本次录像播放完毕!请点击重播");
				isRepeatPlay = true;
				break;
			default:
				break;
			}
		};
	};
	private void startVideoTap() {
		video_nostream_ad.setVisibility(View.VISIBLE);
		video_error_view.setVisibility(View.GONE);
		video_loading_view.setVisibility(View.VISIBLE);
		new Thread(){
			public void run() {
				try {
					videoPlay.playVideoTape();
				} catch (Exception e) {
					e.printStackTrace();
					mHandler.sendEmptyMessage(VIDEOERROR);
				}
			};
		}.start();
	}
	/**
	 * 发送停止播放报警录像。
	 * @param position
	 */
	protected void stopVideoTap() {
		new Thread() {
			public void run() {
				try {
					videoPlay.stopTapVideo();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	//初始化View方法
	private void initViews() {
		videoPlay = (H264MediaPlayer) findViewById(R.id.videoPlay);
		video_nostream_ad = (RelativeLayout) findViewById(R.id.video_nostream_ad);
		video_error_progress = (ProgressBar) findViewById(R.id.video_error_progress);
		video_error_view = (RelativeLayout) findViewById(R.id.video_error_view);
		video_loading_view = (RelativeLayout) findViewById(R.id.video_loading_view);
		video_back = (TextView) findViewById(R.id.video_back);
		video_error_info = (TextView) findViewById(R.id.video_error_info);
		video_channelName = (TextView) findViewById(R.id.video_channelName);
		video_loading_info = (TextView) findViewById(R.id.video_loading_info);
		video_top_operation = (RelativeLayout) findViewById(R.id.video_top_operation);
		
		//video_channelName.setText(VillaApplication.alarm.getChannelName());
		video_channelName.setText(VillaApplication.channelList.get(alarm.getChannelNum()));
		
		video_back.setOnClickListener(this);
		video_error_progress.setOnClickListener(this);
		
		//初始化h264播放参数
		DateFormat fmt =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		long startTime;
		try {
			//向前�?5�?
			startTime = Util.date2FileTime(fmt.parse(alarm.getAlarmtime()))-15*1000*10000;
			videoPlay.setVideoTapeInfo(alarm.getChannelNum(), (byte)3, (byte)0, startTime,
					(short)15, alarm.getAlarmId(), SharePrefUtil.getString(this, "deviceCode", ""));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		videoPlay.setOnVideoDecordListener(new OnVideoDecordListener() {
			public void onVDecord() {
				//解码视频回调
				mHandler.sendEmptyMessage(VIDEODECORD);
			}
		});
		videoPlay.setOnLineVideoListener(new OnLineVideoListener() {
			public void onVideoReceive() {
				mHandler.sendEmptyMessage(VIDEOSTART);
			}
			public void onPlayfailed(int errMsg, byte numChn){
				mHandler.sendEmptyMessage(errMsg);
			}
		});
		
		videoPlay.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//显示顶部导航
				video_top_operation.setVisibility(View.VISIBLE);
				mHandler.removeMessages(DISSMENU);
				Message dissMsg = mHandler.obtainMessage();
				dissMsg.what = DISSMENU;
				mHandler.sendMessageDelayed(dissMsg, 4*1000);
			}
		});
	}
	protected void onDestroy() {
		stopVideoTap();
		Intent stopIntent = new Intent("com.hri.ess.videostop");
		sendBroadcast(stopIntent);
		super.onDestroy();
	}
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.video_back:
			//返回
			finish();
			break;
		case R.id.video_error_progress:
			//重新请求录像
			startVideoTap();
			break;
		default:
			break;
		}
	}
}
