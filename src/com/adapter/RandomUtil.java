package com.adapter;

import java.util.Random;

public class RandomUtil {
    public static final String allChar = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String letterChar = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String numberChar = "0123456789";

    private static String generateString(int length, String baseString) {
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(baseString.charAt(random.nextInt(baseString.length())));
        }
        return sb.toString();
    }
    
    /**
     * 返回一个定长的纯数字字符串
     * @param length
     * @return
     */
    public static String generateNumString(int length){
    	return generateString(length, numberChar);
    }
    
    /**
     * 返回一个定长的纯字母字符串(包括大小写)
     * @param length
     * @return
     */
    public static String generateLetterString(int length){
    	return generateString(length, letterChar);
    }
    
    /**
     * 返回一个定长的随机字符串
     * 
     * @param length
     *            随机字符串长度
     * @return 随机字符串
     */
    public static String generateMixString(int length) {
        return generateString(length, allChar);
    }

    /**
     * 返回一个定长的随机纯小写混合字符串
     * 
     * @param length
     *            随机字符串长度
     * @return 随机字符串
     */
    public static String generateLowerString(int length) {
        return generateMixString(length).toLowerCase();
    }

    
	public static String generateUpperString(int length) {
        return generateMixString(length).toUpperCase();
    }

    /**
     * 生成一个定长的纯0字符串
     * 
     * @param length
     *            字符串长度
     * @return 纯0字符串
     */
    public static String generateZeroString(int length) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            sb.append('0');
        }
        return sb.toString();
    }

    /**
     * 根据数字生成一个定长的字符串，长度不够前面补0
     * 
     * @param num
     *            数字
     * @param fixdlenth
     *            字符串长度
     * @return 定长的字符串
     */
    public static String toFixdLengthString(long num, int fixdlenth) {
        StringBuffer sb = new StringBuffer();
        String strNum = String.valueOf(num);
        if (fixdlenth - strNum.length() >= 0) {
            sb.append(generateZeroString(fixdlenth - strNum.length()));
        } else {
            throw new RuntimeException("将数字" + num + "转化为长度为" + fixdlenth
                    + "的字符串发生异常！");
        }
        sb.append(strNum);
        return sb.toString();
    }

    /**
     * 根据数字生成一个定长的字符串，长度不够前面补0
     * 
     * @param num
     *            数字
     * @param fixdlenth
     *            字符串长度
     * @return 定长的字符串
     */
    public static String toFixdLengthString(int num, int fixdlenth) {
        StringBuffer sb = new StringBuffer();
        String strNum = String.valueOf(num);
        if (fixdlenth - strNum.length() >= 0) {
            sb.append(generateZeroString(fixdlenth - strNum.length()));
        } else {
            throw new RuntimeException("将数字" + num + "转化为长度为" + fixdlenth
                    + "的字符串发生异常！");
        }
        sb.append(strNum);
        return sb.toString();
    }

    public static void main(String[] args) {
        //System.out.println(generateString(6));
        //System.out.println(generateMixString(6));
        //System.out.println(generateLowerString(6));
        //System.out.println(generateUpperString(6));
        //System.out.println(generateZeroString(6));
        //System.out.println(toFixdLengthString(123, 15));
        //System.out.println(toFixdLengthString(123L, 15));
    }

}