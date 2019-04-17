package com.hri.ess.command;

import com.hri.ess.util.Util;


/**
 * 返回消息解析
 * @author yu
 *
 */
public abstract class Message{	 
	  public int loginId;
	  public byte cmd;
	  public byte cmdId;
	  public boolean success = false;
	  public String error;
	  public int dataLen;
	  /**
	   * 返回数据包解�?
	   */
	public boolean Parse(byte[] data) {
		try {
			dataLen = Util.getInt(data, 0);
			loginId = Util.getInt(data, 4);
			cmd = data[8];
			cmdId = data[9];

			if (data[10] == 0) {
				success = true;
			}
			if (!success) {
				error = Util.getString(data, 11, data.length - 11);
			} else {
				byte[] dataArea = Util.getBytes(data, 11, data.length - 11);
				if(dataArea != null)
				ParseData(dataArea);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return success;
	}

	
	  /**
	   * 数据内容解析
	   * @param data
	   */
	  protected abstract void ParseData(byte[] data);
}
