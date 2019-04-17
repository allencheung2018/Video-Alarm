package com.hri.ess.pager;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hri.ess.AlarmVideoActivity;
import com.hri.ess.MainActivity;
import com.hri.ess.R;
import com.hri.ess.adapter.AlarmDetailAdapter;
import com.hri.ess.adapter.ChannelflowRecordsAdapter;
import com.hri.ess.adapter.DoorRecordAdapter;
import com.hri.ess.adapter.FlowChannelAdapter;
import com.hri.ess.app.VillaApplication;
import com.hri.ess.businesslogic.GetPeopleStreamBusinessLogic;
import com.hri.ess.businesslogic.ReadAlarmDetailedBusinessLogic;
import com.hri.ess.businesslogic.ReadAlarmNoteBusinessLogic;
import com.hri.ess.command.CmdReadEntryNote;
import com.hri.ess.command.PeopleStreamInfo;
import com.hri.ess.dbservice.control.AlarmDetailedControl;
import com.hri.ess.dbservice.domain.AlarmDetailed;
import com.hri.ess.util.FileUtil;
import com.hri.ess.util.MyListView;
import com.hri.ess.util.Util;
import com.hri.ess.view.CalendarPopupWindow;
import com.hri.ess.view.CalendarPopupWindow.DateSetChangeListener;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;
import com.sleepbot.datetimepicker.time.TimePickerDialog.OnTimeSetListener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageButton;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 记录查询页面
 * 
 * @author zhuqian
 * 
 */
