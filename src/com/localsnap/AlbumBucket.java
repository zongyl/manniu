package com.localsnap;

import java.util.List;

/**
 * 一个目录的相册对象
 * 
 */
public class AlbumBucket implements Comparable<AlbumBucket>{
	public int count = 0;
	public String bucketName;
	public List<ImageItem> imageList;
	
	
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public String getBucketName() {
		return bucketName;
	}
	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}
	public List<ImageItem> getImageList() {
		return imageList;
	}
	public void setImageList(List<ImageItem> imageList) {
		this.imageList = imageList;
	}


	@Override
	public int compareTo(AlbumBucket arg0) {
		return (int) (Integer.valueOf(this.getBucketName()) - Integer.valueOf(arg0.getBucketName()));
	}

}
