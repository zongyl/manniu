package com.views;

import android.content.Context;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.Platform.ShareParams;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.ShareContentCustomizeCallback;

public class ShareContentCustomizeDemo implements ShareContentCustomizeCallback {

	public static void showShare(String strAppName, Context context, String strTitle, String strText, String strPYQText, 
			String strPYQImageURL, boolean silent, String platform) {
		
		OnekeyShare oks = new OnekeyShare();
		ShareSDK.initSDK(context);
		oks. setShareContentCustomizeCallback ( 
				new ShareContentCustomizeDemo (strTitle,strText,strPYQText,strPYQImageURL)) ; 
				 oks.show (context) ; 
		}
	
	String _textInfo = "";
	String _imageUrl = "";
	String _titleInfo = "";	
	String _wechatMomentsTextInfo = "";
	public ShareContentCustomizeDemo(String strTitle,String strText,String strPYQText,String strImageURL)
	{
		_titleInfo = strTitle;
		_textInfo = strText;
		_wechatMomentsTextInfo = strPYQText; 
		_imageUrl = strImageURL; 
	}	
	@Override
	public void onShare(Platform platform, ShareParams paramsToShare) {
		paramsToShare.setShareType(Platform.SHARE_WEBPAGE);
		String strPN = platform.getName();
		paramsToShare.setTitle(_titleInfo);
		  if  ("Wechat". equals (strPN) )  {
			  //微信好友正文处理
			 /* if(_textInfo!=null && _textInfo.length()>0)
			  {
				  paramsToShare. setText (_textInfo) ;
			  }else if(_imageUrl!=null && _imageUrl.length()>0)
			  {
				  paramsToShare.setImageUrl(_imageUrl);	
			  }
			  paramsToShare.setUrl(Constants.hostUrl);*/
			  
			  int index = _wechatMomentsTextInfo.indexOf("http://");
			  paramsToShare.setTitle(_wechatMomentsTextInfo.substring(0, index));
			  String str = _wechatMomentsTextInfo.substring(index, _wechatMomentsTextInfo.length());
			  paramsToShare.setText("") ;
			  paramsToShare.setImageUrl(_imageUrl);	
			  paramsToShare.setUrl(_wechatMomentsTextInfo.substring(index, _wechatMomentsTextInfo.length()));
		  }
		  else if("WechatMoments".equals(strPN))
		  {
			  //朋友圈正文处理			  
			  int index = _wechatMomentsTextInfo.indexOf("http://");
			  paramsToShare.setTitle(_wechatMomentsTextInfo.substring(0, index));
			  String str = _wechatMomentsTextInfo.substring(index, _wechatMomentsTextInfo.length());
			  paramsToShare.setText("") ;
			  paramsToShare.setImageUrl(_imageUrl);	
			  paramsToShare.setUrl(_wechatMomentsTextInfo.substring(index, _wechatMomentsTextInfo.length()));
		  }
		  
		  
	}

}
