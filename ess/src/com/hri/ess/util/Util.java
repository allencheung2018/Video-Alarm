package com.hri.ess.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Util {

	/**
	 * Unix 时间 1970-01-01 00:00:00 �?Win32 FileTime 时间 1601-01-01 00:00:00 毫秒数差
	 */
	public final static long UNIX_FILETIME_DIFF = 11644473600000L;

	/**
	 * Win32 FileTime 采用 100ns 为单位的，定�?100ns �?1ms 的�?�?
	 */
	public final static int MILLISECOND_MULTIPLE = 10000;
	public final static String RootPath = "/sdcard/Hriv/";

	// 判断字符串是否是合法的IP地址
	public static boolean isIPAddress(String source) {
		String split = ".:";
		String item;
		int count = 0;
		StringTokenizer token = new StringTokenizer(source, split);
		while (token.hasMoreTokens()) {
			item = token.nextToken();
			try {
				int val = Integer.parseInt(item);
				count++;
				if (val < 0) {
					return false;
				} else if (count < 5) {
					if (val > 255)
						return false;
				} else {
					if (val > 65535)
						return false;
				}
			} catch (NumberFormatException e) {
				return false;
			}
		}
		return (count == 4 || count == 5);
	}

	public static String getRootFilePath() {
		File dir = new File(RootPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir.getPath();
	}

	/**
	 * 状�?码转�?
	 * 
	 * @param bls
	 * @return
	 */
	public static int toByte(Boolean[] bls) {
		int sum = 0;
		for (int i = bls.length - 1; i >= 0; i--) {
			if (bls[i]) {
				sum += Math.pow(2, bls.length - i - 1);
			}
		}
		// for(int i=0;i<bls.length;i++){
		// if(bls[i]){
		// sum+=Math.pow(2,i);
		// }
		// }
		return sum;
	}

	/**
	 * byte[]数组分割
	 * 
	 * @param data
	 *            目标数组
	 * @param start
	 *            �?��位置
	 * @param len
	 *            分割长度
	 * @return
	 */
	public static byte[] getBytes(byte[] data, int start, int len) {
		byte[] bytes = new byte[len];
		System.arraycopy(data, start, bytes, 0, len);
		return bytes;
	}

	/**
	 * byte[]转int
	 * 
	 * @param data
	 *            目标数组
	 * @param start
	 *            �?��位置
	 * @return
	 */
	public static int getInt(byte[] data, int start) {
		int iOutcome = 0;
		byte bLoop;
		for (int i = start; i < start + 4; i++) {
			bLoop = data[i];
			iOutcome += (bLoop & 0xFF) << (8 * i);
		}
		return iOutcome;
	}

	/**
	 * int转byte[]
	 * 
	 * @param num
	 * @return
	 */
	public static byte[] IntToBytes(int num) {
		// byte[] result = new byte[4];
		// result[0] = (byte) (i & 0xff);
		// result[1] = (byte) (i >> 8 & 0xff);
		// result[2] = (byte) (i >> 16 & 0xff);
		// result[3] = (byte) (i>> 24 & 0xff);
		// return result;

		byte[] bLocalArr = new byte[4];
		for (int i = 0; i < 4; i++) {
			bLocalArr[i] = (byte) (num >> 8 * i & 0xFF);
		}
		return bLocalArr;

	}

	/**
	 * byte[]转字符串
	 * 
	 * @param data
	 *            目标数组
	 * @param start
	 *            �?��位置
	 * @param len
	 *            长度
	 * @return
	 */
	public static String getString(byte[] data, int start, int len) {
		byte[] bytes = getBytes(data, start, len);
		String msg = "";
		try {
			msg = new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return msg;
	}

	/**
	 * byte[]转Byte[]
	 * 
	 * @param start�?��位置
	 * @param len
	 *            长度
	 * @param data
	 *            目标数组
	 * @param pack
	 *            目的数组
	 */
	public static void bytestoBytes(int start, int len, byte[] data, Byte[] pack) {
		long a = System.currentTimeMillis();
		for (int i = start; i < start + len; i++) {
			pack[i - start] = data[i];
		}
		long b = System.currentTimeMillis() - a;
	}

	/**
	 * Bytes转bytes
	 * 
	 * @param start
	 * @param len
	 * @param data
	 * @param pack
	 */
	public static void Bytestobytes(int start, int len, byte[] bytes,
			Byte[] Bytes) {
		long a = System.currentTimeMillis();
		for (int i = start; i < start + len; i++) {
			bytes[i - start] = Bytes[i];
		}
		long b = System.currentTimeMillis() - a;
	}

	/**
	 * short转byte[]
	 */
	public static byte[] ShortToBytes(short number) {
		int temp = number;
		byte[] b = new byte[2];
		for (int i = 0; i < b.length; i++) {
			b[i] = new Integer(temp & 0xff).byteValue();
			temp = temp >> 8; // 向右�?�?
		}
		return b;

	}

	/**
	 * byte[]�?short
	 */
	public static short byteToShort(byte[] b) {
		short s = 0;
		short s0 = (short) (b[0] & 0xff);// �?���?
		short s1 = (short) (b[1] & 0xff);
		s1 <<= 8;
		s = (short) (s0 | s1);
		return s;
	}

	/**
	 * Long转byte[]
	 * 
	 * @param l
	 * @return
	 */
	public static byte[] LongToBytes(long number) {

		long temp = number;
		byte[] byteNum = new byte[8];
		for (int i = 0; i < byteNum.length; i++) {
			byteNum[i] = new Long(temp & 0xff).byteValue();
			// 将最低位保存在最低位
			temp = temp >> 8; // 向右�?�?
		}

		return byteNum;
	}

	/**
	 * double转byte[]
	 * 
	 * @param l
	 * @return
	 */
	public static byte[] doubleToByte(double d) {
		byte[] b = new byte[8];
		long l = Double.doubleToLongBits(d);
		for (int i = 0; i < 8; i++) {
			b[i] = new Long(l).byteValue();
			l = l >> 8;
		}
		return b;
	}

	/**
	 * 获取当前时间
	 * 
	 * @param type
	 *            yyyy MM dd ww hh mm ss
	 * @return
	 */
	public static String getNowStr(String type) {
		String rsStr = type;
		Calendar ca = Calendar.getInstance();
		String year = GetEnoughLenStr(4, "" + ca.get(Calendar.YEAR));// 获取年份
		String month = GetEnoughLenStr(2, "" + (ca.get(Calendar.MONTH) + 1));// 获取月份
		String day = GetEnoughLenStr(2, "" + ca.get(Calendar.DATE));// 获取�?
		String minute = GetEnoughLenStr(2, "" + ca.get(Calendar.MINUTE));// �?
		String hour = GetEnoughLenStr(2, "" + ca.get(Calendar.HOUR_OF_DAY));// 小时
		String second = GetEnoughLenStr(2, "" + ca.get(Calendar.SECOND));// �?
		String WeekOfYear = GetEnoughLenStr(2,
				"" + ca.get(Calendar.DAY_OF_WEEK));
		rsStr = rsStr.replace("yyyy", year + "");
		rsStr = rsStr.replace("MM", month + "");
		rsStr = rsStr.replace("dd", day + "");
		rsStr = rsStr.replace("ww", WeekOfYear + "");
		rsStr = rsStr.replace("hh", hour + "");
		rsStr = rsStr.replace("HH", hour + "");
		rsStr = rsStr.replace("mm", minute + "");
		rsStr = rsStr.replace("ss", second + "");
		return rsStr;
	}

	public static String GetEnoughLenStr(int num, String basestr) {
		String rs = basestr;
		int baseint = rs.length();
		if (baseint < num) {
			for (int i = baseint; i < num; i++) {
				rs = "0" + rs;
			}
		}
		return rs;
	}

	/**
	 * 获取后N天时�?
	 * 
	 * @param date
	 * @param n
	 * @param field
	 * @param type
	 * @return
	 */
	public static String getBeforeAfterDate(String date, int n, int field,
			String type) {

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat(type);
		if (date.equals(""))
			date = df.format(cal.getTime());
		try {
			cal.setTime(df.parse(date));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cal.set(field, cal.get(field) + n);
		date = df.format(cal.getTime());
		return date;
	}

	/**
	 * byte[]转long
	 */
	public final static long bytesToLong(byte[] buf, boolean asc) {
		if (buf == null) {
			throw new IllegalArgumentException("byte array is null!");
		}
		if (buf.length > 8) {
			throw new IllegalArgumentException("byte array size > 8 !");
		}
		long r = 0;
		if (asc)
			for (int i = buf.length - 1; i >= 0; i--) {
				r <<= 8;
				r |= (buf[i] & 0x00000000000000ff);
			}
		else
			for (int i = 0; i < buf.length; i++) {
				r <<= 8;
				r |= (buf[i] & 0x00000000000000ff);
			}
		return r;
	}

	/**
	 * 提供随机�?
	 */
	public static int getRandom() {
		Random random = new Random(100);
		return random.nextInt();
	}

	/**
	 * �?Java 中的 Date 类型转为 Win32 �?FileTime 结构
	 * 
	 * @param date
	 * @return
	 */
	public static long date2FileTime(Date date) {
		return (UNIX_FILETIME_DIFF + date.getTime()) * MILLISECOND_MULTIPLE;
	}

	/**
	 * �?Win32 �?FileTime 结构转为 Java 中的 Date 类型
	 * 
	 * @param fileTime
	 * @return
	 */
	public static Date fileTime2Date(long fileTime) {
		return new Date(fileTime / MILLISECOND_MULTIPLE - UNIX_FILETIME_DIFF);
	}

	/**
	 * 以Gzip方式压缩byte数组
	 * 
	 * @param data要压缩的byte数组
	 * @return
	 */
	public static byte[] DoGZip(byte[] data) {
		byte[] b = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			GZIPOutputStream gzip = new GZIPOutputStream(bos);
			gzip.write(data);
			gzip.finish();
			gzip.close();
			b = bos.toByteArray();
			bos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return b;
	}

	/***
	 * 解压GZip格式数组
	 * 
	 * @param data要解压的byte数组
	 * @return
	 */
	public static byte[] unGZip(byte[] data) {
		byte[] b = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			GZIPInputStream gzip = new GZIPInputStream(bis);
			byte[] buf = new byte[1024];
			int num = -1;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while ((num = gzip.read(buf, 0, buf.length)) != -1) {
				baos.write(buf, 0, num);
			}
			b = baos.toByteArray();
			baos.flush();
			baos.close();
			gzip.close();
			bis.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return b;
	}

	/**
	 * �?��是否有用户信息保�?
	 * 
	 * @return
	 */
	public static boolean haveuserInfo(Context context) {
		boolean Flag = false;
		try {
			String filename = "userinfo";
			FileInputStream inputStream = context.openFileInput(filename);
			byte[] bytes = new byte[1024];
			ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
			while (inputStream.read(bytes) != -1) {
				arrayOutputStream.write(bytes, 0, bytes.length);
			}
			inputStream.close();
			arrayOutputStream.close();
			String content = new String(arrayOutputStream.toByteArray());
			JSONObject userinfo = new JSONObject(content);
			String userid = userinfo.getString("userid");
			Flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Flag;
	}


	public static int isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			return 0;
		} else {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						NetworkInfo netWorkInfo = info[i];
						if (netWorkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
							return 1;
						} else if (netWorkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
							String extraInfo = netWorkInfo.getExtraInfo();
							if ("cmwap".equalsIgnoreCase(extraInfo)
									|| "cmwap:gsm".equalsIgnoreCase(extraInfo)) {
								return 2;
							}
							return 3;
						}else if(netWorkInfo.getType() == ConnectivityManager.TYPE_ETHERNET){
							return 9;
						}
					}
				}
			}
		}
		return 0;
	}
	public static String getDateStr(Date nowDate,int dayAddNum) {  
	        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd"); 
	        Date newDate2 = new Date(nowDate.getTime() + dayAddNum * 24 * 60 * 60 * 1000);  
	        String dateOk = df.format(newDate2);  
	        return dateOk;  
	  }  
	
	/**
	 * 返回yyyy-MM-dd格式的日期字符串
	 * 
	 * @return
	 */
	public static String getSpecDateStr() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(new Date());
	}
	public static String getSpecDateStr(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(date);
	}
	//获取今天当前时间
	public static String getNowTime(){
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		return sdf.format(new Date());
	}
	public static String getDateTime(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		return sdf.format(date);
	}
	public static String getNowDateTime(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(new Date());
	}
	/**
	 * 判断是否大于当前日期
	 * @param date
	 * @return
	 */
	public static boolean isAfterNowDate(String date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date beforeDate = sdf.parse(date);
			Date nowDate = new Date();
			return beforeDate.getTime() > nowDate.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * 判断指定时间之后的m分钟是否大于当前时间
	 * @param date
	 * @param m
	 * @return
	 */
	public static boolean isAfterNow(String date,int m){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return (sdf.parse(date).getTime()+m*60*1000) > (new Date().getTime());
		} catch (ParseException e) {
		}
		return false;
	}
	/**
	 * 获取当前日期前m分钟的时间日�?
	 * @param date
	 * @param m
	 * @return
	 */
	public static String getNowTimeAfter(int m){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(new Date(new Date().getTime()-m*60*1000));
	}
	/**
	 * 获取2000年的时间
	 * @return
	 */
	public static String get2000YearDate(){
		Calendar calendar = Calendar.getInstance();
		calendar.set(2000, 0, 1, 0, 0, 0);
		Date date = calendar.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(calendar.getTime());
	}
	/**
	 * 获取2050年时�?
	 * @return
	 */
	public static String get2050YearDate(){
		Calendar calendar = Calendar.getInstance();
		calendar.set(2050, 0, 1, 0, 0, 0);
		Date date = calendar.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(calendar.getTime());
	}
}
