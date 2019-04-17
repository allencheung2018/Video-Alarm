package com.hri.ess.network;

import h264.com.SkyeyeVideoViewControl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hri.ess.command.AnswerCmd;
import com.hri.ess.command.AnswerMsgVideo;
import com.hri.ess.command.AqlistPack;
import com.hri.ess.command.CMD;
import com.hri.ess.command.PushMessage;
import com.hri.ess.util.EnumSubjectEvents;
import com.hri.ess.util.EventPublisher;
import com.hri.ess.util.Util;

import android.content.Context;
import android.util.Log;

/**
 * 视频网络模块
 * 
 * @author yu
 * 
 */
public class VideoTcpClient {
	private static final String TAG = "VideoTcpClient";
	private Context tcpContext;
	private Socket socket;
	private ThreadReciver thReciver;
	private String ip;
	private int port;
	private byte cmdId;
	private Map<Byte, Byte[]> hashRecive;
	private EventPublisher publisher;
	public boolean Threadswitch = true;
	public int SkDatRemained;
	private boolean bDebug = true;
	
	private SkyeyeVideoViewControl control;
	private ByteArrayOutputStream outStream;

	public VideoTcpClient(String ip, int port, EventPublisher publisher,SkyeyeVideoViewControl control) {
		this.publisher = publisher;
		this.control = control;
		this.ip = ip;
		this.port = port;
		this.hashRecive = Collections.synchronizedMap(new HashMap<Byte, Byte[]>());
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
		this.socket = new Socket(this.ip, this.port);
		System.out.println("新建sokect");
		outStream.reset();
		return this.socket;
	}

	/**
	 * 发�?数据, 当服务器连接失败或超时时 关闭SOCKET�?
	 * 
	 * @param cmd
	 * @param timeOut
	 * @return
	 * @throws Exception
	 */
	public byte[] sendWait(CMD cmd, int timeout) throws Exception {
		byte[] answer = null;
		Socket sk = this.NewOrOpen();
		OutputStream sOut = sk.getOutputStream();
		this.cmdId = (byte) (this.cmdId == 255 ? 0 : this.cmdId + 1);
		cmd.cmdId = this.cmdId;
		byte[] sendData = cmd.tobyte();
		try {
			sOut.write(sendData);
		} catch (SocketException e) {
			this.closeSocket();	
			 throw e;
		}
		sOut.flush();
		long now = System.currentTimeMillis();
		while (true) {
			long time = System.currentTimeMillis();
			if ((time - now) >= timeout) {
				break;
			}
			synchronized (this.hashRecive) {
				if (this.hashRecive.containsKey(cmd.cmdId)) {
					Byte[] data = this.hashRecive.get(cmd.cmdId);
					this.hashRecive.remove(cmd.cmdId);
					answer = new byte[data.length];
					Util.Bytestobytes(0, data.length, answer, data);
					break;
				}
			}
			Thread.sleep(500);
		}
		if (answer == null) {
			this.closeSocket();		
			throw new Exception("超时");
		}
		return answer;
	}

	/**
	 * 发�?数据
	 * 
	 * @param cmd
	 * @param timeOut
	 * @return
	 * @throws Exception
	 */
	public void send(CMD cmd, int timeout) throws Exception {
		Socket sk = this.NewOrOpen();
		OutputStream sOut = sk.getOutputStream();
		this.cmdId = (byte) (this.cmdId == 255 ? 0 : this.cmdId + 1);
		cmd.cmdId = this.cmdId;
		byte[] sendData = cmd.tobyte();
		try {
			sOut.write(sendData);
			sOut.flush();
		} catch (SocketException e) {
			closeSocket();
		}
		Thread.sleep(timeout/15);		//发出停止视频指令后延时关闭socket
	}

	/**
	 * 推�?消息应答
	 * 
	 * @throws IOException
	 * @throws
	 */

