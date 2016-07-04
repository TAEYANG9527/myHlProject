package com.itcalf.renhe.context.register;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.umeng.analytics.MobclickAgent;

/**
 * Title: NewRegisterActivity.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-6-9 下午1:14:36 <br>
 * @author wangning
 */
public class BindPhoneGuideActivity extends BaseActivity {
	private Button bindButton;
	private boolean isBind;
	private String mobile = "";
	private TextView isBindTv;
	private TextView bindTipTv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new ActivityTemplate().doInActivity(this, R.layout.bind_phone_guide);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("绑定手机——介绍页"); //统计页面
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("绑定手机——介绍页"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息 
		MobclickAgent.onPause(this);
	}

	@SuppressLint("NewApi")
	@Override
	protected void findView() {
		super.findView();
		setTextValue(R.id.title_txt, "绑定手机号码");
		if (null != getIntent().getExtras()) {
			isBind = getIntent().getExtras().getBoolean("isbind", false);
			mobile = getIntent().getExtras().getString("mobile", "");
		}
		isBindTv = (TextView) findViewById(R.id.isbindTv);
		bindTipTv = (TextView) findViewById(R.id.bind_tip_tv);
		bindButton = (Button) findViewById(R.id.bindBt);
		if (isBind) {
			isBindTv.setVisibility(View.VISIBLE);
			isBindTv.setText(getString(R.string.bind_bind) + " " + mobile);
			bindTipTv.setVisibility(View.GONE);
			bindButton.setVisibility(View.GONE);
		} else {
			isBindTv.setText(getString(R.string.bind_nobind));
			bindTipTv.setVisibility(View.VISIBLE);
			bindButton.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void initData() {
		super.initData();
	}

	@Override
	protected void initListener() {
		super.initListener();
		bindButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startActivityForResult(BindPhoneActivity.class, 1);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && requestCode == 1) {
			setResult(RESULT_OK);
			finish();
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
		}
	}
}
