package com.hri.ess.adapter;

import java.util.ArrayList;
import java.util.List;

import com.hri.ess.R;
import com.hri.ess.dbservice.domain.HomeDevice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;


/**
 * 家居联动数据适配器
 * @author zhuqian
 *
 */
public class HomeDeviceAdapter extends BaseAdapter {
	private ArrayList<HomeDevice> lists;
	private Context context;
	private LayoutInflater inflater;
	public HomeDeviceAdapter(Context context,ArrayList<HomeDevice> lists){
		this.context = context;
		this.lists = new ArrayList<HomeDevice>();
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	public void setList(ArrayList<HomeDevice>list){
		this.lists=(ArrayList<HomeDevice>) list.clone();
	}
	public int getCount() {
		return lists.size();
	}
	public Object getItem(int position) {
		return lists.get(position);
	}
	public long getItemId(int position) {
		return position;
	}
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView==null){
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.layout_device_lists_item, null);
			convertView.setTag(holder);
			holder.deviceName = (TextView) convertView.findViewById(R.id.device_item_name);
			holder.deviceState = (CheckBox) convertView.findViewById(R.id.device_item_state);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		HomeDevice device = lists.get(position);
		holder.deviceName.setText(device.getName());
		if(device.getState()==0){
			holder.deviceState.setChecked(false);
		}else{
			holder.deviceState.setChecked(true);
		}
		return convertView;
	}
	static class ViewHolder{
		TextView deviceName;
		CheckBox deviceState;
	}
}
