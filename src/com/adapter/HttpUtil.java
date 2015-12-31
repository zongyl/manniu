package com.adapter;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.basic.APP;
import com.bean.TemplateSMS;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;
import com.manniu.manniu.R;

@SuppressLint("UseValueOf")
public class HttpUtil  {

	private static String TAG = "HttpUtil";
	
	private static AsyncHttpClient client = new AsyncHttpClient();
	
	private static SyncHttpClient syncClient = new SyncHttpClient();
	
	static {
		client.setTimeout(1000 * 10);
		syncClient.setTimeout(1000 * 10);
		client.getHttpClient().getParams().setParameter("http.socket.timeout", new Integer(10000));
		syncClient.getHttpClient().getParams().setParameter("http.socket.timeout", new Integer(10000));
	}
	
	public static void get(String url, AsyncHttpResponseHandler res){
		client.get(url, res);
	}
	
	public static void get(String url, RequestParams params, AsyncHttpResponseHandler res){
		client.get(url, params, res);
	}
	
	public static void get(String url, JsonHttpResponseHandler res){
		client.get(url, res);
	}
	
	public static void get(String url, RequestParams params, JsonHttpResponseHandler res){
		client.get(url, params, res);
	}
	
	public static void post(String url, RequestParams params, JsonHttpResponseHandler res){
		client.post(url, params, res);
	}
	
	public static void postussms(String Authorization, String url, RequestParams params, TextHttpResponseHandler res){
		client.addHeader("Authorization", "Basic " + Base64.encode((Authorization).getBytes()));
		client.post(url, params, res);
	}

	/**
	 * 
	 * @param url
	 * @param params
	 * @param res
	 * @param isAsync 是否异步 
	 */
	public static void get(String url, RequestParams params, JsonHttpResponseHandler res, boolean isAsync){
		if(isAsync){
			Log.v("HttpUtil", "asyncClient!");
			client.get(url, params, res);
		}else{
			Log.v("HttpUtil", "syncClient!");
			syncClient.get(url, params, res);
		}
	}
	
	public static void get(String url, BinaryHttpResponseHandler res){
		client.get(url, res);
	}

	public static AsyncHttpClient getClient(){
		return client;
	}
	
	public static SyncHttpClient getSyncClient(){
		return syncClient;
	}
	
