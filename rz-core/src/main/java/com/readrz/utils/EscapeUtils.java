package com.readrz.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

public class EscapeUtils {
	
	private static final Pattern _pattern1 = Pattern.compile("'");
	private static final Pattern _pattern2 = Pattern.compile("\\\\+");

	public static String escapeSql(String str) {
		
		Matcher m1 = _pattern1.matcher(str);
		str = m1.replaceAll("''");

		Matcher m2 = _pattern2.matcher(str);
		str = m2.replaceAll("\\\\\\\\");
		
		return str;
	}
	
	public static String unescapeHtmlMultilevel(String text) {
		
		while (true) {
			
			// unescape special characters (next level)
			String unescaped = StringEscapeUtils.unescapeHtml(text);
			unescaped = unescaped.replaceAll("&nbsp;|\u00A0", " ");
			unescaped = unescaped.replaceAll("&apos;", "'");
			
			// check if changed
			if (unescaped.equals(text)) {
				
				// cannot escape more
				break;

			} else {
				
				// continue escaping
				text = unescaped;
			}
		}
		
		return text;
	}
}
