package com.views;

import android.content.Context;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.Platform.ShareParams;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.ShareContentCustomizeCallback;

import com.manniu.manniu.R;
import com.utils.Constants;

public class ShareContentCustomizeDemo implements ShareContentCustomizeCallback {

	public static void showShare(String strAppName,Context context,String strTitle,String strText,String strPYQText,String strPYQImageURL,boolean silent, String platform) {
		
		OnekeyShare oks = new OnekeyShare();
		ShareSDK.initSDK(context);//2015.11.01 李德明添加
		/* 网摘内容：
		 * 打开您项目的入口Activity，在其onCreate中插入下面的代码： 如果使用ShareSDK.xml方式配置调用
		 * ShareSDK.initSDK(this);
		 * 如果使用代码配置应用注册信息或者应用后台配置应用注册信息,调用ShareSDK.initSDK(this,”androidv1101″);
		 * androidv1101:是你的应用在ShareSDK注册应用信息时返回的AppKey。 代码会初始化ShareSDK，此后对ShareSDK的操作都以此为基础。如果不在所有ShareSDK的操作之前调用这行代码，会抛出空指针异常
		 * */
		oks.setNotification(R.drawable.ic_launcher, strAppName);
		//oks.setAddress("12345678901");
		//oks.setTitle("来自男人装的分享");
		//oks.setTitleUrl("http://sharesdk.cn");
		//oks.setText("\t\t家，与我同行\r\n[蛮牛云分享视频]\r\n"+"<a href ='"+liveUrl+"'>请点击:"+_title.getText()+"</a>"); 
		//oks.setImagePath("http://img0.bdstatic.com/img/image/shouye/muzrxc1.jpg");//costume.getLargepic()
		oks. setShareContentCustomizeCallback ( 
				new ShareContentCustomizeDemo (strTitle,strText,strPYQText,strPYQImageURL) ) ; 
				 oks. show (context ) ; 
		
		
		//oks.setImageUrl("http://www.9wingo.com/images/qrcode.png");
		//oks.setUrl("http://www.mny9.com/UsersAction_goToMyCloud");
		//oks.setUrl("http://news.163.com/15/1014/00/B5RM9TCT00014AEE.html");
		//oks.setFilePath(costume.getLargepic());
		//oks.setComment(costume.getComment());
		//oks.setSite("男人装");
		//oks.setSiteUrl("http://sharesdk.cn");
		//oks.setVenueName("ShareSDK");
		//oks.setVenueDescription("This is a beautiful place!");
		//oks.setLatitude(23.056081f);
		//oks.setLongitude(113.385708f);
		//oks.setSilent(silent);
		//if (platform != null) {
		//oks.setPlatform(platform);
		//}
		// 去除注释，可令编辑页面显示为Dialog模式
		//oks.setDialogMode();
		// 去除注释，在自动授权时可以禁用SSO方式
		//oks.disableSSOWhenAuthorize();
		// 去除注释，则快捷分享的操作结果将通过OneKeyShareCallback回调
		//oks.setCallback(new OneKeyShareCallback());
		//oks.setShareContentCustomizeCallback(new ShareContentCustomizeDemo());
		// 去除注释，演示在九宫格设置自定义的图标
		//Bitmap logo = BitmapFactory.decodeResource(menu.getResources(), R.drawable.ic_launcher);
		//String label = menu.getResources().getString(R.string.app_name);
		//OnClickListener listener = new OnClickListener() {
		//public void onClick(View v) {
		//String text = "Customer Logo -- ShareSDK " + ShareSDK.getSDKVersionName();
		//Toast.makeText(menu.getContext(), text, Toast.LENGTH_SHORT).show();
		//oks.finish();
		//}
		//};
		//oks.setCustomerLogo(logo, label, listener);
		// 去除注释，则快捷分享九宫格中将隐藏新浪微博和腾讯微博
		//oks.addHiddenPlatform(SinaWeibo.NAME);
		//oks.addHiddenPlatform(TencentWeibo.NAME);
		//oks.show(this);
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
		//APP.ShowToast(strPN);
		paramsToShare.setAddress("12345678901");
		paramsToShare.setTitle(_titleInfo);
		  if  ("Wechat". equals (strPN) )  {
			  //微信好友正文处理
			  if(_textInfo!=null && _textInfo.length()>0)
			  {
				  paramsToShare. setText (_textInfo) ;
			  }
			  if(_imageUrl!=null && _imageUrl.length()>0)
			  {
				  paramsToShare.setImageUrl(_imageUrl);	
			  }
		  }
		  else if("WechatMoments".equals(strPN))
		  {
			  //朋友圈正文处理			  
			  paramsToShare.setText(_wechatMomentsTextInfo) ;
			  paramsToShare.setImageUrl(_imageUrl);			
		  }
		  paramsToShare.setUrl(Constants.hostUrl);
		  
	}

}
