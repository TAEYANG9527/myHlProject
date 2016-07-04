package com.itcalf.renhe.utils;

import android.content.Intent;
import android.net.Uri;

import java.io.File;

/**
 * 邮件发送工具类
 * 
 * */
public class EmailUtil {

	public static Intent emailSendTo(String url, String title, String content) {
		// 系统邮件系统的动作为android.content.Intent.ACTION_SEND
		Intent email = new Intent(android.content.Intent.ACTION_SEND);
		email.setType("text/plain");
		// 设置邮件默认地址
		email.putExtra(android.content.Intent.EXTRA_EMAIL, url);
		// 设置邮件默认标题
		email.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
		// 设置要默认发送的内容
		email.putExtra(android.content.Intent.EXTRA_TEXT, content);
		// 调用系统的邮件系统
		return email;
	}

	public static Intent emailSendTo(String url) {
		//		// 系统邮件系统的动作为android.content.Intent.ACTION_SEND
		//		Intent email = new Intent(android.content.Intent.ACTION_SEND);
		//		email.setType("text/plain");
		//		// 设置邮件默认地址
		//		email.putExtra(android.content.Intent.EXTRA_EMAIL,
		//				new String[] {"mailto:"+url});
		Uri uri = Uri.parse("mailto:" + url);
		Intent email = new Intent(Intent.ACTION_SENDTO, uri);

		return email;
	}

	public static Intent emailSendTo(String url, String title, String content, File file) {
		Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		intent.setType("application/octet-stream");
		intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { url });
		intent.putExtra(android.content.Intent.EXTRA_TEXT, content);
		intent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
		//发送文件
		//		File file = new File(Environment.getExternalStorageDirectory()
		//				.getPath()
		//				+ File.separator + "camera.jpg");
		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
		return intent;
	}
}
