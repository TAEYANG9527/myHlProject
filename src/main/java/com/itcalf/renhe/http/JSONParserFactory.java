package com.itcalf.renhe.http;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;

public class JSONParserFactory {

	//public static final int GOOGLE_GSON_PARSER = 1;
	public static final int FAST_JSON_PARSER = 2;

	public static <T> Object getObjectByParser(int type, String json, Class<T> clazz) {
		if (clazz == null || TextUtils.isEmpty(json)) {
			return null;
		}
		try {
			if (json.startsWith("[")) {
				return JSON.parseArray(json, clazz);
			} else {
				return JSON.parseObject(json, clazz);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
