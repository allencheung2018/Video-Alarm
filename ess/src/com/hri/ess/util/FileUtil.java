package com.hri.ess.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;  
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;  
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;  
import android.util.Log;  

public class FileUtil {  
    private static final String TAG = "FileUtil";
    public static String fileDir="/Test/";
    public static String nameLogFile = "logFile.log";
    /**
     * ï¿½ï¿½È¡ï¿½Ä¼ï¿½(ï¿½ï¿½ï¿½ï¿½ï¿½Úµï¿½ï¿½ï¿½ï¿½ï¿½ï¿?,ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½Ä¼ï¿½ï¿½ï¿½filePath=ï¿½ï¿½/test/ï¿½ï¿½ fileName="test.txt"
     * @param filePath
     * @param fileName
     * @return
     */
    public static File getFile(String filePath,String fileName){  
        File file = null; 
        try{
            File sdCardDir=getStoreDir();
            String path=sdCardDir.getAbsolutePath() + filePath;
            File dir = new File(path);
            boolean tt=dir.exists();
            if (!dir.exists()) {  
               dir.mkdirs();  
            } 
            file = new File(dir, fileName);  
        }
        catch(Exception ex)
        {
        
        }
        return file;  
    } 
    /**
     * ï¿½ï¿½ï¿½ï¿½ï¿½Ä¼ï¿½ï¿½Ðºï¿½ï¿½Ä¼ï¿½(ï¿½ï¿½ï¿½ï¿½ï¿½Úµï¿½ï¿½ï¿½ï¿½ï¿½ï¿?,ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½Ä¼ï¿½ï¿½ï¿½filePath=ï¿½ï¿½/test/ï¿½ï¿½ fileName="test.txt"
     * @param filePath
     * @param fileName
     * @return
     */
    public static File CreateDirAndFile(String filePath,String fileName)
    {
    	File dir=new File(getStoreDir().getAbsoluteFile()+filePath);
    	if(!dir.exists())
    	{
    		dir.mkdirs();
    	}
    	File file=new File(dir,fileName);
    	if(!file.exists())
    	{
    		try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	return file;
    }
    
    /**
     * ï¿½ï¿½È¡ï¿½Ä¼ï¿½Â·ï¿½ï¿½ï¿½Ðµï¿½ï¿½Ä¼ï¿½ï¿½ï¿½
     * @param fileUrl
     * @return
     */
    public static String getFileName(String fileUrl) {  
    	String path="/"+fileUrl;
    	path=path.replace("//", "/");
    	int index = path.lastIndexOf("/");
        return path.substring(index + 1);
    } 
    /**
     * ï¿½ï¿½È¡ï¿½Ä¼ï¿½Â·ï¿½ï¿½ï¿½ï¿½ï¿½Ä¼ï¿½ï¿½ï¿½
     * @param fileUrl
     * @return
     */
    public static String getFileDir(String fileUrl)
    {
    	String path="/"+fileUrl;
    	path=path.replace("//", "/");
    	int index = path.lastIndexOf("/");
        return path.substring(0,index);
    }
    /**
     * ï¿½ï¿½È¡Assetï¿½ï¿½Ô´ï¿½Âµï¿½ï¿½Ä¼ï¿½Â·ï¿½ï¿½
     * @param fileName
     * @return
     */
    public static String GetAssetFilePath(String fileName)
	 {
	     //String Path="file:///android_asset/"+fileName;
    	String Path="file:///android_asset/"+fileName;
	    return Path;
	 }
    
    /**
     * ï¿½ï¿½È¡ï¿½æ´¢Â·ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½sdcardï¿½ï¿½ï¿½Í»ï¿½È¡SDKCARDï¿½ï¿½Â·ï¿½ï¿½,ï¿½ï¿½ï¿½ï¿½ï¿½È¡ANDROIDï¿½ï¿½Ä¿Â¼ï¿½ï¿½
     * @return
     */
    public  static File getStoreDir()
    {
    	File DirFile=null;
    	if (Environment.getExternalStorageState().equals(  
                Environment.MEDIA_MOUNTED))
    	{	
    		DirFile = Environment.getExternalStorageDirectory();
        }
    	else
    	{
    		DirFile=Environment.getRootDirectory();  
    	}
    	return DirFile;
    } 
    public static void writeFileSdcardFile(String fileName, String write_str,
			boolean append)  {
		try {
			//time
			SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd  HH:mm:ss   ");
			Date curDate = new Date(System.currentTimeMillis());//获取当前时间       
			String str  = formatter.format(curDate);
			str += write_str+"\r\n";
			//file
			File file = CreateDirAndFile("/skyeyesvillasecurity/", fileName);
			FileOutputStream stream = new FileOutputStream(file, append);
			byte[] buf = str.getBytes();
			stream.write(buf);
			stream.close();
		} catch (Exception e) {
			Log.e("TestFile", "Error on writeFilToSD.");
			e.printStackTrace();
		}
	}
	public static void writeFileBitData(String fileName, byte[] buffer,
			boolean append,int len)  {
		try {
			File file = CreateDirAndFile("/Download/", fileName);
			FileOutputStream stream = new FileOutputStream(file, append);
			stream.write(buffer, 0, len);
			stream.close();
		} catch (Exception e) {
			Log.e("TestFile", "Error on writeFilToSD.");
			e.printStackTrace();
		}
	}
	public static void savaBitMap(Bitmap videoBitmap,String imgname){
		File imageFile = FileUtil.getFile("/Download/", imgname);
		FileOutputStream outStream = null;
		try {
			outStream = new FileOutputStream(imageFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		videoBitmap.compress(Bitmap.CompressFormat.JPEG,100,outStream);
        try {
			outStream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
} 