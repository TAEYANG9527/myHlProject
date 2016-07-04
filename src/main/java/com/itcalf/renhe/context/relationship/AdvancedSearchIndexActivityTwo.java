package com.itcalf.renhe.context.relationship;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.contacts.MailBoxList;
import com.itcalf.renhe.context.contacts.MobileMailList;
import com.itcalf.renhe.context.contacts.SearchForContactsActivity;
import com.itcalf.renhe.context.more.TwoDimencodeActivity;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.utils.LoggerFileUtil;
import com.itcalf.renhe.zxing.ui.ActivityScan;

public class AdvancedSearchIndexActivityTwo extends BaseActivity {

	private Context mContext;
	private String selfTwoDimenCodeUrl;
	private Dialog mAlertDialog;
	private ImageView dialogAvatarIv, dialogTwoCodeIv;
	private TextView dialogNameTv, dialogJobTv, dialogAddressTv, despTv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		RenheApplication.getInstance().addActivity(this);
		new ActivityTemplate().doInActivity(this, R.layout.advanced_search_index_two);
		setTextValue(1, "添加人脉");
		mContext = this;
	}

	@Override
	protected void initListener() {
		super.initListener();

		findViewById(R.id.ly_search).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(mContext, SearchForContactsActivity.class));
			}
		});

		findViewById(R.id.ly_qrcode).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				Intent intent = new Intent(AdvancedSearchIndexActivityTwo.this, TwoDimencodeActivity.class);
				intent.putExtra("type", TwoDimencodeActivity.SHARE_TYPE_ARCHIVE);
				startHlActivity(intent);
				overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			}
		});

		findViewById(R.id.ly_scan).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(AdvancedSearchIndexActivityTwo.this, ActivityScan.class);
				intent.putExtra("state", 1);
				startHlActivity(intent);
				//和聊统计
				String content = "5.134" + LoggerFileUtil.getConstantInfo(AdvancedSearchIndexActivityTwo.this) + "|" + 3;
				LoggerFileUtil.writeFile(content, true);
			}
		});

		findViewById(R.id.ly_mobile).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startHlActivity(new Intent(mContext, MobileMailList.class));
				//和聊统计
				String content = "5.138" + LoggerFileUtil.getConstantInfo(AdvancedSearchIndexActivityTwo.this);
				LoggerFileUtil.writeFile(content, true);
			}
		});

		findViewById(R.id.ly_mailBox).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startHlActivity(new Intent(mContext, MailBoxList.class));
				//和聊统计
				String content = "5.140" + LoggerFileUtil.getConstantInfo(AdvancedSearchIndexActivityTwo.this);
				LoggerFileUtil.writeFile(content, true);
			}
		});

		findViewById(R.id.ly_nearby).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//				ToastUtil.showToast(mContext, "附近人");
				startHlActivity(new Intent(mContext, NearbyActivity.class));
				//和聊统计
				String content = "5.142" + LoggerFileUtil.getConstantInfo(AdvancedSearchIndexActivityTwo.this);
				LoggerFileUtil.writeFile(content, true);
			}
		});

	}
}