	public static Bitmap executeGetBitmap(String weburl) {
	       Bitmap bitmap = null;
	       InputStream in = null;
	        try {
	            HttpClient client = new DefaultHttpClient();
	            HttpGet request = new HttpGet();
	            request.setURI(new URI(weburl));
	            HttpResponse response = client.execute(request);
	            in = response.getEntity().getContent();
	            bitmap = BitmapFactory.decodeStream(in);
	        } catch (Exception e) {
	        	APP.ShowToast("获取失败");//TODO:此处需要换成字符串资源处理方式处理
	            e.printStackTrace();
	        } finally {
	            if (in != null) {
	                try {
	                    in.close();
	                    in = null;
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
	        return bitmap;
	    }
	
	public static byte[] executeGetBytes(String weburl) {
	       InputStream in = null;
	       byte[] byteArray = null;
	        try {
	            HttpClient client = new DefaultHttpClient();
	            HttpGet request = new HttpGet(weburl);
	           // request.setURI(new URI(weburl));
	            
	            HttpResponse response = client.execute(request);
	            in = response.getEntity().getContent();
	            
	            byte[] bytes = new byte[1024];
		          ByteArrayOutputStream bos = new ByteArrayOutputStream();
		          int count = 0;
		          while((count = in.read(bytes)) != -1){
		        	  bos.write(bytes, 0, count);
		          }
		          byteArray = bos.toByteArray();
	            
	        } catch (Exception e) {
	        	APP.ShowToast("获取失败");
	            e.printStackTrace();
	        } finally {
	            if (in != null) {
	                try {
	                    in.close();
	                    in = null;
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
	        return byteArray;
	    }
	
	public static InputStream getFileInputStream(String weburl,boolean isRange,int startPosition,int contentLength) {
		InputStream in = null;
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(weburl);
            if(isRange){//多线程下载  
            	//httpGet.addHeader("Range", "bytes="+startPosition+"-"+contentLength);  
            	httpGet.addHeader("Range", "bytes="+startPosition+"-"+(contentLength-1)); 
            } 
            HttpResponse response = client.execute(httpGet);
            in = response.getEntity().getContent();
        } catch (Exception e) {
        } /*finally {
            if (in != null) {
                try {
                    in.close();
                    in = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }*/
        return in;
    }
	
	public static void get(Context context, String url, RequestParams params){
		HttpUtil.get(context.getResources().getString(R.string.server_address)+url, 
				params, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject json) {}
			
			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
			}
			});
	}
	
	
	public static String executeHttpGet(String weburl){
		String result = "";
		URL url = null;
		HttpURLConnection connection = null;
		InputStreamReader in = null;
		try {
			url = new URL(weburl);
			connection = (HttpURLConnection)url.openConnection();
			in = new InputStreamReader(connection.getInputStream());
			BufferedReader bufferedReader = new BufferedReader(in);
			StringBuffer strbuffer = new StringBuffer();
			String line = null;
			while((line = bufferedReader.readLine()) != null){
				strbuffer.append(line);
			}
			result = strbuffer.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(connection !=null){
				connection.disconnect();
			}
			if(in != null){
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	
	/*public static String executeHttpPost(String weburl) {
        String result = null;
        URL url = null;
        HttpURLConnection connection = null;
        InputStreamReader in = null;
        try {
            url = new URL(weburl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Charset", "utf-8");
            DataOutputStream dop = new DataOutputStream(
                    connection.getOutputStream());
            dop.writeBytes("userId = 2 ");
            dop.flush();
            dop.close();
 
            in = new InputStreamReader(connection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(in);
            StringBuffer strBuffer = new StringBuffer();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                strBuffer.append(line);
            }
            result = strBuffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
 
        }
        return result;
    }*/
	
	public static String executeGet(String weburl) {
        String result = null;
        BufferedReader reader = null;
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet();
            request.setURI(new URI(weburl));
            HttpResponse response = client.execute(request);
            reader = new BufferedReader(new InputStreamReader(response
                    .getEntity().getContent()));
 
            StringBuffer strBuffer = new StringBuffer("");
            String line = null;
            while ((line = reader.readLine()) != null) {
                strBuffer.append(line);
            }
            result = strBuffer.toString();
 
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                    reader = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
 
        return result;
    }
	
	/**
	 * 
	 * @param weburl
	 * @param params
	 * @return
	 */
	public static String executePost(String weburl, Map<String, String> params) {
		
		Log.v("=======HTTPUtil========", "weburl:"+weburl);
		
        String result = null;
        BufferedReader reader = null;
        try {
            HttpClient client = new DefaultHttpClient();
            HttpPost request = new HttpPost();
            request.setURI(new URI(weburl));
            List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            
            if(params != null){
            	Set<String> keys = params.keySet();
            	for(String key : keys){
            		postParameters.add(new BasicNameValuePair(key, params.get(key)));
            		Log.v("=======HTTPUtil========", "key:"+key+" | value:"+params.get(key));
            	}
            }
            
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(
                    postParameters);
            request.setEntity(formEntity);
 
            try{
            	HttpResponse response = client.execute(request);
            	
            	Log.v("=======HTTPUtil========", ""+response.getStatusLine());
            	
                reader = new BufferedReader(new InputStreamReader(response
                        .getEntity().getContent()));
     
                StringBuffer strBuffer = new StringBuffer("");
                String line = null;
                while ((line = reader.readLine()) != null) {
                    strBuffer.append(line);
                }
                result = strBuffer.toString();
            }catch(Exception e){
            	result = "failure";
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                    reader = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
 
        return result;
    }
	
	/**
	 * 群发短信 
	 * @param moblies 手机号 
	 * @param content 短信内容
	 * @return
	 */
	public static void sms(String[] mobiles, String content) {
		for(String mobile : mobiles){
			sms(mobile, content);
		}
	}
	
	/**
	 * 发送短信
	 * @param moblie 手机号
	 * @param content 短信内容
	 * @return
	 */
	public static String sms(String mobile, String content) {
		Log.d(TAG, mobile + " 发送短息!");
		String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

		//方春林的账号
//		String accountId = "53dc9401f6256a66156fab430821a5cc";
//		String authToken = "0c022845aba1143e7060ae5aa2e0daca";
//		String appId = "c8ae3c234f76480d80bd80dab914d02f";
//		
		String accountId = "9a76b0ee4e465bc6de01014a01e509a0";
		String authToken = "ffbccb9169ec750a4156648630644476";
		String appId = "085ec69c65d34b01a9452a945f834892";
		
		String sig = Utils.MD5(accountId + authToken + timestamp).toUpperCase();
		String Authorization = Base64.encode((accountId+":"+timestamp).getBytes());
		String weburl = "https://api.ucpaas.com/2014-06-30/Accounts/"+accountId+"/Messages/templateSMS?sig="+sig;
		
		String result = null;
        URL url = null;
        HttpURLConnection connection = null;
        InputStreamReader in = null;
        try {
            url = new URL(weburl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8;");
           // connection.setRequestProperty("Content-Length", "256");
            connection.setRequestProperty("Charset", "utf-8");
            connection.setRequestProperty("Authorization", Authorization);
            
            TemplateSMS template = new TemplateSMS();
            template.appId = appId;
            template.templateId = "4110";//7204
            template.param = content;//+",3"
            template.to = mobile;
            
            Map map = new HashMap<String, Object>();
            map.put("templateSMS", template);
            
            System.out.println("json:"+JSON.toJSONString(map));
            
            DataOutputStream dop = new DataOutputStream(
                    connection.getOutputStream());
            dop.writeBytes(JSON.toJSONString(map));
            dop.flush();
            dop.close();
 
            in = new InputStreamReader(connection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(in);
            StringBuffer strBuffer = new StringBuffer();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                strBuffer.append(line);
            }
            result = strBuffer.toString();
            Log.v("", ""+result);
            Log.v("", "code:"+content);
            
            JSON.parseObject(result).getJSONObject("resp").getString("respCode");
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
 
        }
        return result;
    }
	
	public static String sms(String moblie) {
		return sms(moblie, "");
	}
	
}