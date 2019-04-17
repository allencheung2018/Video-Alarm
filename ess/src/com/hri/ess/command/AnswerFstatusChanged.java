package com.hri.ess.command;





/**
 * 状�?变更指令
 * @author zhuqian
 */
public class AnswerFstatusChanged extends PushMessage {
	public byte status;//״̬
	protected void ParseData(byte[] data) {
		status = data[0];
	}
}
