package com.utils;

import java.util.Locale;

/**
 * 判断当前语言
 * @author pc
 *
 */
public class LanguageUtil {
	
	public static String getLanguageEnv1(){
		Locale l = Locale.getDefault();
		String language = l.getLanguage();
		String country = l.getCountry().toLowerCase();
		 if ("zh".equals(language)) {  
	           if ("cn".equals(country)) {  
	               language = "zh_CN";  
	           } else if ("tw".equals(country)) {  
	               language = "zh_TW";  
	           }  
	       } else if ("pt".equals(language)) {  
	           if ("br".equals(country)) {  
	               language = "pt_BR";  
	           } else if ("pt".equals(country)) {  
	               language = "pt_PT";  
	           }  
	       }
		return language;
	}
	

}
