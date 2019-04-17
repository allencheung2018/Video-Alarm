package com.hri.ess.command;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.hri.ess.network.NetCmd;
import com.hri.ess.util.Util;

/**
 * 获取人流统计数目
 * @author yu
 *
 */
public class CmdPeopleStream extends CMD{
	//通道号
	public byte channelNum;
	//开始时间
	public long startTime;
	//查询类型
	public byte queryType;
	//统计数目
	public int  number;
	//统计周期
	public int cycle;
	//统计周期单位
	public byte unit;
	
	public CmdPeopleStream(){
		this.cmdCode = NetCmd.cmd_PeopleStream;
	}

	@Override
	public byte[] dataToByte() {
		byte[] data = new byte[19];
		int index = 0 ;
		byte[] channelNumbytes = new byte[]{channelNum};
		byte[] startTimebytes = new byte[8];		
		startTimebytes = Util.LongToBytes(startTime);
	
		byte[] queryTypebytes = new byte[]{queryType};
		byte[] numberbytes = Util.IntToBytes(number);
		byte[] cyclebytes = Util.IntToBytes(cycle);
		byte[] unitbytes = new byte[]{unit};
		
		System.arraycopy(channelNumbytes, 0, data, index, channelNumbytes.length);
		index+= channelNumbytes.length;
		System.arraycopy(startTimebytes, 0, data, index, startTimebytes.length);
		index+= startTimebytes.length;
		System.arraycopy(queryTypebytes, 0, data, index, queryTypebytes.length);
		index+= queryTypebytes.length;
		System.arraycopy(numberbytes, 0, data, index, numberbytes.length);
		index+= numberbytes.length;
		System.arraycopy(cyclebytes, 0, data, index, cyclebytes.length);
		index+= cyclebytes.length;
		System.arraycopy(unitbytes, 0, data, index, unitbytes.length);
		return data;
	}
	
	public MsgPeopleStream ParseToMsg(byte[] data){
		MsgPeopleStream msgPeople = new MsgPeopleStream();
		msgPeople.Parse(data);
		return msgPeople;		
	}
	
	public class MsgPeopleStream extends Message{

		public byte channelNum;
		public long time;
		public int inPeopleStream;
		public int peopleStream;	
		public List<PeopleStreamInfo> streamList = new ArrayList<PeopleStreamInfo>();
		
		@Override
		protected void ParseData(byte[] data) {
			int count = data.length/17;
			SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm");
			for (int i = 0; i < count; i++) {
				byte[] itemData = new byte[17];
				System.arraycopy(data, (i)*17, itemData, 0, itemData.length);				
				channelNum = itemData[0];
				byte[] timebytes = Util.getBytes(itemData, 1, 8);
				time = Util.bytesToLong(timebytes,true);				
				byte[] inStreambytes = Util.getBytes(itemData, 9, 4);
				inPeopleStream = Util.getInt(inStreambytes, 0);
				byte[] allStreambytes = Util.getBytes(itemData, 13, 4);
				peopleStream = Util.getInt(allStreambytes, 0);
				
				PeopleStreamInfo info = new PeopleStreamInfo();
				info.channelNum = channelNum;
				info.Time = time;
				info.inPeopleStream = inPeopleStream+"";
				info.residentTime = (inPeopleStream/60)+"分"+(inPeopleStream%60)+"秒";
				info.outPeopleStream = peopleStream+"";
				streamList.add(info);
			}
		}
	}

}
