package com.utils;

import java.io.BufferedReader;  
import java.io.File;  
import java.io.InputStream;  
import java.io.InputStreamReader;  
import java.util.ArrayList;  
import java.util.List;  

import com.basic.APP;
import com.views.Fun_Setting;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Environment;  
import android.os.StatFs;

/**
 * Created by IntelliJ IDEA. User: li_jianhua Date: 2014-11-17 上午10:51:54
 * To change this template use File | Settings | File Templates.
 * Description：
 */

public class SdCardUtils {
	// 返回值不带File seperater "/",如果没有外置第二个sd卡,返回null  
    public static String getSecondExterPath() {
        List<String> paths = getAllExterSdcardPath();  
        if (paths.size() >= 2) {
            for (String path : paths) {
                if (path != null && !path.equals(getFirstExterPath())) {
                    return path;
                }
            }
            return null;
        } else {
            return null;
        }
    }
    //判断内置存储卡
    public static boolean isFirstSdcardMounted(){
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return false;  
        }
        return true;
    }  
    //获取外置SD卡路径  没有返回Null
    public static String getExternalSDcardPath() {
        String sd2 = getSecondExterPath();
        if (sd2 == null) {
            return null;
        }
        return checkExternalSD(sd2 + File.separator);
    }
  
    // 测试外置sd卡是否卸载，不能直接判断外置sd卡是否为null，因为当外置sd卡拔出时，仍然能得到外置sd卡路径。 
    // 创建一个文件，然后立即删除，看是否卸载外置sd卡  
    private static String checkExternalSD(String dir) {
        if (dir == null)
            return null;
        dir = dir+"Android/data/com.sdmc/files/";
        if(!makeRootDirectory(dir)){
        	return null;
        }
        try {
        	File f = new File(dir + "1.txt");
        	f.createNewFile();
        	if(f.isFile()){
        		f.delete();
        	}
        	return dir;
        } catch (Exception e) {
        	return null;
        }
    }
    
	public static boolean makeRootDirectory(String filePath) {
		File file = null;
		try {
			file = new File(filePath);
			if (!file.exists()) {
				file.mkdirs();
				file.createNewFile();
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}
  
    public static String getFirstExterPath() {
        return Environment.getExternalStorageDirectory().getPath();  
    }  
  
    @SuppressLint("DefaultLocale")
	public static List<String> getAllExterSdcardPath() {
        List<String> SdList = new ArrayList<String>();  
        String firstPath = getFirstExterPath();  
        // 得到路径  
        try {
            Runtime runtime = Runtime.getRuntime();  
            Process proc = runtime.exec("mount");  
            InputStream is = proc.getInputStream();  
            InputStreamReader isr = new InputStreamReader(is);  
            String line;  
            BufferedReader br = new BufferedReader(isr);  
            while ((line = br.readLine()) != null) {
            	// 将常见的linux分区过滤掉  
                if (line.contains("secure"))  
                    continue;  
                if (line.contains("asec"))  
                    continue;  
                if (line.contains("media"))  
                    continue;  
                if (line.contains("system") || line.contains("cache")
                        || line.contains("sys") || line.contains("data")
                        || line.contains("tmpfs") || line.contains("shell")
                        || line.contains("root") || line.contains("acct")
                        || line.contains("proc") || line.contains("misc")
                        || line.contains("obb")) {
                    continue;
                }  
                if (line.contains("fat") || line.contains("fuse") || (line  
                        .contains("ntfs"))) {
                    String columns[] = line.split(" ");  
                    if (columns != null && columns.length > 1) {
                        String path = columns[1];
                        if (path != null && !SdList.contains(path) && path.toLowerCase().contains("sd"))
                        	SdList.add(columns[1]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();  
        }
        if (!SdList.contains(firstPath)) {
            SdList.add(firstPath);
        }
        return SdList;
    }  
    
    
    /**
     * @param stat
     *            文件StatFs对象
     * @return 剩余存储空间的MB数
     * 
     */
    private static float calculateSizeInMB(StatFs stat) {
        if (stat != null)
            return stat.getAvailableBlocks() * (stat.getBlockSize() / (1024f * 1024f));
        return 0.0f;
    }
    
    /**
     * @param path
     *            文件路径
     * @return 文件路径的StatFs对象
     * @throws Exception
     *             路径为空或非法异常抛出
     */
    @SuppressWarnings("static-access")
	private static StatFs getStatFs(String path) {
        try {
        	String tmpPath = path;
        	SharedPreferences preferences = APP.GetMainActivity().getSharedPreferences(Fun_Setting.SAVEFILE, APP.GetMainActivity().MODE_PRIVATE);
    		int stoStep = preferences.getInt("stoStep", 1);
    		if(stoStep == 0){ //选择存储卡
    			tmpPath = getExternalSDcardPath();
    		}
            return new StatFs(tmpPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 
     * @return 根据文件路径获取剩余存储空间MB数
     */
    public static float getSurplusStorageSize(String path) {
        StatFs stat = getStatFs(path);
        return calculateSizeInMB(stat);
    }
    

}
