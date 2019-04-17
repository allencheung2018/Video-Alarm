package com.hri.ess.command;

import com.hri.ess.network.NetCmd;
import com.hri.ess.util.Util;


/**
 * 请求实时视频
 * @author yu
 *
 */
public class CmdPlayRVS extends CMD{
	//频道
	public byte[] channelNum;
	//视频来源
	public byte[] videoSources;
	//分辨�?
	public byte[] resolution;
	
	public  CmdPlayRVS(){
		this.cmdCode = NetCmd.cmd_PlayRvs;
		
	}

	@Override
	public byte[] dataToByte() {
		byte[] data = new byte[channelNum.length+videoSources.length+resolution.length];
		int index = 0;
		System.arraycopy(channelNum, 0, data, index, channelNum.length);
		index += channelNum.length;
		System.arraycopy(videoSources, 0, data, index, videoSources.length);
		index += videoSources.length;
		System.arraycopy(resolution, 0, data, index, resolution.length);	
		return data;
	}
	
	public MsgPlayRVS ParseToMsg(byte[] data){
		MsgPlayRVS msgPlay = new MsgPlayRVS();
		msgPlay.Parse(data);
		return msgPlay;		
	}
	
	
	
	public class MsgPlayRVS extends Message{


		public byte[] channelNum;
		public byte[] videoSources;
		public byte[] resolution;
		public byte[] playHear;
		public byte[] w;
		public byte[] h;
		public short width , hight;
		public byte frameRate;
		
		@Override
		protected void ParseData(byte[] data) {
			channelNum = new byte[]{data[0]};
			videoSources = new byte[] {data[1]};
			resolution = new byte[]{data[2]};
			playHear = new byte[data.length-3]; 
			System.arraycopy(data, 3, playHear, 0, data.length-3);	
			if(playHear.length == 5 ){
			w = new byte[2];
			h = new byte[2];
			System.arraycopy(playHear, 0, w, 0, 2);
			System.arraycopy(playHear, 2, h, 0, 2);
			width = Util.byteToShort(w);
			hight = Util.byteToShort(h);
			}
			frameRate = data[7];
		}
		
	}

}
