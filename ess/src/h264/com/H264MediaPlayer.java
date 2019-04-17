package h264.com;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import com.hri.ess.PlayVideoActivity;
import com.hri.ess.PlayVideoActivity.OnLineVideoListener;
import com.hri.ess.PlayVideoActivity.OnVideoDecordListener;
import com.hri.ess.R;
import com.hri.ess.command.AnswerMsgVideo;
import com.hri.ess.network.NetCmd;
import com.hri.ess.network.VideoTcpClient;
import com.hri.ess.util.EnumSubjectEvents;
import com.hri.ess.util.EventPublisher;
import com.hri.ess.util.FileUtil;
import com.hri.ess.util.IEventNotify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

/**
 * 视频显示View
 * 
 * @author yu
 * 
 */
public class H264MediaPlayer extends View implements IEventNotify {

	private static final String TAG = "H264MediaPlayer";
	private Context mcontext;
	private SkyeyeVideoViewControl videoControl;
	private EventPublisher mypublisher;
	private byte channelNum, videoSources, resolution;
	private String deviceCode;
	private long startTime;
	private short playTime;
	private String alarmId;
	private boolean isOnlineVideo = false;

	private byte[] mPixel = null;
	private ByteBuffer buffer;
	private Bitmap videoBitmap;

	public String videoTapPath = "";
	private OnLineVideoListener onLineVideoListener;
	private OnVideoDecordListener onVideoDecordListener;

	public void setOnLineVideoListener(OnLineVideoListener onLineVideoListener) {
		this.onLineVideoListener = onLineVideoListener;
	}

	public void setOnVideoDecordListener(
			OnVideoDecordListener onVideoDecordListener) {
		this.onVideoDecordListener = onVideoDecordListener;
	}

	public Bitmap getVideoBitmap() {
		return videoBitmap;
	}

	public void setVideoBitmap(Bitmap videoBitmap) {
		this.videoBitmap = videoBitmap;
	}

	private float playWidth = 800;
	private float playHeight = 480;

	private int vedioWidth = 352;
	private int vedioHeight = 288;
	
	private int sizeImgW = vedioWidth;
	private int sizeImgH = vedioHeight;

	int mTrans = 0x0F0F0F0F;
	private long currentTime = 0;
	private long maxInterval = 10;
	int iTemp = 0;
	int nalLen;
	private int frameCount = 0;
	private long oldTime = 0;
	boolean bFirst = true;
	boolean bFindPPS = true;
	int bytesRead = 0;
	int NalBufUsed = 0;
	int SockBufUsed = 0;
	int escapeMax = 5;
	int escapeLen = 0;
	byte[] NalBuf;
	private Queue<byte[]> queue = new LinkedList<byte[]>();
	private boolean playSwitch = false;
	private boolean isView = false;		
	private boolean addHead = false;
	private PlayThread playThread;
	private PlayVideoTapThread playVideoTapThread;
	private int startVideo = 0;		//已播放帧数
	private byte frameRate;

	private Paint mPaint;

	private int width;
	private int height;
	
	private MyThread ViewThread;
	private Matrix matrix = new Matrix();
	private double playVideoFramerate, realFps, validFps;
	private int getTapDataDelay;			//获取录像视频数据时间间隔ms
	private boolean isDebug = true;
	private boolean isVideoTapFinished = false;	//录像视频数据达到完成为true
	int numRevH264 = 0;							//实际接收到的H264数据帧数
	int iqueuesize;								//原始数据队列长度
	private int RGBQueuesize, h264Queuesize;	//解码库视频队列长度
	private int SkDatRemained;
	//test
	private FileWriter InDataGap; 
	private FileOutputStream H264source;
	private long H264lastclock = 0;	//us
	private long H264curclock = 0;
	private long H264averclock = 0;
	private long H264sumclock = 0;
	private int framecnt = 0;

