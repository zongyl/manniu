package com.adapter;

public class Menu {
	/** 显示文本 */
	private String text;
	/** icon */
	private Integer iconResid;
	/** link */
	private String link;
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getIconResid() {
		return iconResid;
	}
	public void setIconResid(Integer iconResid) {
		this.iconResid = iconResid;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
}