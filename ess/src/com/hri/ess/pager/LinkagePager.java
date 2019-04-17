package com.hri.ess.pager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.hri.ess.R;
import com.hri.ess.adapter.HomeDeviceAdapter;
import com.hri.ess.businesslogic.GetIOChannelBusinessLogic;
import com.hri.ess.businesslogic.ReadDeviceChannelBusinessLogic;
import com.hri.ess.businesslogic.SetIOChannelBusinessLogic;
import com.hri.ess.dbservice.domain.HomeDevice;
import com.hri.ess.dbservice.domain.IOchannelInfo;
import com.hri.ess.view.RefreshableView;
import com.hri.ess.view.RefreshableView.PullToRefreshListener;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 家局联动页面
 * @author zhuqian
 *
 */
public class LinkagePager extends BasePager {
	private ListView device_lists;
	private TextView device_loading;
	private HomeDeviceAdapter mAdapter;
	RefreshableView refreshableView;

	private ArrayList<HomeDevice> divices = new ArrayList<HomeDevice>();
	
	private ReadDeviceChannelBusinessLogic channelBusinessLogic;
	private GetIOChannelBusinessLogic ioChannelBusinessLogic;
	private SetIOChannelBusinessLogic setIOChannelBusinessLogic;
	
	private int ioChannel;//联动口数目
	private boolean isFinishgetIO = false;	//IO状态获取完成标志，完成：false、未完成：true
	
