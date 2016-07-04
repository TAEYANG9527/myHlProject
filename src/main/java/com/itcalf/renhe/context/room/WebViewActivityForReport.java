package com.itcalf.renhe.context.room;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.umeng.analytics.MobclickAgent;

/**
 * Title: WebViewActivity.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-8-15 下午2:02:50 <br>
 *
 * @author wangning
 */
public class WebViewActivityForReport extends BaseActivity {
    private WebView webView;
    private ProgressBar loadingPb;
    private String userId;
    private String msgId;
    private String msgObjectId;
    private String userString = "";
    private int type;// 1为举报人脉圈 2为举报人

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ActivityTemplate().doInActivity(this, R.layout.webview);
        webView = (WebView) findViewById(R.id.webView1);
        loadingPb = (ProgressBar) findViewById(R.id.loading_progressbar);
        webView.getSettings().setJavaScriptEnabled(true);//设置使用够执行JS脚本
        webView.getSettings().setBuiltInZoomControls(true);//设置使支持缩放
        userString = webView.getSettings().getUserAgentString();
        webView.getSettings().setUserAgentString(userString + " RenheApp/" + getResources().getString(R.string.versionname));
        setTextValue(R.id.title_txt, "举报");
        userId = getIntent().getStringExtra("sid");
        msgId = getIntent().getStringExtra("entityId");
        msgObjectId = getIntent().getStringExtra("entityObjectId");
        type = getIntent().getIntExtra("type", 1);
        webView.loadUrl("http://m.renhe.cn/spamview.shtml?sid=" + userId + "&type=" + type + "&entityId=" + msgId + "&entityObjectId="
                + msgObjectId + "&fromSid=" + RenheApplication.getInstance().getUserInfo().getSid());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);// 使用当前WebView处理跳转
                return true;//true表示此事件在此处被处理，不需要再广播
            }

            @Override
            //转向错误时的处理
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(WebViewActivityForReport.this, "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                setTextValue(title);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    loadingPb.setVisibility(View.GONE);
                } else {
                    if (loadingPb.getVisibility() == View.GONE)
                        loadingPb.setVisibility(View.VISIBLE);
                    loadingPb.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("举报/屏蔽"); //统计页面
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("举报/屏蔽"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
    }

    @Override
    //默认点回退键，会退出Activity，需监听按键操作，使回退在WebView内发生
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
