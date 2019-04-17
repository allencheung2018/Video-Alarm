package com.hri.ess.adapter;

import java.util.ArrayList;
import java.util.List;

import com.hri.ess.R;
import com.hri.ess.dbservice.domain.DeviceStateRecord;
import com.hri.ess.util.SharePrefUtil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DeviceRecordAdapter extends BaseAdapter {
	private Context context;
	private List<DeviceStateRecord> lists;
	private LayoutInflater inflater;
	public DeviceRecordAdapter(Context context,ArrayList<DeviceStateRecord> lists){
		this.context = context;
		this.lists = new ArrayList<DeviceStateRecord>();
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
   public void setList(ArrayList<DeviceStateRecord> lists){
	   this.lists=(List<DeviceStateRecord>)lists.clone();
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
			convertView = inflater.inflate(R.layout.layout_device_record_item, null);
			holder.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
			holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
			holder.tv_user = (TextView) convertView.findViewById(R.id.tv_user);
			holder.tv_desc = (TextView) convertView.findViewById(R.id.tv_desc);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		DeviceStateRecord record = lists.get(position);
		//填充数据
		holder.tv_date.setText(record.getTime().split(" ")[0]);
		holder.tv_time.setText(record.getTime().split(" ")[1]);
		
		//holder.tv_user.setText("用户"+SharePrefUtil.getString(context, "username", ""));
		holder.tv_desc.setText(record.getDesc());
		return convertView;
	}
	static class ViewHolder{
		TextView tv_date,tv_time,tv_user,tv_desc;
	}
}
