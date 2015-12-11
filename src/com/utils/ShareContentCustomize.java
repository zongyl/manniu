package com.utils;

import android.content.Context;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.Platform.ShareParams;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

public class ShareContentCustomize /*implements ShareContentCustomizeCallback */{

	String _textInfo = "";
	String _imageUrl = "";
	String _titleInfo = "";	
	String _wechatMomentsTextInfo = "";
	
	public ShareContentCustomize(String strTitle,String strText,String strPYQText,String strImageURL) {
		_titleInfo = strTitle;
		_textInfo = strText;
		_wechatMomentsTextInfo = strPYQText; 
		_imageUrl = strImageURL; 
	}
	
	public static void showShare(String strAppName,Context context,String strTitle,String strText,
			String strPYQText,String strPYQImageURL,boolean silent, String platform) {
		
		OnekeyShare oks = new OnekeyShare();
		ShareSDK.initSDK(context);
		//oks.setNotification(R.drawable.ic_launcher, strAppName);
		//oks.setShareContentCustomizeCallback(new ShareContentCustomize(strTitle,strText,strPYQText,strPYQImageURL)); 
		
		//oks.disableSSOWhenAuthorize();
		oks.setText("share text123123131");
		oks.setTitle("title");
		oks.setImagePath("http://www.mny9.com/images/1.jpeg");//本地图片路径
		oks.setFilePath("http://www.mny9.com/update/update.txt");//本地路径 
		oks.setUrl("http://www.mny9.com/");
		oks.show(context) ; 
	}

	//@Override
	public void onShare(Platform platform, ShareParams paramsToShare) {/*
		paramsToShare.setShareType(Platform.SHARE_WEBPAGE);
		String strPN = platform.getName();
		paramsToShare.setTitle(_titleInfo);
		  if("Wechat".equals(strPN)){
			  paramsToShare.setTitle("title");
			  paramsToShare.setText ("text");
			  paramsToShare.setImagePath("http://www.mny9.com/images/img1.jpg");
			  paramsToShare.setUrl("http://www.baidu.com");
		  }else if("WechatMoments".equals(strPN)){
			  paramsToShare.setText(_wechatMomentsTextInfo);
			  paramsToShare.setImageUrl(_imageUrl);		
		  }else if("WechatFavorite".equals(strPN)){
			  
		  }
		  //paramsToShare.setUrl(Constants.hostUrl);
	*/}

}
