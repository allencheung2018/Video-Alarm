package com.hri.ess.fragment;

import java.util.ArrayList;
import java.util.List;

import com.hri.ess.MainActivity;
import com.hri.ess.R;
import com.hri.ess.adapter.HomePagerAdapter;
import com.hri.ess.pager.BasePager;
import com.hri.ess.pager.DevicePager;
import com.hri.ess.pager.HousePager;
import com.hri.ess.pager.LinkagePager;
import com.hri.ess.pager.RecordPager;
import com.hri.ess.view.LazyViewPager;
import com.hri.ess.view.LazyViewPager.OnPageChangeListener;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;

/**
 * 主页�?
 * 
 * @author zhuqian
 * 
 */
public class HomeFragment extends BaseFragment {

	private List<BasePager> pagers;

	private RelativeLayout title_view;

	private RadioGroup home_radio;
	private RadioButton home_house, home_linkage, home_record, home_device;
	private LazyViewPager home_pagers;

	private HomePagerAdapter homeAdapter;
	private ArrayList<String> channelList;

	public View initView(LayoutInflater inflater) {
		view = inflater.inflate(R.layout.layout_fragment_home, null);
		title_view = (RelativeLayout) view.findViewById(R.id.home_titlebar);
		return view;
	}

	public void initData(Bundle savedInstanceState) {
		this.channelList = ((MainActivity) context).getChannelList();
		// 初始化四个页�?
		initPagers();
		initViews();

	}

	private void initViews() {
		home_radio = (RadioGroup) view.findViewById(R.id.home_radio);
		home_pagers = (LazyViewPager) view.findViewById(R.id.home_pagers);
		home_house = (RadioButton) view.findViewById(R.id.home_house);
		home_linkage = (RadioButton) view.findViewById(R.id.home_linkage);
		home_record = (RadioButton) view.findViewById(R.id.home_record);
		home_device = (RadioButton) view.findViewById(R.id.home_device);

		if (homeAdapter == null) {
			homeAdapter = new HomePagerAdapter(pagers, context);
			home_pagers.setAdapter(homeAdapter);
		}
		// 设置底部导航监听
		home_radio.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				int currentPager = 0;
				switch (checkedId) {
				case R.id.home_house:
					currentPager = 0;
					break;
				case R.id.home_linkage:
					currentPager = 1;
					break;
				case R.id.home_record:
					currentPager = 2;
					break;
				case R.id.home_device:
					currentPager = 3;
					break;
				default:
					break;
				}
				home_pagers.setCurrentItem(currentPager, false);
				pagers.get(currentPager).initData();
				pagers.get(currentPager).initTitle(title_view);
			}
		});
		home_house.setChecked(true);
		home_pagers.setOnPageChangeListener(new OnPageChangeListener() {
			public void onPageSelected(int position) {
				// 限制SlidingMenu
				if (position == 0) {
					sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);// 设置SlidingMenu全屏可以划出
				} else {
					sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);// 设置SlidingMenu都不可以划出
				}
				switch (position) {
				case 0:
					home_house.setChecked(true);
					break;
				case 1:
					home_linkage.setChecked(true);
					break;
				case 2:
					home_record.setChecked(true);
					break;
				case 3:
					home_device.setChecked(true);
					break;
				default:
					break;
				}
			}

			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {

			}

			public void onPageScrollStateChanged(int state) {

			}
		});
	}

	private void initPagers() {
		pagers = new ArrayList<BasePager>();
		pagers.add(new HousePager(channelList, context, title_view));
		pagers.add(new LinkagePager(context, title_view));
		pagers.add(new RecordPager(context, title_view));
		pagers.add(new DevicePager(context, title_view));
	}

	public void changeDeviceState(int state) {
		((DevicePager) pagers.get(3)).changeDeviceState(state);

	}

	public void notifyHaveNewAlarm(String alarmId, boolean isAlarm) {
		((RecordPager) pagers.get(2)).notifyHaveNewAlarm(alarmId, isAlarm);
	}
}
