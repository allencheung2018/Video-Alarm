package com.hri.ess.command;



/**
 * ָ���װ����?
 * @author yu
 *
 */
 public abstract class CMD{	
	/**
	 * ��½ID
	 */
	public byte[] loginID = null;
	/**
	 * ������
	 */
	public byte cmdCode;
	/**
	 * ����ID
	 */
	public byte cmdId;
	
	/**
	 * ����ת��
	 * @return
	 */
	public abstract byte[] dataToByte();
	
	/**
	 * ��ȡ�����װ����?
	 * @return
	 */
	public byte[] tobyte() {	

		int index = 0;
		byte[] data = dataToByte();
		byte[] bytes = null;
		if(data==null){
			bytes = new byte[18];
		}else{
			bytes = new byte[18+data.length];
		}
		byte[] heard = new byte[]{0x48,0x72,0x49,0x3c};//��ͷ
		int len = 0;
		if(data==null){
			len = 6;
		}else{
			len = 6+data.length;		
		}
		byte[] packLeng = toBytes(len);
//        byte[] loginId = toBytes(loginID);
		byte[] loginId = loginID;
        byte[] cmdbytes = new byte[]{cmdCode};
        byte[] cmdIdbytes = new byte[]{cmdId};
        byte[] tail = new byte[]{0x3e,0x68,0x52,0x69};
        //Ҫ�������飬�ӵڼ�λ��ʼ���ƣ�Ŀ�����飬�ӵڼ���ճ�������Ƶĸ���
        System.arraycopy(heard, 0, bytes, index, heard.length);//���ư�ͷ
        index = heard.length;
        System.arraycopy(packLeng, 0, bytes, index, packLeng.length);//���ư�����
        index += packLeng.length;
        System.arraycopy(loginId, 0, bytes,index, loginId.length);//���Ƶ�½ID
        index += loginId.length;
        System.arraycopy(cmdbytes, 0, bytes,index,cmdbytes.length);//����������
        index += cmdbytes.length;
        System.arraycopy(cmdIdbytes, 0, bytes, index,cmdIdbytes.length);//��������ID
        index += cmdIdbytes.length;
        if(data==null){
        	
        }else{
        	System.arraycopy(data, 0, bytes, index,data.length);//��������
        	index += data.length;
        }
        System.arraycopy(tail, 0, bytes,index,tail.length);//���ư�β	
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
