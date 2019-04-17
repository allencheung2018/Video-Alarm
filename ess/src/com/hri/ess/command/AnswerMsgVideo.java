package com.hri.ess.command;

import com.hri.ess.util.Util;

/**
 * 视频数据解析(服务器主动发�?
 * @author yu
 *
 */
public class AnswerMsgVideo extends PushMessage{
	//通道�?
	public byte channelNum;
	//视频编码
	public byte videoCode;
	//特殊编码
	public byte specialCode;
	//分发ID
	public byte distributionId;
	//视频数据
	public byte[] videoData;
	
	public AnswerMsgVideo(){
		
	}

	@Override
	protected void ParseData(byte[] data) {
		channelNum = data[0];
		videoCode = data[1];
		specialCode = data[2];
		distributionId = data[3];
		int offsetLength = distributionId * 4 + 4;
		videoData = new byte[data.length-offsetLength];
		System.arraycopy(data, offsetLength, videoData, 0, videoData.length);
	}
	
	public VideoAnswer MsgToAnswer(){
		VideoAnswer answer = new VideoAnswer();
		answer.cmdCode = 0;
		answer.cmdId = this.cmdId;
		answer.loginID = Util.IntToBytes(this.loginId);
		return answer;
		
	}
	
	public class VideoAnswer extends AnswerCmd{

		@Override
		public byte[] dataToByte() {
			byte[] data = new byte[]{};
			return data;
		}
	}

}
