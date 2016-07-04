//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.itcalf.renhe.utils;

import java.util.Map;

public class MapUtils {
	public static final String DEFAULT_KEY_AND_VALUE_SEPARATOR = ":";
	public static final String DEFAULT_KEY_AND_VALUE_PAIR_SEPARATOR = ",";

	public MapUtils() {
	}

	public static <K, V> boolean isEmpty(Map<K, V> sourceMap) {
		return sourceMap == null || sourceMap.size() == 0;
	}

}
