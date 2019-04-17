package com.hri.ess.adapter;

import java.util.ArrayList;
import java.util.List;

import com.hri.ess.PlayVideoActivity;
import com.hri.ess.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * 视频通道列表适配�?
 * @author zhuqian
 *
 */
public class VideoListAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<String> videos;
	private Bitmap videothumbnail = null;
	private Drawable drawable;
	private LayoutInflater inflater;
	private int mChildCount = 0;
	
	public VideoListAdapter(Context context,ArrayList<String> videos){
		this.context = context;
		this.videos = new ArrayList<String>();
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Resources resources = context.getResources();
		drawable = resources.getDrawable(R.drawable.video_play_icon);
	}
	public void setList(ArrayList<String> list){
		this.videos=(ArrayList<String>)list.clone();
	}
	public int getCount() {
		return videos.size();
	}
	public Object getItem(int position) {
		return videos.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView==null){
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.video_list_item, null);
			holder.video_play = (ImageView) convertView.findViewById(R.id.video_iv_play);
			holder.video_channelName = (TextView) convertView.findViewById(R.id.video_tv_channelName);
			holder.Video_channelType = (TextView) convertView.findViewById(R.id.video_tv_channeltype);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		//通道�?
		String channelName = videos.get(position);
		
		if(!channelName.contains("[")){
			//不包含[]的是报警通道
			holder.video_channelName.setText(channelName);
			holder.Video_channelType.setText("报警通道");
		}else if(channelName.contains("[N]")){
			//包含[N]的是出入通道
			String subName = channelName.substring(0, channelName.indexOf("[N]")-1);
			holder.video_channelName.setText(subName);
			holder.Video_channelType.setText("出入通道");
		}else if(channelName.contains("[KEEP]")){
			//包含[KEEP]表示禁用通道
			String subName = channelName.substring(0, channelName.indexOf("[KEEP]")-1);
			holder.video_channelName.setText(subName);
			holder.Video_channelType.setText("禁用通道");
		}
		
		videothumbnail = PlayVideoActivity.getVideoImg(position);
		if(videothumbnail != null){ 
			holder.video_play.setImageBitmap(videothumbnail);
		}
		else{
			holder.video_play.setImageDrawable(drawable);
		}
		return convertView;
	}
	
	public static class ViewHolder{
		ImageView video_play;
		TextView video_channelName,Video_channelType;
	}
	
	public void setVideoList(ArrayList<String> videos){
		this.videos = (ArrayList<String>) videos;
	}
}
