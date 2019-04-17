package com.hri.ess.dbservice.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


/**
 * 改变设备状态记录
 * @author zhuqian
 *
 */
@DatabaseTable(tableName="t_device_state")
public class DeviceStateRecord {
	@DatabaseField(generatedId=true,columnName="_id")
	private int id;
	@DatabaseField(useGetSet=true,columnName="_time")
	private String time;
	@DatabaseField(useGetSet=true,columnName="_desc")
	private String desc;
	@DatabaseField(useGetSet=true,columnName="_user")
	private String user;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
}
