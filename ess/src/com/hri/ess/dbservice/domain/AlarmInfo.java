package com.hri.ess.dbservice.domain;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 报警信息�?
 * @author yu
 *
 */
@DatabaseTable(tableName="t_alarm") // tableName 表名
public class AlarmInfo {

	
	public static final String ALARMID="alarmid";  //报警id
	
	public static final String ALARMTYPE = "alarmtype";//报警类型
	
	public static final String ALARMCHANNEL = "alarmchannel";//频道�?
	
	public static final String ALARMTIME = "alarmtime";//报警时间
	
	public static final String ALARMIMG = "alarmimg";//报警图片
	
	public static final String ALARMX = "alarmx";//报警坐标X
	
	public static final String ALARMY = "alarmy";//报警坐标y
	
	public static final String ALARMSTATE = "alarmstate";//状�?0.未查�?1.已查�?2.处理�?3.已结�?
	
	public static final String ALARMVIDEOTAPPATH = "alarmvideotappath";//报警录像地址
		
	@DatabaseField(generatedId=true)     
	private int id;  
	
	@DatabaseField(useGetSet=true,columnName=ALARMID)    
	private String alarmId;
	
	@DatabaseField(useGetSet=true,columnName=ALARMTYPE)    
	private byte alarmType;
	
	@DatabaseField(useGetSet=true,columnName=ALARMCHANNEL)    
	private byte alarmChannel;
	 
	@DatabaseField(useGetSet=true,columnName=ALARMTIME)    
	private String alarmTime;
	
	@DatabaseField(useGetSet=true,columnName=ALARMX)    
	private String alarmX = 0+"";
	
	@DatabaseField(useGetSet=true,columnName=ALARMY)    
	private String alarmY = 0+"";
	
	@DatabaseField(dataType = DataType.BYTE_ARRAY,useGetSet=true,columnName=ALARMIMG)    
	private byte[] alarmImg;
	
	@DatabaseField(useGetSet=true,columnName=ALARMSTATE)    
	private int alarmState;
	
	@DatabaseField(useGetSet=true,columnName=ALARMVIDEOTAPPATH)    
	private String alarmVideoTapPath;

	public String getAlarmId() {
		return alarmId;
	}

	public void setAlarmId(String alarmId) {
		this.alarmId = alarmId;
	}

	public byte getAlarmType() {
		return alarmType;
	}

	public void setAlarmType(byte alarmType) {
		this.alarmType = alarmType;
	}

	public byte getAlarmChannel() {
		return alarmChannel;
	}

	public void setAlarmChannel(byte alarmChannel) {
		this.alarmChannel = alarmChannel;
	}

	public String getAlarmTime() {
		return alarmTime;
	}

	public void setAlarmTime(String alarmTime) {
		this.alarmTime = alarmTime;
	}

	public byte[] getAlarmImg() {
		return alarmImg;
	}

	public void setAlarmImg(byte[] alarmImg) {
		this.alarmImg = alarmImg;
	}

	public int getAlarmState() {
		return alarmState;
	}

	public void setAlarmState(int alarmState) {
		this.alarmState = alarmState;
	}
	
	public String getAlarmX() {
		return alarmX;
	}

	public void setAlarmX(String alarmX) {
		this.alarmX = alarmX;
	}

	public String getAlarmY() {
		return alarmY;
	}

	public void setAlarmY(String alarmY) {
		this.alarmY = alarmY;
	}

	public String getAlarmVideoTapPath() {
		return alarmVideoTapPath;
	}

	public void setAlarmVideoTapPath(String alarmVideoTapPath) {
		this.alarmVideoTapPath = alarmVideoTapPath;
	}
}
