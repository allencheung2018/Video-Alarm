package com.hri.ess.view;

import java.util.ArrayList;
import java.util.List;

import com.hri.ess.R;
import com.hri.ess.util.Util;
import com.hri.ess.view.CustomCalendar.OnCalendarClickListener;
import com.hri.ess.view.CustomCalendar.OnCalendarDateChangedListener;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CalendarPopupWindow extends PopupWindow {
	private DateSetChangeListener dateSetChangeListener;
	//之前选择的日期
	private String boforeDate;
	
	
	public void setDateSetChangeListener(DateSetChangeListener dateSetChangeListener) {
		this.dateSetChangeListener = dateSetChangeListener;
	}
	public CalendarPopupWindow(Context mContext,String date) {

		this.boforeDate = date;
		View view = View.inflate(mContext, R.layout.popupwindow_calendar,
				null);
		view.startAnimation(AnimationUtils.loadAnimation(mContext,
				R.anim.fade_in));
		LinearLayout ll_popup = (LinearLayout) view
				.findViewById(R.id.ll_popup);
		ll_popup.startAnimation(AnimationUtils.loadAnimation(mContext,
				R.anim.push_bottom_in_1));

		setWidth(LayoutParams.FILL_PARENT);
		setHeight(LayoutParams.FILL_PARENT);
		setBackgroundDrawable(new BitmapDrawable());
		setFocusable(true);
		setOutsideTouchable(true);
		setContentView(view);

		final TextView popupwindow_calendar_month = (TextView) view
				.findViewById(R.id.popupwindow_calendar_month);
		final CustomCalendar calendar = (CustomCalendar) view
				.findViewById(R.id.popupwindow_calendar);
		Button popupwindow_calendar_bt_enter = (Button) view
				.findViewById(R.id.popupwindow_calendar_bt_enter);

		popupwindow_calendar_month.setText(calendar.getCalendarYear() + "年"
				+ calendar.getCalendarMonth() + "月");

		if(boforeDate==null){
			boforeDate = Util.getSpecDateStr();
		}
		if (null != boforeDate) {

			int years = Integer.parseInt(boforeDate.substring(0,
					boforeDate.indexOf("-")));
			int month = Integer.parseInt(boforeDate.substring(
					boforeDate.indexOf("-") + 1, boforeDate.lastIndexOf("-")));
			popupwindow_calendar_month.setText(years + "年" + month + "月");

			calendar.showCalendar(years, month);
			calendar.setCalendarDayBgColor(boforeDate,
					R.drawable.calendar_date_focused);				
		}
		
		List<String> list = new ArrayList<String>(); //设置标记列表
		list.add("2014-04-01");
		list.add("2014-04-02");
		calendar.addMarks(list, 0);

		//监听所选中的日期
		calendar.setOnCalendarClickListener(new OnCalendarClickListener() {

			public void onCalendarClick(int row, int col, String dateFormat) {
				int month = Integer.parseInt(dateFormat.substring(
						dateFormat.indexOf("-") + 1,
						dateFormat.lastIndexOf("-")));
				
				if (calendar.getCalendarMonth() - month == 1//跨年跳转
						|| calendar.getCalendarMonth() - month == -11) {
					calendar.lastMonth();
					
				} else if (month - calendar.getCalendarMonth() == 1 //跨年跳转
						|| month - calendar.getCalendarMonth() == -11) {
					calendar.nextMonth();
					
				} else {
					calendar.removeAllBgColor(); 
					calendar.setCalendarDayBgColor(dateFormat,
							R.drawable.calendar_date_focused);
					boforeDate = dateFormat;
				}
			}
		});

		//监听当前月份
		calendar.setOnCalendarDateChangedListener(new OnCalendarDateChangedListener() {
			public void onCalendarDateChanged(int year, int month) {
				popupwindow_calendar_month
						.setText(year + "年" + month + "月");
			}
		});
		
		//上月监听按钮
		RelativeLayout popupwindow_calendar_last_month = (RelativeLayout) view
				.findViewById(R.id.popupwindow_calendar_last_month);
		popupwindow_calendar_last_month
				.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						calendar.lastMonth();
					}
				});
		
		//下月监听按钮
		RelativeLayout popupwindow_calendar_next_month = (RelativeLayout) view
				.findViewById(R.id.popupwindow_calendar_next_month);
		popupwindow_calendar_next_month
				.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						calendar.nextMonth();
					}
				});
		
		//关闭窗口
		popupwindow_calendar_bt_enter
				.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						if(dateSetChangeListener!=null){
							dateSetChangeListener.dateSetChange(boforeDate);
						}
						dismiss();
					}
				});
	}
	//事件改便监听
	public interface DateSetChangeListener{
		void dateSetChange(String date);
	}
}
