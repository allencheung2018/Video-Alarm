package com.hri.ess.dbservice.control;

import java.sql.SQLException;
import java.util.List;
import android.content.Context;
import com.hri.ess.dbservice.VillaSecurityDBHelper;
import com.hri.ess.dbservice.domain.AlarmInfo;
import com.j256.ormlite.dao.Dao;

/**
 * 报警消息操作
 * @author yu
 *
 */
public class ALarmInfoControl {
	
	private Context mcontext;
	private VillaSecurityDBHelper secDb = null;
	private Dao<AlarmInfo,Integer> alarmDao = null;
	
	public ALarmInfoControl(Context context){
		secDb = new VillaSecurityDBHelper(context);
		try {
			alarmDao = secDb.getDao(AlarmInfo.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 增加新记�?
	 * @param login
	 */
	public  boolean add(AlarmInfo alarmInfo){
		boolean Tag = false;
		try {
			List <AlarmInfo> list= Query();
			for(int i = 0; i < list.size(); i++){
				String alarmId = list.get(i).getAlarmId();
				if(alarmInfo.getAlarmId().equals(alarmId)){
					System.out.println("该警报已经存。");
					return false;
				}
			}
			alarmInfo.setAlarmState(0);
			alarmDao.createIfNotExists(alarmInfo);
			Tag = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return Tag;
	}
	
	/**
	 * 更新某条记录
	 */
	public  void updata(AlarmInfo alarmInfo){		
		try {
			alarmDao.update(alarmInfo);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 查询整个表记�?
	 * @return
	 */
	public List<AlarmInfo> Query(){
		List<AlarmInfo> list = null ;
		try {
			 list= alarmDao.queryForAll();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public void deleteAlarm(AlarmInfo alarmInfo){
		try {
			alarmDao.delete(alarmInfo);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void deleteAlarmAll(){
		try {
			for(int i = 0; i <Query().size();i++ )
			alarmDao.delete(Query().get(i));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
