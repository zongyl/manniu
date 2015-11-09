package com.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Environment;
import android.util.Log;

import com.adapter.HttpUtil;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class Loger {

    private LogerImp instance;
    
    //日志名称
    private String logerName;
    
    protected static boolean isOpen = true;
    
    /**
     * 开始输入日志信息<br\>
     * （只作为程序日志开关，在个人设置中开启，其他应用中不得调用）
     */
    public static void openPrint(){
            if (isOpen){
                    LogerImp.instance.startRun();
            }
    }
    
    public static void openPrint(String logName, String serverAddress){
        if (isOpen){
        	LogerImp.instance.setLogName(logName);
        	LogerImp.instance.setServerAdress(serverAddress);
        	LogerImp.instance.startRun();
        	LogerImp.instance.sendHttpServer();
        }
}
    /**
     * 关闭日志打印 <br\>
     * （只作为程序日志开关，在个人设置中开启，其他应用中不得调用）
     */
    public static void closePrint(){
            if (isOpen){
                    LogerImp.instance.stopRun();
            }
    }
    
    private static Loger loger = new Loger("[Loger]");
    
    /**
     * 输出日志信息
     * @param msg String 日志
     */
    public synchronized static void print(String msg){
            if (isOpen){
                    loger.output(msg);
            }
    }
    
    /**
     * 输出日志信息及异常发生的详细信息
     * @param msg String 日志
     * @param e Exception
     */
    public synchronized static void print(String msg, Exception e){
            if (isOpen){
                    loger.output(msg, e);
            }
    }
    
    /**
     * 构造函数
     * @param name String
     */
    public Loger(String name){
            logerName = name;
            instance = LogerImp.getInstance();
    }
    
    /**
     * 输出日志信息
     * @param msg String 日志
     */
    public synchronized void output(String msg){
            if (isOpen){
                    Log.i(logerName, msg);
                    if(instance == null){
                    	instance = LogerImp.getInstance();
                    }
                    instance.submitMsg(logerName+" "+msg);
            }
    }

    /**
     * 输出日志信息及异常发生的详细信息
     * @param msg String 日志
     * @param e Exception
     */
    public synchronized void output(String msg, Exception e){
            if (isOpen){
                    Log.i(logerName, msg, e);
                    StringBuffer buf = new StringBuffer(msg);
                    buf.append(logerName).append(" : ").append(msg).append("\n");
                    buf.append(e.getClass()).append(" : ");
                    buf.append(e.getLocalizedMessage());
                    buf.append("\n");
                    StackTraceElement[] stack = e.getStackTrace();
                    for(StackTraceElement trace : stack){
                            buf.append("\t at ").append(trace.toString()).append("\n");
                    }
                    instance.submitMsg(buf.toString());
            }
    }
    
    /**
     * 打印当前的内存信息
     */
    public void printCurrentMemory(){
            if (isOpen){
                    StringBuilder logs = new StringBuilder();
                    long freeMemory = Runtime.getRuntime().freeMemory()/1024;
                    long totalMemory = Runtime.getRuntime().totalMemory()/1024;
                    long maxMemory = Runtime.getRuntime().maxMemory()/1024;
                    logs.append("\t[Memory_free]: ").append(freeMemory).append(" kb");
                    logs.append("\t[Memory_total]: ").append(totalMemory).append(" kb");
                    logs.append("\t[Memory_max]: ").append(maxMemory).append(" kb");
                    Log.i(logerName, logs.toString());
                    instance.submitMsg(logerName+" "+logs.toString());
            }
    }
}

/**
* 日志输出的具体实现类
* @author Administrator
*
*/
class LogerImp implements Runnable{

	public String TAG = "Loger";
	
    private Loger log = new Loger("[LogerImp]");
    
    static LogerImp instance = new LogerImp();
    
    //日志存放的队列
    private List<String> printOutList = new ArrayList<String>();
    
    //日志文件
    private FileOutputStream fos = null;
    
    //日志输出流
    private PrintWriter print = null;
    
    //时间格式
    private DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    //线程轮询标识
    private boolean runFlag = false;
    
    //当前天，每天生成一个日志文件
    private int currDay = -1;

    private String logName;
    
    private String serverAddress;
    
    private GcCheck gcRun = new GcCheck();
            
    private String dateName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    
    class GcCheck implements Runnable {
            
            boolean flag = true;
            
            @Override
            public void run() {
                    int count = 40;
                    StringBuffer logs = new StringBuffer();
                    while(flag){
                            if (count >= 50){
                                    long freeMemory = Runtime.getRuntime().freeMemory()/1024;
                                    long totalMemory = Runtime.getRuntime().totalMemory()/1024;
                                    long maxMemory = Runtime.getRuntime().maxMemory()/1024;
                                    logs.append("\t[Memory_free]:").append(freeMemory).append(" kb");
                                    logs.append("\t[Memory_total]:").append(totalMemory).append(" kb");
                                    logs.append("\t[Memory_max]:").append(maxMemory).append(" kb");
                                    synchronized (printOutList) {
                                            printOutList.add(logs.toString());
                                    }
                                    Log.i("Memory", logs.toString());
                                    logs.setLength(0);
                                    if (freeMemory < 400){
                                            System.gc();
                                            count = 40;
                                            logs.append("<GC>");
                                    }else{
                                            count = 0;
                                    }
                            }
                            try {
                                    count++;
                                    Thread.sleep(10);
                            } catch (InterruptedException e) {
                                    e.printStackTrace();
                            }
                    }
            }
    };

    public void setLogName(String logName){
    	this.logName = logName;
    }
    
