package com.hri.ess.pager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.hri.ess.MainActivity;
import com.hri.ess.PlayVideoActivity;
import com.hri.ess.R;
import com.hri.ess.adapter.VideoListAdapter;
import com.hri.ess.businesslogic.GetChRealImgBusinessLogic;
import com.hri.ess.util.Util;
import com.hri.ess.view.CalendarPopupWindow;
import com.hri.ess.view.CalendarPopupWindow.DateSetChangeListener;
import com.hri.ess.view.VideoTimePop;
import com.hri.ess.view.VideoTimePop.OnTimeCheckListener;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;
import com.sleepbot.datetimepicker.time.TimePickerDialog.OnTimeSetListener;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 我看我家页面
 * @author zhuqian
 *
 */
public class HousePager extends BasePager implements OnClickListener, DateSetChangeListener, OnTimeSetListener {

	protected static final String TAG = "HousePager";
	private Button onlineVideo_btn;
	private Button videoTap_btn;
	private ListView videoList;
	private RelativeLayout videoTapView;//录像回放View
	private LinearLayout onlineVidoView;//实时视频View
	
	private VideoListAdapter mVideoAdapter;//视频通道列表适配器
	private ArrayList<String> channelList;
	
	private CalendarPopupWindow calendarPopupWindow;
	private TextView videoTapStartDate,videoTapStartTime,videoTapPlayTime;
	
	private TimePickerDialog timePickerDialog;
	
	public static final String TIMEPICKER_TAG = "timepicker";
	
	private VideoTimePop videoTimePop;
	
	private int chListSize;
	
