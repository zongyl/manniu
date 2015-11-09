package com.utils;

import android.os.Environment;
/**
 * 作者 杨明*/
public class Tools {
    /**
     * 检查是否存在SDCard
     * @return
     */
    public static boolean hasSdcard(){
            String state = Environment.getExternalStorageState();
            if(state.equals(Environment.MEDIA_MOUNTED)){
                    return true;
            }else{
                    return false;
            }
    }
}