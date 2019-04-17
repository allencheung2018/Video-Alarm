package com.hri.ess.pager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
/**
 * �?��pager的父�?
 * @author Administrator
 *
 */
public abstract class BasePager {
	protected Context context;
	protected View view;
	protected LayoutInflater inflater;
	protected View titleView;
	
	public int isLoad;//数据是否加载成功,0没有�?成功�?正在加载
	public BasePager(Context context,View titleView){
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.titleView = titleView;
		this.view = initView();
	}
	
	public View getView() {
		return view;
	}

	public abstract View initView();
	public abstract void initData();
	//初始化标题栏
	public abstract void initTitle(View titleView);
	
	protected void showToast(String msg,int time){
		Toast.makeText(context, msg, time).show();
	}
	protected void showToast(String msg){
		Toast.makeText(context, msg, 0).show();
	}
}
