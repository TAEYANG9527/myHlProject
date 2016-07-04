package com.itcalf.renhe.context.more;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;

import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.utils.AsyncImageLoader;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.SwitchButton;
import com.umeng.analytics.MobclickAgent;

public class SettingAllActivity extends BaseActivity implements OnCheckedChangeListener, OnClickListener {
	private SwitchButton smoothDragCB;
	private SwitchButton exitClearCacheCB;
	private RelativeLayout nowClearCacheRl;
	SharedPreferences userInfo;
	Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new ActivityTemplate().doInActivity(this, R.layout.setting_toall);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("设置——通用"); //统计页面
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("设置——通用"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息 
		MobclickAgent.onPause(this);
	}

	@Override
	protected void findView() {
		super.findView();
		userInfo = getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0);
		editor = userInfo.edit();
		smoothDragCB = (SwitchButton) findViewById(R.id.smoothdrag_cb);
		exitClearCacheCB = (SwitchButton) findViewById(R.id.exit_clearcache_cb);
		nowClearCacheRl = (RelativeLayout) findViewById(R.id.now_clearcache_rl);
		if (userInfo.getBoolean("fastdrag", false)) {
			smoothDragCB.setChecked(true);
		} else {
			smoothDragCB.setChecked(false);
		}
		if (userInfo.getBoolean("clearcache", false)) {
			exitClearCacheCB.setChecked(true);
		} else {
			exitClearCacheCB.setChecked(false);
		}
	}

	@Override
	protected void initData() {
		super.initData();
		setTextValue(R.id.title_txt, "通用设置");
	}

	@Override
	protected void initListener() {
		super.initListener();
		smoothDragCB.setOnCheckedChangeListener(this);
		exitClearCacheCB.setOnCheckedChangeListener(this);
		nowClearCacheRl.setOnClickListener(this);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.smoothdrag_cb:
			if (isChecked) {
				editor.putBoolean("fastdrag", true);
			} else {
				editor.putBoolean("fastdrag", false);
			}
			break;
		case R.id.exit_clearcache_cb:
			if (isChecked) {
				editor.putBoolean("clearcache", true);
			} else {
				editor.putBoolean("clearcache", false);
			}
			break;
		default:
			break;
		}
		editor.commit();
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.now_clearcache_rl:
			clearCache();
			break;
		default:
			break;
		}
	}

	private void clearCache() {
		AsyncImageLoader.getInstance().clearCache();
		CacheManager.getInstance().populateData(this)
				.clearCache(((RenheApplication) getApplicationContext()).getUserInfo().getEmail(), true);
		ToastUtil.showToast(this, "清除成功!");
	}
}
