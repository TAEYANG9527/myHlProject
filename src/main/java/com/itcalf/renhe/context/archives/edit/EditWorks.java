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
import com.itcalf.renhe.dto.Profile.UserInfo.WorkExperienceInfo;
import com.umeng.analytics.MobclickAgent;

public class EditWorks extends BaseActivity {
	private LinearLayout worksLl;
	private RelativeLayout addWorksRl;
	private WorkExperienceInfo[] workExperienceInfos;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new ActivityTemplate().doInActivity(this, R.layout.archieve_edit_works);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("工作经历");
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("工作经历");
		MobclickAgent.onPause(this);
	}

	@Override
	protected void findView() {
		super.findView();
		worksLl = (LinearLayout) findViewById(R.id.works_ll);
		addWorksRl = (RelativeLayout) findViewById(R.id.add_works_rl);
	}

	@Override
	protected void initData() {
		super.initData();
		setTextValue(R.id.title_txt, "工作经历");
		Profile profile = (Profile) getIntent().getSerializableExtra("Profile");
		if (profile != null) {
			populateData(profile);
		}
	}

	private void populateData(final Profile result) {
		workExperienceInfos = result.getUserInfo().getWorkExperienceInfo();
		worksLl.removeAllViews();
		if (workExperienceInfos != null && workExperienceInfos.length > 0) {
			worksLl.setVisibility(View.VISIBLE);
			int workNum = workExperienceInfos.length;
			for (int i = 0; i < workNum; i++) {
				final WorkExperienceInfo workInfo = workExperienceInfos[i];
				View view = LayoutInflater.from(EditWorks.this).inflate(R.layout.works_item, null);
				RelativeLayout workItemRl = (RelativeLayout) view.findViewById(R.id.work_item_rl);
				TextView companyTv = (TextView) view.findViewById(R.id.company_tv);
				TextView timeIfoTv = (TextView) view.findViewById(R.id.time_info_tv);
				final String company = workInfo.getCompany();
				if (!TextUtils.isEmpty(company)) {
					companyTv.setText(company);
				}
				String time = workInfo.getTimeInfo();
				if (!TextUtils.isEmpty(time)) {
					timeIfoTv.setText(time);
				}
				workItemRl.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(EditWorks.this, EditWorksItem.class);
						intent.putExtra("workInfo", workInfo);
						intent.putExtra("isAdd", false);
						intent.putExtra("simple", false);//只编辑公司和职位
						startActivityForResult(intent, 0);
						overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
					}
				});
				if (i == workNum - 1) {
                    view.findViewById(R.id.separate_line).setVisibility(View.INVISIBLE);
				}
				worksLl.addView(view);
			}
		} else {
			worksLl.setVisibility(View.GONE);
		}
	}

	@Override
	protected void initListener() {
		super.initListener();
		addWorksRl.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(EditWorks.this, EditWorksItem.class);
				intent.putExtra("isAdd", true);
				intent.putExtra("simple", false);//只编辑公司和职位
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
