package com.hri.ess.dbservice.control;

import java.sql.SQLException;
import java.util.List;

import android.content.Context;

import com.hri.ess.dbservice.VillaSecurityDBHelper;
import com.hri.ess.dbservice.domain.DeviceStateRecord;
import com.hri.ess.util.SharePrefUtil;
import com.j256.ormlite.dao.Dao;

public class DeviceStateRecordControl {

	private Context mcontext;
	private VillaSecurityDBHelper secDb = null;
	private Dao<DeviceStateRecord,Integer> dao = null;
	
	public DeviceStateRecordControl(Context context){
		secDb = new VillaSecurityDBHelper(context);
		try {
			dao = secDb.getDao(DeviceStateRecord.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public List<DeviceStateRecord> getAll(){
		List<DeviceStateRecord> lists = null;
		String username = SharePrefUtil.getString(mcontext, "username", "");
		try {
			//lists = dao.queryBuilder().orderBy("_time", true).query();
			lists = dao.queryBuilder().orderBy("_time", true).where().eq("_user", username).query();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lists;
	}
	
	public void add(DeviceStateRecord record){
		//先判断是否数据库记录是否超过300�?
		try {
			List<DeviceStateRecord> lists = dao.queryBuilder().orderBy("_time", true).query();
			int count = lists.size();
			if(count==300){
				//超过300条删�?
				dao.deleteById(lists.get(0).getId());
			}
			dao.createIfNotExists(record);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
