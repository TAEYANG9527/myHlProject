package com.itcalf.renhe.utils;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.itcalf.renhe.RenheApplication;

public class DraftUtil {

	public static final String DRAFT_COMMENT = "draft_comment";
	public static final String DRAFT_MESSAGE = "draft_message";

	private static SaveDraftHandler draftHandler;

	/**
	 * 获取草稿内容
	 * @param draftname 草稿类型名
	 * @param key 草稿key
	 * */
	public static String getDraft(String draftname, String key) {
		SharedPreferences prefs = RenheApplication.getInstance().getSharedPreferences(draftname, 0);
		return prefs.getString(key, "");
	}

	/**
	 * 保存草稿内容
	 * @param draftname 草稿类型名
	 * */
	public static boolean putDraft(String draftname, String key, String content) {
		SharedPreferences prefs = RenheApplication.getInstance().getSharedPreferences(draftname, 0);
		return prefs.edit().putString(key, content).commit();
	}

	/**
	 * 删除指定key的草稿内容
	 * @param draftname 草稿类型名
	 * */
	public static boolean removeDraft(String draftname, String key) {
		SharedPreferences prefs = RenheApplication.getInstance().getSharedPreferences(draftname, 0);
		return prefs.edit().remove(key).commit();
	}

	/**
	 * 清空某一类草稿
	 * @param draftname 草稿类型名
	 * */
	public static boolean clearDraft(String draftname) {
		SharedPreferences prefs = RenheApplication.getInstance().getSharedPreferences(draftname, 0);
		return prefs.edit().clear().commit();
	}

	/**
	 * 清除各种草稿
	 * */
	public static void clearAllDraft() {
		clearDraft(DRAFT_COMMENT);
		clearDraft(DRAFT_MESSAGE);
	}

	/**
	 * 输入完成后保存草稿内容，用于EditText输入状态监听方法中
	 * @param draftname 草稿类型名
	 * */
	public static void saveDraft(String draftname, String key, String content) {
		if (draftHandler == null) {
			draftHandler = new SaveDraftHandler();
		} else {
			draftHandler.removeMessages(1000);
		}
		draftHandler.setParams(draftname, key, content);
		draftHandler.sendEmptyMessageDelayed(1000, 800);
	}

	/**
	 * 组合生成草稿key
	 * */
	public static String createKey(String... strs) {
		StringBuffer buff = new StringBuffer("");
		if (strs != null && strs.length > 0) {
			for (int i = 0, len = strs.length; i < len; i++) {
				if (!TextUtils.isEmpty(strs[i]))
					buff.append("-" + strs[i]);
			}
			buff.replace(0, 1, "");
		}
		return buff.toString();
	}

	private static class SaveDraftHandler extends Handler {
		private String draftname;
		private String key;
		private String content;

		public void setParams(String draftname, String key, String content) {
			this.draftname = draftname;
			this.key = key;
			this.content = content;
		}

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1000) {
				if (TextUtils.isEmpty(content) || TextUtils.isEmpty(content.trim()))
					removeDraft(draftname, key);
				else
					putDraft(draftname, key, content);
			}
		}
	}

}