	/**
	 * 获取解码库版本号
	 * 
	 * @return
	 */
	public native static String getDecLibVer();
	/**
	 * 初始化解码底层函数及队列
	 * 
	 * @return
	 */
	public native int reclaimThreadFunc();
	/**
	 * 初始化解码底层函数及队列
	 * 
	 * @return
	 */
	public native int initDecoderThreadFunc(int outW, int outH);
	/**
	 * H264数据进入解码队列
	 * 
	 * @param in
	 * @param insize
	 * @return
	 */
	public native int H264DataInDecQueue(byte[] in, int insize);
	/**
	 * 获取RGB数据
	 * 
	 * @param in
	 * @param insize
	 * @return
	 */
	public native int getRGBData(byte[] in);
	/**
	 * 获取RGB队列长度
	 * @return RGBsize
	 */
	public native int getRGBQueueSize();
	/**
	 * 获取H264队列长度
	 * @return RGBsize
	 */
	public native int getH264QueueSize();
	/**
	 * 获取H264帧率
	 * @return h264帧率
	 */
	public native double getH264Framerate();

	// 调用lib库
	static {
		try {
			System.loadLibrary("avutil");
			System.loadLibrary("swresample");
			System.loadLibrary("avcodec");
			System.loadLibrary("swscale");
			System.loadLibrary("DecoderLib");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public H264MediaPlayer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public H264MediaPlayer(Context context) {
		super(context);
		init(context);
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		width = getMeasuredWidth();
		height = getMeasuredHeight();
	}

	/**
	 * 初始化播放界面
	 * 
	 * @param context
	 */
	private void init(Context context) {
		this.mcontext = context;
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
		mPaint.setTextSize(20.0f);
		updateBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.video_updata_ico);
		downloadBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.video_download_ico);
		mypublisher = new EventPublisher();
		mypublisher.subject(EnumSubjectEvents.TCPClient_DeviceAlarm, this);
		NalBuf = new byte[409800];
		DisplayMetrics dm = getResources().getDisplayMetrics();
		this.playWidth = dm.widthPixels;
		this.playHeight = dm.heightPixels;
		setDisplay();
		startVideo = 0;
	}

	/**
	 * 设置在线视频信息（播发在线视频时必须）
	 * 
	 * @param deviceCode
	 */
	public void setVideoInfo(byte channelnum, byte videosources,
			byte resolutions, String devicecode) {
		channelNum = channelnum;
		videoSources = videosources;
		resolution = resolutions;
		deviceCode = devicecode;
		videoControl = new SkyeyeVideoViewControl(mcontext, mypublisher);
		videoControl.deviceCode = deviceCode;
	}

	/**
	 * 录像视频信息（播放录像时必须）
	 * 
	 * @param channelnum
	 *            通道号
	 * @param videosources
	 *            图像源
	 * @param starttime
	 *            开始时间
	 * @param playtime
	 *            播放时长
	 * @param alarmid
	 *            报警ID
	 * @param devicecode
	 */
	public void setVideoTapeInfo(byte channelnum, byte videosources,
			byte resolutions, long starttime, short playtime, String alarmid,
			String devicecode) {
		channelNum = channelnum;
		videoSources = videosources;
		resolution = resolutions;
		startTime = starttime;
		playTime = playtime;
		alarmId = alarmid;
		deviceCode = devicecode;
		videoControl = new SkyeyeVideoViewControl(mcontext, mypublisher);
		videoControl.deviceCode = deviceCode;
		Log.i("setVideoTapeInfo", "channelNum:"+channelNum + " videoSources:"+videoSources
				+ " resolution:"+resolution + " playTime:"+playTime + " alarmId:"+alarmId);
	}

