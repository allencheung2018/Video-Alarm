package com.hri.ess.dbservice.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


/**
 * 登录用户�?
 * @author zhuqian
 */
@DatabaseTable(tableName="t_username") // tableName 表名
public class LoginUser {

	public static final String NAME="name"; 
	public static final String PWD="pwd"; 
	@DatabaseField(generatedId=true)     
	private int id;  
	@DatabaseField(useGetSet=true,columnName=NAME)    
	private String name;
	@DatabaseField(useGetSet=true,columnName=PWD)
	private String pwd;
	 
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}  

}