public class RecordPager extends BasePager implements OnClickListener,
		DateSetChangeListener, OnTimeSetListener, OnScrollListener {
	private Button onlineVideo_btn, videoTap_btn, passenger_flow;
	private ImageView flow_pager_rbtn, iv_downPanel;
	private LinearLayout alarm_record_ll, flow_record_ll, 
		chanenlflow_record_ll;
	private RelativeLayout lyo_entrySetting, door_record_ll, ststcBar;

	private ListView alarmRecords, flowRecords,
			channelflowRecords;
	private PullToRefreshListView doorRecords;

	private Button record_pager_querybtn;

	private TextView doorRecord_startDate, doorRecord_startTime,
			doorRecord_endDate, doorRecord_endTime, doorRecord_normalTime,
			doorRecords_loading_info, alarmRecords_loading_info,
			doorrecords_no, alarmrecords_no, txt_flowStatisticsDate_value,
			flowrecords_no, flowrecords_loading_text, txt_flow_value,
			channelflowrecords_loading_text, channelflowrecords_no,
			total_entrynum,show_entrynum;
	private Spinner doorRecord_normalTimeSpinner, channel_Spinner,
			doorRecord_Spinner;
	private ReadAlarmNoteBusinessLogic alarmNoteBusinessLogic;
	private ReadAlarmDetailedBusinessLogic alarmDetailedBusinessLogic;
	private GetPeopleStreamBusinessLogic flowBusinessLogic;
	private AlarmDetailedControl alarmDetailedControl;

	// 单任务的线程池，一个一个执行
	private ExecutorService singleThreadExecutor = Executors
			.newSingleThreadExecutor();
	private ExecutorService alarmThreadExecutor = Executors
			.newSingleThreadExecutor();
	private ExecutorService flowThreadExecutor = Executors
			.newSingleThreadExecutor();
	private ExecutorService channelThreadExecutor = Executors
			.newSingleThreadExecutor();
	private List<String> doorRecordList;
	private List<String> alarmRecordList;

	private CalendarPopupWindow calendarPopupWindow;
	private TimePickerDialog timePickerDialog;

	public static final String TIMEPICKER_TAG = "timepicker";

	private int startDate;// 开始日期开关
	private int startTime;// 开始日期开关
	

	private DoorRecordAdapter doorAdapter;
	private AlarmDetailAdapter alarmAdapter;
	private FlowChannelAdapter flowAdapter;
	private ChannelflowRecordsAdapter channelAdpater;
	private ArrayAdapter<String> adapter_doorRecord_Spinner;
	private ArrayList<AlarmDetailed> alarms = new ArrayList<AlarmDetailed>();
	private ArrayList<AlarmDetailed> doors = new ArrayList<AlarmDetailed>();
	private ArrayList<PeopleStreamInfo> flows = new ArrayList<PeopleStreamInfo>();
	private ArrayList<PeopleStreamInfo> channelflows = new ArrayList<PeopleStreamInfo>();
	private PeopleStreamInfo taotalPeopleStream = null;
	private PeopleStreamInfo cuurentChannelflow = null;
	
	private boolean isfresh_doorrec = true;
	private int lastTotalNum=0;		//记录上次读取总条数
	private int doorPageNum=0;
	private int detailNum=0;		//需加载明细条数
	private int addedNum=0;			//成功加载明细条数
	private byte bEntryChnNum;
	private int cntToast = 0;

	private int mTouchShop=400;//最小滑动距离
    protected float mFirstY;//触摸下去的位置
    protected float mCurrentY;//滑动时Y的位置
    protected int direction=-1;//判断是否上滑或者下滑的标志
    protected boolean mShow;//判断是否执行了上滑动画
    private int lastVisibleItem=0;
    private boolean scrollFlag = false;// 标记是否滑动
    
	public RecordPager(Context context, View titleView) {
		super(context, titleView);
	}

	public View initView() {
		view = inflater.inflate(R.layout.layout_record_pager, null);
		door_record_ll = (RelativeLayout) view.findViewById(R.id.door_record_ll);
		alarm_record_ll = (LinearLayout) view
				.findViewById(R.id.alarm_record_ll);
		flow_record_ll = (LinearLayout) view.findViewById(R.id.flow_record_ll);
		chanenlflow_record_ll = (LinearLayout) view.findViewById(R.id.chanenlflow_record_ll);
		lyo_entrySetting = (RelativeLayout) view.findViewById(R.id.lyo_entrySetting);
		ststcBar = (RelativeLayout) view.findViewById(R.id.ststcBar);
		iv_downPanel = (ImageView) view.findViewById(R.id.iv_downPanel);
		doorRecords = (PullToRefreshListView) view.findViewById(R.id.pull_refresh_list);
		alarmRecords = (ListView) view.findViewById(R.id.alarmRecords);
		record_pager_querybtn = (Button) view
				.findViewById(R.id.record_pager_querybtn);
		doorRecord_startDate = (TextView) view
				.findViewById(R.id.doorRecord_startDate);
		doorRecord_startTime = (TextView) view
				.findViewById(R.id.doorRecord_startTime);
		doorRecord_endDate = (TextView) view
				.findViewById(R.id.doorRecord_endDate);
		doorRecord_endTime = (TextView) view
				.findViewById(R.id.doorRecord_endTime);
//		doorRecord_normalTime = (TextView) view
//				.findViewById(R.id.doorRecord_normalTime);
//		doorRecord_normalTimeSpinner = (Spinner) view
//				.findViewById(R.id.doorRecord_normalTimeSpinner);
		doorRecord_Spinner = (Spinner) view
				.findViewById(R.id.doorRecord_Spinner);
		channel_Spinner = (Spinner) view.findViewById(R.id.channel_Spinner);
		doorRecords_loading_info = (TextView) view
				.findViewById(R.id.doorrecords_loading_text);
		alarmRecords_loading_info = (TextView) view
				.findViewById(R.id.alarmrecords_loading_text);
		doorrecords_no = (TextView) view
				.findViewById(R.id.doorrecords_no_record);
		flowrecords_no = (TextView) view.findViewById(R.id.flowrecords_no);
		flowrecords_loading_text = (TextView) view
				.findViewById(R.id.flowrecords_loading_text);
		alarmrecords_no = (TextView) view.findViewById(R.id.alarmrecords_no);
		txt_flowStatisticsDate_value = (TextView) view
				.findViewById(R.id.txt_flowStatisticsDate_value);
		txt_flow_value = (TextView) view.findViewById(R.id.txt_flow_value);
		channelflowrecords_loading_text = (TextView) view
				.findViewById(R.id.channelflowrecords_loading_text);
		channelflowrecords_no = (TextView) view
				.findViewById(R.id.channelflowrecords_no);
		total_entrynum = (TextView) view.findViewById(R.id.total_entrynum);
		show_entrynum = (TextView) view.findViewById(R.id.show_entrynum);
		flowRecords = (ListView) view.findViewById(R.id.flowRecords);
		channelflowRecords = (ListView) view
				.findViewById(R.id.channelflowRecords);
		
		// Add an end-of-list listener
		doorRecords.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

			@Override
			public void onLastItemVisible() {	
				if(!isfresh_doorrec){
					Toast.makeText(context, "加载中...请稍候", Toast.LENGTH_SHORT).show();
					return;
				}
				isfresh_doorrec = false;
				//
				isLoadDoor = 3;		//上拉加载更多
				singleThreadExecutor.execute(new Runnable() {
					public void run() {
						try {
							getDoorNote();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				});
			}
		});
		//下拉刷新
		doorRecords.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				if(!isfresh_doorrec){
					Toast.makeText(context, "加载中...请稍候", Toast.LENGTH_SHORT).show();
					return;
				}
				isfresh_doorrec = false;
				//
				Log.i("onRefresh", "Starting");
				doorRecord_endTime.setText(Util.getNowTime());
				onClick(record_pager_querybtn);

			}
		});
		doorRecords.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				try {
					//使用了handmark.pulltorefresh.library,这里position要减1
					AlarmDetailed alarm = (AlarmDetailed) doorAdapter.getItem(position-1);
					VillaApplication.alarm = alarm;
					Intent intent = new Intent(context,
							AlarmVideoActivity.class);
					context.startActivity(intent);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		
		doorRecords.getRefreshableView().setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Log.i(TAG, "doorRecords onTouch");
				switch (event.getAction()) {
                	case MotionEvent.ACTION_DOWN:
                		mFirstY = event.getY();//按下时获取位置
                    break;
                	case MotionEvent.ACTION_MOVE:
                		mCurrentY = event.getY();//得到滑动的位置
                		Log.i(TAG, "doorRecords onTouch-mFirstY="+mFirstY + " mCurrentY="+mCurrentY);
	                    if(mCurrentY - mFirstY > mTouchShop){//滑动的位置减去按下的位置大于最小滑动距离  则表示向下滑动
	                        direction = 0;//down
	                    }else if(mFirstY - mCurrentY > mTouchShop){//反之向上滑动
	                        direction = 1;//up
	                    }

	                    if(direction == 1){//判断如果是向上滑动 则执行向上滑动的动画
	                        if(mShow){//判断动画是否执行了  执行了则改变状态
	                            //执行往上滑动的动画
	                            hideTopPanel();
	                            mShow = !mShow;
	                            Log.i(TAG, "setting is Gone");
	                        }
	                    }else if(direction == 0){//判断如果是向下滑动 则执行向下滑动的动画
	                        if(!mShow){//判断动画是否执行了  执行了则改变状态
	                            //执行往下滑动的动画
//	                            showTopPanel();
	                            mShow = !mShow;
	                            Log.i(TAG, "setting is show");
	                        }
	                    }

                    break;
				}
				return false;
			}	
		});

		doorRecords.setOnScrollListener(new OnScrollListener(){

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				Log.i(TAG, "doorRecords OnScrollListener onScroll - firstVisibleItem="+firstVisibleItem
						+ " visibleItemCount="+visibleItemCount + " totalItemCount="+totalItemCount
						+ " lastVisibleItem="+lastVisibleItem);
//				if(lastVisibleItem != firstVisibleItem){
//					if((firstVisibleItem-lastVisibleItem) > 1){
//						direction = -1;		//up
//						lastVisibleItem = firstVisibleItem;
//					}
//					if((firstVisibleItem-lastVisibleItem) < -1){
//						direction = 1;		//down
//						lastVisibleItem = firstVisibleItem;
//					}
//				}else{
////					direction = 0;
//				}
			}
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				Log.i(TAG, "doorRecords onScrollStateChanged");
				switch (scrollState) {  
                // 当不滚动时  
                case OnScrollListener.SCROLL_STATE_IDLE:// 是当屏幕停止滚动时  
//                    scrollFlag = false;  
//                    Log.i(TAG, "doorRecords OnScrollListener onScrollStateChanged SCROLL_STATE_IDLE"
//                    		+ " direction="+direction);  
//                    if(lyo_entrySetting.isShown() && direction==-1){
////                    	lyo_entrySetting.startAnimation(hideAnim);
//                    	lyo_entrySetting.setVisibility(View.GONE);
//                    }
//                    if(!lyo_entrySetting.isShown() && direction==1){
////                    	lyo_entrySetting.startAnimation(showAnim);
//                    	lyo_entrySetting.setVisibility(View.VISIBLE);
//                    }
                    break;  
                case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:// 滚动时  
//                    scrollFlag = true;  
//                    Log.i(TAG, "doorRecords OnScrollListener onScrollStateChanged SCROLL_STATE_TOUCH_SCROLL");
                    break;  
                case OnScrollListener.SCROLL_STATE_FLING:// 是当用户由于之前划动屏幕并抬起手指，屏幕产生惯性滑动时  
//                    scrollFlag = false;  
//                    Log.i(TAG, "doorRecords OnScrollListener onScrollStateChanged SCROLL_STATE_FLING");
                    break;
				}
			}
			
		});
		flowRecords.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				try {
					cuurentChannelflow = flows.get(position);
					door_record_ll.setVisibility(View.GONE);
					alarm_record_ll.setVisibility(View.GONE);
					flow_record_ll.setVisibility(View.GONE);
					flowrecords_no.setVisibility(View.GONE);
					chanenlflow_record_ll.setVisibility(View.VISIBLE);
					channel_Spinner.setSelection(cuurentChannelflow.channelNum);
					setChannelFlowRecordView(true);		//在控件channel_Spinner时间处理器中也有此语句
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		alarmRecords.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				try {
					AlarmDetailed alarm = (AlarmDetailed) alarmAdapter
							.getItem(position);
					VillaApplication.alarm = alarm;
					Intent intent = new Intent(context,
							AlarmVideoActivity.class);
					context.startActivity(intent);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
//		ImageView selectDayId = (ImageView) view.findViewById(R.id.selectDayId);
		
		// 初始化监听
//		selectDayId.setOnClickListener(this);
		view.findViewById(R.id.selectChannelId).setOnClickListener(this);
		doorRecord_startDate.setOnClickListener(this);
		doorRecord_startTime.setOnClickListener(this);
		doorRecord_endDate.setOnClickListener(this);
		doorRecord_endTime.setOnClickListener(this);
		//doorRecord_normalTime.setOnClickListener(this);
		record_pager_querybtn.setOnClickListener(this);
		txt_flowStatisticsDate_value.setOnClickListener(this);
		alarmNoteBusinessLogic = new ReadAlarmNoteBusinessLogic(context);
		alarmDetailedBusinessLogic = new ReadAlarmDetailedBusinessLogic(context);
		flowBusinessLogic = new GetPeopleStreamBusinessLogic(context);
		alarmDetailedControl = new AlarmDetailedControl(context);
		channelflowRecords.setOnScrollListener(this);
		iv_downPanel.setOnClickListener(this);
		txt_flowStatisticsDate_value.setText(Util.getSpecDateStr());
		initSpinnerView();// channel_Spinner=(Spinner)
		return view;
	}

	public void initSpinnerView() {
		String[] m = { "今天", "昨天", "近三天" };
		//doorRecord_normalTime.setVisibility(View.GONE);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
				android.R.layout.simple_spinner_item, m);
		// 设置下拉列表的风格
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 将adapter 添加到spinner中
		//doorRecord_normalTimeSpinner.setAdapter(adapter);
		// 添加事件Spinner事件监听
		// doorRecord_normalTimeSpinner.setOnItemSelectedListener(new \\\);
		// 设置默认值
		//doorRecord_normalTimeSpinner.setVisibility(View.VISIBLE);
//		doorRecord_normalTimeSpinner
//				.setOnItemSelectedListener(new SpinnerSelectedListener());
		// channel_Spinner
		ArrayAdapter<String> adapter_channel = new ArrayAdapter<String>(
				context, android.R.layout.simple_spinner_item,
				VillaApplication.channelList);
		// adapter_channel.add();
		adapter_channel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		channel_Spinner.setAdapter(adapter_channel);
		channel_Spinner.setOnItemSelectedListener(new SpinnerSelectedListener() {
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						try {
							//这里channel_Spinner是下拉列表控件
							cuurentChannelflow.channelNum = (byte) arg2;
							if(isLoadFlow != 0)			//第一次不运行
							{
								setChannelFlowRecordView(true);
							}
							isLoadFlow = 1;
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}

					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});
		// doorRecord_Spinner
		ArrayList<String> clist = (ArrayList<String>) VillaApplication.channelList
				.clone();
		clist.add("所有通道");
		adapter_doorRecord_Spinner = new ArrayAdapter<String>(context,
				android.R.layout.simple_spinner_item, clist);
		adapter_doorRecord_Spinner
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		doorRecord_Spinner.setAdapter(adapter_doorRecord_Spinner);
		doorRecord_Spinner.setSelection(clist.size() - 1);
		doorRecord_Spinner
				.setOnItemSelectedListener(new SpinnerSelectedListener() {
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						try {
							onClick(record_pager_querybtn);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}

					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});
	}

	class SpinnerSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			try {
				if (arg2 == 0) {// 今天
					doorRecord_startDate.setText(Util.getSpecDateStr());
					doorRecord_startTime.setText("00:00:00");
					doorRecord_endDate.setText(Util.getSpecDateStr());
					doorRecord_endTime.setText(Util.getNowTime());
				} else if (arg2 == 1) {
					doorRecord_startDate.setText(Util
							.getDateStr(new Date(), -1));
					doorRecord_startTime.setText("00:00:00");
					doorRecord_endDate.setText(Util.getDateStr(new Date(), -1));
					doorRecord_endTime.setText("23:59:59");
				} else {
					doorRecord_startDate.setText(Util
							.getDateStr(new Date(), -2));
					doorRecord_startTime.setText("00:00:00");
					doorRecord_endDate.setText(Util.getSpecDateStr());
					doorRecord_endTime.setText(Util.getNowTime());
				}
				onClick(record_pager_querybtn);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}

	private static final int GETDOORNOTESUCCESS = 200;
	private static final int GETDOORNOTEERROR = 202;
	protected static final String TAG = "RecordPager";
	private static final int GETALARMDETAILSUCCESS = 300;
	private static final int GETALARMDETAILERROR = 303;

	private static final int GETALARMNOTESUCCESS = 400;
	private static final int GETALARMNOTEERROR = 404;
	private static final int GETDOORDETAILSUCCESS = 500;
	private static final int GETDOORDETAILERROR = 505;
	private static final int GETFLOWSUCCESS = 600;
	private static final int GETFLOWFINISH = 601;
	private static final int GETFLOWERROR = 602;
	private static final int GETFLOWTOTALSUCCESS = 606;
	private static final int GETCHANNELFLOWSUCCESS = 603;
	private static final int GETCHANNELFLOWFINISH = 604;
	private static final int GETCHANNELFLOWERROR = 605;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case GETDOORNOTESUCCESS:
				// 获取成功
				total_entrynum.setText("共 "+CmdReadEntryNote.itemTotal + " 条记录");
				show_entrynum.setText("当前显示 " + doors.size() + " 条");
				//无记录
				if(CmdReadEntryNote.itemTotal == 0){
					doorRecords_loading_info.setVisibility(View.GONE);
					doorrecords_no.setVisibility(View.VISIBLE);
					isfresh_doorrec = true;
					break;
				}
				//有新记录增加
				if(CmdReadEntryNote.itemTotal > lastTotalNum && lastTotalNum > 0){
					Toast.makeText(context, "有新纪录", Toast.LENGTH_SHORT).show();
					isfresh_doorrec = true;
					onClick(record_pager_querybtn);
					break;
				}
				//
				hideTopPanel();
				//
				lastTotalNum = CmdReadEntryNote.itemTotal;
				//不再获取明细
				if(doorPageNum == CmdReadEntryNote.pageTotal)
				{
					Toast.makeText(context, R.string.nomoredata, Toast.LENGTH_SHORT).show();
					Log.i("getDoorNote", "pageNum = "+CmdReadEntryNote.pageNum 
							+ " pageTotal = "+CmdReadEntryNote.pageTotal);
					isfresh_doorrec = true;
					break;
				}
				doorPageNum += 1;
				//获取明细
				try {
					addedNum = 0;	//清空加载明细条数
					doorRecords_loading_info.setVisibility(View.GONE);
					//for (final String id : doorRecordList) {
					for(int i=0; i<detailNum; i++)
					{
						final String id = doorRecordList.get(i);
						final AlarmDetailed alarm = alarmDetailedControl.getAlarmById(id);
						if (alarm == null) {
							// 从服务器获取
							singleThreadExecutor.execute(new Runnable() {
								public void run() {
									try {
										AlarmDetailed detailed = alarmDetailedBusinessLogic
												.executionReadAlarmDetailed(id);
										alarmDetailedControl.addAlarm(detailed);
										Message msg = mHandler.obtainMessage();
										msg.what = GETDOORDETAILSUCCESS;
										msg.obj = id;
										mHandler.sendMessage(msg);
									} catch (Exception e) {
										e.printStackTrace();
										Message msg = mHandler.obtainMessage();
										msg.what = GETALARMDETAILERROR;
										msg.obj = "获取id=" + id + "出入记录错误:"
												+ e.getMessage();
										Log.i(TAG, "获取出入记录明细失败");
										mHandler.sendMessage(msg);
										
										addedNum++;
										if(addedNum == detailNum){
											//刷新条件
											isfresh_doorrec = true;
										}
									}
								}
							});
						} else {
							// 本地数据库获取
							Log.i(TAG, "从本地获取出入记录明细成功");
							if (!doors.contains(alarm)) {
								doors.add(alarm);
							}
							show_entrynum.setText("当前显示 " + doors.size() + " 条");
							// 排序
							Collections.sort(doors,
									new Comparator<AlarmDetailed>() {
										public int compare(AlarmDetailed lhs,
												AlarmDetailed rhs) {
											int result = rhs.getAlarmtime()
													.compareTo(
															lhs.getAlarmtime());
											return result;
										}
									});
							((DoorRecordAdapter) doorAdapter).setList(doors);
							doorAdapter.notifyDataSetChanged();
							
							addedNum++;
							if(addedNum == detailNum){
								//刷新条件
								isfresh_doorrec = true;
							}
						}
					}
				} catch (Exception e) {

				}
				//doorRecordList.clear();
				doorRecords.onRefreshComplete();		//收起上拉
				break;
			case GETDOORNOTEERROR:
				doorRecords_loading_info.setVisibility(View.GONE);
				doorrecords_no.setVisibility(View.VISIBLE);
				if (msg.obj != null) {
					Toast.makeText(context, (String) msg.obj, 0).show();
				}
				break;
			case GETALARMNOTESUCCESS:
				int alarmListSize = alarmRecordList.size();
				Log.i(TAG, "获取报警列表成功 共 "+alarmListSize+"条");
				try {
					// 获取报警列表成功
					if (alarmRecordList != null && alarmRecordList.size() == 0) {
						alarmrecords_no.setVisibility(View.VISIBLE);
					}
					alarmRecords_loading_info.setVisibility(View.GONE);
					if (alarmAdapter == null) {
						alarmAdapter = new AlarmDetailAdapter(context, alarms);
						alarmRecords.setAdapter(alarmAdapter);
					}
					alarms.clear();
					Log.i(TAG, "开始获取报警明细");
					if(alarmListSize >= 10)			//只显示前10条
					{
						alarmListSize = 10;
					}
					for(int i=0;i<alarmListSize;i++){
					//for (final String id : alarmRecordList) {
						final String id = alarmRecordList.get(i);
						final AlarmDetailed alarm = alarmDetailedControl.getAlarmById(id);
						if (alarm == null) {
							// 冲服务器获取
							alarmThreadExecutor.execute(new Runnable() {
								public void run() {
									try {
										AlarmDetailed detailed = alarmDetailedBusinessLogic
												.executionReadAlarmDetailed(id);
										alarmDetailedControl.addAlarm(detailed);
										alarms.add(detailed);
										// 排序
										Collections
												.sort(alarms,
														new Comparator<AlarmDetailed>() {
															public int compare(
																	AlarmDetailed lhs,
																	AlarmDetailed rhs) {
																int result = rhs
																		.getAlarmtime()
																		.compareTo(
																				lhs.getAlarmtime());
																return result;
															}
														});
										Log.i(TAG, "从服务器获取报警明细成功");
										mHandler.sendEmptyMessage(GETALARMDETAILSUCCESS);
									} catch (Exception e) {
										e.printStackTrace();
										Log.i(TAG, "获取报警明细失败");
										Message msg = mHandler.obtainMessage();
										msg.what = GETALARMDETAILERROR;
										msg.obj = "获取id=" + id + "错误";
										mHandler.sendMessage(msg);
									}
								}
							});
						} else {
							Log.i(TAG, "从本地数据库获取报警明细成功");
							if (!alarms.contains(alarm)) {
								alarms.add(alarm);
							}
							// 排序
							Collections.sort(alarms,
									new Comparator<AlarmDetailed>() {
										public int compare(AlarmDetailed lhs,
												AlarmDetailed rhs) {
											int result = rhs.getAlarmtime()
													.compareTo(
															lhs.getAlarmtime());
											return result;
										}
									});// 排序
							((AlarmDetailAdapter)alarmAdapter).setChannelList
								((ArrayList<AlarmDetailed>)alarms, VillaApplication.channelList);
							alarmAdapter.notifyDataSetChanged();
						}
					}
				} catch (Exception e) {

				}
				break;
			case GETALARMNOTEERROR:
				alarmRecords_loading_info.setVisibility(View.GONE);
				alarmrecords_no.setVisibility(View.VISIBLE);
				if (msg.obj != null) {
					Toast.makeText(context, (String) msg.obj, 0).show();
				}
				break;
			case GETALARMDETAILSUCCESS:
				// 获取报警明细成功
				((AlarmDetailAdapter)alarmAdapter).setChannelList
					((ArrayList<AlarmDetailed>)alarms, VillaApplication.channelList);
				alarmAdapter.notifyDataSetChanged();
				break;
			case GETALARMDETAILERROR:
				if (msg.obj != null) {
					//Toast.makeText(context, (String) msg.obj, 0).show();	//不提示到用户界面
					Log.i(TAG, (String) msg.obj);
				}
				break;
			case GETDOORDETAILSUCCESS:
				addedNum++;
				if(addedNum == detailNum){
					//刷新条件
					isfresh_doorrec = true;
				}
				//根据ID数据库中获取此摘要信息
				String id = (String) msg.obj;
				AlarmDetailed alarm = alarmDetailedControl.getAlarmById(id);
				Log.i("GETALARMDETAILSUCCESS","id:"+id);
				if(alarm == null)
					break;
				byte num = alarm.getChannelNum();
				if(num == bEntryChnNum || bEntryChnNum == (byte)255){
					if(!doors.contains((AlarmDetailed)alarm))
						doors.add(alarm);
					int size = doors.size();
					show_entrynum.setText("当前显示 " + size + " 条");
					if(size > CmdReadEntryNote.itemTotal){
						total_entrynum.setText("共 "+size + " 条记录");
					}
				}
				else{
					break;
				}
				Collections.sort(doors,	new Comparator<AlarmDetailed>() {
					public int compare(AlarmDetailed lhs, AlarmDetailed rhs){
						int result = rhs.getAlarmtime().compareTo(lhs.getAlarmtime());
						return result;
					}
				}); // 排序
				((DoorRecordAdapter) doorAdapter).setList(doors);
				doorAdapter.notifyDataSetChanged();
				break;
			case GETDOORDETAILERROR:
				if (msg.obj != null) {
					Toast.makeText(context, (String) msg.obj, 0).show();
				}
				break;
			case GETFLOWSUCCESS:// 获取客流量成功.
				if (flowAdapter == null) {
					flowAdapter = new FlowChannelAdapter(context);
					flowRecords.setAdapter(flowAdapter);
				}
				((FlowChannelAdapter) flowAdapter).setList(flows);
				flowAdapter.notifyDataSetChanged();
				break;
			case GETFLOWFINISH:
				if (flows.size() == 0) {
					flowrecords_no.setVisibility(View.VISIBLE);
				}
				flowrecords_loading_text.setVisibility(View.GONE);
				if (flowAdapter == null) {
					flowAdapter = new FlowChannelAdapter(context);
					flowRecords.setAdapter(flowAdapter);
				}
				txt_flow_value
						.setText((taotalPeopleStream != null ? taotalPeopleStream.outPeopleStream
								: ""));
				((FlowChannelAdapter) flowAdapter).setList(flows);
				flowAdapter.notifyDataSetChanged();
				break;
			case GETFLOWERROR:
				Toast.makeText(context, (String) msg.obj, 0).show();
				flowrecords_loading_text.setVisibility(View.GONE);
				break;
			case GETCHANNELFLOWSUCCESS:
				if (channelAdpater == null) {
					channelAdpater = new ChannelflowRecordsAdapter(context);
					channelflowRecords.setAdapter(channelAdpater);
				}
				((ChannelflowRecordsAdapter) channelAdpater)
						.setList(channelflows);
				channelAdpater.notifyDataSetChanged();
				break;
			case GETCHANNELFLOWFINISH:
				channelflowrecords_loading_text.setVisibility(View.GONE);
				if (channelAdpater == null) {
					channelAdpater = new ChannelflowRecordsAdapter(context);
					channelflowRecords.setAdapter(channelAdpater);
				}
				((ChannelflowRecordsAdapter) channelAdpater)
						.setList(channelflows);
				channelAdpater.notifyDataSetChanged();
				break;
			case GETCHANNELFLOWERROR:
				Toast.makeText(context, (String) msg.obj, 0).show();
				flowrecords_loading_text.setVisibility(View.GONE);
				break;
			default:
				break;
			}
		};
	};

	public void initData() {
		setNowDay();
	}
	//出入摘要
	private void getDoorNote() {
		String str = "getDoorNote-entre" + " isLoadDoor="+isLoadDoor;
		FileUtil.writeFileSdcardFile(FileUtil.nameLogFile, str, true);
		//初始化Adapter
		if (doorAdapter == null) {
			doorAdapter = new DoorRecordAdapter(context);
			doorRecords.setAdapter(doorAdapter);
		}
		// 查询按钮
		if(isLoadDoor == 1){
			
			lastTotalNum = 0;
			doorPageNum = 0;

			CmdReadEntryNote.pageNum = 1;
			CmdReadEntryNote.pageTotal = 0;
		}
		//读取ID
		try {
			Log.i(TAG, "开始获取开关门记录");
			byte cid = (byte) doorRecord_Spinner.getSelectedItemPosition();
			if (cid == (adapter_doorRecord_Spinner.getCount() - 1)
					|| adapter_doorRecord_Spinner.getCount() == 0) {
				cid = (byte) 255;
			}
			bEntryChnNum = cid;
			// 获取开始时间和结束时间
			String startTime = doorRecord_startDate.getText().toString() + " "
					+ doorRecord_startTime.getText().toString();
			String endTime = doorRecord_endDate.getText().toString() + " "
					+ doorRecord_endTime.getText().toString();
			str = "chn="+cid + " startTime:"+startTime + " endTime:"+endTime
					+ " doorRecordList="+detailNum;
			Log.i(TAG, str);
			if(doorRecordList != null)
				doorRecordList.clear();
			doorRecordList = alarmNoteBusinessLogic.executionReadEntryNote(cid,
					(byte) 26, startTime, endTime);
			detailNum = doorRecordList.size();
			Log.i(TAG, "获取开关门记录成功  detailNum = "+detailNum);
			mHandler.sendEmptyMessage(GETDOORNOTESUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			Message msg = mHandler.obtainMessage();
			msg.what = GETDOORNOTEERROR;
			msg.obj = e.getMessage();
			mHandler.sendMessage(msg);
			//刷新条件
			isfresh_doorrec = true;
		}
	}

	private void getAlarmNotes() {
		try {
			Log.i(TAG, "开始获取报警列表");
			alarmRecordList = alarmNoteBusinessLogic.executionReadAlarmNote(
					(byte) 255, (byte) 6, Util.get2000YearDate(),
					Util.get2050YearDate());
			Log.i(TAG, "获取报警列表成功");
			isLoadAlarm = 1;
			mHandler.sendEmptyMessage(GETALARMNOTESUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			isLoadAlarm = 0;
			Log.i(TAG, "获取报警列表失败");
			Message msg = mHandler.obtainMessage();
			msg.what = GETALARMNOTEERROR;
			msg.obj = e.getMessage();
			mHandler.sendMessage(msg);
		}
	}

	// 重置为今天日期和时间
	private void setNowDay() {
		try {
			doorRecord_startDate.setText(Util.getSpecDateStr());
			doorRecord_startTime.setText("00:00:00");
			doorRecord_endDate.setText(Util.getSpecDateStr());
			doorRecord_endTime.setText(Util.getNowTime());
			//doorRecord_normalTime.setText("今天");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void initTitle(View titleView) {
		// 初始化标题栏
		LinearLayout video_selector = (LinearLayout) titleView
				.findViewById(R.id.video_selector);
		passenger_flow = (Button) titleView
				.findViewById(R.id.title_passenger_flow);
		passenger_flow.setVisibility(View.VISIBLE);
		flow_pager_rbtn = (ImageView) view
				.findViewById(R.id.flow_pager_rbtn);
		TextView home_tv_text = (TextView) titleView
				.findViewById(R.id.home_tv_text);
		ImageView home_menu_s = (ImageView) titleView
				.findViewById(R.id.home_menu_s);
		video_selector.setVisibility(View.VISIBLE);
		home_tv_text.setVisibility(View.GONE);
		onlineVideo_btn = (Button) titleView
				.findViewById(R.id.title_online_video_btn);
		videoTap_btn = (Button) titleView
				.findViewById(R.id.title_video_tap_btn);

		onlineVideo_btn.setOnClickListener(this);
		videoTap_btn.setOnClickListener(this);
		passenger_flow.setOnClickListener(this);
		flow_pager_rbtn.setOnClickListener(this);
		home_menu_s.setOnClickListener(this);

		onlineVideo_btn.setText(context.getResources().getString(
				R.string.home_title_record_door));
		videoTap_btn.setText(context.getResources().getString(
				R.string.home_title_record_alarm));

		setDoorRecordView();
	}

	private int isLoadDoor = 0;
	private int isLoadAlarm = 0;
	private int isLoadFlow = 0;

	/**
	 * 获取开关门记录。
	 */
	private void setDoorRecordView() {
		onlineVideo_btn.setEnabled(false);
		passenger_flow.setEnabled(true);
		videoTap_btn.setEnabled(true);
		door_record_ll.setVisibility(View.VISIBLE);
		alarm_record_ll.setVisibility(View.GONE);
		doorrecords_no.setVisibility(View.GONE);
		flow_record_ll.setVisibility(View.GONE);
		chanenlflow_record_ll.setVisibility(View.GONE);
		if (isLoadDoor == 0) {
			doorRecords_loading_info.setVisibility(View.VISIBLE);
			isLoadDoor = 2;
			singleThreadExecutor.execute(new Runnable() {
				public void run() {
					try {
						getDoorNote();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});
		}
	}

	/**
	 * 获取报警记录
	 */
	private void setAlarmRecordView() {
		onlineVideo_btn.setEnabled(true);
		videoTap_btn.setEnabled(false);
		passenger_flow.setEnabled(true);

		door_record_ll.setVisibility(View.GONE);
		alarm_record_ll.setVisibility(View.VISIBLE);
		flow_record_ll.setVisibility(View.GONE);
		chanenlflow_record_ll.setVisibility(View.GONE);

		alarmrecords_no.setVisibility(View.GONE);
		if (isLoadAlarm == 0) {
			alarmRecords_loading_info.setVisibility(View.VISIBLE);
			isLoadAlarm = 2;
			alarmThreadExecutor.execute(new Runnable() {
				public void run() {
					try {
						getAlarmNotes();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});
		}
	}

	/**
	 * 获取人流统计
	 */
	private void setFlowRecordView() {
		onlineVideo_btn.setEnabled(true);
		videoTap_btn.setEnabled(true);
		passenger_flow.setEnabled(false);
		door_record_ll.setVisibility(View.GONE);
		alarm_record_ll.setVisibility(View.GONE);
		flow_record_ll.setVisibility(View.VISIBLE);
		flowrecords_no.setVisibility(View.GONE);
		chanenlflow_record_ll.setVisibility(View.GONE);
		flowrecords_loading_text.setVisibility(View.VISIBLE);
		flowThreadExecutor.execute(new Runnable() {
			public void run() {
				ArrayList chs = VillaApplication.channelList;
				List<PeopleStreamInfo> totallist = null;
				flows.clear();
				int totalPS = 0;
				String date = (String) txt_flowStatisticsDate_value.getText();
				taotalPeopleStream = new PeopleStreamInfo();
				try {
					totallist = flowBusinessLogic.executionGetPeopleStream((byte) 255, date
									+ " 00:00:00".toString(), (byte) 0, 1, 1, (byte) 3);
				} catch (Exception e1) {
					e1.printStackTrace();
					System.out.println("无出入人流数据！");
				}
				for (int i=0; i < chs.size() && chs != null && totallist != null; i++) {
					PeopleStreamInfo p = new PeopleStreamInfo();
					p.channelNum = (byte) i;
					p.inPeopleStream = "0";
					p.outPeopleStream = "0";
					p.residentTime = "0";
					p.Time = 0;
					for(int j=0; j<totallist.size(); j++){	//实际的出入统计结果，无有效数据则为0
						if (totallist.get(j).channelNum == p.channelNum) {
							p.outPeopleStream = totallist.get(j).outPeopleStream;
							totalPS += Integer.parseInt(p.outPeopleStream);
							FileUtil.writeFileSdcardFile("test.txt", date + "通道"
									+ i + "客流:" + totallist.get(0).outPeopleStream
									+ "\n\r", true);
						}
					}
					if(!p.outPeopleStream.equals("0"))	//只显示不为0的通道
						flows.add(p);
					if (i > 0 && (i % 5) == 0) {
						mHandler.sendEmptyMessage(GETFLOWSUCCESS);
					}
				}
				taotalPeopleStream.outPeopleStream = totalPS + "";
				mHandler.sendEmptyMessage(GETFLOWFINISH);
			}
		});
	}

	Date dateNow = new Date();

	private void setChannelFlowRecordView(final boolean isReload) {
		channelflowrecords_no.setVisibility(View.GONE);
		channelflowrecords_loading_text.setVisibility(View.VISIBLE);
		channelThreadExecutor.execute(new Runnable() {
			int size = 10;// 加载10天内的数据

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (isReload) {
					dateNow = new Date();
					channelflows.clear();
				}
				for (int i = 0; i < size; i++) {
					PeopleStreamInfo p = new PeopleStreamInfo();
					p.channelNum = cuurentChannelflow.channelNum;
					p.inPeopleStream = "0";
					p.outPeopleStream = "0";
					p.residentTime = Util.getSpecDateStr(dateNow);
					p.Time = 0;
					try {
						List<PeopleStreamInfo> list = flowBusinessLogic
								.executionGetPeopleStream(cuurentChannelflow.channelNum,
										Util.getSpecDateStr(dateNow)
												+ " 00:00:00".toString(),
										(byte) 0, 1, 1, (byte) 3);
						if (p.channelNum == list.get(0).channelNum) {
							p.outPeopleStream = list.get(0).outPeopleStream;
							Log.e("客流量"+p.channelNum, p.outPeopleStream);
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					channelflows.add(p);
					dateNow = new Date(dateNow.getTime() - 1000 * 3600 * 24);
					if (i % 5 == 0)
						mHandler.sendEmptyMessage(GETCHANNELFLOWSUCCESS);
				}
				mHandler.sendEmptyMessage(GETCHANNELFLOWFINISH);
			}
		});
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.title_online_video_btn:
			setDoorRecordView();
			break;
		case R.id.title_video_tap_btn:
			setAlarmRecordView();
			break;
		case R.id.title_passenger_flow: // 客流统计
			setFlowRecordView();
			break;
		case R.id.home_menu_s:
			// 打开菜单
			((MainActivity) context).openMenu();
			break;
		case R.id.doorRecord_startDate:
			// 置开始日期开关为1
			startDate = 1;
			// 设置开关门记录开始日期
			if (calendarPopupWindow == null) {
				calendarPopupWindow = new CalendarPopupWindow(context, null);
				calendarPopupWindow.setDateSetChangeListener(this);
			}
			calendarPopupWindow.showAtLocation(titleView, Gravity.BOTTOM, 0, 0);
			break;
		case R.id.doorRecord_startTime:
			// 设置开关门记录开始时间
			startTime = 1;
			if (timePickerDialog == null) {
				Calendar calendar = Calendar.getInstance();
				timePickerDialog = TimePickerDialog.newInstance(this,
						calendar.get(Calendar.HOUR_OF_DAY),
						calendar.get(Calendar.MINUTE), false, false);
			}
			timePickerDialog.show(
					((MainActivity) context).getSupportFragmentManager(),
					TIMEPICKER_TAG);
			break;
		case R.id.doorRecord_endDate:
			// 设置开关门记录结束日期
			startDate = 0;
			if (calendarPopupWindow == null) {
				calendarPopupWindow = new CalendarPopupWindow(context, null);
				calendarPopupWindow.setDateSetChangeListener(this);
			}
			calendarPopupWindow.showAtLocation(titleView, Gravity.BOTTOM, 0, 0);
			break;
		case R.id.doorRecord_endTime:
			// 设置开关门记录结束时间
			startTime = 0;
			if (timePickerDialog == null) {
				Calendar calendar = Calendar.getInstance();
				timePickerDialog = TimePickerDialog.newInstance(this,
						calendar.get(Calendar.HOUR_OF_DAY),
						calendar.get(Calendar.MINUTE), false, false);
			}
			timePickerDialog.show(
					((MainActivity) context).getSupportFragmentManager(),
					TIMEPICKER_TAG);
			break;
//		case R.id.doorRecord_normalTime:
//			// 将开始时间和日期重置
//			setNowDay();
//			break;
		case R.id.record_pager_querybtn:
			if(isLoadDoor == 2)	//首次不执行 
			{
				isLoadDoor = 1;
				break;
			}
			isLoadDoor = 1;		//查询按钮
			if(doors.size() != 0){
				doors.clear();
				((DoorRecordAdapter)doorAdapter).setList((ArrayList<AlarmDetailed>)doors);
				doorAdapter.notifyDataSetChanged();
				doorRecordList.clear();				//明细ID
			}
			doorRecords_loading_info.setVisibility(View.VISIBLE);	//加载中
			doorrecords_no.setVisibility(View.GONE);				//无记录
			singleThreadExecutor.execute(new Runnable() {
				public void run() {
					try {
						getDoorNote();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});
			//test
//			AlarmDetailed ad = new AlarmDetailed();
//			ad.setAlarmtime("ddd ddd");
//			doors.add(ad);
//			doorAdapter.notifyDataSetChanged();
			break;
//		case R.id.selectDayId:
//			doorRecord_normalTimeSpinner.performClick();
//			onClick(doorRecord_normalTimeSpinner);
//			break;
		case R.id.selectChannelId:
			this.doorRecord_Spinner.performClick();
			break;
		case R.id.txt_flowStatisticsDate_value:
			// 设置开关门记录开始日期
			startDate = 3;
			if (calendarPopupWindow == null) {
				calendarPopupWindow = new CalendarPopupWindow(context, null);
				calendarPopupWindow.setDateSetChangeListener(this);
			}
			calendarPopupWindow.showAtLocation(titleView, Gravity.BOTTOM, 0, 0);
			break;
		case R.id.flow_pager_rbtn:
			setFlowRecordView();
			break;
		case R.id.iv_downPanel:
			updownPanel();
			break;
		default:
			break;
		}
	}

	public void dateSetChange(String date) {
		try {
			if (startDate == 1) {
				doorRecord_startDate.setText(date);
			} else if (startDate == 0) {
				doorRecord_endDate.setText(date);
			} else {
				txt_flowStatisticsDate_value.setText(date);
				setFlowRecordView();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
		DecimalFormat format = new DecimalFormat("00");
		if (startTime == 1) {
			doorRecord_startTime.setText(format.format(hourOfDay) + ":"
					+ format.format(minute) + ":00");
		} else {
			doorRecord_endTime.setText(format.format(hourOfDay) + ":" 
					+ format.format(minute) + ":00");
		}
	}

	public void notifyHaveNewAlarm(final String alarmId, final boolean isAlarm) {
		// 判断是否包含这个警情id
		try {
			if (doors != null && doors.contains(alarmId)) {
				Log.i(TAG, "包含这个出入记录ID，返回");
				return;
			}
			if (alarms != null && alarms.contains(alarmId)) {
				Log.i(TAG, "包含这个报警ID，返回");
				return;
			}
			// 如果还没有加载过，直接返回
			if (isLoadDoor == 0 && !isAlarm) {
				Log.i(TAG, "还没加载出入记录页面，返回");
				return;
			} else if (isAlarm && isLoadAlarm == 0) {
				Log.i(TAG, "还没加载报警记录页面，返回");
				return;
			} else if ((isAlarm && alarmRecordList != null && alarmRecordList
					.contains(alarmId))
					|| (!isAlarm && doorRecordList != null && doorRecordList
							.contains(alarmId))) {
				// 如果报警ID列表或者出入记录列表含有这个alarmId，直接返回
				Log.i(TAG, "包含这个报警ID，过滤");
				return;
			} else {
				new Thread() {
					public void run() {
						try {
							AlarmDetailed detailed = alarmDetailedBusinessLogic
									.executionReadAlarmDetailed(alarmId);
							Log.i(TAG, "推送报警明细获取成功");
							// 添加到数据库
							alarmDetailedControl.addAlarm(detailed);
							if (isAlarm) {
								// 报警加载
								alarms.add(detailed);
								// 排序
								Collections.sort(alarms,
										new Comparator<AlarmDetailed>() {
											public int compare(
													AlarmDetailed lhs,
													AlarmDetailed rhs) {
												int result = rhs
														.getAlarmtime()
														.compareTo(
																lhs.getAlarmtime());
												return result;
											}
										});
								mHandler.sendEmptyMessage(GETALARMDETAILSUCCESS);
								Log.i(TAG, "推送报警明细添加到列表成功");
							} else {
								// 出入记录加载
//								doors.add(detailed);
//								// 排序
//								Collections.sort(doors,
//										new Comparator<AlarmDetailed>() {
//											public int compare(
//													AlarmDetailed lhs,
//													AlarmDetailed rhs) {
//												int result = rhs
//														.getAlarmtime()
//														.compareTo(
//																lhs.getAlarmtime());
//												return result;
//											}
//										});
								//有新的出入记录更新结束时间为当前时刻
								doorRecord_endTime.setText(Util.getNowTime());
								//
								Message msg = mHandler.obtainMessage();
								msg.what = GETDOORDETAILSUCCESS;
								msg.obj = alarmId;
								mHandler.sendMessage(msg);
								Log.i(TAG, "推送出入记录名细添加到列表成功");
							}
						} catch (Exception e) {
							e.printStackTrace();
							Log.i(TAG, "推送报警明细获取失败");
						}
					};
				}.start();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		try {
			if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
					&& view.getLastVisiblePosition() == (view.getCount() - 1)) {
				setChannelFlowRecordView(false);
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub

	}
	//隐藏设置栏
		public void hideTopPanel() {
			if(!lyo_entrySetting.isShown())
				return;
	        int heightPanel = lyo_entrySetting.getHeight();
	        int heightBar = ststcBar.getHeight();
	        Log.i(TAG, "hideTopPanel:"+heightPanel + " heightBar:"+heightBar);
	        Animation mHideAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
	                Animation.ABSOLUTE, 0f, Animation.ABSOLUTE, -heightPanel);
	        mHideAction.setDuration(1000);
	        lyo_entrySetting.startAnimation(mHideAction);
	        lyo_entrySetting.setVisibility(View.GONE);
	        iv_downPanel.setImageResource(R.drawable.qz_icon_navbar_drop_down);
	    }
		//
		public void showTopPanel(){
			if(lyo_entrySetting.isShown())
				return;
	        int heightPanel = lyo_entrySetting.getHeight();
	        int heightBar = ststcBar.getHeight();
	        Log.i(TAG, "showTopPanel:"+heightPanel + " heightBar:"+heightBar);
	        Animation mShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
	                Animation.ABSOLUTE, -heightPanel, Animation.ABSOLUTE, 0f);
	        mShowAction.setDuration(1000);
	        lyo_entrySetting.startAnimation(mShowAction);
	        lyo_entrySetting.setVisibility(View.VISIBLE);
	        iv_downPanel.setImageResource(R.drawable.qz_icon_navbar_drop_up);
	    }
		//
		public void updownPanel() {
			if(lyo_entrySetting.isShown()){
				hideTopPanel();
			}else{
				showTopPanel();
			}
		}
}
//暂无出入记录 出入统计 DVR 记录查询  统计通道 出入总数 正在加载