	public void answer(AnswerCmd cmd) throws IOException {
		// byte[] answer = null;
		Socket sk = this.NewOrOpen();
		OutputStream sOut = sk.getOutputStream();
		byte[] sendData = cmd.tobyte();
		try {
			sOut.write(sendData);
		} catch (SocketException e) {
			this.closeSocket();
		}
	}
	/**
	 * 接收数据
	 * 
	 * @throws IOException
	 */
	public void Recive() {
		outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024 * 10];
		byte[] pack;
		//byte[] yo;
		List<Byte[]> lst = new ArrayList<Byte[]>();
		AqlistPack aqlist = new AqlistPack();
		Socket sk = null;
		InputStream sInput = null;
		try {
			sk = NewOrOpen();
			sInput = sk.getInputStream();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		while (Threadswitch) {
			try {				
				sk = NewOrOpen();
				SkDatRemained = sInput.available();		//socket剩余数据
				if (SkDatRemained == 0) {
					Thread.sleep(50);
					continue;
				}
				
				int len = sInput.read(buffer);
				if(bDebug){
					Log.i("Receice", "len = "+len +" SkDatRemained = "+SkDatRemained);
					//continue;
				}

				outStream.write(buffer, 0, len);

				pack = outStream.toByteArray();
				//分包
				lst = aqlist.sqlist(pack);
				//填充到哈希
				parse(lst);
				outStream.reset();
				int ct = pack.length - aqlist.tailIndex - 1;
				if (ct > 0) {
					outStream.write(pack, aqlist.tailIndex + 1, ct);
					byte[] yo = outStream.toByteArray();
				}
			} catch (Exception e) {
				e.printStackTrace();
				//closeSocket();
			}
		}
	}

	private void parse(List<Byte[]> lst) {

		for (int i = 0; i < lst.size(); i++) {
			Byte[] data = lst.get(i);
			byte cmd = data[8];
			byte cmdId = data[9];
			if (cmd == 0) {
				synchronized (this.hashRecive) {
					this.hashRecive.put(cmdId, data);
				}
			} else {
				byte[] bytes = new byte[data.length];
				Util.Bytestobytes(0, data.length, bytes, data);
				PushMessage msg = Parse(bytes, cmd);
				if (msg != null)
					this.publisher.publish(this,
							EnumSubjectEvents.TCPClient_DeviceAlarm, msg, cmd);
			}
		}

	}

	/**
	 * 视频到达解析
	 * 
	 * @param data
	 * @param cmdId
	 * @return
	 */
	private PushMessage Parse(byte[] data, byte cmd) {
		PushMessage msg = null;
		if (cmd == NetCmd.cmd_VideoArrive) {
			msg = new AnswerMsgVideo();
			msg.Parse(data);
			return msg;
		} else if (cmd == NetCmd.cmd_VideoTapeOver) {
			System.out.println("录像到达完成");
			this.publisher.publish(this,EnumSubjectEvents.TCPClient_DeviceAlarm, "录像到达完成", cmd);
		}
		return msg;
	}

	public void closeSocketAndDespoyThread() {
		Threadswitch = false;
		if (this.socket != null) {
			try {
				this.socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.socket = null;
		}
	}
	/**
	 * 关闭连接，但不销毁线
	 */
	public void closeSocket() {
		Threadswitch = false;
		if (this.socket != null) {
			try {
				//直接关闭socket不会立马关闭
				this.socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.socket = null;
			//control.loginId = new byte[] { 0, 0, 0, 0 };
		}
	}

	/**
	 * 数据接收线程
	 * 
	 * @author yu
	 * 
	 */
	private class ThreadReciver extends Thread {
		private VideoTcpClient client;

		public ThreadReciver(VideoTcpClient client) {
			this.client = client;
		}

		public void run() {
			this.client.Recive();
		}
	}

	/**
	 * 数据接收线程类
	 * 
	 * @author allen
	 * 
	 */
	public void StartReceiveData() {
		Threadswitch = true;
		this.thReciver = new ThreadReciver(this);
		this.thReciver.start();
	}

}