	/**
	 * 播放在线视频
	 * 
	 * @throws Exception
	 */
	public void playVideo() throws Exception {
		//setDisplay();
		Log.i("playVideo", "开始播放实时视频："+channelNum+videoSources+resolution);
		//开始接收数据
		queue.clear();
		numRevH264 = 0;
		isVideoArrive = 0;
		videoStop = false;
		try {
			videoControl.startVideo(channelNum, videoSources, resolution);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
		if (videoControl.Head.length == 5) {
			vedioWidth = videoControl.videoWidth;
			vedioHeight = videoControl.videoHeight;
			Log.i(TAG, "Head len = 0 vW="+vedioWidth + " vH="+vedioHeight);
			if (vedioWidth == 0 || vedioHeight == 0) {
				vedioWidth = 352;
				vedioHeight = 288;
				//throw new Exception(channelNum + "Head len = 0 and vw or vh = 0");
				
			}
		} else {
			vedioWidth = 352;
			vedioHeight = 288;
		}
		Log.i("playVideo","videoWidth：" + vedioWidth + "，videoHeight："+vedioHeight 
				+" framerate = "+videoControl.frameRate);
		setDisplay();

		initDecoderThreadFunc(vedioWidth, vedioHeight);
		if (videoControl.videoSources == (byte) 2)
			queue.offer(videoControl.Head);
		addHead = true;
		//
		startVideo = 0;		//须在播放线程启动前设置
		playSwitch = true;
		playThread = new PlayThread();
		playThread.start();
		ViewThread = new MyThread();
		ViewThread.start();
		isOnlineVideo = true;
		videoStop = false;
	}

	public boolean isVideoStop() {
		return videoStop;
	}

	/**
	 * 播放录像视频
	 * 
	 * @throws Exception
	 */
	public void playVideoTape() throws Exception {
		Log.i("playVideoTape", "开始播放录像视频："+channelNum+videoSources+resolution);
		//开始接收数据
		queue.clear();
		numRevH264 = 0;
		isVideoArrive = 0;
		videoStop = false;
		isVideoTapFinished = false;
		try {
			videoControl.startVideoTape(channelNum, videoSources, resolution,
					startTime, playTime, alarmId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("" + channelNum);
		}
		frameRate = videoControl.frameRate;
		vedioWidth = videoControl.videoWidth;
		vedioHeight = videoControl.videoHeight;
		Log.i("264MP", "获取设备录像参数--frameRate = "+frameRate +" vedioWidth = "+vedioWidth
				+ " vedioHeight = "+vedioHeight);
		if (vedioWidth == 0 || vedioHeight == 0) {
			Log.i("", "vedioWidth = "+vedioWidth + " vedioHeight = "+vedioHeight);
			vedioWidth = 352;
			vedioHeight = 288;
		}
		setDisplay();
		initDecoderThreadFunc(vedioWidth, vedioHeight);
		if (videoControl.videoSources == (byte) 2)
			queue.offer(videoControl.Head);
		addHead = true;
		
		startVideo = 0;		//须在播放线程启动前设置
		playSwitch = true;
		playVideoTapThread = new PlayVideoTapThread(alarmId,
				videoControl.filelength - videoControl.Head.length);
		playVideoTapThread.start();
		ViewThread = new MyThread();
		ViewThread.start();
		isOnlineVideo = false;
		videoStop = false;
		
	}

	public void setDisplay() {
		mPixel = new byte[this.vedioWidth * this.vedioHeight * 2];
		buffer = ByteBuffer.wrap(mPixel);
		videoBitmap = Bitmap.createBitmap(this.vedioWidth, this.vedioHeight,
				Config.RGB_565);
		int i = mPixel.length;
		for (i = 0; i < mPixel.length; i++) {
			mPixel[i] = (byte) 0x00;
		}
		float w = playWidth / (float) vedioWidth;
		float h = playHeight / (float) vedioHeight;
		matrix.setScale(w, h);
	}

	/**
	 * 填充视频数据 老的播放方式
	 */
	public synchronized void sendVideoData(byte[] videoData) {
		SockBufUsed = 0;
		bytesRead = videoData.length;
		while (bytesRead - SockBufUsed > 0) {
			nalLen = MergeBuffer(NalBuf, NalBufUsed, videoData, SockBufUsed,
					bytesRead - SockBufUsed);
			NalBufUsed += nalLen;
			SockBufUsed += nalLen;
			while (mTrans == 1) {
				mTrans = 0xFFFFFFFF;
				if (bFirst == true) // the first start flag
				{
					bFirst = false;
				} else // a complete NAL data, include 0x00000001 trail.
				{
					if (bFindPPS == true) // true
					{
						if ((NalBuf[4] & 0x1F) == 7) {
							bFindPPS = false;
						} else {
							NalBuf[0] = 0;
							NalBuf[1] = 0;
							NalBuf[2] = 0;
							NalBuf[3] = 1;
							NalBufUsed = 4;
							break;
						}
					}
					iTemp = 0;
//					 FileUtil.writeFileBitData("解码前的数据.txt", NalBuf, true,
//							 NalBufUsed - 4);
					synchronized (mPixel) {
						//iTemp = DecoderNal(NalBuf, NalBufUsed - 4, mPixel);
					}
					if (iTemp > 0) // 解码成功
					{
						currentTime = System.currentTimeMillis();
						if (oldTime != 0)// 判断是否是第一帧解出
						{
							long tempTimeLen = currentTime - oldTime;
							if (tempTimeLen < maxInterval) {
								try {
									Thread.sleep(maxInterval - tempTimeLen);
								} catch (InterruptedException e) {
									e.printStackTrace();
								} finally {
								}
							}
						}
						if (!isOnlineVideo && ((int) frameRate) < 10) {
							// 防止快进
							try {
								Thread.sleep(130);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						postInvalidate();
						startVideo++;
						if (startVideo == 1) {
							// 第二次被调用的时候，视频才是真正的解码出来，关闭进度条，回调
							if (onLineVideoListener != null) {
								onLineVideoListener.onVideoReceive();
							}
						}
						oldTime = currentTime;
					} // 解码成功
					frameCount++;
					//callbackobj.reviceFrame(frameCount+"");
				}

				escapeLen++;

				NalBuf[0] = 0;
				NalBuf[1] = 0;
				NalBuf[2] = 0;
				NalBuf[3] = 1;
				NalBufUsed = 4;
				System.gc();
			}
		}
	}

	public synchronized int getH264Data(byte[] videoData) {
		if(videoData == null)
			return -1;
		
		int h264datalen = videoData.length;
		
		if(h264datalen > 0)
			iTemp = H264DataInDecQueue(videoData, h264datalen);
		//test
//		H264curclock = System.nanoTime()/1000000;
//		if(framecnt >= 1){
//			H264sumclock += H264curclock-H264lastclock;
//			H264averclock = H264sumclock/framecnt;
//		}	
//		try {
//			InDataGap.write("H264 Data Gap : " + (H264curclock-H264lastclock) + "\t len: "
//					+ h264datalen + "\n" 
//					+ "Average Gap : " + H264averclock + "\n");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		try {
//		H264source.write(videoData);
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
//		
//		H264lastclock = H264curclock;
//		framecnt +=1;
		//test
		return iTemp;
	}
	/**
	 * 
	 * @param NalBuf
	 * @param NalBufUsed
	 * @param SockBuf
	 * @param SockBufUsed
	 * @param SockRemain
	 * @return
	 */
	int MergeBuffer(byte[] NalBuf, int NalBufUsed, byte[] SockBuf,
			int SockBufUsed, int SockRemain) {
		int i = 0;
		byte Temp;

		for (i = 0; i < SockRemain; i++) {
			Temp = SockBuf[i + SockBufUsed];
			NalBuf[i + NalBufUsed] = Temp;
			mTrans <<= 8;
			mTrans |= Temp;
			if (mTrans == 1) // 找到一个开始字
			{
				i++;
				break;
			}
		}

		return i;
	}

	private boolean videoStop = false;

	/**
	 * 停止视频 即不关闭SOCKET也不销毁接收线程，只是发送停止播放视频命令
	 * 
	 * @throws Exception
	 */
	public void stopVideo() throws Exception {
		Log.i("264MP", "停止播放实时视频："+channelNum);
//		if(videoBitmap != null)
//			PlayVideoActivity.setVideoImg(Bitmap.createScaledBitmap(
//					videoBitmap,sizeImgW,sizeImgH,true), channelNum);
		videoStop = true;
		playSwitch = false;
		videoControl.stopVideo();
		reclaimThreadFunc();
		//test
		InDataGap.close();
		H264source.close();
	}

	/**
	 * 停止录像 即不关闭SOCKET也不销毁接收线程
	 * 
	 * @throws Exception
	 */
	public void stopTapVideo() throws Exception {
		Log.i("stopTapVideo","isVideoArrive = "+isVideoArrive + " numRevH264="+numRevH264);
		videoStop = true;
		playSwitch = false;
		videoControl.stopTapVideo();
		
		reclaimThreadFunc();
	}

	/**
	 * 停止本地视频
	 * 
	 * @throws Exception
	 */
	public void stopLocalVideo() {
		playSwitch = false;
		System.out.println("停止本地录像");
	}

	private int rxRate;// 接收网速
	private int txRate;// 发送网速
	private Bitmap updateBitmap;// 上传图标
	private Bitmap downloadBitmap;// 下载图标

	protected void onDraw(Canvas canvas) {
		// RectF rectF = new RectF(0, 0, playWidth, playHeight);
		buffer.position(0);
		synchronized (mPixel) {
			videoBitmap.copyPixelsFromBuffer(buffer);
		}
		// 当viewpager滑动的时候也会调用该方法
		canvas.drawBitmap(videoBitmap, matrix, null);
	}

	private int isVideoArrive = 0;// 视频第一次到达，通知界面开始解码
	private AnswerMsgVideo msgVideo;
	private VideoTcpClient vtc;
	private byte[] videoData;
	private long startTimeRecvData;

	public void Notify(Object sender, EnumSubjectEvents event, Object eArgs,
			byte cmdId) {
		if (videoStop) {
			return;
		}
		//视频播放完成
		if(cmdId == NetCmd.cmd_VideoTapeOver){
			isVideoTapFinished = true;
			//只对录像视频测算帧率
			if(alarmId.length() == 0){
				realFps = numRevH264*1.0/playTime;
				if(realFps>=4 && realFps<=30){
					validFps = realFps - 1.5;
				}
			}
			Log.i("H264MediaPlayer-Notity", "cmdId = "+cmdId + " isVideoArrive="+isVideoArrive
					 + " numRevH264="+numRevH264 +" realFps="+realFps + " validFps="+validFps);
		}
		isVideoArrive++;
		if (isVideoArrive == 1) {
			if (onVideoDecordListener != null) {
				onVideoDecordListener.onVDecord();
				Log.i("Notify", "收到第1帧数据："+isVideoArrive);
			}
		}
		if (eArgs instanceof AnswerMsgVideo) {
			msgVideo = (AnswerMsgVideo) eArgs;
			videoData = msgVideo.videoData;
			Log.i("Notify", "videoData:"+videoData.length + " numRevH264="+numRevH264);
			try {
//				if(queue.size() >= 50)
//					queue.clear();
				synchronized(queue){
					queue.offer(videoData);
					numRevH264 += 1;
					//统计帧率
					if(numRevH264 == 1){
						startTimeRecvData = System.currentTimeMillis();	//ms
					}else{
						//实时视频才使用统计修正
						if(numRevH264%200 == 0 && isOnlineVideo){
							long ldelay = System.currentTimeMillis() - startTimeRecvData;
							realFps = (float)1000.0*numRevH264/ldelay;
							Log.i("Notify","realFps="+realFps + " ldelay="+ldelay);
							if(realFps>=4 && realFps<=30){
								validFps = realFps - 1.5;
								Log.i("Notify","validFps="+validFps);
							}
						}	
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
//		if(sender instanceof VideoTcpClient){
//			vtc = (VideoTcpClient) sender;
//			SkDatRemained = vtc.SkDatRemained;
//		}
	}

	public class PlayThread extends Thread {
		public void run() {
			byte[] playData;
			//test
			String filePath = "/sdcard/InDataGap.log";
			try {
				InDataGap = new FileWriter(filePath);
				H264source = new FileOutputStream("/sdcard/source.h264");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//queue.clear(); 		//开始前清除所有数据
			while (playSwitch) {
				try {
					h264Queuesize = getH264QueueSize();
					synchronized(queue)
					{
						if(h264Queuesize < 40)	//解码库RGB队列长度40
						{
							if ((playData = queue.poll()) != null) {
								getH264Data(playData);
							}
						}
						iqueuesize = queue.size();
					}
					if(iqueuesize > 0){
						InDataGap.write("SkDatRemained = "+SkDatRemained + "\t queuesize = "+iqueuesize 
								+ "\t h264Queuesize = "+h264Queuesize	+ "\t RGBQueuesize = "+RGBQueuesize +"\n");
						Log.i("PlayThread","SkDatRemained = "+SkDatRemained + " queue.size = "+iqueuesize);
					}
					Thread.sleep(25);						
				} catch (Exception e) {
					Log.i("PlayThread","queue.size = "+queue.size());
					e.printStackTrace();
				}
			}
		}
	}

	public class PlayVideoTapThread extends Thread {
		private String fileRootpath = Environment.getExternalStorageDirectory()
				.getPath() + "/hriVideoTap";
		private File saveFile;
		private long filelength;

		public PlayVideoTapThread(String fileName, long filelength) {
			try {
				Log.i("PlayVideoTapThread", "fileName : "+fileName + " filelength = "+filelength);
				if (fileName.length() > 0) {
					this.filelength = filelength;
					File dir = new File(fileRootpath);
					if (!dir.exists()) {// 判断文件目录是否存在
						dir.mkdirs();
					}
					saveFile = new File(fileRootpath, fileName);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		public void run() {
			FileOutputStream fileOut = null;
			try {
				if (alarmId.length() > 0) {
					fileOut = new FileOutputStream(saveFile);
				}
				else{
					//存储录像
					saveFile = new File(fileRootpath, "redvideo.h264");
					fileOut = new FileOutputStream(saveFile);
				}
				Log.i("PlayVideoTapThread", "saveFile : "+saveFile + " fileOut : "+fileOut);
				InDataGap = new FileWriter("/sdcard/InDataGap.log");
			} catch (Exception e) {
				e.printStackTrace();
			}
			byte[] playData;
			while (playSwitch) {
//				long timestart = System.nanoTime()/1000;	//us
				try {
					h264Queuesize = getH264QueueSize();
					if(h264Queuesize < 40)	//解码库RGB队列长度40
					{
						synchronized(queue){
							if ((playData = queue.poll()) != null){ 
								getH264Data(playData);
								if(saveFile != null){
									fileOut.write(playData);
								}
							}
						}
					}
					iqueuesize = queue.size();
					Log.i("PlayVideoTapThread", "queue.size = "+iqueuesize + " h264Queuesize="+h264Queuesize);
					InDataGap.write("SkDatRemained = "+SkDatRemained + "\t queuesize = "+iqueuesize 
							+ "\t h264Queuesize = "+h264Queuesize	+ "\t RGBQueuesize = "+RGBQueuesize +"\n");
					Thread.sleep(25);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
//				long timedelay = System.nanoTime()/1000 - timestart;
//				int dif = (int) (250*1000/playVideoFramerate);
//				while(timedelay < (getTapDataDelay - dif))	//此处减去XXus为了读取速度不小于播放速度
//				{
//					timedelay = System.nanoTime()/1000 - timestart;		//unit = us	
//				}
			}
			if (alarmId.length()>0 || saveFile!=null) {
				try {
					fileOut.close();
				} catch (Exception ex) {
				}
			}
		}
	}

	/**
	 * 销毁播放器(关闭TCP连接 销毁播放线程和数据下载线程)
	 */
	public void destroyPlayer() {
		playSwitch = false;
		videoControl.destroyTcpClient();
	}
	
	/**
	 * 控制更新MyView的画图线程
	 * @author Administrator
	 *
	 */
	public class MyThread extends Thread {
		private int videofps = (int) (62*1000);		//播放时间间隔 us
		private int clkcut = 1000;					//每次成功播放1帧视频应减去的时间误差
		private int deltaCut=500;					//播放循环扣除时间偏差量
		private int isVideodata = 0;
		private boolean isReadyRGB = false;			//RGB队列为0为false、缓冲1s后为true
		private long llastclk;
		private long timedelay;						//本次循环延时时长
		//test
		private String fileRootpath = Environment.getExternalStorageDirectory()
				.getPath() + "/hriVideoTap";
		private File saveFile = new File(fileRootpath, "recVideo.rgb");
		private FileOutputStream fileOut = null;
		public void run() {
			/*while(playSwitch){
				getRGBData(mPixel);
				try {
					Thread.sleep(40);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}*/
			//test
			try {
				fileOut = new FileOutputStream(saveFile);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			
			while (playSwitch) {	
				long timestart = System.nanoTime()/1000;	//us
				//计算上一帧实际播放延时在当前帧扣除
				int framedelay = (int) (timestart-llastclk);
				clkcut = framedelay-videofps;
				if(clkcut > 0 && clkcut < videofps*2/10)
				{
					clkcut += deltaCut;
				}
				else{
					clkcut = deltaCut;
				}
				llastclk = timestart;
				if(isDebug)
					Log.i("播放线程MyThread","framedelay = "+framedelay + " clkcut = "+clkcut 
						+ " timedelay = "+timedelay + " videofps = "+videofps);
				//1ms循环计时
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//20s无有效视频信号提示播放失败
				isVideodata += 10;
				if(isVideodata > 20000){
					try {
						if(isOnlineVideo)
							stopVideo();
						else
							stopTapVideo();
						Log.i("MyThread","isVideodata = "+isVideodata);
						onLineVideoListener.onPlayfailed(303, channelNum);	//播放失败
						isVideodata = 0;
						break;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				RGBQueuesize = getRGBQueueSize();
				double h264Framerate = getH264Framerate();
				if(h264Framerate >= 4 && h264Framerate <= 30)	
				{
					playVideoFramerate = h264Framerate;		//解码库帧率
					//解码帧率大于设备帧率
					if(videoControl.frameRate >=4 && videoControl.frameRate <= 30){
						if(videoControl.frameRate < h264Framerate){
							if(validFps>=4 && validFps<=30){
								playVideoFramerate = validFps;
							}else{
								playVideoFramerate = videoControl.frameRate*0.9;	//设备帧率
							}
						}
					}
				}
				else if(videoControl.frameRate >=4 && videoControl.frameRate <= 30)	
				{
					if(validFps>=4 && validFps<=30){
						playVideoFramerate = validFps;
					}else{
						playVideoFramerate = videoControl.frameRate*0.9;	//设备帧率
					}
				}
				else	
				{
					playVideoFramerate = 16.0;					//默认帧率
				}
				videofps = (int)(1000*1000/playVideoFramerate);
				getTapDataDelay = videofps;
				//时间调整 - 取消
//				if(RGBQueuesize > 4*playVideoFramerate)
//				{
//					videofps -= 3*100*1000/playVideoFramerate;
//				}
//				else if(RGBQueuesize > 3*playVideoFramerate)
//				{
//					videofps -= 2*100*1000/playVideoFramerate;
//				}
//				else if(RGBQueuesize > 2*playVideoFramerate)
//				{
//					videofps -= 100*1000/playVideoFramerate;
//				}
				if(startVideo%100==0	|| isVideodata%1000==0 && isDebug){
					Log.i("播放线程MyThread", "videofps = "+videofps+" us " +" playVideoFramerate = "+playVideoFramerate
							+ " h264Framerate = "+h264Framerate + " videoControl.frameRate = "+videoControl.frameRate
							+ " | h264Queuesize = "+h264Queuesize + " RGBQueuesize = "+RGBQueuesize
							+ " isVideodata = "+isVideodata+" ms ");
				}
				//缓冲或结束
				if(RGBQueuesize <= 0 && startVideo >= 1 && isReadyRGB){
					Log.i(TAG, "是否收到录像到达完成："+isVideoTapFinished);
					if(!isOnlineVideo && isVideoTapFinished && RGBQueuesize <= 0 && h264Queuesize <= 0 
							&& iqueuesize <= 0){
						Log.i("录像播放","playTime = "+playTime + " startVideo = "+startVideo);
						onLineVideoListener.onPlayfailed(503, channelNum);	//录像播放结束
						try {
							stopTapVideo();
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;		//录像播放完毕则退出循环
					}
					isReadyRGB = false;
					onLineVideoListener.onPlayfailed(501, channelNum);	//缓冲字样
				}
				if(RGBQueuesize >= playVideoFramerate){
					isReadyRGB = true;
					onLineVideoListener.onPlayfailed(502, channelNum);
				}
				int RGBlen = 0;
				if(isReadyRGB)
					RGBlen = getRGBData(mPixel);
				if (RGBlen > 0) {		
					//test - 存储RGB文件
//					try {
//						fileOut.write(mPixel);
//					} catch (IOException e1) {
//						e1.printStackTrace();
//					}
					
					postInvalidate();
					startVideo++;
					//有视频图片
					if (onLineVideoListener != null) {
						onLineVideoListener.onVideoReceive();
						Log.i("播放线程MyThread", "第1帧显示视频:"+startVideo);
					}
					//显示30帧后保存1张缩略图
					if(startVideo == 30){				
						PlayVideoActivity.setVideoImg(Bitmap.createScaledBitmap(
								videoBitmap,sizeImgW,sizeImgH,true), channelNum);
					}
					Log.i("播放线程MyThread",h264Queuesize + " ------"+startVideo+"帧图像被绘画------- " 
							+ RGBQueuesize);
					isVideodata = 0;
					timedelay = System.nanoTime()/1000 - timestart;
					//System.gc();
					while(timedelay < (videofps - clkcut))	//此处减去XXus为了画图不慢于位图数据
					{
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						timedelay = System.nanoTime()/1000 - timestart;		//unit = us
					}
				}	
				else if(RGBlen == -1){
					playSwitch = false;		//stop draw
				}
			}
		}
	}
}
//获取id 播放时长 订阅成功