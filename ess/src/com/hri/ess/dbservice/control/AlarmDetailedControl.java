package com.hri.ess.dbservice.control;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.hri.ess.dbservice.VillaSecurityDBHelper;
import com.hri.ess.dbservice.domain.AlarmDetailed;
import com.hri.ess.dbservice.domain.LoginUser;
import com.hri.ess.util.SharePrefUtil;
import com.j256.ormlite.dao.Dao;

import android.content.Context;


/**
 * 报警明细控制�?
 * @author zhuqian
 *
 */
public class AlarmDetailedControl {
	private Context mcontext;
	private VillaSecurityDBHelper vHelper = null;

	private Dao<AlarmDetailed, Integer> dao;

	public AlarmDetailedControl(Context context) {
		this.mcontext = context;

		vHelper = new VillaSecurityDBHelper(context);
		try {
			dao = vHelper.getDao(AlarmDetailed.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	//查询�?��报警，当前登录用户的报警
	public List<AlarmDetailed> queryAlarms(){
		List<AlarmDetailed> lists = null;
		try {
			lists = dao.queryBuilder().where().eq("_username", SharePrefUtil.getString(mcontext, "username", "")).query();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lists;
	}
	//查询当前报警ID的报警记录明�?
	public AlarmDetailed getAlarmById(String id){
		AlarmDetailed alarm = null;
		try {
			List<AlarmDetailed> lists = dao.queryBuilder().where().eq("_id", id).query();
			if(lists.size()>0){
				alarm = lists.get(0);
				return alarm;
			}
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return alarm;
	}
	
	public void addAlarm(AlarmDetailed alarm){
		//设备用户名，表明是该用户的报�?
		alarm.setUsername(SharePrefUtil.getString(mcontext, "username", ""));
		try {
			List<AlarmDetailed> lists = dao.queryBuilder().where().eq("_username", SharePrefUtil.getString(mcontext, "username", "")).query();
			//降序排序
			Collections.sort(lists, new Comparator<AlarmDetailed>() {
				public int compare(AlarmDetailed lhs, AlarmDetailed rhs) {
					int result = lhs.getAlarmtime().compareTo(rhs.getAlarmtime());
					return result;
				}
			});
			//如果超过300条，则删除一�?
			if(lists.size()==300){
				dao.delete(lists.get(0));
			}
			//再添�?
			dao.createIfNotExists(alarm);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
