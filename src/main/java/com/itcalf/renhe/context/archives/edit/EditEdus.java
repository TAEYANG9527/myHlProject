package com.itcalf.renhe.context.archives.edit;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.Profile;
import com.itcalf.renhe.dto.Profile.UserInfo.EduExperienceInfo;
import com.umeng.analytics.MobclickAgent;

public class EditEdus extends BaseActivity {
	private LinearLayout edusLl;
	private RelativeLayout addEdusRl;
	private EduExperienceInfo[] eduExperienceInfos;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new ActivityTemplate().doInActivity(this, R.layout.archieve_edit_edus);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("教育经历");
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("教育经历");
		MobclickAgent.onPause(this);
	}

	@Override
	protected void findView() {
		super.findView();
		edusLl = (LinearLayout) findViewById(R.id.edus_ll);
		addEdusRl = (RelativeLayout) findViewById(R.id.add_edus_rl);
	}

	@Override
	protected void initData() {
		super.initData();
		setTextValue(R.id.title_txt, "教育经历");
		Profile profile = (Profile) getIntent().getSerializableExtra("Profile");
		if (profile != null) {
			populateData(profile);
		}
	}

	private void populateData(Profile result) {
		edusLl.removeAllViews();
		eduExperienceInfos = result.getUserInfo().getEduExperienceInfo();
		if (eduExperienceInfos != null && eduExperienceInfos.length > 0) {
			edusLl.setVisibility(View.VISIBLE);
			int eduNum = eduExperienceInfos.length;
			for (int i = 0; i < eduNum; i++) {
				final EduExperienceInfo eduInfo = eduExperienceInfos[i];
				View view = LayoutInflater.from(EditEdus.this).inflate(R.layout.edu_item, null);
				RelativeLayout eduItemRl = (RelativeLayout) view.findViewById(R.id.edu_item_rl);
				TextView schoolTv = (TextView) view.findViewById(R.id.school_tv);
				TextView timeIfoTv = (TextView) view.findViewById(R.id.time_info_tv);
				final String school = eduInfo.getSchoolName();
				if (!TextUtils.isEmpty(school)) {
					schoolTv.setText(school);
				}
				String time = eduInfo.getTimeInfo();
				if (!TextUtils.isEmpty(time)) {
					timeIfoTv.setText(time);
				}
				eduItemRl.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(EditEdus.this, EditEdusItem.class);
						intent.putExtra("eduInfo", eduInfo);
						intent.putExtra("isAdd", false);
						startActivityForResult(intent, 0);
						overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
					}
				});
				if (i == eduNum - 1) {
					view.findViewById(R.id.separate_line).setVisibility(View.INVISIBLE);
				}
				edusLl.addView(view);
			}
		} else {
            edusLl.setVisibility(View.GONE);
        }
	}

	@Override
	protected void initListener() {
		super.initListener();
		addEdusRl.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(EditEdus.this, EditEdusItem.class);
				intent.putExtra("isAdd", true);
				startActivityForResult(intent, 0);
				overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case 0:
			if (resultCode == RESULT_OK) {
				Profile pf = (Profile) data.getSerializableExtra("Profile");
				if (pf != null) {
					populateData(pf);
				}
			}
			break;
		default:
			break;
		}
	}
}
