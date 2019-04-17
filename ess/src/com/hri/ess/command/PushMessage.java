package com.hri.ess.command;

import com.hri.ess.util.Util;



/**
 * 服务器推送消息解�?
 * @author yu
 *
 */
public abstract class PushMessage{	 
	  public int loginId;
	  public byte cmdCode;
	  public byte cmdId;
	  public String error;
	  public int dataLen;
	  /**
	   * 推�?数据包解�?
	   */
	public boolean Parse(byte[] data) {
		boolean tag = false;
		try {
			dataLen = Util.getInt(data, 0);
			loginId = Util.getInt(data, 4);
			cmdCode = data[8];
			cmdId = data[9];
            byte[] dataArea = Util.getBytes(data, 10, data.length - 10);
			if(dataArea != null)
			ParseData(dataArea);
			tag = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tag;
	}

	
	  /**
	   * 数据内容解析
	   * @param data
	   */
	  protected abstract void ParseData(byte[] data);
}
