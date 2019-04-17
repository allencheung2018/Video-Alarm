package com.hri.ess.adapter;
import java.util.ArrayList;
import java.util.List;

import com.hri.ess.R;
import com.hri.ess.adapter.AlarmDetailAdapter.ViewHolder;
import com.hri.ess.app.VillaApplication;
import com.hri.ess.command.PeopleStreamInfo;
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

public class FlowChannelAdapter extends BaseAdapter {
	private Context context;
	private LayoutInflater inflater;
	ArrayList<PeopleStreamInfo> flows;
	public FlowChannelAdapter(Context context){
		this.context = context;
		this.flows = new ArrayList<PeopleStreamInfo>();
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	public void setList(ArrayList<PeopleStreamInfo> flows){
		this.flows=(ArrayList<PeopleStreamInfo>) flows.clone();
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return this.flows.size();
	}
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return this.flows.get(position);
	}
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewChannelFlowHolder holder = null;
		if(convertView==null){
			convertView = inflater.inflate(R.layout.layout_channelflow_list, null);
			holder = new ViewChannelFlowHolder();
			//holder.img_channelflow = (ImageView) convertView.findViewById(R.id.img_channelflow);
			holder.txt_channelname=(TextView)convertView.findViewById(R.id.txt_channelname);
			holder.txt_channelflow=(TextView)convertView.findViewById(R.id.txt_channelflow);
			convertView.setTag(holder);
		}else{
			holder = (ViewChannelFlowHolder) convertView.getTag();
		}
		PeopleStreamInfo flow = flows.get(position);
		//获取图片
		//Bitmap img = BitmapFactory.decodeByteArray(alarm.getAlarmImg(), 0, alarm.getAlarmImg().length);
		//holder.img_channelflow.setImageBitmap(img);
		String channelName=flow.channelNum+"";
		try{
			channelName=VillaApplication.channelList.get((int)flow.channelNum);
		}catch(Exception ex)
		{	
		}
		holder.txt_channelname.setText(channelName);
		holder.txt_channelflow.setText(flow.outPeopleStream);
		return convertView;
	}
	static class ViewChannelFlowHolder{
		//ImageView img_channelflow;
		TextView txt_channelname,txt_channelflow;
	}
}
