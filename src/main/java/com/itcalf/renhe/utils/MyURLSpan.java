package com.itcalf.renhe.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.style.ClickableSpan;
import android.view.View;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;

/**
 * 处理TextView上URL响应方式自定义
 * @author Administrator
 *
 */
public class MyURLSpan extends ClickableSpan {

	private String mUrl;
	private Context mContext;

	public MyURLSpan(String url, Context context) {
		mUrl = url;
		mContext = context;
	}

	@Override
	public void onClick(View widget) {
		// ToastUtil.showToast(LookatMsgActivity.this, mUrl);
		if (mUrl.contains("viewprofile")) {
			String sid = mUrl.substring(mUrl.indexOf("=") + 1);
			if (sid != null && sid.length() > 0) {
				Intent intent = new Intent(mContext, MyHomeArchivesActivity.class);
				intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, sid);
				intent.putExtra("from", Constants.ADDFRIENDTYPE[3]);
				mContext.startActivity(intent);
			}
		} else {
			if (mUrl != null && mUrl.toUpperCase().startsWith("HTTP")) {
				Uri uri = Uri.parse(mUrl);
				Intent it = new Intent(Intent.ACTION_VIEW, uri);
				mContext.startActivity(it);
			}

		}
	}
}