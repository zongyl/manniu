package com.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import android.util.Log;

import com.basic.APP;
import com.views.NewLogin;

/**
 * @author: li_jianhua Date: 2015-8-12 下午1:31:56
 * To change this template use File | Settings | File Templates.
 * Description： 此方法暂时不用
 * 
 * httpclient封装需要注意：(研究不够透彻，尽量不要使用单例httpclient)
1.每次都新建httpclient
2.在请求得到http的错误码（非200的情况），需要调用request.abort();
3.超时或者其他请求异常时，catch里面调用httpclient.getConnectionManager().shutdown();// 释放连接

 */
public class httpClientUtils {
	
	public static HttpClient httpClient = null;
	private static int DEFAULT_SOCKET_TIMEOUT  = 20*1000;
	private static int DEFAULT_HOST_CONNECTIONS = 100;
	private static int DEFAULT_MAX_CONNECTIONS = 50;
    private static final int DEFAULT_SOCKET_BUFFER_SIZE = 8192;  
  
 /*   static {  
        final HttpParams httpParams = new BasicHttpParams();  
  
        ConnManagerParams.setTimeout(httpParams, 1000);  
        ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(10));  
        ConnManagerParams.setMaxTotalConnections(httpParams, DEFAULT_MAX_CONNECTIONS);  
  
        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);  
        HttpProtocolParams.setContentCharset(httpParams, "UTF-8");  
        HttpConnectionParams.setStaleCheckingEnabled(httpParams, false);  
        HttpClientParams.setRedirecting(httpParams, false);  
        HttpProtocolParams.setUserAgent(httpParams, "Android client");  
        HttpConnectionParams.setSoTimeout(httpParams, DEFAULT_SOCKET_TIMEOUT);  
        HttpConnectionParams.setConnectionTimeout(httpParams, DEFAULT_SOCKET_TIMEOUT);  
        HttpConnectionParams.setTcpNoDelay(httpParams, true);  
        HttpConnectionParams.setSocketBufferSize(httpParams, DEFAULT_SOCKET_BUFFER_SIZE);  
  
        SchemeRegistry schemeRegistry = new SchemeRegistry();  
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));  
        try {  
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());  
            trustStore.load(null, null);  
            SSLSocketFactory sf = new MySSLSocketFactory(trustStore);  
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);  
            schemeRegistry.register(new Scheme("https", sf, 443));  
        } catch (Exception ex) {  
            // do nothing, just keep not crash  
        }  
  
        ClientConnectionManager manager = new ThreadSafeClientConnManager(httpParams, schemeRegistry);  
        httpClient = new DefaultHttpClient(manager, httpParams);  
    }*/
    
    public static void closeQuietly() {
        if (httpClient != null) {
            if (httpClient instanceof Closeable) {
                try {
                    ((Closeable) httpClient).close();
                    //httpClient.getConnectionManager().shutdown();// 释放连接
                } catch (final IOException ignore) {
                }
            }
        }
    }
    
    private static class MySSLSocketFactory extends SSLSocketFactory {  
        SSLContext sslContext = SSLContext.getInstance("TLS");  
  
        public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException,  
        KeyManagementException, KeyStoreException, UnrecoverableKeyException {  
            super(truststore);  
  
            TrustManager tm = new X509TrustManager() {  
                @Override  
                public void checkClientTrusted(X509Certificate[] chain, String authType)  
                        throws CertificateException {  
                }  
  
                @Override  
                public void checkServerTrusted(X509Certificate[] chain, String authType)  
                        throws CertificateException {  
                }  
  
                @Override  
                public X509Certificate[] getAcceptedIssuers() {  
                    return null;  
                }  
            };  
  
            sslContext.init(null, new TrustManager[] {  
                    tm  
            }, null);  
        }  
  
        @Override  
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose)  
                throws IOException, UnknownHostException {  
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);  
        }  
  
        @Override  
        public Socket createSocket() throws IOException {  
            return sslContext.getSocketFactory().createSocket();  
        }  
    }
	
	
	public static HttpClient getHttpClient(){
		if(httpClient == null) {
			LogUtil.d("httpClientUtils", "init httpclient....");
			HttpParams mHttpParams=new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(mHttpParams, 20*1000);
			HttpConnectionParams.setSoTimeout(mHttpParams, 20*1000);
			HttpConnectionParams.setSocketBufferSize(mHttpParams, 8*1024);
			HttpClientParams.setRedirecting(mHttpParams, true);
			httpClient=new DefaultHttpClient(mHttpParams);
		}
		return httpClient;
	}
	
	
	
	
	
	//解析域名www.mny9.com
	public static String getServerHostAddress(String hostName) {
        try {
        	String tem = hostName.substring(7, hostName.length());
            InetAddress inetHost = InetAddress.getByName(tem);
//            String hostName = inetHost.getHostName();
//            System.out.println("The host name was: " + hostName);
            //System.out.println("The hosts IP address is: " + inetHost.getHostAddress()); 
            return "http://"+inetHost.getHostAddress()+"/NineCloud";
        } catch(UnknownHostException ex) {
            System.out.println("Unrecognized host");
            return hostName;
        }
    }
	
	//CloseableHttpClient 方式........................
	/*public static void post(String url){
		// 创建默认的httpClient实例.    
        CloseableHttpClient httpclient = HttpClients.createDefault();  
        // 创建httppost    
        HttpPost httppost = new HttpPost(url); 
//        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000).build();//设置请求和传输超时时间
//        httppost.setConfig(requestConfig);
        // 创建参数队列    
        List<NameValuePair> params = new ArrayList<NameValuePair>();  
        params.add(new BasicNameValuePair("userId", APP.GetSharedPreferences(NewLogin.SAVEFILE, "sid", "")));
		params.add(new BasicNameValuePair("sessionId", Constants.sessionId+""));
        UrlEncodedFormEntity uefEntity;  
        try {  
            uefEntity = new UrlEncodedFormEntity(params, "UTF-8");  
            httppost.setEntity(uefEntity);  
            System.out.println("executing request " + httppost.getURI()); 
            HttpConnectionParams.setConnectionTimeout(httpclient.getParams(),30000);
			HttpConnectionParams.setSoTimeout(httpclient.getParams(), 30000);
            CloseableHttpResponse response = httpclient.execute(httppost);  
            try {
            	if (response.getStatusLine().getStatusCode() == 200) {
            		HttpEntity entity = response.getEntity();  
                    if (entity != null) {  
                        System.out.println("--------------------------------------");  
                        System.out.println("Response content: " + EntityUtils.toString(entity, "UTF-8"));  
                        System.out.println("--------------------------------------");  
                    }
            	}else {
    				int status = response.getStatusLine().getStatusCode();
    				Log.i("BaseAppliction", "status="+status);
    			}
            } finally {  
                response.close();  
            }  
        } catch (ClientProtocolException e) {  
            e.printStackTrace();  
        } catch (UnsupportedEncodingException e1) {  
            e1.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {  
            // 关闭连接,释放资源    
            try {  
                httpclient.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }
	}*/
	
	
    

}
