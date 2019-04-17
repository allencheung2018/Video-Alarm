package com.hri.ess.pager;

import java.util.ArrayList;
import java.util.List;

import com.hri.ess.R;
import com.hri.ess.adapter.DeviceRecordAdapter;
import com.hri.ess.businesslogic.ChangeDeviceStateBusinessLogic;
import com.hri.ess.businesslogic.ReadDeviceInfoBusinessLogic;
import com.hri.ess.businesslogic.ReadDeviceStateBusinessLogic;
import com.hri.ess.dbservice.control.DeviceStateRecordControl;
import com.hri.ess.dbservice.domain.DeviceStateRecord;
import com.hri.ess.util.SharePrefUtil;
import com.hri.ess.util.Util;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
/**
 * 设备管理页面
 * @author zhuqian
 *
 */
public class DevicePager extends BasePager implements OnClickListener {
	protected static final String TAG = "DevicePager";
	private TextView device_state,device_close_date;
	private RadioGroup device_radio;
	private RadioButton manual,trusteeship,automatic,dissalarm_hand;
	private Button dissalarm_noraml;//临时撤防
	
	private int deviceState;//设备状态
	
	private AlertDialog loadingDialog;
	
	private ReadDeviceStateBusinessLogic readDeviceStateBusinessLogic;
	private ReadDeviceInfoBusinessLogic readDeviceInfoBusinessLogic;
	private ChangeDeviceStateBusinessLogic changeDeviceStateBusinessLogic;
	
	private DeviceStateRecordControl deviceStateRecordControl;
	private ListView device_handle_records;
	
	private ArrayList<DeviceStateRecord> records;
	private DeviceRecordAdapter recordAdapter;
	
	private boolean isNotify = false;//是否是推送过来的设备变更
	
