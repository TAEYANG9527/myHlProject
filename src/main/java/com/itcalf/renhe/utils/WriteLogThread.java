package com.itcalf.renhe.utils;

import android.content.Context;
import android.util.Log;

/**
 * 写日志线程
 * @author YZQ
 *
 */
public class WriteLogThread extends Thread {

	private Context context;
	private String logId;
	private String[] others;

	/**
	 * @param context
	 * @param logId 日志id
	 * @param others 其他日志 如果没有传null
	 */
	public WriteLogThread(Context context, String logId, String[] others) {
		this.context = context;
		this.logId = logId;
		this.others = others;
	}

	@Override
	public void run() {
		super.run();
		if (context == null) {
			Log.e("WriteLogThread", "context == null");
			return;
		}
		StringBuilder content = new StringBuilder();
		content.append(logId).append(LoggerFileUtil.getConstantInfo(context));
		if (others != null && others.length > 0) {
			content.append("|");
			for (String string : others) {
				content.append(string).append("|");
			}
			content.deleteCharAt(content.length() - 1);
		}
		LoggerFileUtil.writeFile(content.toString(), true);
	}

}
