package com.hri.ess.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hri.ess.command.AnswerCmd;
import com.hri.ess.command.AnswerFstatusChanged;
import com.hri.ess.command.AnswerMsgAlarm;
import com.hri.ess.command.AqlistPack;
import com.hri.ess.command.CMD;
import com.hri.ess.command.PushMessage;
import com.hri.ess.util.EnumSubjectEvents;
import com.hri.ess.util.EventPublisher;
import com.hri.ess.util.SharePrefUtil;
import com.hri.ess.util.Util;

import android.content.Context;
import android.util.Log;
/**
 * 指令发送
 * 
 * @author yu
 * 
 */
public class TcpClient {

	private static final String TAG = "TcpClient";
	private Context tcpContext;
	private Socket socket;
	private ThreadReciver thReciver;
	private byte cmdId;
	private Map<Byte, Byte[]> hashRecive;
	private EventPublisher publisher;
	public boolean Threadswitch = true;
	
	private final int PORT = 4015;

	public TcpClient(EventPublisher publisher, Context context) {
		tcpContext = context;
		this.publisher = publisher;
		this.thReciver = new ThreadReciver(this);
		this.hashRecive = Collections
				.synchronizedMap(new HashMap<Byte, Byte[]>());
		this.thReciver.start();
	}

	/**
	 * 获取socket
	 * 
	 * @return
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	private synchronized Socket NewOrOpen() throws UnknownHostException,
			IOException {
		if (this.socket != null && this.socket.isConnected()) {
			return this.socket;
		}
		this.socket = new Socket(SharePrefUtil.getString(tcpContext, "ip_config", ""),PORT);
		Log.i("NewOrOpen", ":"+socket);
		return this.socket;
	}

	/**
	 * 发送数据
	 * 
	 * @param cmd
	 * @param timeOut
	 * @return
	 * @throws Exception
	 */
	public byte[] sendWait(CMD cmd, int timeout) throws Exception {
		if(Util.isNetworkAvailable(tcpContext)==0){
			throw new Exception("请检查当前网络状态");
		}
		byte localCMDid;
		byte[] answer = null;
		Socket sk = this.NewOrOpen();
		OutputStream sOut = sk.getOutputStream();
		this.cmdId = (byte) (this.cmdId == 255 ? 0 : this.cmdId + 1);
		localCMDid = cmd.cmdId = this.cmdId;
		Log.i("sendWait","发送 cmdCode:"+cmd.cmdCode + " cmdId:"+localCMDid);
		byte[] sendData = cmd.tobyte();
		try {
			sOut.write(sendData);
		} catch (SocketException e) {
			closeSocket();
		}
		sOut.flush();

		long now = System.currentTimeMillis();
		while (true) {
			long time = System.currentTimeMillis();
			if ((time - now) >= timeout) {
				break;
			}
			synchronized (this.hashRecive) {
				if (this.hashRecive.containsKey(localCMDid)) {
					Log.i("sendWait","收到并删除数据 cmdId:"+localCMDid);
					Byte[] data = this.hashRecive.get(localCMDid);
					this.hashRecive.remove(localCMDid);
					answer = new byte[data.length];
					Util.Bytestobytes(0, data.length, answer, data);
					break;
				}
			}
			Thread.sleep(20);
		}

		if (answer == null) {
			this.closeSocket();
			throw new Exception("超时");
		}
		return answer;
	}

	/**
	 * 推送消息应答
	 * 
	 * @throws IOException
	 * @throws
	 */

	public void answer(AnswerCmd cmd) throws IOException {
		byte[] answer = null;
		Socket sk = this.NewOrOpen();
		OutputStream sOut = sk.getOutputStream();
		byte[] sendData = cmd.tobyte();
		try {
			sOut.write(sendData);
		} catch (SocketException e) {
			closeSocket();
		}
	}
	public void send(CMD cmd) throws UnknownHostException, IOException {
		Socket sk = this.NewOrOpen();
		OutputStream sOut = sk.getOutputStream();
		this.cmdId = (byte) (this.cmdId == 255 ? 0 : this.cmdId + 1);
		cmd.cmdId = this.cmdId;
		Log.i("send","发送 cmdCode:"+cmd.cmdCode + " cmdId:"+cmd.cmdId);
		byte[] sendData = cmd.tobyte();
		try {
			sOut.write(sendData);
		} catch (SocketException e) {
			closeSocket();
		}
	}
	private int mLen;
	/**
	 * 接收数据
	 * 
	 * @throws IOException
	 */
	public void Recive() {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		while (Threadswitch) {
			try {
				
				Socket sk = this.NewOrOpen();
				InputStream sInput = sk.getInputStream();
				if (sInput.available() == 0) {
					mLen = 0;
					Thread.sleep(50);
					continue;
				}
				byte[] buffer = new byte[1024 * 10];
				int len = sInput.read(buffer);
				Log.i("Recive","原始数据长度："+len);
				mLen += len;
				outStream.write(buffer, 0, len);
				byte[] pack = outStream.toByteArray();
				// 分包
				AqlistPack aqlist = new AqlistPack();
				List<Byte[]> lst = aqlist.sqlist(pack);
				// 填充到哈希
				parse(lst);
				outStream.reset();
				
				int ct = pack.length - aqlist.tailIndex - 1;
				if (ct >=0) {
					outStream.write(pack, aqlist.tailIndex + 1, ct);
					byte[] yo = outStream.toByteArray();
				}
			} catch (SocketException e) {
				e.printStackTrace();
				closeSocket();
			} catch (SocketTimeoutException e) {
				e.printStackTrace();
				closeSocket();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 分包解析
	 * 
	 * @param lst
	 */
	private void parse(List<Byte[]> lst) {

		for (int i = 0; i < lst.size(); i++) {
			Byte[] data = lst.get(i);
			byte cmd = data[8];
			byte cmdId = data[9];
			Log.i("parse","收到 cmd:"+cmd + " cmdId:" + (int)cmdId);
			System.out.println();
			if (cmd == 0) {
				synchronized (this.hashRecive) {
					this.hashRecive.put(cmdId, data);
				}
			} else {
				byte[] bytes = new byte[data.length];
				Util.Bytestobytes(0, data.length, bytes, data);
				PushMessage msg = Parse(bytes, cmd);
				if (msg != null)
					this.publisher.publish(this, EnumSubjectEvents.TCPClient_DeviceAlarm, msg, cmd);
			}
		}

	}

	/**
	 * 主动报警信息解析
	 * 
	 * @param data
	 * @param cmdId
	 * @return
	 */
	private PushMessage Parse(byte[] data, byte cmdId) {
		PushMessage msg = null;
		if (cmdId == NetCmd.cmd_Alarm) {
			msg = new AnswerMsgAlarm();
			msg.Parse(data);
			return msg;
		}else if(cmdId == NetCmd.cmd_fStatusChanged){
			msg = new AnswerFstatusChanged();
			msg.Parse(data);
			return msg;
		}
		return msg;
	}

	public void closeSocket() {
		if (this.socket != null) {
			try {
				SIVMClient siv = SIVMClient.getIntance(tcpContext);
				if(this.socket!=null && this.socket.isConnected()){
					this.socket.close();
				}
				this.socket = null;
				siv.loginId = new byte[] { 0, 0, 0, 0 };
				Log.i("closeSocket", "loginId:"+siv.loginId.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 数据接收线程类
	 * 
	 * @author yu
	 * 
	 */
	private class ThreadReciver extends Thread {
		private TcpClient client;

		public ThreadReciver(TcpClient client) {
			this.client = client;
		}

		public void run() {
			this.client.Recive();
		}
	}
}
