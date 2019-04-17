package com.hri.ess.fragment;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * 基础的fragment
 * @author zhuqian
 *
 */
public abstract class BaseFragment extends Fragment {
	protected View view;
	protected Context context;
	
	protected SlidingMenu sm;
	
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		sm = ((SlidingFragmentActivity)context).getSlidingMenu();
		initData(savedInstanceState);
	}
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.context = getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
		this.view = initView(inflater);
		return this.view;
	}
	public View getView() {
		return view;
	}
	public abstract View initView(LayoutInflater inflater);
	public abstract void initData(Bundle savedInstanceState);

}