	//单任务的线程池，一个一个执行
	private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
	public LinkagePager(Context context, View titleView) {
		super(context, titleView);
	}
	public View initView() {
		view = inflater.inflate(R.layout.layout_linkage_pager, null);
		device_lists = (ListView) view.findViewById(R.id.device_lists);
		refreshableView = (RefreshableView) view.findViewById(R.id.refreshable_view);
		device_loading = (TextView) view.findViewById(R.id.device_loading);
		channelBusinessLogic = new ReadDeviceChannelBusinessLogic(context);
		ioChannelBusinessLogic = new GetIOChannelBusinessLogic(context);
		setIOChannelBusinessLogic = new SetIOChannelBusinessLogic(context);
		
		
		device_lists.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {
				final HomeDevice device = (HomeDevice) mAdapter.getItem(position);
				if(!device.isCando()){
					Toast.makeText(context, "该联动口不允许操作", 0).show();
					return;
				}
				singleThreadExecutor.execute(new Runnable() {
					byte cnNum;
					public void run() {
						int state=0;
						try {
							//获取串口状态
							if(device.getState()==0){
								state = 1;
							}else{
								state = 0;
							}
							cnNum = device.getCnNum();
							Log.i(TAG, "改变"+cnNum+"联动口："+state);
							setIOChannelBusinessLogic.executionSetIOChannel(cnNum, (byte)state);
							device.setState(state);
							Log.i(TAG, "改变"+cnNum+"联动口："+state+"成功");
							mHandler.sendEmptyMessage(CHANGECHANNELSTATESUCCESS);
						} catch (Exception e) {
							e.printStackTrace();
							Log.i(TAG, "改变"+cnNum+"联动口："+state+"失败");
						}
					}
				});
			}
		});
		
		refreshableView.setOnRefreshListener(new PullToRefreshListener() {
			@Override
			public void onRefresh() {
			    try {
			     Thread.sleep(1000);
			    } catch (InterruptedException e) {
			     e.printStackTrace();
			    }
			    new Thread(){
					public void run() {
						try {
							Log.i(TAG, "获取IO口总数");
							ioChannel = channelBusinessLogic.executionReadDeviceChannel();
							mHandler.sendEmptyMessage(GETCHANNELSUCCESS);
							isLoad = 1;
							Log.i(TAG, "获取IO口总数成功");
						} catch (Exception e) {
							e.printStackTrace();
							mHandler.sendEmptyMessage(GETCHANNELERROR);
							Log.i(TAG, "获取IO口总数失败");
							isLoad = 0;
						}
					};
				}.start();
			    refreshableView.finishRefreshing();
			   }
			  }, 0);
		
		return view;
	}
	private static final int GETCHANNELSUCCESS = 200;
	private static final int GETCHANNELERROR = 202;
	private static final int GETIOSTATESUCCESS = 300;
	private static final int CHANGECHANNELSTATESUCCESS = 400;
	private static final int CHANGECHANNELSTATEERROR = 404;

	protected static final String TAG = "LinkagePager";
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case GETCHANNELSUCCESS:
				if(isFinishgetIO)
					break;
				isFinishgetIO = true;
				//隐藏加载信息
				device_loading.setVisibility(View.GONE);
				divices.clear();
				//mAdapter.notifyDataSetChanged();
				//开始获取设备列表
				for(int i=0;i<ioChannel;i++){
//					if(i==0){
//						divices.clear();
//					}
					final int index = i;
					singleThreadExecutor.execute(new Runnable() {
						public void run() {
							Log.i(TAG, "开始获取IO"+index+"口信息");
							//获取设备信息
							try {
								IOchannelInfo iOchannelInfo = ioChannelBusinessLogic.executionGetIOChannel((byte)index);
								if(iOchannelInfo.IOchannelName.contains("[N]")){
									//不可用
									return;
								}else{
									//可用
									HomeDevice device = new HomeDevice();
									device.setName(iOchannelInfo.IOchannelName);
									device.setCnNum((byte)index);			//联动口通道号
									if(iOchannelInfo.isCanDo==0){
										device.setCando(false);
									}else{
										device.setCando(true);
									}
									device.setState(iOchannelInfo.IOchannelState);
									divices.add(device);
								}
								Log.i(TAG, "获取IO"+index+"口信息成功");
								mHandler.sendEmptyMessage(GETIOSTATESUCCESS);
							} catch (Exception e) {
								e.printStackTrace();
								mHandler.sendEmptyMessage(CHANGECHANNELSTATEERROR);
								Log.i(TAG, "获取IO"+index+"口信息失败");
							}
							//全部IO口状态获取完毕，才能再获取IO状态
							if(index == (ioChannel-1)){
								isFinishgetIO = false;
							}
						}
					});
				}
				break;
			case GETCHANNELERROR:
				Toast.makeText(context, "获取设备信息失败", 0).show();
				break;
			case GETIOSTATESUCCESS:
				((HomeDeviceAdapter)mAdapter).setList(divices);
				mAdapter.notifyDataSetChanged();
				break;
			case CHANGECHANNELSTATESUCCESS:
				((HomeDeviceAdapter)mAdapter).setList(divices);
				mAdapter.notifyDataSetChanged();
				break;
			case CHANGECHANNELSTATEERROR:
				Toast.makeText(context, "控制联动口失败", 0).show();
				break;
			default:
				break;
			}
		};
	};
	public void initData() {
		//加载家具联动开关状态,没有数据
		if(isLoad==0){
			//开始加载设备信息
			isLoad = 2;
			new Thread(){
				public void run() {
					try {
						Log.i(TAG, "获取IO口总数");
						ioChannel = channelBusinessLogic.executionReadDeviceChannel();
						mHandler.sendEmptyMessage(GETCHANNELSUCCESS);
						isLoad = 1;
						Log.i(TAG, "获取IO口总数成功");
					} catch (Exception e) {
						e.printStackTrace();
						mHandler.sendEmptyMessage(GETCHANNELERROR);
						Log.i(TAG, "获取IO口总数失败");
						isLoad = 0;
					}
				};
			}.start();
			mAdapter = new HomeDeviceAdapter(context,null);
			device_lists.setAdapter(mAdapter);
		}
	}
	public void initTitle(View titleView) {
		LinearLayout video_selector = (LinearLayout) titleView.findViewById(R.id.video_selector);
		Button passenger_flow =(Button)titleView.findViewById(R.id.title_passenger_flow);
		passenger_flow.setVisibility(View.GONE);
		TextView home_tv_text = (TextView) titleView.findViewById(R.id.home_tv_text);
		video_selector.setVisibility(View.GONE);
		home_tv_text.setVisibility(View.VISIBLE);
		
		home_tv_text.setText(context.getResources().getString(R.string.home_title_text_linkedage));
	}

}
