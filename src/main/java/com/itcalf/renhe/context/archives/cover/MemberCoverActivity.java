package com.itcalf.renhe.context.archives.cover;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.MemberCoverPicGridAdapter;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.room.ConverViewPhotoActivity;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.MemberCover;
import com.itcalf.renhe.view.NoScrollGridView;
import com.umeng.analytics.MobclickAgent;

import java.util.concurrent.Executors;

/**
 * Title: MemberCoverActivity.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-11-13 下午4:59:39 <br>
 * @author wangning
 */
public class MemberCoverActivity extends BaseActivity {
	private NoScrollGridView gridView;
	private final static int REQUEST_CODE_UPDATE_MEMBER_COVER = 2014;
	private TextView descTv;
	private ImageView avartarIv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new ActivityTemplate().doInActivity(this, R.layout.member_cover);
	}

	@Override
	protected void findView() {
		super.findView();
		setTextValue(1, "更换档案封面");
		gridView = (NoScrollGridView) findViewById(R.id.Gridview);
		descTv = (TextView) findViewById(R.id.desc_tv);
		avartarIv = (ImageView) findViewById(R.id.avatar_img);
		//		descTv.setText("\"摄影于我，是一种激情，一种生活方式\"");
	}

	@Override
	protected void initData() {
		super.initData();
		initCoverGridView();
	}

	private void initCoverGridView() {
		new GetMemberCoverListTask(this) {
			public void doPre() {
			};

			public void doPost(com.itcalf.renhe.dto.MemberCoverOperation result) {
				if (null != result && result.getState() == 1 && null != result.getProfileCoverList()) {
					MemberCover[] memberCovers = result.getProfileCoverList();
					MemberCoverPicGridAdapter adapter = new MemberCoverPicGridAdapter(MemberCoverActivity.this,
							result.getProfileCoverList());
					gridView.setAdapter(adapter);
					final CharSequence[] middlePics = new CharSequence[memberCovers.length];
					for (int i = 0; i < memberCovers.length; i++) {
						middlePics[i] = memberCovers[i].getCover() + "#" + memberCovers[i].getId();
					}
					if (null != middlePics && middlePics.length > 0) {
						gridView.setOnItemClickListener(new OnItemClickListener() {

							@Override
							public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
								Intent intent = new Intent(MemberCoverActivity.this, ConverViewPhotoActivity.class);
								intent.putExtra("ID", arg2);
								intent.putExtra("middlePics", middlePics);
								intent.putExtra("isTocover", true);
								//								startActivity(intent);
								startActivityForResult(intent, REQUEST_CODE_UPDATE_MEMBER_COVER);
								overridePendingTransition(R.anim.zoom_enter, 0);
							}
						});
					}
				}
			};
		}.executeOnExecutor(Executors.newCachedThreadPool(), RenheApplication.getInstance().getUserInfo().getSid(),
				RenheApplication.getInstance().getUserInfo().getAdSId());
	}

	@Override
	protected void initListener() {
		super.initListener();
		avartarIv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(MemberCoverActivity.this, MyHomeArchivesActivity.class);
				intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, "27cff3129b4d8020");//跳到会员朱文新的档案
				startActivity(intent);
				overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("更换档案封面——会员作品"); //统计页面
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("更换档案封面——会员作品"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息 
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_CODE_UPDATE_MEMBER_COVER) {
				if (null == data) {
					data = new Intent();
				}
				data.putExtra("isFromMemberCover", true);
				setResult(RESULT_OK, data);
				finish();
			}
		}
	}
}
