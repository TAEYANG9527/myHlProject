package com.itcalf.renhe.context.room;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.template.BaseActivity;

/**
 * Created by wangning on 2015/8/25.
 */
public class ShareToWeichatSettingActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {
	private SwitchCompat shareToWeichatTb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getTemplate().doInActivity(this, R.layout.share_to_weichat_setting);
		setTextValue(getString(R.string.renmaiquan_publish_share_to_weichat_tip));
	}

	@Override
	protected void findView() {
		super.findView();
		shareToWeichatTb = (SwitchCompat) findViewById(R.id.share_to_weichat_tb);
	}

	@Override
	protected void initData() {
		super.initData();
		if (RenheApplication.getInstance().getUserSharedPreferences().getBoolean(Constants.SHAREDPREFERENCES_KEY.RENMAIQUAN_SYNC_TO_WEICHAT, false)) {
			shareToWeichatTb.setChecked(true);
		} else {
			shareToWeichatTb.setChecked(false);
		}
	}

	@Override
	protected void initListener() {
		super.initListener();
		shareToWeichatTb.setOnCheckedChangeListener(this);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.share_to_weichat_tb:
			if (isChecked) {
				RenheApplication.getInstance().getUserEditor().putBoolean(Constants.SHAREDPREFERENCES_KEY.RENMAIQUAN_SYNC_TO_WEICHAT, true);
			} else {
				RenheApplication.getInstance().getUserEditor().putBoolean(Constants.SHAREDPREFERENCES_KEY.RENMAIQUAN_SYNC_TO_WEICHAT, false);
			}
			break;
		default:
			break;
		}
		RenheApplication.getInstance().getUserEditor().commit();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		setResult(RESULT_OK);
	}
}
