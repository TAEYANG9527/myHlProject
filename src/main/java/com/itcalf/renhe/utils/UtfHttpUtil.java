package com.itcalf.renhe.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.common.KickOutActivity;
import com.itcalf.renhe.dto.ParentObject;
import com.itcalf.renhe.utils.entity.HttpRequest;
import com.itcalf.renhe.utils.http.okhttp.OkHttpClientManager;
import com.orhanobut.logger.Logger;
import com.vdurmont.emoji.EmojiParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * HTTP 接口类
 *
 * @author xp
 */
public class UtfHttpUtil {
	public static Object doOKHttpRequest(String webService, Map<String, Object> reqParams, Class<?> clazz, Context context)
			throws Exception {
		Object result = null;
		String responseString = OkHttpClientManager.postAsyn(webService, reqParams,clazz);
		if (null != responseString) {
			responseString = EmojiParser.parseToUnicode(responseString);
			if (Constants.DEBUG_MODE) {
				Log.d("OKhttp***" + webService + "——response", responseString + "");
			}
			if (null != responseString) {
				Gson gson = new GsonBuilder().create();
				result = gson.fromJson(responseString, clazz);
				try {
					ParentObject parentObject = gson.fromJson(responseString, ParentObject.class);
					if (null != parentObject && parentObject.getState() == -10001) {
						if (Constants.DEBUG_MODE) {
							Log.e("qch", "账户过期");
						}
						if (RenheApplication.getInstance().isUserExist()) {
							Intent intent = new Intent(context, KickOutActivity.class);
							intent.putExtra("tiker", context.getString(R.string.account_exception));
							context.startActivity(intent);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				gson = null;
			}
			responseString = null;
		}
		return result;
	}

	/**
	 * HttpUrlConncetion 请求的公共接口
	 *
	 * @return 封装好的JSON对象
	 * @throws Exception
	 */

	public static Object doHttpRequest(String httpUrl, Map<String, Object> parasMap, Class<?> clazz, Context context)
			throws Exception {
		StringBuffer logBuffer = new StringBuffer();
		if (null != parasMap && parasMap.size() > 0) {
			for (Map.Entry<String, Object> entry : parasMap.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue().toString();
				logBuffer.append(key + ":" + value + " ");
			}
			//			if (Constants.DEBUG_MODE) {
			//				Log.w(httpUrl + "", logBuffer + "");
			//			}
		}
		HttpRequest httpRequest = new HttpRequest(httpUrl, parasMap);
		com.itcalf.renhe.utils.entity.HttpResponse response = httpPost(httpRequest);
		if (null == response)
			return null;
		Object result = null;
		String responseString = response.getResponseBody();
		responseString = EmojiParser.parseToUnicode(responseString);
		if (Constants.DEBUG_MODE) {
			Logger.w(httpUrl + "——response: " + responseString + "", httpUrl);
			//			Log.w(httpUrl + "——response", responseString + "");
		}
		if (null != responseString) {
			Gson gson = new GsonBuilder().create();
			result = gson.fromJson(responseString, clazz);
			try {
				ParentObject parentObject = gson.fromJson(responseString, ParentObject.class);
				if (null != parentObject && parentObject.getState() == -10001) {
					if (Constants.DEBUG_MODE) {
						Log.e("qch", "账户过期");
					}
					if (RenheApplication.getInstance().isUserExist()) {
						Intent intent = new Intent(context, KickOutActivity.class);
						intent.putExtra("tiker", context.getString(R.string.account_exception));
						context.startActivity(intent);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public static com.itcalf.renhe.utils.entity.HttpResponse httpPost(HttpRequest request) {
		if (request == null) {
			return null;
		} else {
			BufferedReader input = null;
			HttpURLConnection con = null;

			try {
				URL e = new URL(request.getUrl());
				try {
					com.itcalf.renhe.utils.entity.HttpResponse e1 = new com.itcalf.renhe.utils.entity.HttpResponse(
							request.getUrl());
					con = (HttpURLConnection) e.openConnection();
					setURLConnection(request, con);
					con.setRequestMethod("POST");
					con.setDoOutput(true);
					con.setDoInput(true);
					//设置请求体的类型是文本类型
					String paras = request.getUTFParas();
					if (!StringUtils.isEmpty(paras)) {
						con.getOutputStream().write(paras.getBytes());
						if (Constants.DEBUG_MODE) {
							Logger.w(request.getUrl() + "?" + paras);
							//							Log.w(request.getUrl() + "", request.getUrl() + "?" + paras);
						}
					}

					input = new BufferedReader(new InputStreamReader(con.getInputStream(), Constants.HTTP_CHAR_SET_UTF));
					StringBuilder sb = new StringBuilder();

					String s;
					while ((s = input.readLine()) != null) {
						sb.append(s).append("\n");
					}
					e1.setResponseBody(sb.toString());
					setHttpResponse(con, e1);
					com.itcalf.renhe.utils.entity.HttpResponse var8 = e1;
					return var8;
				} catch (IOException var20) {
					var20.printStackTrace();
				}
			} catch (MalformedURLException var21) {
				var21.printStackTrace();
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException var19) {
						var19.printStackTrace();
					}
				}

				if (con != null) {
					con.disconnect();
				}

			}

			return null;
		}
	}

	public static com.itcalf.renhe.utils.entity.HttpResponse httpPost(String httpUrl) {
		return httpPost(new HttpRequest(httpUrl));
	}

	public static String httpPostString(String httpUrl) {
		com.itcalf.renhe.utils.entity.HttpResponse response = httpPost(new HttpRequest(httpUrl));
		return response == null ? null : response.getResponseBody();
	}

	private static void setHttpResponse(HttpURLConnection urlConnection, com.itcalf.renhe.utils.entity.HttpResponse response) {
		if (response != null && urlConnection != null) {
			try {
				response.setResponseCode(urlConnection.getResponseCode());
			} catch (IOException var3) {
				response.setResponseCode(-1);
			}

			response.setResponseHeader("expires", urlConnection.getHeaderField("Expires"));
			response.setResponseHeader("cache-control", urlConnection.getHeaderField("Cache-Control"));
		}
	}

	private static void setURLConnection(HttpRequest request, HttpURLConnection urlConnection) {
		if (request != null && urlConnection != null) {
			setURLConnection(request.getRequestProperties(), urlConnection);
			urlConnection.setConnectTimeout(Constants.ConfigureTimeouts.CONNECT_TIME_OUT * 1000);
			urlConnection.setReadTimeout(Constants.ConfigureTimeouts.REQUEST_TIME_OUT * 1000);
		}
	}

	public static void setURLConnection(Map<String, String> requestProperties, HttpURLConnection urlConnection) {
		if (!MapUtils.isEmpty(requestProperties) && urlConnection != null) {
			Iterator i$ = requestProperties.entrySet().iterator();

			while (i$.hasNext()) {
				Entry entry = (Entry) i$.next();
				if (!StringUtils.isEmpty((String) entry.getKey())) {
					urlConnection.setRequestProperty((String) entry.getKey(), (String) entry.getValue());
				}
			}

		}
	}
}
