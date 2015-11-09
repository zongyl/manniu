package com.utils;

import java.io.File;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA. User: li_jianhua Date: 2014-12-19 上午10:34:14 To
 * change this template use File | Settings | File Templates. Description：
 */

public class SortUtils {

	public SortUtils() {
	}

	public static void sort(int[] array) {
		java.util.Arrays.sort(array); // 调用Arrays的静态方法sort()
	}

	public static void sort(String[] array) {
		Arrays.sort(array);
	}

	// 一个特殊的对象数组的比较.使用冒泡法为原型.
	public static void sort(File[] array) {
		File temp = null;
		boolean condition = false;
		for (int i = 0; i < array.length; i++) {
			for (int j = array.length - 1; j > i; j--) {
				condition = array[j].lastModified() > array[j - 1]
						.lastModified();
				if (condition) {
					temp = array[j];
					array[j] = array[j - 1];
					array[j - 1] = temp;
				}
			}
		}
	}

}
