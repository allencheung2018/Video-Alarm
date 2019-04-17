package com.hri.ess.dbservice.domain;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
/**
 * 报警明细
 * @author yu
 *
 */
@DatabaseTable(tableName="t_alarmdetail") // tableName 表名
public class AlarmDetailed {
	
	//报警记录ID
	@DatabaseField(id=true,columnName="_id",useGetSet=true)
	private  String alarmId;
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	//报警通道显示名称
	@DatabaseField(columnName="_channelName",useGetSet=true)
	private String channelName;
	//处理状�?
	@DatabaseField(columnName="_State",useGetSet=true)
	private byte State;
	//报警类型
	@DatabaseField(columnName="_alarmType",useGetSet=true)
	private byte alarmType;
	//报警通道
	@DatabaseField(columnName="_channelNum",useGetSet=true)
	private byte channelNum;
	//是否报警
	@DatabaseField(columnName="_isAlarm",useGetSet=true)
	private byte isAlarm;
	//报警时间
	@DatabaseField(columnName="_alarmtime",useGetSet=true)
	private String alarmtime;
	//报警图片
	@DatabaseField(columnName="_alarmImg",useGetSet=true,dataType=DataType.BYTE_ARRAY)
	private byte[] alarmImg;
	//用户
	@DatabaseField(columnName="_username",useGetSet=true)
	private String username;
	public String getAlarmId() {
		return alarmId;
	}
	public void setAlarmId(String alarmId) {
		this.alarmId = alarmId;
	}
	public String getChannelName() {
		return channelName;
	}
	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}
	public byte getState() {
		return State;
	}
	public void setState(byte state) {
		State = state;
	}
	public byte getAlarmType() {
		return alarmType;
	}
	public void setAlarmType(byte alarmType) {
		this.alarmType = alarmType;
	}
	public byte getChannelNum() {
		return channelNum;
	}
	public void setChannelNum(byte channelNum) {
		this.channelNum = channelNum;
	}
	public byte getIsAlarm() {
		return isAlarm;
	}
	public void setIsAlarm(byte isAlarm) {
		this.isAlarm = isAlarm;
	}
	public String getAlarmtime() {
		return alarmtime;
	}
	public void setAlarmtime(String alarmtime) {
		this.alarmtime = alarmtime;
	}
	
	public byte[] getAlarmImg() {
		return alarmImg;
	}
	public void setAlarmImg(byte[] alarmImg) {
		this.alarmImg = alarmImg;
	}

}