    public void setServerAdress(String serverAddress){
    	this.serverAddress = serverAddress;
    }
    
    /**
     * 得到单例对象
     * [url=home.php?mod=space&uid=309376]@return[/url] LogerImp
     */
    public static LogerImp getInstance(){
            return instance;
    }
    
    /**
     * 私有方法，单例
     */
    private LogerImp(){
    }
    
//    void listenGC(){
//            gcRun.flag = true;
//            new Thread(gcRun).start();
//    }
    
//    void stopLintenGC(){
//            gcRun.flag = false;
//    }
    
    //初始化文件输出流
    private void initPrint(){
            Calendar date = Calendar.getInstance();
            currDay = date.get(Calendar.DAY_OF_YEAR);
           // DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd");
            String fileName = new String(logName +"_"+ dateName +".txt");
            String path = null;
            try {
                    if (null != print){
                            close();
                    }
                    path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/manniu/";
                    File dir = new File(path);
                    if (!dir.exists()){
                            dir.mkdir();
                    }
                    fos = new FileOutputStream(path+fileName, true);
                    print = new PrintWriter(fos, true);
            } catch (Exception e) {
                    log.output("[LogerImp] 未能打开文件:"+path+" 文件名："+fileName+" 异常描述:"+e.getLocalizedMessage());
            }
    }
    
    /**
     * 线程开启
     */
    public void startRun(){
            /*if (!runFlag){
                    runFlag = true;
                    new Thread(this).start();
            }else{
                    log.output("[LogerImp] < warn > thread already run !");
            }*/
    }
    
    /**
     * 线程停止
     */
    public void stopRun(){
            /*if (runFlag){
                    gcRun.flag = false;
                    runFlag = false;
                    Log.i("Thread", "队列大小："+printOutList.size());
                    printToFile("[LogerImp] < info > thread stop !");
                    close();
            }*/
    }
    
    private void close() {
            print.flush();
            print.close();
            print = null;
            try{
                    fos.close();
                    fos = null;
            }catch(IOException e){
                    e.printStackTrace();
            }
    }

    /**
     * 向队列中增加日志数据
     * @param msg String 日志数据
     */
    protected synchronized void submitMsg(String msg) {
            synchronized (printOutList) {
                    printOutList.add(msg);
            }
    }
    
    public void run(){
            try{
            	Log.d(TAG, "run()");
                    initPrint();
                    printToFile("[LogerImp] < info > start new thread ... ");
                    while(runFlag){
                            //runMethod();
                            Thread.sleep(10);
                    }
                    runFlag = false;
            }catch(Exception e){
                    printToFile("[LogerImp] < warn > thread error : "+e.getLocalizedMessage());
                    if (runFlag){
                            printToFile("[LogerImp] 线程强制中断 "+e.getLocalizedMessage());
                            new Thread(this).start();
                    }
            }
    }
    
    //线程需要重复执行的操作
    private void runMethod() throws Exception {
            String line = null;
            synchronized (printOutList) {
                    if (!printOutList.isEmpty()){
                            line = printOutList.remove(0);
                    }
            }
            if (null != line){
                    printToFile(line);
            }else{
                    Thread.sleep(10);
            }
    }
    
    //发送文件到web服务器
    public void sendHttpServer(){
    	Log.d(TAG, "Loger sendHttpServer!");
    	
		File dirs = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/manniu/");
		File[] files = dirs.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if(pathname.getPath().contains(logName)){
					if(pathname.getPath().contains(dateName)){
						return false;
					}
					return true;
				}
				return false;
			}
		});
		
		Log.d(TAG, "file.length: " + files.length);
		for(File f : files){
			Log.d(TAG, "###: " + f.getPath());
			RequestParams params = new RequestParams();
			try {
				params.put("file", f);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			params.put("path", "logs"+File.separator+logName);
		
		HttpUtil.post(serverAddress+"/mobile/upload", params, new JsonHttpResponseHandler(){
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//				try {
//					Log.v(TAG, "onSuccess >> statusCode:" + statusCode + ", response:" + response.toString());
//					print.println("\r\n>>> onSuccess >> statusCode:" + statusCode + ", response:" + response.toString());
//					String file = response.getString("file");
//					print.println("file:" + file.substring(file.lastIndexOf("/")));
//					Log.d("file:", file.substring(file.lastIndexOf("/")));
//					delFile(file.substring(file.lastIndexOf("/")));
//				} catch (JSONException e) {
//				}
				
			};
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				Log.d(TAG, "responseString:" + responseString);
			}
			
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
//				if(errorResponse == null){
//					Log.v(TAG, "onFailure >> statusCode:" + statusCode);
//					print.println("\r\n>>> onFailure >> statusCode:" + statusCode);
//				}else{
//					Log.v(TAG, "onFailure >> statusCode:" + statusCode + ", response:" + errorResponse.toString());
//					print.println("\r\n>>> onFailure >> statusCode:" + statusCode + ", response:" + errorResponse.toString());
//				}
				
			};
		});
		}
    }
    
    /**
     * 删除文件
     * @param fileName
     * @return
     */
    private boolean delFile(String fileName){
    	Log.d(TAG, "delFile:"+fileName);
    	print.println("delFile:"+fileName);
    	return new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/manniu"+fileName).delete();
    }
    
    //把数据持久到文件
    private void printToFile(String line){
            Calendar date = Calendar.getInstance();
            int day = date.get(Calendar.DAY_OF_YEAR);
            if (day != currDay){
                initPrint();
            }
            if (null == print){
                    return;
            }
            print.println("\r\n>>> "+format.format(date.getTime())+" -- "+line);
            print.flush();
    }

}
