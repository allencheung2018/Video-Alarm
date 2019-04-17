package com.hri.ess.view;

import com.hri.ess.R;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * 播放报警视频录像时间选择POP
 * @author zhuqian
 */
public class VideoTimePop extends PopupWindow {
	private Context context;
	private View view;
	private ListView lv_leve;
	
	private String[] times = new String[]{"1分钟","2分钟","3分钟","4分钟","5分钟","6分钟","7分钟","8分钟"};
	private LayoutInflater inflater;
	
	private int currentTime;//当前选择的时�?
	
	private TimeAdapter mAdapter;
	
	public OnTimeCheckListener onTimeCheckListener;
	public void setOnTimeCheckListener(OnTimeCheckListener onTimeCheckListener) {
		this.onTimeCheckListener = onTimeCheckListener;
	}
	public VideoTimePop(Context context){
		
		super(context);
		this.context = context;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.layout_level_devide_area, null);
		lv_leve = (ListView) view.findViewById(R.id.lv_leve);
		
		mAdapter = new TimeAdapter();
		lv_leve.setAdapter(mAdapter);
		
		lv_leve.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//选择了播放时�?
				currentTime = position;
				mAdapter.notifyDataSetChanged();
				
				if(onTimeCheckListener!=null){
					onTimeCheckListener.onTimeCheck(position);
				}
			}
		});
		// 设置显示的View
		this.setContentView(view);
		// 设置显示的宽�?
		this.setWidth(LayoutParams.MATCH_PARENT);
		// 设置显示的高�?
		this.setHeight(LayoutParams.MATCH_PARENT);
		// 设置窗体可以被点�?
		this.setFocusable(true);
		
		// 设置弹出的动画效�?
		this.setAnimationStyle(R.style.AnimBottom);
		// 实例化一个ColorDrawable
		ColorDrawable bkg = new ColorDrawable(0xb0000000);
		// 设置背景
		this.setBackgroundDrawable(bkg);
	}
	
	public class TimeAdapter extends BaseAdapter{
		public int getCount() {
			return times.length;
		}
		public Object getItem(int position) {
			return times[position];
		}
		public long getItemId(int position) {
			return position;
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if(convertView==null){
				
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.video_tap_time_item, null);
				holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
				holder.cb_check = (CheckBox) convertView.findViewById(R.id.cb_check);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			if(position==currentTime){
				holder.cb_check.setVisibility(View.VISIBLE);
				holder.cb_check.setChecked(true);
			}else{
				holder.cb_check.setVisibility(View.INVISIBLE);
			}
			holder.tv_time.setText(times[position]);
			return convertView;
		}
		
	}
	public class ViewHolder{
		TextView tv_time;
		CheckBox cb_check;
	}
	//时间选了监听
	public interface OnTimeCheckListener{
		//position是�?择的位置
		void onTimeCheck(int position);
	}
}
