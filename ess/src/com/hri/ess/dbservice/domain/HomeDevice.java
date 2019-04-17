package com.hri.ess.dbservice.domain;


/**
 * 家具联动设备
 * @author zhuqian
 *
 */
public class HomeDevice {

	private String name;
	private int state;
	private boolean cando;
	private byte cnNum;
	public boolean isCando() {
		return cando;
	}
	public void setCando(boolean isCando) {
		this.cando = isCando;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public byte getCnNum() {
		return cnNum;
	}
	public void setCnNum(byte num) {
		cnNum = num;
	}
}
