package com.hri.ess.adapter;

import java.util.ArrayList;

import com.hri.ess.R;
import com.hri.ess.adapter.FlowChannelAdapter.ViewChannelFlowHolder;
import com.hri.ess.app.VillaApplication;
import com.hri.ess.command.PeopleStreamInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ChannelflowRecordsAdapter  extends BaseAdapter {
	private Context context;
	private LayoutInflater inflater;
	ArrayList<PeopleStreamInfo> flows;
	public ChannelflowRecordsAdapter(Context context){
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
			convertView = inflater.inflate(R.layout.layout_channelflow_item, null);
			holder = new ViewChannelFlowHolder();
			holder.txt_channelDate=(TextView)convertView.findViewById(R.id.txt_channelDate);
			holder.txt_channelflowValue=(TextView)convertView.findViewById(R.id.txt_channelflowValue);
			convertView.setTag(holder);
		}else{
			holder = (ViewChannelFlowHolder) convertView.getTag();
		}
		PeopleStreamInfo flow = flows.get(position);
		holder.txt_channelDate.setText(flow.residentTime);
		holder.txt_channelflowValue.setText(flow.outPeopleStream);
		return convertView;
	}
	static class ViewChannelFlowHolder{
		TextView txt_channelDate,txt_channelflowValue;
	}
}
