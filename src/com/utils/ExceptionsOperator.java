package com.utils;

public class ExceptionsOperator {
	public static String getExceptionInfo(Exception exception){
        StringBuilder bExceptionInfo = new StringBuilder();
        bExceptionInfo.append(" message= ' "+exception.toString()+" '.");
        bExceptionInfo.append("\n\t");
        StackTraceElement[] stackTraceElements = exception.getStackTrace();
        for (int i = 0; i < stackTraceElements.length; i++){
            bExceptionInfo.append("             "+stackTraceElements[i].toString());
            if(i!=stackTraceElements.length-1){
                bExceptionInfo.append("\r\n");
            }
        }
        return bExceptionInfo.toString();
    }
    public static String getErrorInfo(Error e){
        StringBuilder sb = new StringBuilder();
        sb.append(e.toString()+"\r\n");
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        for (int i = 0; i < stackTraceElements.length; i++){
            sb.append("             "+stackTraceElements[i].toString());
            if(i!=stackTraceElements.length-1){
                sb.append("\r\n");
            }
        }
        return sb.toString();
    }
}