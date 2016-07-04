package com.itcalf.renhe.utils;

import android.content.Intent;
import android.net.Uri;

/**
 * 短信发送工具类
 * 
 * */
public class MessageSendUtil {

	public static Intent messageSendTo(String uri, String content) {
		Uri smsToUri = Uri.parse(uri);// 联系人地址
		Intent mIntent = new Intent(android.content.Intent.ACTION_SENDTO, smsToUri);
		mIntent.putExtra("sms_body", content);// 短信的内容
		return mIntent;
	}
}
