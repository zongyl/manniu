package com.ctrl;

import android.view.View;

import com.localmedia.XVideoAdapter;

/**
 * Created by IntelliJ IDEA. User: li_jianhua Date: 2014-8-5 上午10:33:50
 * To change this template use File | Settings | File Templates.
 * Description：
 */
public interface IX_MediaList {
	void OnClickedItem(XVideoAdapter parent, View item, int postion, Object obj);
}
