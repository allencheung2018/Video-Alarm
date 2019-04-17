package com.hri.ess;

import h264.com.H264MediaPlayer;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.hri.ess.adapter.VideoPagerAdapter;
import com.hri.ess.util.SharePrefUtil;
import com.hri.ess.util.Util;
import com.hri.ess.view.LazyViewPager;
import com.hri.ess.view.LazyViewPager.OnPageChangeListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

/**
 * 播放视频Activity
 * 
 * @author zhuqian
 * 
 */
public class PlayVideoActivity extends Activity implements OnClickListener {

	private List<String> channelList;
	private static List<Bitmap> videoimglist = new ArrayList<Bitmap>();	//视频缩略图

	private LinearLayout video_bottom_operation;// 底部操作View

	private RelativeLayout video_top_operation;// 顶部操作View

	private RelativeLayout video_nostream_ad;// 没有视频时�?显示

	private RelativeLayout video_error_view;// 播放失败View
	private RelativeLayout video_loading_view;// 正在连接视频View

	private LazyViewPager videoPagers;

	private TextView video_loading_info, video_channelName, video_back,
			video_download_rate, tv_date, video_error_info;

	private List<H264MediaPlayer> pagers = new ArrayList<H264MediaPlayer>();

	private int onlineVideo;// 是否是实时视频
	private boolean isCreated = false;

	private long startTime;// 播放录像�?��时间
	private short playtime;// 播放时长
	private String alarmId = "";// 报警ID

	private VideoPagerAdapter mPagerAdapter;

	private ProgressBar videoErrorPB;

	private PowerManager pm;
	private PowerManager.WakeLock wakeLock;
	
