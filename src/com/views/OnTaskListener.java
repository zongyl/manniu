package com.views;

public interface OnTaskListener {
	/**
	 * 任务后台运行
	 * @param what
	 * @param arg1
	 * @param arg2
	 * @param obj
	 * @return
	 */
	Object OnDoInBackground(int what, int arg1, int arg2, Object obj);
	/**
	 * 运行后结果显示,可以更新界面
	 * @param what
	 * @param arg1
	 * @param arg2
	 * @param obj
	 * @return
	 */
	int OnPostExecute(int what, int arg1, int arg2, Object obj, Object ret);

}
