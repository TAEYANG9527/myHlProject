//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.itcalf.renhe.utils;

import com.itcalf.renhe.Constants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class StringUtils {
	public StringUtils() {
	}

	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}
    public static String utf8Encode(String str) {
        if (!isEmpty(str) && str.getBytes().length != str.length()) {
            try {
                return URLEncoder.encode(str, Constants.HTTP_CHAR_SET_UTF);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("UnsupportedEncodingException occurred. ", e);
            }
        }
        return str;
    }
    public static String gbkEncode(String str) {
        if (!isEmpty(str) && str.getBytes().length != str.length()) {
            try {
                return URLEncoder.encode(str, Constants.HTTP_CHAR_SET);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("UnsupportedEncodingException occurred. ", e);
            }
        }
        return str;
    }
}