	private boolean isHandle = true;//是否是用户自己手动操作
	private boolean isFailedRadio = false;	//获取状态失败
	private String showState = "";
	public DevicePager(Context context, View titleView) {
		super(context, titleView);
		readDeviceStateBusinessLogic = new ReadDeviceStateBusinessLogic(context);
		readDeviceInfoBusinessLogic = new ReadDeviceInfoBusinessLogic(context);
		changeDeviceStateBusinessLogic = new ChangeDeviceStateBusinessLogic(context);
		deviceStateRecordControl = new DeviceStateRecordControl(context);
	}
	public View initView() {
		view = inflater.inflate(R.layout.layout_device_pager, null);
		device_handle_records = (ListView) view.findViewById(R.id.device_handle_records);
		device_state = (TextView) view.findViewById(R.id.device_state);
		device_close_date = (TextView) view.findViewById(R.id.device_close_date);
		device_radio = (RadioGroup) view.findViewById(R.id.device_radio);
		manual = (RadioButton) view.findViewById(R.id.manual);
		trusteeship = (RadioButton) view.findViewById(R.id.trusteeship);
		automatic = (RadioButton) view.findViewById(R.id.automatic);
		dissalarm_hand = (RadioButton) view.findViewById(R.id.dissalarm_hand);
		dissalarm_noraml = (Button) view.findViewById(R.id.dissalarm_noraml);
		
		device_radio.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(!isHandle){
					Log.i(TAG, "不是用户手动操作");
					return;
				}
				if(isNotify){
					Log.i(TAG, "推送过来的，操作禁止");
					return;
				}
				if(isFailedRadio){
					Log.i("device_radio", "获取状态失败，返回按钮当前状态");
					return;
				}
				
				if(loadingDialog==null){
					loadingDialog = new AlertDialog.Builder(context).create();
				}
				loadingDialog.show();
				loadingDialog.getWindow().setContentView(R.layout.cs_dialog_comm_login_loading);
				((TextView)loadingDialog.findViewById(R.id.TextViewLoading)).setText("正在改变设备状态...");
				//改变设备状态
				int tempState = 0;
				switch (checkedId) {
				case R.id.manual:
					tempState = 0;
					showState = "手动布防";
					break;
				case R.id.automatic:
					tempState = 1;
					showState = "自动布防";
					break;
				case R.id.trusteeship:
					tempState = 2;
					break;
				case R.id.dissalarm_hand:
					tempState = 5;
					showState = "手动撤防";
					break;
				default:
					break;
				}
				new AsyncTask<Integer, Void, Integer>(){
					protected Integer doInBackground(Integer... params) {
						int temp =0, result = 10;
						try {
							temp = params[0];
							Log.i(TAG, "改变设备状态："+temp);
							changeDeviceStateBusinessLogic.executionChangeDeviceState((byte)temp);
							Thread.sleep(1000);
							result = readDeviceStateBusinessLogic.executionReadDeviceState();
							Log.i(TAG, "改变设备状态成功："+result);
						} catch (Exception e) {
							e.printStackTrace();
							Log.i(TAG, "改变设备状态失败："+temp);
						}
						Log.i("device_radio", "deviceState = "+deviceState);
						if(temp == result)
							return result;
						else
							return 10;
					}
					protected void onPostExecute(Integer result) {
						Log.i("device_radio", "result = "+result);						
						if(loadingDialog.isShowing()){
							loadingDialog.dismiss();
						}
						if(result == 10){
							Toast.makeText(context, showState+"失败", 0).show();
							setRadioButtonState();
							return;
						}
						//通知界面
						//changeDeviceStateText();
						records.add(addRecord(result));
						//更新列表
						((DeviceRecordAdapter)recordAdapter).setList((ArrayList<DeviceStateRecord>)records);
						recordAdapter.notifyDataSetChanged();
						device_handle_records.setSelection(records.size());
					};
				}.execute(tempState);
				
				
			}
		});
		dissalarm_noraml.setOnClickListener(this);
		return view;
	}
	
	private void setRadioButtonState() {
		isFailedRadio = true;
		String state = device_state.getText().toString();
		if(state.equals("手动布防")){
			manual.setChecked(true);
		}
		else if(state.equals("自动布防")){
			automatic.setChecked(true);
		}
		else if(state.equals("手动撤防")){
			dissalarm_hand.setChecked(true);
		}else{
			manual.setChecked(false);
			automatic.setChecked(false);
			dissalarm_hand.setChecked(false);
		}
		isFailedRadio = false;
	}
	
	public void initData() {
		if(isLoad == 0){
			isHandle = false;
			//开始获取设备信息
			device_state.setText("正在获取......");
			device_close_date.setText("正在获取......");
			isLoad = 2;
			new AsyncTask<Void, Integer, String[]>(){
				protected String[] doInBackground(Void... params) {
					//开始获取设备状态和设备信息
					String deviceInfo[] = null;
					try {
						Log.i(TAG, "开始获取设备信息");
						deviceState = readDeviceStateBusinessLogic.executionReadDeviceState();
						deviceInfo = readDeviceInfoBusinessLogic.executionReadDeviceInfo(SharePrefUtil.getString(context, "deviceCode", ""));
						//获取成功
						isLoad = 1;
						Log.i(TAG, "获取设备信息成功");
					} catch (Exception e) {
						e.printStackTrace();
						Log.i(TAG, "获取设备信息失败");
						//Toast.makeText(context, "获取设备信息失败", 0).show();
						isLoad = 0;
					}
					return deviceInfo;
				}
				protected void onPostExecute(String[] result) {
					if(result!=null){
						try{
						//获取结束，开始更新界面
						changeDeviceStateText();
						device_close_date.setText(result[7].substring(0, result[7].indexOf(" ")));
						//isHandle = true;
						}catch(Exception ex){
							ex.printStackTrace();
						}
					}else{
						device_state.setText("获取失败...请重试");
						device_close_date.setText("获取失败...请重试");
						Toast.makeText(context, "获取设备信息失败", 0).show();
					}
					isHandle = true;
				};
				protected void onPreExecute() {
					
				};
			}.execute();
			records = (ArrayList<DeviceStateRecord>) deviceStateRecordControl.getAll();
			if(recordAdapter == null){
				recordAdapter = new DeviceRecordAdapter(context, null);
				device_handle_records.setAdapter(recordAdapter);
			}
			if(records != null){
				//更新列表
				((DeviceRecordAdapter)recordAdapter).setList((ArrayList<DeviceStateRecord>)records);
				recordAdapter.notifyDataSetChanged();
				//选中最后一条
				device_handle_records.setSelection(records.size());
			}
			else{
				records = new ArrayList<DeviceStateRecord>();
			}
		}
	}
	public void initTitle(View titleView) {
		LinearLayout video_selector = (LinearLayout) titleView.findViewById(R.id.video_selector);
		Button passenger_flow =(Button)titleView.findViewById(R.id.title_passenger_flow);
		passenger_flow.setVisibility(View.GONE);
		TextView home_tv_text = (TextView) titleView.findViewById(R.id.home_tv_text);
		video_selector.setVisibility(View.GONE);
		home_tv_text.setVisibility(View.VISIBLE);
		
		home_tv_text.setText(context.getResources().getString(R.string.home_title_text_device));
	}
	private void changeDeviceStateText(){
		//改变设备状态，修改文本，radiobutton
		switch (deviceState) {
		case 0:
			//手动布防状态
			device_state.setText("手动布防");
			manual.setChecked(true);
			break;
		case 1:
			//自动布防
			device_state.setText("自动布防");
			automatic.setChecked(true);
			break;
		case 2:
			//设备托管
			device_state.setText("设备托管");
			trusteeship.setChecked(true);
			break;
		case 5:
			//手动撤防
			device_state.setText("手动撤防");
			dissalarm_hand.setChecked(true);
			break;
		default:
			break;
		}
	}
	public void onClick(View v) {
		//临时撤防
		if(loadingDialog==null){
			loadingDialog = new AlertDialog.Builder(context).create();
		}
		loadingDialog.show();
		loadingDialog.getWindow().setContentView(R.layout.cs_dialog_comm_login_loading);
		((TextView)loadingDialog.findViewById(R.id.TextViewLoading)).setText("正在改变设备状态...");
		
		new AsyncTask<Void, Void, Integer>(){
			protected Integer doInBackground(Void... params) {
				int temp = 10;
				try {
					changeDeviceStateBusinessLogic.executionChangeDeviceState((byte)3);
					temp = readDeviceStateBusinessLogic.executionReadDeviceState();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return temp;
			}
			protected void onPostExecute(Integer result) {
				if(loadingDialog.isShowing()){
					loadingDialog.dismiss();
				}
				Log.i("onClick", "result = "+result);
				if(result != 3)					//临时撤防不成功
				{	
					Toast.makeText(context, "临时撤防失败", 0).show();
					return;
				}
				Toast.makeText(context, "临时撤防成功", 0).show();
				records.add(addRecord(3));
                ((DeviceRecordAdapter)recordAdapter).setList(records);
				recordAdapter.notifyDataSetChanged();
				device_handle_records.setSelection(records.size());
			};
		}.execute();
	}
	private DeviceStateRecord addRecord(int state){
		//添加记录
		String desc = null;
		switch (state) {
		case 0:
			desc = "手动布防";
			break;
		case 1:
			desc = "自动布防";
			break;
		case 2:
			desc = "设备托管";
			break;
		case 3:
			desc = "临时撤防";
			break;
		case 5:
			desc = "手动撤防";
			break;
		default:
			break;
		}
		DeviceStateRecord record = new DeviceStateRecord();
		String username = SharePrefUtil.getString(context, "username", "");
		record.setDesc(desc);
		record.setTime(Util.getNowDateTime());
		record.setUser(username);
		deviceStateRecordControl.add(record);
		return record;
	}
	public void changeDeviceState(int state) {
		isNotify = true;
		if(isLoad==0){
			//没有加载成功的时候
			Toast.makeText(context, "您的设备状态已变更", 0).show();
			//isHandle = false;
		}
		Log.i("changeDeviceState", "state="+state);
		deviceState = state;
		changeDeviceStateText();
		isNotify = false;
	}
}
