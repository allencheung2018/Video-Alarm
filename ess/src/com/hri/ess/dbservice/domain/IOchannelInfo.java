package com.hri.ess.dbservice.domain;

public class IOchannelInfo {

	public String toString() {
		return "IOchannelInfo [IOchannelState=" + IOchannelState + ", isCanDo="
				+ isCanDo + ", IOchannelName=" + IOchannelName + "]";
	}

	// 当前状�?
	public byte IOchannelState;
	// 是否允许手动操作
	public byte isCanDo;
	// 联动口名�?
	public String IOchannelName;

}
