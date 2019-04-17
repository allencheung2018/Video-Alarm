package com.hri.ess.adapter;

import java.util.List;
import com.hri.ess.pager.BasePager;
import com.hri.ess.view.LazyViewPager;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class HomePagerAdapter extends PagerAdapter{
	private List<BasePager> list;
	private Context context;
	public HomePagerAdapter(List<BasePager> list,Context context){
		this.list = list;
		this.context = context;
	}
	public int getCount() {
		return list.size();
	}

	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}
	public void destroyItem(ViewGroup container, int position, Object object) {
		((LazyViewPager)container).removeView(list.get(position).getView());
	}
	public Object instantiateItem(ViewGroup container, int position) {
		View view = list.get(position).getView();
		((LazyViewPager)container).addView(view);
		return view;
	}
}
