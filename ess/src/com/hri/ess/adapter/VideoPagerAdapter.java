package com.hri.ess.adapter;

import h264.com.H264MediaPlayer;
import java.util.List;
import com.hri.ess.view.LazyViewPager;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class VideoPagerAdapter extends PagerAdapter {
	private List<String> channels;
	
	private List<H264MediaPlayer> videoPlays;
	private Context context;
	public VideoPagerAdapter(List<H264MediaPlayer> videoPlays){
		this.videoPlays = videoPlays;
	}
	public int getCount() {
		return videoPlays.size();
	}
	public void destroyItem(ViewGroup container, int position, Object object) {
		((LazyViewPager)container).removeView(videoPlays.get(position));
	}

	public Object instantiateItem(ViewGroup container, int position) {
		((LazyViewPager)container).addView(videoPlays.get(position));
		return videoPlays.get(position);
	}

	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0==arg1;
	}

}
