package com.itcalf.renhe.context.contacts;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.view.ProgressWebView;
import com.umeng.analytics.MobclickAgent;

/**
 * @author chan
 * @createtime 2014-11-24
 * @功能说明 网页链接展示在webview里面，带提取标题
 */
public class MailBoxWebView extends BaseActivity {
    private WebView webView;
    private ProgressBar loadingPb;
    String url = "", userString = "";
    String loginString = "";
    private boolean isFirst = false;//解决onPageFinished返回多次

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RenheApplication.getInstance().addActivity(this);
        new ActivityTemplate().doInActivity(this, R.layout.webview);

        webView = (WebView) findViewById(R.id.webView1);
        loadingPb = (ProgressBar) findViewById(R.id.loading_progressbar);
        webView.getSettings().setJavaScriptEnabled(true);//设置使用够执行JS脚本
        webView.getSettings().setBuiltInZoomControls(true);//设置使支持缩放

        userString = webView.getSettings().getUserAgentString();
        webView.getSettings().setUserAgentString(userString + " RenheApp/" + getResources().getString(R.string.versionname));

        //		url = this.getIntent().getExtras().getString("url");
        String sid = RenheApplication.getInstance().getUserInfo().getSid();
        String adSid = RenheApplication.getInstance().getUserInfo().getAdSId();
        if (Constants.DEBUG_MODE) {
            url = "http://m.renhe.cn/userLoginAuthTest.shtml?url=register/appImport.shtml";
        } else {
            url = "http://m.renhe.cn/userLoginAuth.shtml?url=register/appImport.shtml";
        }
        url = url + "&sid=" + sid + "&adSId=" + adSid;

        setTextValue(1, "从邮箱导入");
        webView.loadUrl(url);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);// 使用当前WebView处理跳转
                return true;//true表示此事件在此处被处理，不需要再广播
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (url.startsWith("http://m.renhe.cn/register/sendemail.shtml") && !isFirst) {
                    isFirst = true;
                    //导入成功，跳转到获取联系人列表界面
                    Intent intent = new Intent();
                    intent.setClass(MailBoxWebView.this, MailBoxList.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
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
        MobclickAgent.onPageStart("邮箱导入网页链接"); //统计页面
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("邮箱导入网页链接"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
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
