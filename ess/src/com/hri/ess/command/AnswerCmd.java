package com.hri.ess.command;



/**
 * 指令封装基类
 * @author yu
 *
 */
 public abstract class AnswerCmd{	
	/**
	 * 登陆ID
	 */
	public byte[] loginID = null;
	/**
	 * 命令�?
	 */
	public byte cmdCode;
	/**
	 * 命令ID
	 */
	public byte cmdId;
	
	/**
	 * 执行结果
	 */
	public byte results;
	/**
	 * 数据转换
	 * @return
	 */
	public abstract byte[] dataToByte();
	
	/**
	 * 收到数据解析
	 * @return
	 */
	public byte[] tobyte() {	

		int index = 0;
		byte[] data = dataToByte();
		byte[] bytes = new byte[19+data.length];
		byte[] heard = new byte[]{0x48,0x72,0x49,0x3c};//包头
		int len =7+data.length;		
		byte[] packLeng = toBytes(len);
//        byte[] loginId = toBytes(loginID);
		byte[] loginId = loginID;
        byte[] cmdbytes = new byte[]{cmdCode};
        byte[] cmdIdbytes = new byte[]{cmdId};
        byte[] cmdResultsbytes  = new byte[]{results};
        byte[] tail = new byte[]{0x3e,0x68,0x52,0x69};
        //要拷贝数组，从第几位�?��复制，目标数组，从第几个粘贴，复制的个数
        System.arraycopy(heard, 0, bytes, index, heard.length);//复制包头
        index = heard.length;
        System.arraycopy(packLeng, 0, bytes, index, packLeng.length);//复制包长�?
        index += packLeng.length;
        System.arraycopy(loginId, 0, bytes,index, loginId.length);//复制登陆ID
        index += loginId.length;
        System.arraycopy(cmdbytes, 0, bytes,index,cmdbytes.length);//复制命令�?
        index += cmdbytes.length;
        System.arraycopy(cmdIdbytes, 0, bytes, index,cmdIdbytes.length);//复制命令ID
        index += cmdIdbytes.length;
        System.arraycopy(cmdResultsbytes, 0, bytes, index,cmdResultsbytes.length);//复制执行结果
        index += cmdResultsbytes.length;
        System.arraycopy(data, 0, bytes, index,data.length);//复制数据
        index += data.length;
        System.arraycopy(tail, 0, bytes,index,tail.length);//复制包尾	
		return bytes;
	}
	
	private byte[] toBytes(int num) {
		byte[] bLocalArr = new byte[4];
		for (int i = 0; i < 4; i++) {
			bLocalArr[i] = (byte) (num >> 8 * i & 0xFF);
		}
		return bLocalArr;

	}
}