	public HousePager(ArrayList<String> channelList,Context context, View titleView) {
		super(context, titleView);
		this.channelList = channelList;
		chListSize = this.channelList.size();
		mTimer.schedule(task, 1000, 1000); // 1s后执行task,经过1s再次执行
	}
	public View initView() {
		view = inflater.inflate(R.layout.layout_house_pager, null);
	
		videoTapView = (RelativeLayout) view.findViewById(R.id.house_pager_videoTap);
		onlineVidoView = (LinearLayout) view.findViewById(R.id.house_pager_onlineVideo);
		videoList = (ListView) view.findViewById(R.id.housePager_videoList);
		videoTapStartDate = (TextView) view.findViewById(R.id.house_pager_videoTap_startDate);
		videoTapStartTime = (TextView) view.findViewById(R.id.house_pager_videoTap_startTime);
		videoTapPlayTime = (TextView) view.findViewById(R.id.house_pager_video_playTime);
		
		//初始化当前日期和默认播放时长
		videoTapStartDate.setText(Util.getNowDateTime().split(" ")[0]);
		videoTapStartTime.setText(Util.getNowDateTime().split(" ")[1]);
		videoTapPlayTime.setText("1分钟");
		
		videoList.setOnItemClickListener(new OnItemClickListener() {
			//点击播放实时视频或者录像回放
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				//通道名
				String channelName = channelList.get(position);
				if(channelName.contains("[KEEP]")){
					Toast.makeText(context, "此通道已被禁用", 0).show();
					return;
				}
				ArrayList<String> tempList = new ArrayList<String>();
				
				//计算position前面有几个禁止通道
				int keepCount = 0;
				for(int i=0;i<chListSize;i++){
					String item = channelList.get(i);
					if(!item.contains("[KEEP]")){
						//去除禁用通道
						//item += i;			//这里item+1改变了通道名称，所以注释
						tempList.add(item);
						
					}else{
						if(i<position){
							keepCount++;
						}
					}
				}
				if(onlineVideo_btn.isEnabled()){
					//播放报警录像
					Intent intent = new Intent(context,PlayVideoActivity.class);
					intent.putStringArrayListExtra("channelList", tempList);
					intent.putExtra("channelnum",(position-keepCount));
					intent.putExtra("onlineVideo",0);
					
					//计算开始时间和播放时长
					//计算开始时间,判断传过来的时间+播放时间，是否会大于当前时间
					//拼接开始时间
					selectedDate = videoTapStartDate.getText().toString();
					selectedTime = videoTapStartTime.getText().toString();
					playTime = Integer.parseInt(videoTapPlayTime.getText().toString().substring(0, 1));
					String startDate = selectedDate+" "+selectedTime;
					if(Util.isAfterNow(startDate, playTime)){
						intent.putExtra("startTime",Util.getNowTimeAfter(playTime));
					}else{
						intent.putExtra("startTime",startDate);
					}
					intent.putExtra("playtime", (short)(playTime*60));
					
					Log.i(TAG, "开始播放录像，"+"开始播放时间："+startDate+"，播放时长："+playTime);
					context.startActivity(intent);
				}else{
					//播放实时视频
					Log.i(TAG, "开始播放实时视频");
					Intent intent = new Intent(context,PlayVideoActivity.class);
					intent.putStringArrayListExtra("channelList", tempList);
					int num = position-keepCount;
					intent.putExtra("channelnum", num);
					intent.putExtra("onlineVideo", 1);
					context.startActivity(intent);
				}
			}
		});
		videoTapStartDate.setOnClickListener(this);
		videoTapStartTime.setOnClickListener(this);
		videoTapPlayTime.setOnClickListener(this);
		return view;
	}
	int count = 0;
	Handler handler = new Handler() {  
		public void handleMessage(Message msg) {  
			if (msg.what == 1) {  
				((VideoListAdapter)mVideoAdapter).setList(channelList);
				mVideoAdapter.notifyDataSetChanged();
				count += 1;
				Log.i("timer", "count = "+count + " videoimglist:"+PlayVideoActivity.getVideoImg(count));
				if(count >= chListSize)
					mTimer.cancel();
	        }  
	        super.handleMessage(msg);  
	    };  
	};
	    
	Timer mTimer = new Timer();    
	TimerTask task = new TimerTask() {  
		@Override  
        public void run() {  
            // 需要做的事:发送消息  
            Message message = new Message();  
            message.what = 1;  
            handler.sendMessage(message);  
        }  
	};
	  
	public void initData() {
		if(mVideoAdapter==null){
			mVideoAdapter = new VideoListAdapter(context, channelList);
			videoList.setAdapter(mVideoAdapter);
		}
		mVideoAdapter.setList(channelList);
		mVideoAdapter.notifyDataSetChanged();
	}

	public void initTitle(View titleView) {
		//初始化标题栏
		LinearLayout video_selector = (LinearLayout) titleView.findViewById(R.id.video_selector);
		Button passenger_flow =(Button)titleView.findViewById(R.id.title_passenger_flow);
		passenger_flow.setVisibility(View.GONE);
		TextView home_tv_text = (TextView) titleView.findViewById(R.id.home_tv_text);
		ImageView home_menu_s = (ImageView) titleView.findViewById(R.id.home_menu_s);
		video_selector.setVisibility(View.VISIBLE);
		home_tv_text.setVisibility(View.GONE);
		onlineVideo_btn = (Button) titleView.findViewById(R.id.title_online_video_btn);
		videoTap_btn = (Button) titleView.findViewById(R.id.title_video_tap_btn);
		onlineVideo_btn.setOnClickListener(this);
		videoTap_btn.setOnClickListener(this);
		home_menu_s.setOnClickListener(this);
		
		onlineVideo_btn.setText(context.getResources().getString(R.string.home_title_online_video));
		videoTap_btn.setText(context.getResources().getString(R.string.home_title_video_tap));
		
		setOlineView();
	}
	//初始化实时视频View
	private void setOlineView(){
		onlineVideo_btn.setEnabled(false);
		videoTap_btn.setEnabled(true);
		
		videoTapView.setVisibility(View.GONE);
		onlineVidoView.setVisibility(View.VISIBLE);
	}
	//初始化录像回放View
	private void setVideoTapView(){
		onlineVideo_btn.setEnabled(true);
		videoTap_btn.setEnabled(false);
		
		videoTapView.setVisibility(View.VISIBLE);
		onlineVidoView.setVisibility(View.GONE);
	}
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.title_online_video_btn:
			//切换到实时视频页面
			setOlineView();
			break;
		case R.id.title_video_tap_btn:
			//切换到录像页面
			setVideoTapView();
			break;
		case R.id.home_menu_s:
			//打开菜单
			((MainActivity)context).openMenu();
			break;
		case R.id.house_pager_videoTap_startDate:
			//弹出日期选择框
			if(calendarPopupWindow==null){
				calendarPopupWindow = new CalendarPopupWindow(context, null);
				calendarPopupWindow.setDateSetChangeListener(this);
			}
			calendarPopupWindow.showAtLocation(titleView, Gravity.BOTTOM, 0, 0);
			break;
		case R.id.house_pager_videoTap_startTime:
			//选择开始时间
			if(timePickerDialog==null){
				Calendar calendar = Calendar.getInstance();
				timePickerDialog = TimePickerDialog.newInstance(this, calendar.get(Calendar.HOUR_OF_DAY) ,calendar.get(Calendar.MINUTE), false, false);
			}
			timePickerDialog.show(((MainActivity)context).getSupportFragmentManager(),TIMEPICKER_TAG);
			break;
		case R.id.house_pager_video_playTime:
			//选择播放时长
			if(videoTimePop==null){
				videoTimePop = new VideoTimePop(context);
				videoTimePop.setOnTimeCheckListener(new OnTimeCheckListener() {
					public void onTimeCheck(int position) {
						//播放时长被选择
						playTime = position+1;
						videoTapPlayTime.setText(playTime+"分钟");
						videoTimePop.dismiss();
					}
				});
			}
			videoTimePop.showAsDropDown(videoTapView);
			break;
		default:
			break;
		}
	}
	private String selectedDate;
	private String selectedTime;
	private int playTime;
	public void dateSetChange(String date) {
		if(Util.isAfterNowDate(date)){
			Toast.makeText(context, "选择的日期不能大于当前日期", 0).show();
			return;
		}
		//日期选中
		selectedDate = date;
		videoTapStartDate.setText(selectedDate);
	}
	public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
		//前面补0
		DecimalFormat format = new DecimalFormat("00");
		selectedTime = format.format(hourOfDay)+":"+format.format(minute)+":00";
		videoTapStartTime.setText(selectedTime);
	}
}
