package com.itcalf.renhe.context.more;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.view.SwitchButton;
import com.umeng.analytics.MobclickAgent;

public class SettingGPRSActivity extends BaseActivity implements OnCheckedChangeListener {
	private SwitchButton roomShowPicCB;
	SharedPreferences userInfo;
	Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new ActivityTemplate().doInActivity(this, R.layout.setting_gprs);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("设置——流量控制"); //统计页面
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("设置——流量控制"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息 
		MobclickAgent.onPause(this);
	}

	@Override
	protected void findView() {
		super.findView();
		userInfo = getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0);
		editor = userInfo.edit();
		roomShowPicCB = (SwitchButton) findViewById(R.id.room_showpic_cb);
		if (userInfo.getBoolean("roomshowpic", true)) {
			roomShowPicCB.setChecked(true);
		} else {
			roomShowPicCB.setChecked(false);
		}
	}

	@Override
	protected void initData() {
		super.initData();
		setTextValue(R.id.title_txt, "流量控制");
	}

	@Override
	protected void initListener() {
		super.initListener();
		roomShowPicCB.setOnCheckedChangeListener(this);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.room_showpic_cb:
			if (isChecked) {
				editor.putBoolean("roomshowpic", true);
			} else {
				editor.putBoolean("roomshowpic", false);
			}
			break;
		default:
			break;
		}
		editor.commit();
	}
}
