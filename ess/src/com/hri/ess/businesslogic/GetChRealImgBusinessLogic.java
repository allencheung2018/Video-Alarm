package com.hri.ess.businesslogic;

import android.content.Context;
import com.hri.ess.network.SIVMClient;

public class GetChRealImgBusinessLogic {
	
	private Context mcontext;
	private SIVMClient sivmClient = null;
	
	public GetChRealImgBusinessLogic(Context context){
		mcontext = context;
	}
	/**
	 * 网络连接
	 * @param name
	 * @param psw
	 * @return 
	 * @throws Exception
	 */
	public byte[] executionGetChRealImg(byte channelNum) throws Exception{
		byte[] imgData;
		sivmClient = SIVMClient.getIntance(mcontext);	
		try {
			imgData = sivmClient.getChRealImg(channelNum);
		} catch (Exception e) {
			throw e;
		}
		return imgData;
	}
}
