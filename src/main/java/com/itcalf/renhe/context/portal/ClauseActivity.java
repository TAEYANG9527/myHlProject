package com.itcalf.renhe.context.portal;

import android.os.Bundle;
import android.webkit.WebView;
import android.widget.Button;

import com.itcalf.renhe.R;
import com.itcalf.renhe.context.template.BaseActivity;
import com.umeng.analytics.MobclickAgent;

public class ClauseActivity extends BaseActivity {
	private Button mBackBt;
	//private TextView mInfoTv;
	private WebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setMyContentView(R.layout.portal_clause);
		//		setTitle("人和网服务条款");
		setTextValue(1, getString(R.string.hl_terms_service));
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(getString(R.string.hl_terms_service)); //统计页面
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(getString(R.string.hl_terms_service)); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息 
		MobclickAgent.onPause(this);
	}
	@Override
	protected void findView() {
		super.findView();
		mBackBt = (Button) findViewById(R.id.backBt);
		//mInfoTv = (TextView) findViewById(R.id.infoTv);
		webView = (WebView) findViewById(R.id.webview);
	}
	@Override
	protected void initData() {
		super.initData();
		//webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY) ;

		switch (getWindowManager().getDefaultDisplay().getWidth()) {
		case 480:
			webView.loadUrl("file:///android_asset/local_800.html");
			break;
		case 320:
			webView.loadUrl("file:///android_asset/local_480.html");
			break;
		case 240:
			webView.loadUrl("file:///android_asset/local_320.html");
			break;
		default:
			webView.loadUrl("file:///android_asset/local.html");
			break;
		}

	}
	@Override
	protected void initListener() {
		super.initListener();
	}
}
