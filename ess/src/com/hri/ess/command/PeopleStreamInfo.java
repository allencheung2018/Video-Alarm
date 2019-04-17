package com.hri.ess.command;

public class PeopleStreamInfo {
	//通道号
	public byte channelNum;
	//时间
	public long Time = 0;
    //进入人数或平均驻留时间
	public String inPeopleStream = "";
	//平均驻留时间
	public String residentTime = "";
	//出人流数
	public String outPeopleStream = "";
}

