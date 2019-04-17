package com.hri.ess.command;

import com.hri.ess.network.NetCmd;
import com.hri.ess.util.Util;

/**
 * 播放录像视频
 * @author yu
 *
 */
public class CmdPlayVideoTape extends CMD{
	//频道
	public byte[] channelNum;
	//视频来源
	public byte[] videoSources;
	//分辨�?
	public byte[] resolution ;
	//�?��时间
	public long startTime = 0;
	//播放时长
	public short playTime = 0;
	//报警id
	public String alarmId = "";
	
	public  CmdPlayVideoTape(){
		this.cmdCode = NetCmd.cmd_PlayVideoTape;
		
	}

	public byte[] dataToByte() {
		byte[] data = null;
		try{
		byte[] startTimebytes = Util.LongToBytes(startTime);
		byte[] playTimebytes = Util.ShortToBytes(playTime);
		byte[] alarmbytes = null;
		if(alarmId.length()==0){
			data = new byte[channelNum.length+videoSources.length+resolution.length
			                +startTimebytes.length+playTimebytes.length];
		}else{
			alarmbytes = alarmId.getBytes("UTF8");
			data = new byte[channelNum.length+videoSources.length+resolution.length
			                +startTimebytes.length+playTimebytes.length+alarmbytes.length];
		}
		int index = 0;
		System.arraycopy(channelNum, 0, data, index, channelNum.length);
		index += channelNum.length;
		System.arraycopy(videoSources, 0, data, index, videoSources.length);
		index += videoSources.length;
		System.arraycopy(resolution, 0, data, index, resolution.length);	
		index += resolution.length;
		System.arraycopy(startTimebytes, 0, data, index, startTimebytes.length);	
		index += startTimebytes.length;
		System.arraycopy(playTimebytes, 0, data, index, playTimebytes.length);	
		index += playTimebytes.length;
		if(alarmId.length()>0){
			System.arraycopy(alarmbytes, 0, data, index, alarmbytes.length);		
		}
		}catch(Exception e){
			e.printStackTrace();
		}
		return data;
	}
	
	public MsgPlayVideoTape ParseToMsg(byte[] data){
		MsgPlayVideoTape msgPlayVideoTape = new MsgPlayVideoTape();
		msgPlayVideoTape.Parse(data);
		return msgPlayVideoTape;		
	}
	
	
	
	public class MsgPlayVideoTape extends Message{

		public byte[] channelNum;
		public byte[] videoSources;
		public byte[] resolution;
		private byte[] filelength;
		public long fileLength;
		public byte[] playHear;
		public byte frameRate;
		public short width;
		public short hight;
		public byte[] w;
		public byte[] h;
		
		protected void ParseData(byte[] data) {
			channelNum = new byte[]{data[0]};
			videoSources = new byte[] {data[1]};
			resolution = new byte[]{data[2]};	
			filelength = Util.getBytes(data, 3, 8);			
			playHear = new byte[data.length-11]; 
			System.arraycopy(data, 11, playHear, 0, data.length-11);	
			fileLength =Util.bytesToLong(filelength,true);
			if(playHear.length == 5){
				w = new byte[2];
				h = new byte[2];
				System.arraycopy(playHear, 0, w, 0, 2);
				System.arraycopy(playHear, 2, h, 0, 2);
				width = Util.byteToShort(w);
				hight = Util.byteToShort(h);
			}else{
				width = 352;
				hight = 288;
			}
			frameRate = data[15];
		}
		
	}

}
