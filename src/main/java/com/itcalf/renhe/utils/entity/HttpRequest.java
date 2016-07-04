//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.itcalf.renhe.utils.entity;

import com.itcalf.renhe.utils.StringUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HttpRequest {
	private String url;
	private int connectTimeout;
	private int readTimeout;
	private Map<String, Object> parasMap;
	private Map<String, String> requestProperties;

	/** url and para separator **/
	public static final String URL_AND_PARA_SEPARATOR = "?";
	/** parameters separator **/
	public static final String PARAMETERS_SEPARATOR = "&";
	/** paths separator **/
	public static final String PATHS_SEPARATOR = "/";
	/** equal sign **/
	public static final String EQUAL_SIGN = "=";

	public HttpRequest(String url) {
		this.url = url;
		this.connectTimeout = -1;
		this.readTimeout = -1;
		this.requestProperties = new HashMap();
	}

	public HttpRequest(String url, Map<String, Object> parasMap) {
		this.url = url;
		this.parasMap = parasMap;
		this.connectTimeout = -1;
		this.readTimeout = -1;
		this.requestProperties = new HashMap();
	}

	public String getUrl() {
		return this.url;
	}

	public int getConnectTimeout() {
		return this.connectTimeout;
	}

	public void setConnectTimeout(int timeoutMillis) {
		if (timeoutMillis < 0) {
			throw new IllegalArgumentException("timeout can not be negative");
		} else {
			this.connectTimeout = timeoutMillis;
		}
	}

	public int getReadTimeout() {
		return this.readTimeout;
	}

	public void setReadTimeout(int timeoutMillis) {
		if (timeoutMillis < 0) {
			throw new IllegalArgumentException("timeout can not be negative");
		} else {
			this.readTimeout = timeoutMillis;
		}
	}

	public Map<String, Object> getParasMap() {
		return this.parasMap;
	}

	public void setParasMap(Map<String, Object> parasMap) {
		this.parasMap = parasMap;
	}

	public String getUTFParas() {
		return joinParasWithUTFEncodedValue(this.parasMap);
	}
	public String getGBKParas() {
		return joinParasWithGBKEncodedValue(this.parasMap);
	}
	public void setRequestProperty(String field, String newValue) {
		this.requestProperties.put(field, newValue);
	}

	public String getRequestProperty(String field) {
		return (String) this.requestProperties.get(field);
	}

	public void setUserAgent(String value) {
		this.requestProperties.put("User-Agent", value);
	}

	public Map<String, String> getRequestProperties() {
		return this.requestProperties;
	}

	public void setRequestProperties(Map<String, String> requestProperties) {
		this.requestProperties = requestProperties;
	}

	/**
	 * join paras with encoded value
	 *
	 * @param parasMap
	 * @return
	 */
	public static String joinParasWithUTFEncodedValue(Map<String, Object> parasMap) {
		StringBuilder paras = new StringBuilder("");
		if (parasMap != null && parasMap.size() > 0) {
			Iterator<Map.Entry<String, Object>> ite = parasMap.entrySet().iterator();
			try {
				while (ite.hasNext()) {
					Map.Entry<String, Object> entry = ite.next();
					if (entry.getValue() instanceof String[]) {
						String[] entryValues = (String[]) entry.getValue();
						for (int i = 0; i < entryValues.length; i++) {
							paras.append(entry.getKey()).append(EQUAL_SIGN).append(StringUtils.utf8Encode(entryValues[i]));
							if (i < entryValues.length - 1) {
								paras.append(PARAMETERS_SEPARATOR);
							}
						}
					} else if (entry.getValue() instanceof int[]) {
						int[] entryValues = (int[]) entry.getValue();
						for (int i = 0; i < entryValues.length; i++) {
							paras.append(entry.getKey()).append(EQUAL_SIGN)
									.append(StringUtils.utf8Encode(String.valueOf(entryValues[i])));
							if (i < entryValues.length - 1) {
								paras.append(PARAMETERS_SEPARATOR);
							}
						}
					} else {
						paras.append(entry.getKey()).append(EQUAL_SIGN)
								.append(StringUtils.utf8Encode(entry.getValue().toString()));
					}
					if (ite.hasNext()) {
						paras.append(PARAMETERS_SEPARATOR);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return paras.toString();
	}
	/**
	 * join paras with encoded value
	 *
	 * @param parasMap
	 * @return
	 */
	public static String joinParasWithGBKEncodedValue(Map<String, Object> parasMap) {
		StringBuilder paras = new StringBuilder("");
		if (parasMap != null && parasMap.size() > 0) {
			Iterator<Map.Entry<String, Object>> ite = parasMap.entrySet().iterator();
			try {
				while (ite.hasNext()) {
					Map.Entry<String, Object> entry = ite.next();
					if (entry.getValue() instanceof String[]) {
						String[] entryValues = (String[]) entry.getValue();
						for (int i = 0; i < entryValues.length; i++) {
							paras.append(entry.getKey()).append(EQUAL_SIGN).append(StringUtils.gbkEncode(entryValues[i]).replace("+", "%2B"));
							if (i < entryValues.length - 1) {
								paras.append(PARAMETERS_SEPARATOR);
							}
						}
					} else if (entry.getValue() instanceof int[]) {
						int[] entryValues = (int[]) entry.getValue();
						for (int i = 0; i < entryValues.length; i++) {
							paras.append(entry.getKey()).append(EQUAL_SIGN)
									.append(StringUtils.gbkEncode(String.valueOf(entryValues[i])).replace("+", "%2B"));
							if (i < entryValues.length - 1) {
								paras.append(PARAMETERS_SEPARATOR);
							}
						}
					} else {
						paras.append(entry.getKey()).append(EQUAL_SIGN)
								.append(StringUtils.gbkEncode(entry.getValue().toString()).replace("+", "%2B"));
					}
					if (ite.hasNext()) {
						paras.append(PARAMETERS_SEPARATOR);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return paras.toString();
	}
}
