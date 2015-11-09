package com.ctrl;

import android.content.Intent;
import android.graphics.drawable.Drawable;
/**
 * 主界面下拉菜单  重定义 popupMenu  MenuItem类 
 */
public class MenuItem {

    private int itemId;
    private String title;
    private Drawable icon;
    private Intent intent;
    private Object[] params ;
    
    public Object[] getParams() {
		return params;
	}
	public void setParams(Object... params) {
		this.params = params;
	}
	public void setItemId(int itemId) {
        this.itemId = itemId;
    }
    public int getItemId() {
        return itemId;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getTitle() {
        return title;
    }
    public void setIcon(Drawable icon) {
        this.icon = icon;
    }
    public Drawable getIcon() {
        return icon;
    }
    public void setIntent(Intent intent) {
        this.intent = intent;
    }
    public Intent getIntent() {
        return intent;
    }
}