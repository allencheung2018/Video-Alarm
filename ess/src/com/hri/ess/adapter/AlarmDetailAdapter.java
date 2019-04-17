package com.hri.ess.adapter;

import java.util.ArrayList;
import java.util.List;

import com.hri.ess.R;
import com.hri.ess.app.VillaApplication;
import com.hri.ess.dbservice.domain.AlarmDetailed;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * 详情报警记录列表
 * @author zhuqian
 *
 */
public class AlarmDetailAdapter extends BaseAdapter {
	private Context context;
	List<AlarmDetailed> alarms;
	private ArrayList<String> videos;
	private LayoutInflater inflater;
	public AlarmDetailAdapter(Context context,ArrayList<AlarmDetailed> alarms){
		this.context = context;
		this.alarms = new ArrayList<AlarmDetailed>();
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	public void setList(ArrayList<AlarmDetailed> alarms){
		this.alarms=	(ArrayList<AlarmDetailed>)alarms.clone();
	}
	public void setChannelList(ArrayList<AlarmDetailed> listDetailed, ArrayList<String> listChannel){
		alarms=(ArrayList<AlarmDetailed>) listDetailed.clone();
		this.videos =(ArrayList<String>) listChannel.clone();
	}
	public int getCount() {
		return alarms.size();
	}
	public Object getItem(int position) {
		return alarms.get(position);
	}
	public long getItemId(int position) {
		return position;
	}
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView==null){
			convertView = inflater.inflate(R.layout.layout_alarm_record_item, null);
			holder = new ViewHolder();
			holder.alarm_Image = (ImageView) convertView.findViewById(R.id.alarm_Image);
			holder.alarm_channelName = (TextView) convertView.findViewById(R.id.alarm_channel);
			holder.alarm_type = (TextView) convertView.findViewById(R.id.alarm_type);
			holder.alarm_date = (TextView) convertView.findViewById(R.id.alarm_date);
			holder.alarm_time = (TextView) convertView.findViewById(R.id.alarm_time);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		AlarmDetailed alarm = alarms.get(position);
		//获取图片
		Bitmap img = BitmapFactory.decodeByteArray(alarm.getAlarmImg(), 0, alarm.getAlarmImg().length);
		holder.alarm_Image.setImageBitmap(img);
		//设置信息
		holder.alarm_channelName.setText(videos.get(alarm.getChannelNum()));	//使用通道列表名称
		//holder.alarm_channelName.setText(alarm.getChannelName());				//使用报警明细名称
		holder.alarm_type.setText(VillaApplication.getAlarmType(alarm.getAlarmType()));
		holder.alarm_date.setText(alarm.getAlarmtime().split(" ")[0]);
		holder.alarm_time.setText(alarm.getAlarmtime().split(" ")[1]);
		return convertView;
	}
	static class ViewHolder{
		ImageView alarm_Image;
		TextView alarm_channelName,alarm_type,alarm_date,alarm_time;
	}
}