	private boolean isInitViews = false;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video);
		channelList = getIntent().getStringArrayListExtra("channelList");

		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK,
				LoginActivity.class.getSimpleName());

		channelnum = (byte) getIntent().getIntExtra("channelnum", 0);
		oldChnNum = channelnum;
		onlineVideo = getIntent().getIntExtra("onlineVideo", 0);
		if (onlineVideo == 0) {
			String start = getIntent().getStringExtra("startTime");
			DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				startTime = Util.date2FileTime(fmt.parse(start));
			} catch (ParseException e) {
				e.printStackTrace();
			}// 纳秒转秒(15*1000*10000)
			playtime = getIntent().getShortExtra("playtime", (short) 15);
		} else if (onlineVideo == 2) {
			// 推送过来的报警参数
			alarmId = getIntent().getStringExtra("alarmId");
		}
		initViews();

		if (onlineVideo == 1) {
			// 开始实时视频
			startVideo();
		} else if(onlineVideo == 0) {
			// 开始录像播放
			startVideoTap();
		}
		// 开始计算网速
		calcNetRate();
		isCreated = true;
	}

	

	private static final int VIDEODECORD = 200;
	private static final int VIDEOSTART = 202;

	private static final int DISSMENU = 300;
	private static final int VIDEOERROR = 303;

	private static final int VIDEORATE = 400;
	
	private static final int VIDEOBUFFERING = 501;	//缓冲
	private static final int VIDEOBUFFERED = 502;	//缓冲完成
	private static final int VIDEOTAPFINISH = 503;	//录像播放结束

	private static final String TAG = "PlayVideoActivity";

	private byte channelnum, oldChnNum;

	private List<View> dorts = new ArrayList<View>();
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case VIDEODECORD:
				// 开始解码视频
				video_loading_info.setText("视频正在解码中…请稍候");
				video_loading_view.setVisibility(View.VISIBLE);
				break;
			case VIDEOSTART:
				// �?��播放视频
				haveVideoStream();
				break;
			case DISSMENU:
				// 隐藏菜单
				video_bottom_operation.setVisibility(View.GONE);
				video_top_operation.setVisibility(View.GONE);
				break;
			case VIDEOERROR:
				// 播放失败
				setErrorStream();
				break;
			case VIDEORATE:
				// 计算网�?
				if (video_download_rate.isShown()) {
					video_download_rate.setText(rxRate + "kb/s");
				}
				tv_date.setText(Util.getNowDateTime());
				calcNetRate();
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
				showTapFinish();
				break;
			default:
				break;
			}
		};
	};

	private void initViews() {
		videoPagers = (LazyViewPager) findViewById(R.id.video_pagers);
		video_bottom_operation = (LinearLayout) findViewById(R.id.video_bottom_operation);
		video_top_operation = (RelativeLayout) findViewById(R.id.video_top_operation);
		video_nostream_ad = (RelativeLayout) findViewById(R.id.video_nostream_ad);
		video_error_view = (RelativeLayout) findViewById(R.id.video_error_view);
		video_loading_view = (RelativeLayout) findViewById(R.id.video_loading_view);
		video_back = (TextView) findViewById(R.id.video_back);
		video_channelName = (TextView) findViewById(R.id.video_channelName);
		video_loading_info = (TextView) findViewById(R.id.video_loading_info);
		video_error_info = (TextView) findViewById(R.id.video_error_info);
		videoErrorPB = (ProgressBar) findViewById(R.id.video_error_progress);
		video_download_rate = (TextView) findViewById(R.id.video_download_rate);
		tv_date = (TextView) findViewById(R.id.tv_date);

		tv_date.setText(Util.getNowDateTime());
		videoErrorPB.setOnClickListener(this);

		String channelName = channelList.get(channelnum);
		// 去除[]和里面的内容
		if (channelName.contains("[")) {
			channelName = channelName
					.substring(0, channelName.indexOf("[") - 1);
		} else {
			channelName = channelName.substring(0, channelName.length() - 1);
		}
		video_channelName.setText(channelName);

		// 添加点
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT));
		params.leftMargin = 8;
		params.rightMargin = 8;
		for (int i = 0; i < channelList.size(); i++) {
			H264MediaPlayer player = new H264MediaPlayer(this);
			player.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// 显示底部和顶部菜单
					video_top_operation.setVisibility(View.VISIBLE);
					video_bottom_operation.setVisibility(View.VISIBLE);
					mHandler.removeMessages(DISSMENU);
					Message dissMsg = mHandler.obtainMessage();
					dissMsg.what = DISSMENU;
					mHandler.sendMessageDelayed(dissMsg, 4 * 1000);
				}
			});
			player.setOnVideoDecordListener(new OnVideoDecordListener() {
				public void onVDecord() {
					mHandler.sendEmptyMessage(VIDEODECORD);
				}
			});
			player.setOnLineVideoListener(new OnLineVideoListener() {
				public void onVideoReceive() {
					mHandler.sendEmptyMessage(VIDEOSTART);
				}
				public void onPlayfailed(int errMsg, byte numChn){
					oldChnNum = numChn;
					mHandler.sendEmptyMessage(errMsg);
				}
			});
			String chn = channelList.get(i);
			if (onlineVideo == 1) {
				// 设置播放实时视频参数
				byte chnum = (byte) i;
				player.setVideoInfo(chnum, (byte) 3, (byte) 0,
						SharePrefUtil.getString(this, "deviceCode", ""));
			} else {
				// 设置播放录像参数
				// player.setVideoTapeInfo((byte)Integer.parseInt(chn.substring(chn.length()-1,
				// chn.length())), (byte) 2, (byte)1,
				// startTime, playtime, alarmId,
				// SharePrefUtil.getString(this, "deviceCode", ""));
				player.setVideoTapeInfo((byte) i, (byte) 3, (byte) 0,
						startTime, playtime, alarmId,
						SharePrefUtil.getString(this, "deviceCode", ""));
			}
			pagers.add(player);
			View dort = LayoutInflater.from(this).inflate(
					R.layout.layout_channel_dort, null);
			dorts.add(dort);
			if ((byte) i == channelnum) {
				dort.setBackgroundResource(R.drawable.video_channel_dort_select);
			} else {
				dort.setBackgroundResource(R.drawable.video_channel_dort_normal);
			}
			video_bottom_operation.addView(dort, params);
		}
		if (mPagerAdapter == null) {
			mPagerAdapter = new VideoPagerAdapter(pagers);
			videoPagers.setAdapter(mPagerAdapter);
		}
		videoPagers.setOnPageChangeListener(new OnPageChangeListener() {
			public void onPageSelected(int position) {
				if(isCreated == false)			//未初始化完成
					return;
				System.out.println("页面改变");
				video_nostream_ad.setVisibility(View.VISIBLE);
				dorts.get(channelnum).setBackgroundResource(
						R.drawable.video_channel_dort_normal);
				dorts.get(position).setBackgroundResource(
						R.drawable.video_channel_dort_select);
				if (onlineVideo == 1) {
					stopVideo(channelnum);
				} else {
					stopVideoTap(channelnum);
				}
				String chn = channelList.get(position);
				channelnum = (byte) position;
				if (onlineVideo == 1) {
					startVideo();
				} else {
					startVideoTap();
				}

				String channelName = channelList.get(position);
				// 去除[]和里面的内容
				if (channelName.contains("[")) {
					channelName = channelName.substring(0,
							channelName.indexOf("[") - 1);
				} else {
					//channelName = channelName.substring(0, channelName.length() - 1);	//这里长度截取1，结果不对，所以注释
				}
				video_channelName.setText(channelName);
			}

			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {

			}

			public void onPageScrollStateChanged(int state) {
				// 根据滑动状�?判断是否显示背景
				if (state == 1) {
					if (video_nostream_ad.isShown()) {
						video_nostream_ad.setVisibility(View.GONE);
					}
				}
			}
		});
		videoPagers.setCurrentItem(channelnum);
		video_back.setOnClickListener(this);
	}

	private void startVideo() {
		
		new Thread() {
			public void run() {
				try {
					pagers.get(channelnum).playVideo();
				} catch (Exception e) {
					e.printStackTrace();
					if (!pagers.get(channelnum).isVideoStop()) {
						System.out.println("不是当前页面出错，返回...");
						//return;
					}
					mHandler.sendEmptyMessage(VIDEOERROR);
				}
			};
		}.start();
		
		video_loading_info.setText("视频正在连接中…请稍候");
		noVideoStream();
	}

	private void stopVideo(final byte position) {
		new Thread() {
			public void run() {
				try {
					pagers.get(position).stopVideo();
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		}.start();
	}

	private void startVideoTap() {
		
		new Thread() {
			public void run() {
				try {
					pagers.get(channelnum).playVideoTape();
				} catch (Exception e) {
					e.printStackTrace();
					if (!pagers.get(channelnum).isVideoStop()) {
						System.out.println("不是当前页面出错，返回...");
						//return;
					}
					mHandler.sendEmptyMessage(VIDEOERROR);
				}
			};
		}.start();
		
		video_loading_info.setText("视频正在连接中…请稍候");
		noVideoStream();
	}
	/**
	 * 发送停止播放录像命令。
	 * @param position
	 */
	protected void stopVideoTap(final byte position) {
		new Thread() {
			public void run() {
				try {
					Log.i("stopVideoTap", "position="+position + " channelnum="+channelnum);
					pagers.get(position).stopTapVideo();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	// 切换到有视频流状�?
	public void haveVideoStream() {
		video_error_view.setVisibility(View.GONE);
		video_loading_view.setVisibility(View.GONE);
		video_nostream_ad.setVisibility(View.GONE);

		//video_bottom_operation.setVisibility(View.GONE);
		//video_top_operation.setVisibility(View.GONE);

		video_download_rate.setVisibility(View.VISIBLE);
		tv_date.setVisibility(View.VISIBLE);
	}

	public void noVideoStream() {
		video_error_view.setVisibility(View.GONE);
		video_loading_view.setVisibility(View.VISIBLE);
		video_nostream_ad.setVisibility(View.VISIBLE);

		video_bottom_operation.setVisibility(View.GONE);
		video_download_rate.setVisibility(View.GONE);
		tv_date.setVisibility(View.GONE);
		video_top_operation.setVisibility(View.GONE);
	}

	// 播放出错
	public void setErrorStream() {
		video_error_info.setText("播放失败");
		video_error_view.setVisibility(View.VISIBLE);
		video_loading_view.setVisibility(View.GONE);
		video_nostream_ad.setVisibility(View.VISIBLE);

		video_bottom_operation.setVisibility(View.GONE);
		video_download_rate.setVisibility(View.GONE);
		video_top_operation.setVisibility(View.GONE);

//		videoErrorPB.setOnClickListener(new OnClickListener() {
//			public void onClick(View v) {
//				video_loading_view.setVisibility(View.VISIBLE);
//				video_error_view.setVisibility(View.GONE);
//				if (onlineVideo == 1) {
//					startVideo();
//				} else {
//					startVideoTap();
//				}
//			}
//		});
	}
	// 播放结束
	public void showTapFinish(){
		video_error_view.setVisibility(View.VISIBLE);
		video_error_info.setText("本次录像播放完毕!请点击重播");
	}
	
	public static void setVideoImg(Bitmap img, int position){
		if(position >= 0){
			videoimglist.set(position, img);
		}
	}
	public static Bitmap getVideoImg(int position){
		if(videoimglist == null || position >= videoimglist.size())
			return null;
		if(videoimglist.get(position) != null)
			return videoimglist.get(position);
		else
			return null;
	}
	
	public static int getVideoImgListSize(){
		return videoimglist.size();
	}
	
	public static void InitVideoList(int size){
		videoimglist.clear();
		for(int i=0; i<size; i++)
			videoimglist.add(null);
		System.out.println("videoimglist size = "+videoimglist.size());
	}
	
	public static void clearVideoList(){
		videoimglist.clear();
	}

	public interface OnLineVideoListener {
		void onVideoReceive();
		void onPlayfailed(int msg, byte numChn);
	}

	public interface OnVideoDecordListener {
		void onVDecord();
	}

	protected void onResume() {
		wakeLock.acquire();
		super.onResume();
	}

	protected void onDestroy() {
		// 停止视频
		if (onlineVideo == 1) {
			stopVideo(channelnum);
		}else{
			stopVideoTap(channelnum);
		}
		// 移除handle
		mHandler.removeCallbacksAndMessages(null);
		// 发�?停止视频广播
		Intent stopIntent = new Intent("com.hri.ess.videostop");
		sendBroadcast(stopIntent);

		// 关闭常量屏幕
		wakeLock.release();
		super.onDestroy();
	}

	private long oldRx;// 上一秒接收的总字节数
	private long oldTx;// 上一秒发送的总字节数
	private int rxRate;// 接收网�?

	// 计算网�?,没一秒计�?
	private void calcNetRate() {
		if (oldRx == 0) {
			oldRx = TrafficStats.getTotalRxBytes();
		} else {
			rxRate = (int) (TrafficStats.getTotalRxBytes() - oldRx) / 1024;
			oldRx = TrafficStats.getTotalRxBytes();
			oldTx = TrafficStats.getTotalTxBytes();
		}
		Message msg = mHandler.obtainMessage();
		msg.what = VIDEORATE;
		mHandler.sendMessageDelayed(msg, 1000);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.video_back:
			// 返回
			finish();
			break;
		case R.id.video_error_progress:
			video_loading_view.setVisibility(View.VISIBLE);
			video_error_view.setVisibility(View.GONE);
			if (onlineVideo == 1) {
				startVideo();
			} else {
				startVideoTap();
			}
			break;
		default:
			break;
		}
	}
}
