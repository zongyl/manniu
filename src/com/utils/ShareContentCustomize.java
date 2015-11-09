package com.utils;

import android.content.Context;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.Platform.ShareParams;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.ShareContentCustomizeCallback;

import com.manniu.manniu.R;

public class ShareContentCustomize implements ShareContentCustomizeCallback {

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
		oks.setNotification(R.drawable.ic_launcher, strAppName);
		oks.setShareContentCustomizeCallback(new ShareContentCustomize(strTitle,strText,strPYQText,strPYQImageURL)); 
				 oks.show(context) ; 
	}
	
	
	
	@Override
	public void onShare(Platform platform, ShareParams paramsToShare) {
		paramsToShare.setShareType(Platform.SHARE_WEBPAGE);
		String strPN = platform.getName();
		paramsToShare.setTitle(_titleInfo);
		  if("Wechat".equals(strPN)){
			  paramsToShare.setTitle("title");
			  paramsToShare.setText ("text");//_textInfo
			//.  paramsToShare.setImageUrl("http://www.mny9.com/images/1.jpeg");
			  paramsToShare.setImagePath("http://www.mny9.com/images/img1.jpg");
			  paramsToShare.setUrl("http://www.baidu.com");
		  }else if("WechatMoments".equals(strPN)){
			  paramsToShare.setText(_wechatMomentsTextInfo);
			  paramsToShare.setImageUrl(_imageUrl);		
		  }else if("WechatFavorite".equals(strPN)){
			  
		  }
		  //paramsToShare.setUrl(Constants.hostUrl);
	}

}
