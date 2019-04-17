package com.hri.ess.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hri.ess.R;
import com.hri.ess.dbservice.domain.AlarmDetailed;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * 开关门记录数据适配器
 * @author zhuqian
 *
 */
public class DoorRecordAdapter extends BaseAdapter {
	private ArrayList<AlarmDetailed> doors;
	private Context context;
	private LayoutInflater inflater;
	public DoorRecordAdapter(Context context){
		this.doors = new ArrayList<AlarmDetailed>();
		this.context = context;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	public void setList(ArrayList<AlarmDetailed> doors){
		this.doors=(ArrayList<AlarmDetailed>)doors.clone();
	}
	public int getCount() {
		return doors.size();
	}

	public Object getItem(int position) {
		return doors.get(position);
	}

	public long getItemId(int position) {
		return position;
	}
	public View getView(int position, View convertView, ViewGroup parent) {
		AlarmDetailed doorRecord = doors.get(position);
		ViewHolder holder = null;
		if(convertView==null){
			convertView = inflater.inflate(R.layout.layout_door_records_item, null);
			holder = new ViewHolder();
			convertView.setTag(holder);
			holder.doorImg = (ImageView) convertView.findViewById(R.id.doorImage);
			holder.door_record_date = (TextView) convertView.findViewById(R.id.door_record_date);
			holder.door_record_time = (TextView) convertView.findViewById(R.id.door_record_time);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		holder.doorImg.setImageBitmap(BitmapFactory.decodeByteArray(doorRecord.getAlarmImg(), 0, doorRecord.getAlarmImg().length));
		holder.door_record_date.setText(doorRecord.getAlarmtime().split(" ")[0]);
		holder.door_record_time.setText(doorRecord.getAlarmtime().split(" ")[1]);
		return convertView;
	}
	static class ViewHolder{
		ImageView doorImg;
		TextView door_record_date,door_record_time;
	}
}
