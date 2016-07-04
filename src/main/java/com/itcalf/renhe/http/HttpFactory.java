package com.itcalf.renhe.http;

public class HttpFactory {

	public static final int FLAG_OK_HTTP = 0;

	public static HttpSender getHttpSender(int id,String tag) {
		switch (id) {
		case FLAG_OK_HTTP:
			return new OkHttpSender(tag);
		}
		return new OkHttpSender(tag);
	}

}
