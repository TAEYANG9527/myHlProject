package com.itcalf.renhe.context.more;

import android.os.Bundle;
import android.webkit.WebView;
import android.widget.Button;

import com.itcalf.renhe.R;
import com.itcalf.renhe.context.template.BaseActivity;
import com.umeng.analytics.MobclickAgent;

public class ContactAuthClauseActivity extends BaseActivity {
	private Button mBackBt;
	//private TextView mInfoTv;
	private WebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setMyContentView(R.layout.portal_clause);
		//		setTitle("免责声明");
		setTextValue(1, "免责声明");
		findView();
		initData();
		initListener();
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("免责声明"); //统计页面
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("免责声明"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息 
		MobclickAgent.onPause(this);
	}

	protected void findView() {
		mBackBt = (Button) findViewById(R.id.backBt);
		//mInfoTv = (TextView) findViewById(R.id.infoTv);
		webView = (WebView) findViewById(R.id.webview);
	}

	protected void initData() {
		//webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY) ;

		//		switch (getWindowManager().getDefaultDisplay().getWidth()) {
		//		case 480:
		//			webView.loadUrl("file:///android_asset/local_800.html");
		//			break;
		//		case 320:
		//			webView.loadUrl("file:///android_asset/local_480.html");
		//			break;
		//		case 240:
		//			webView.loadUrl("file:///android_asset/local_320.html");
		//			break;
		//		default:
		webView.loadUrl("file:///android_asset/Disclaimer.html");
		//			break;
		//		}

	}

	protected void initListener() {
	}
}
