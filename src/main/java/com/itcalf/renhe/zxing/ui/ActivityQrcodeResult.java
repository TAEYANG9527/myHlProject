package com.itcalf.renhe.zxing.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.context.wukong.im.ActivityCircleDetail;
import com.itcalf.renhe.view.TextViewFixTouchConsume;
import com.itcalf.renhe.view.WebViewActWithTitle;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActivityQrcodeResult extends BaseActivity {

	private TextViewFixTouchConsume tx_result;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		RenheApplication.getInstance().addActivity(this);
		getTemplate().doInActivity(this, R.layout.activity_qrcode_result);
		//		setTitle("二维码详情页");
		setTextValue(1, "二维码详情页");
	}

	@Override
	protected void findView() {
		super.findView();
		tx_result = (TextViewFixTouchConsume) findViewById(R.id.tx_result);
	}

	@Override
	protected void initData() {
		super.initData();

		String result = getIntent().getStringExtra("result");
		if (TextUtils.isEmpty(result)) {
			finish();
			return;
		}

		Pattern pattern = Pattern.compile(Constants.PATTERN_URL);
		Matcher matcher = pattern.matcher(result);
		if (matcher.find()) {
			if (result.startsWith(Constants.PUBLIC_FILETER_URL.RENHE_PROFILE)) {//跳转到用户档案
				String sid = getUrlPramNameAndValue(result).get("sid");
				if (TextUtils.isEmpty(sid)) {
					Intent intent = new Intent(this, WebViewActWithTitle.class);
					intent.putExtra("url", result);
					startHlActivity(intent);
					finish();
				} else {
					Intent intent = new Intent(this, MyHomeArchivesActivity.class);
					intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, sid);
					startHlActivity(intent);
					finish();
				}
			} else if (result.startsWith(Constants.PUBLIC_FILETER_URL.RENHE_CIRCLE)) {
				String circleId = getUrlPramNameAndValue(result).get("id");
				//跳转圈子详情
				if (TextUtils.isEmpty(circleId)) {
					Intent intent = new Intent(this, WebViewActWithTitle.class);
					intent.putExtra("url", result);
					startHlActivity(intent);
					finish();
				} else {
					Intent intent = new Intent();
					intent.putExtra("circleId", circleId);
					intent.setClass(ActivityQrcodeResult.this, ActivityCircleDetail.class);
					startHlActivity(intent);
					finish();
				}

			} else {
				Intent intent = new Intent(this, WebViewActWithTitle.class);
				intent.putExtra("url", result);
				startHlActivity(intent);
				finish();
			}

		} else {
			tx_result.setText(result);
		}
	}

	private Map<String, String> getUrlPramNameAndValue(String url) {
		String regEx = "(\\?|&+)(.+?)=([^&]*)";//匹配参数名和参数值的正则表达式
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(url);
		// LinkedHashMap是有序的Map集合，遍历时会按照加入的顺序遍历输出
		Map<String, String> paramMap = new LinkedHashMap<String, String>();
		while (m.find()) {
			String paramName = m.group(2);//获取参数名
			String paramVal = m.group(3);//获取参数值
			paramMap.put(paramName, paramVal);
		}
		return paramMap;
	}
}
