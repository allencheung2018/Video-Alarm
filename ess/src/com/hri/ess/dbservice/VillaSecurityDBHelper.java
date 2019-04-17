package com.hri.ess.dbservice;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.hri.ess.dbservice.domain.AlarmDetailed;
import com.hri.ess.dbservice.domain.AlarmInfo;
import com.hri.ess.dbservice.domain.DeviceStateRecord;
import com.hri.ess.dbservice.domain.LoginUser;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;


/**
 * 数据库操作类
 * @author zhuqian
 */
public class VillaSecurityDBHelper extends OrmLiteSqliteOpenHelper{
	//数据库名
	private static String DBName = "ess.db";
	//数据库版�?
	private static int DBVersion = 1;	
	public VillaSecurityDBHelper(Context context){
		super(context, DBName, null, DBVersion);
	}

	public void onCreate(SQLiteDatabase arg0, ConnectionSource arg1) {
		try {
			TableUtils.createTable(connectionSource, LoginUser.class);
			TableUtils.createTable(connectionSource, AlarmInfo.class);
			TableUtils.createTable(connectionSource, AlarmDetailed.class);
			TableUtils.createTable(connectionSource, DeviceStateRecord.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void onUpgrade(SQLiteDatabase arg0, ConnectionSource arg1, int arg2,
			int arg3) {
		try {
			TableUtils.dropTable(connectionSource, LoginUser.class, true);
			TableUtils.dropTable(connectionSource, AlarmInfo.class, true);
			TableUtils.createTable(connectionSource, AlarmDetailed.class);
			TableUtils.createTable(connectionSource, DeviceStateRecord.class);
			onCreate(arg0, arg1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
