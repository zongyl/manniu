package com.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.util.Log;

/**
 * @author: li_jianhua Date: 2015-8-19 下午3:38:45
 * To change this template use File | Settings | File Templates.
 * Description：登录、登出
 */
public class HttpURLConnectionTools {
	
	/**
	 * 使用get方式
	 */
	public static Map<String, Object> get(String url) {
		Log.d("httpClientUtils", "get.url:" + url);
		Map<String, Object> map = new HashMap<String, Object>();
		HttpURLConnection conn = null;
		try {
			// 利用string url构建URL对象
			URL mURL = new URL(url);
			conn = (HttpURLConnection) mURL.openConnection();
			conn.setRequestMethod("GET");
			conn.setReadTimeout(20*1000);
			conn.setConnectTimeout(10000);
			conn.connect();
			InputStream is = conn.getInputStream();
			String state = "";
			if (is != null) {
				state = getStringFromInputStream(is);
			}
			int responseCode = conn.getResponseCode();
			map.put("code", responseCode);
			if (responseCode == 200) {
				map.put("data", state);
			} else {
				LogUtil.i("httpClientUtils", "访问失败" + responseCode);
			}
			is.close();
		} catch (Exception e) {
			map.put("code", 528);
			LogUtil.e("httpClientUtils", ExceptionsOperator.getExceptionInfo(e));
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return map;
	}
	
	/**
	 * 使用post方式
	 * @param url
	 * @param data
	 * @return
	 */
	public static Map<String, Object> post(String url,Map<String, String> params) {
		Map<String, Object> map = new HashMap<String, Object>();
		HttpURLConnection conn = null;
		PrintWriter printWriter = null;
		try {
			// 创建一个URL对象
			URL mURL = new URL(url);
			// 调用URL的openConnection()方法,获取HttpURLConnection对象
			conn = (HttpURLConnection) mURL.openConnection();
			conn.setRequestMethod("POST");// 设置请求方法为post
			conn.setReadTimeout(5000);// 设置读取超时为5秒
			conn.setConnectTimeout(10000);// 设置连接网络超时为10秒
			conn.setDoOutput(true);// 设置此方法,允许向服务器输出内容
			conn.setDoInput(true);
//			conn.setRequestProperty("contentType", "utf-8");
			// 获得一个输出流,向服务器写数据,默认情况下,系统不允许向服务器输出内容
			// 获取URLConnection对象对应的输出流
			printWriter = new PrintWriter(conn.getOutputStream());
			// 发送请求参数
			printWriter.write(PostRequestUrl(params).toString());
			// flush输出流的缓冲
			printWriter.flush();
			InputStream is = conn.getInputStream();
			String state = "";
			if (is != null) {
				state = getStringFromInputStream(is);
			}
			int responseCode = conn.getResponseCode();// 调用此方法就不必再使用conn.connect()方法
			map.put("code", responseCode);
			if (responseCode == 200) {
				map.put("data", state);
			} else {
				LogUtil.i("httpClientUtils", "访问失败" + responseCode);
			}
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();// 关闭连接
			}
			if (printWriter != null) {
				printWriter.close();
			}
		}
		return map;
	}
	
	/**
	 * 根据流返回一个字符串信息	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static String getStringFromInputStream(InputStream is)
			throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		// 模板代码 必须熟练
		byte[] buffer = new byte[1024];
		int len = -1;
		// 一定要写len=is.read(buffer)
		// 如果while((is.read(buffer))!=-1)则无法将数据写入buffer中
		while ((len = is.read(buffer)) != -1) {
			os.write(buffer, 0, len);
		}
		is.close();
		String state = os.toString();// 把流中的数据转换成字符串,采用的编码是utf-8(模拟器默认编码)
		os.close();
		return state;
	}
	
	/**
	 * 拼装POST访问URL
	 * 
	 * @param params
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("rawtypes")
	private static StringBuffer PostRequestUrl(Map<String, String> params) {
		StringBuffer buffers = new StringBuffer();
		Iterator<Entry<String, String>> it = params.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry element = (Map.Entry) it.next();
			buffers.append(element.getKey());
			buffers.append("=");
			buffers.append(element.getValue());
			buffers.append("&");
		}
		if (buffers.length() > 0) {
			buffers.deleteCharAt(buffers.length() - 1);
		}
		return buffers;
	}

}
