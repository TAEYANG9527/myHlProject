package com.itcalf.renhe.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.context.topic.PostOrEditTopicActivity;
import com.itcalf.renhe.context.wukong.im.ShareWebview;
import com.itcalf.renhe.utils.DeviceUitl;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.emoji.EmojiFragment;
import com.itcalf.renhe.view.emoji.EmojiUtil;
import com.itcalf.renhe.widget.emojitextview.Emotion;
import com.orhanobut.logger.Logger;
import com.umeng.analytics.MobclickAgent;

import org.aisen.android.common.utils.SystemUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wangning
 * @createtime 2016-4-14
 * @功能说明 "话题" 相关专用webview
 */
public class WebViewForIndustryCircle extends BaseActivity implements EmojiFragment.OnEmotionSelectedListener {
    private WebView webView;
    private String url = "", userString = "", isShare = "", sharePic = "", shareTitle = "", shareContent = "";
    private String loginString = "";
    private int actionItemType = -1;//用来标示右上角应该显示发话题还是分享，默认什么都不显示
    private static final String ACTION_ITEM_PUBLISH_TOPIC = "发话题";//右上角，发话题
    private int circleId;//发布话题所需要的circleId
    private boolean isShareable;
    /**
     * 底部评论区域
     */
    private LinearLayout bottomReplyLl;
    private EditText replyEt;
    private ImageButton goReplyIb;
    private ProgressBar replyProgressBar;
    private ImageView imagefaceIv;
    private LinearLayout chatFaceContainer;
    private EmojiFragment emotionFragment;
    private int emotionHeight;
    private EmojiUtil emojiUtil;
    /**
     * 底部评论区域 end
     */

    private static final int REQUEST_CODE_POST_TOPIC = 1;//发布
    private static final int REQUEST_CODE_EDIT_TOPIC = 2;//修改话题
    private static final String TOPIC_DETAIL_URL_FILTER = "topic/detail";
    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadCallbackAboveL;
    private final static int FILE_CHOOSER_RESULT_CODE = 3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ActivityTemplate().doInActivity(this, R.layout.webview_for_industry_circle);
    }

    @Override
    protected void findView() {
        super.findView();
        webView = (WebView) findViewById(R.id.webView1);
        /******* 回复，评论相关 *****************/
        bottomReplyLl = (LinearLayout) findViewById(R.id.bottom_reply_ll);
        bottomReplyLl.setVisibility(View.GONE);
        replyEt = (android.widget.EditText) findViewById(R.id.reply_edt);
        goReplyIb = (ImageButton) findViewById(R.id.gotoReply);
        replyProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        // 表情图标
        imagefaceIv = (ImageView) findViewById(R.id.image_face);
        // 表情布局
        chatFaceContainer = (LinearLayout) findViewById(R.id.chat_face_container);
        emotionFragment = (EmojiFragment) getSupportFragmentManager().findFragmentByTag("EmotionFragemnt");
        if (emotionFragment == null) {
            emotionFragment = EmojiFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.chat_face_container, emotionFragment, "EmotionFragemnt").commit();
        }
        /******* 回复，评论相关 end*****************/
    }

    @Override
    protected void initData() {
        super.initData();
        circleId = getIntent().getIntExtra("circleId", -1);
        webView.getSettings().setJavaScriptEnabled(true);//设置使用够执行JS脚本
        webView.addJavascriptInterface(new JsInteration(), "android");
//        webView.getSettings().setBuiltInZoomControls(true);//设置使支持缩放
        userString = webView.getSettings().getUserAgentString();
        webView.getSettings().setUserAgentString(userString + " RenheApp/" + getResources().getString(R.string.versionname));
        url = this.getIntent().getExtras().getString("url");
        if (!TextUtils.isEmpty(url) && !url.contains("http")) {
            url = "http://" + url;
        }
        loginString = this.getIntent().getExtras().getString("login");
        isShare = this.getIntent().getExtras().getString("type");
        sharePic = this.getIntent().getExtras().getString("picture");
        shareContent = this.getIntent().getExtras().getString("title");
        if (null != isShare && isShare.equals("share")) {
            setTextValue("行业头条");
        } else {
            setTextValue("和聊");
        }
        if (null == loginString) {
            loginString = "";
        }
        url = url + loginString;
        Logger.d("正在打开网址：" + url);
        webView.loadUrl(url);//"http://www.renhe.cn/forgot.html"
        //表情部分初始化
        emojiUtil = new EmojiUtil(this);
        initActionItemView();
    }

    @Override
    protected void initListener() {
        super.initListener();
        webView.setDownloadListener(new MyWebViewDownLoadListener());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!url.startsWith("heliaoapp")) {
                    if (!url.equals(WebViewForIndustryCircle.this.url)) {
                        isShare += "false";
                    }
                    Logger.d("正在进入子网页：" + url);
                    if (!url.endsWith(".jpg") && !url.endsWith(".png")) {
                        WebViewForIndustryCircle.this.url = url;
                    }
                    if (url.contains("download")) {
                        Uri uri = Uri.parse(url);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                        return true;
                    }
                } else {
                    if (url.startsWith(Constants.PUBLIC_FILETER_URL.HELIAO_PROFILE)) {//跳转到用户档案
                        String sid = getUrlPramNameAndValue(url).get("sid");
                        Intent intent = new Intent(WebViewForIndustryCircle.this, MyHomeArchivesActivity.class);
                        intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, sid);
                        startHlActivity(intent);
                        return true;
                    }
                }

                return false;//true表示此事件在此处被处理，不需要再广播
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Logger.d("onPageStarted");
                bottomReplyLl.setVisibility(View.GONE);//开始加载页面的时候先让底部评论框处于隐藏状态
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Logger.d("onPageFinished");
                setTextValue(view.getTitle());
                WebViewForIndustryCircle.this.url = url;
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
            }

            //转向错误时的处理
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//                Toast.makeText(WebViewActWithTitle.this, "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                Logger.d("onReceivedTitle");
                setTextValue(title);
                if (null != isShare && isShare.equals("share")) {
                    shareContent = shareContent == null || "".equals(shareContent) ? title : shareContent;
                } else {
                    shareContent = title;
                }
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
//                if (message != null) {
//                    //弹出对话框
//                    ToastUtil.showToast(WebViewActWithTitle.this, message);
//                }
                if (null != result)
                    result.cancel();    //一定要cancel，否则会出现各种奇怪问题
                return true;
            }
            // For Android 3.0+
            public void openFileChooser(ValueCallback uploadMsg) {
                mUploadMessage = uploadMsg;
//                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//                i.addCategory(Intent.CATEGORY_OPENABLE);
//                i.setType("image/*");
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(i, "请选择图片"), FILE_CHOOSER_RESULT_CODE);
            }

            // For Android 3.0+
            public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
                mUploadMessage = uploadMsg;
//                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//                i.addCategory(Intent.CATEGORY_OPENABLE);
//                i.setType("*/*");
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(
                        Intent.createChooser(i, "请选择文件"),
                        FILE_CHOOSER_RESULT_CODE);
            }

            //For Android 4.1
            public void openFileChooser(ValueCallback uploadMsg, String acceptType, String capture) {
                mUploadMessage = uploadMsg;
//                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//                i.addCategory(Intent.CATEGORY_OPENABLE);
//                i.setType("image/*");
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(i, "请选择图片"), FILE_CHOOSER_RESULT_CODE);
            }

            // For Android 5.0+
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                mUploadCallbackAboveL = filePathCallback;
//                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//                i.addCategory(Intent.CATEGORY_OPENABLE);
//                i.setType("*/*");
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(
                        Intent.createChooser(i, "请选择文件"),
                        FILE_CHOOSER_RESULT_CODE);
                return true;
            }
        });
        //表情部分初始化
        replyEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View arg0, boolean arg1) {
            }
        });
        replyEt.setFilters(new InputFilter[]{emojiUtil.emotionFilter});
        replyEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (chatFaceContainer.isShown()) {
                    hideEmotionView(true);
                }
            }
        });
        // 表情点击按钮
        imagefaceIv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (chatFaceContainer.isShown()) {
                    hideEmotionView(true);
                } else {
                    showEmotionView();
                }
            }
        });
        //表情键盘的点击事件
        emotionFragment.setOnEmotionListener(this);
        goReplyIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(replyEt.getText().toString().trim())) {
                    //回复不能为空改为评论不能为空
                    ToastUtil.showToast(WebViewForIndustryCircle.this, getResources().getString(R.string.renmaiquan_detail));
                } else {
                    goReplyIb.setVisibility(View.GONE);
                    replyProgressBar.setVisibility(View.VISIBLE);
                    appInvokeJsMethodOfReply(replyEt.getText().toString().trim());
                }
            }
        });
        //表情部分初始化 end
//        webView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (isSoftKeyBoardShowed) {
//                    SystemUtils.hideSoftInput(replyEt);
//                    hideEmotionView(false);
//                    return true;
//                }
//                return false;
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("网页链接"); //统计页面
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("网页链接"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
    }

    /**
     * 初始化actionMenu相关的参数
     */
    private void initActionItemView() {
        if (url.contains(TOPIC_DETAIL_URL_FILTER)) {//话题详情，需要显示右上角分享
            actionItemType = 1;
            invalidateOptionsMenu();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_save:
                postTopic();
                return true;
            case R.id.item_share:
                appInvokeJsMethodOfShare(webView);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main, menu);
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem publishItem = menu.findItem(R.id.item_save);
        MenuItem shareItem = menu.findItem(R.id.item_share);
        shareItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        switch (actionItemType) {
            case 1:
                shareItem.setVisible(true);
                break;
            case 2:
                publishItem.setTitle(ACTION_ITEM_PUBLISH_TOPIC);
                publishItem.setVisible(true);
                break;
            default:
                shareItem.setVisible(false);
                publishItem.setVisible(false);
                break;
        }
        return super.onPrepareOptionsMenu(menu);
    }
//
//    @Override
//    //默认点回退键，会退出Activity，需监听按键操作，使回退在WebView内发生
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
//            webView.goBack();
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    @Override
    public void onBackPressed() {
        DeviceUitl.hideSoftInput(replyEt);
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            setResult(RESULT_OK);
            super.onBackPressed();
        }
    }

    @Override
    @Deprecated
    protected Dialog onCreateDialog(int id) {
        return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.waitting).cancelable(false).build();
    }

    @Override
    protected void onDestroy() {
        if (null != webView)
            webView.reload();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_POST_TOPIC:
                    String topicDetailUrl = data.getStringExtra("topicDetailUrl");
//                    if (!TextUtils.isEmpty(topicDetailUrl)) {
//                        actionItemType = 1;
//                        invalidateOptionsMenu();
//                        webView.loadUrl(topicDetailUrl);
//                    } else {
                    webView.loadUrl(url);
//                    }
                    break;
                case REQUEST_CODE_EDIT_TOPIC:
                    onBackPressed();
                    break;
                case FILE_CHOOSER_RESULT_CODE:
                    if (null == mUploadMessage && null == mUploadCallbackAboveL) return;
                    Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
                    if (mUploadCallbackAboveL != null) {
                        onActivityResultAboveL(requestCode, resultCode, data);
                    } else if (mUploadMessage != null) {
                        mUploadMessage.onReceiveValue(result);
                        mUploadMessage = null;
                    }
                    break;
            }
        }
    }

    private void postTopic() {
        if (circleId >= 0) {
            Intent intent = new Intent(this, PostOrEditTopicActivity.class);
            intent.putExtra("circleId", circleId);
            startHlActivityForResult(intent, REQUEST_CODE_POST_TOPIC);
        }
    }

    private void editTheTopic(int topicId, int postId, String title, String content) {
        Intent intent = new Intent(this, PostOrEditTopicActivity.class);
        intent.putExtra("topicId", topicId);
        intent.putExtra("postId", postId);
        intent.putExtra("topicTitle", title);
        intent.putExtra("topicContent", content);
        startHlActivityForResult(intent, REQUEST_CODE_EDIT_TOPIC);
    }

    private void toShare() {
        boolean noNeedShare = false;
        if (null != isShare && isShare.equals("share")) {
            noNeedShare = true;
            //来自行业头条的分享
            shareTitle = "和聊 - 行业头条";
        } else {
            //来自人脉圈网页链接
            shareTitle = "来自人脉圈的分享";
        }
        //传不带登入信息的url
        String newUrl;
        if (url.endsWith(loginString)) {
            newUrl = url.substring(0, url.length() - loginString.length());
        } else {
            newUrl = url;
        }
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(webView.getWindowToken(), 0);
        new ShareWebview(WebViewForIndustryCircle.this, webView, sharePic, shareTitle, shareContent, newUrl, noNeedShare);
    }

    @Override
    public void onEmotionSelected(Emotion emotion) {
        if (null != emotion)
            emojiUtil.onEmotionSelected(emotion, replyEt);
    }

    //显示表情键盘
    private void showEmotionView() {
        emotionHeight = SystemUtils.getKeyboardHeight(this);
        SystemUtils.hideSoftInput(replyEt);
        imagefaceIv.setImageDrawable(getResources().getDrawable(R.drawable.chat_emo_normal_on));
        chatFaceContainer.getLayoutParams().height = emotionHeight;
        chatFaceContainer.setVisibility(View.VISIBLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        replyEt.requestFocus();
    }

    //隐藏表情键盘
    private void hideEmotionView(boolean showKeyBoard) {
        if (showKeyBoard) {
            SystemUtils.showKeyBoard(replyEt);
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        imagefaceIv.setImageDrawable(getResources().getDrawable(R.drawable.chat_emo_normal));
        chatFaceContainer.setVisibility(View.GONE);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent data) {
        if (requestCode != FILE_CHOOSER_RESULT_CODE
                || mUploadCallbackAboveL == null) {
            return;
        }
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {

            } else {
                String dataString = data.getDataString();
                ClipData clipData = data.getClipData();

                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }

                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        mUploadCallbackAboveL.onReceiveValue(results);
        mUploadCallbackAboveL = null;
    }
    /**
     * WebView控制调用相应的WEB页面进行展示。安卓源码当碰到页面有下载链接的时候，点击上去是一点反应都没有的。
     * 原来是因为WebView默认没有开启文件下载的功能，如果要实现文件下载的功能，需要设置WebView的DownloadListener，
     * 通过实现自己的DownloadListener来实现文件的下载。
     */
    class MyWebViewDownLoadListener implements DownloadListener {

        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
                                    long contentLength) {
            Logger.d("url=" + url);
            Logger.d("userAgent=" + userAgent);
            Logger.d("contentDisposition=" + contentDisposition);
            Logger.d("mimetype=" + mimetype);
            Logger.d("contentLength=" + contentLength);
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }

    /**
     * 话题详情评论
     * app调用js代码
     * webView调用js的基本格式为webView.loadUrl(“javascript:methodName(parameterValues)”)
     */
    private void appInvokeJsMethodOfReply(String content) {
        String call = "javascript:addReplyTopic(\"" + content + "\")";
        webView.loadUrl(call);
        SystemUtils.hideSoftInput(replyEt);
        hideEmotionView(false);
        replyEt.setText("");
        goReplyIb.setVisibility(View.VISIBLE);
        replyProgressBar.setVisibility(View.GONE);
    }

    /**
     * app调用js有参数有返回值的函数
     * Android 4.4之后使用evaluateJavascript即可。
     *
     * @param webView
     */
    private void appInvokeJsMethodOfShare(WebView webView) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            webView.evaluateJavascript("isShare()", new ValueCallback<String>() {
//                @Override
//                public void onReceiveValue(String value) {
//                    if (null != value) {
//                        try {
//                            isShareable = Boolean.parseBoolean(value);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        Logger.e("isShare++" + isShareable);
//                    } else {
//                        isShareable = false;
//                    }
//                    if (isShareable) {
//                        toShare();
//                    } else {
//                        ToastUtil.showToast(WebViewForIndustryCircle.this, getString(R.string.topic_is_unshareable));
//                    }
//                }
//            });
//        }
        String call = "javascript:isShare()";
        webView.loadUrl(call);
    }

    /**
     * Java和js交互
     */
    public class JsInteration {
        //## H5中js调用APP中的方法定义

        // 1、发布话题
        @JavascriptInterface
        public void sendTopic(int circleId) {
            Logger.e("sendTopic++" + circleId);
            WebViewForIndustryCircle.this.circleId = circleId;
            postTopic();
        }

        // 2、修改话题
        @JavascriptInterface
        public void editTopic(int topicId, int postId, String title, String content) {
            Logger.e("editTopic++" + topicId);
            editTheTopic(topicId, postId, title, content);
            DeviceUitl.hideSoftInput(replyEt);
        }

        //3、要不要显示回复框
        @JavascriptInterface
        public void isShowReply(final boolean showReply) {
            Logger.e("isShowReply++" + showReply);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (showReply)
                        bottomReplyLl.setVisibility(View.VISIBLE);
                    else
                        bottomReplyLl.setVisibility(View.GONE);
                }
            });
        }

        //4、右侧导航显示内容 注解：1、显示分享
        @JavascriptInterface
        public void showNavigation(int type) {
            Logger.e("showNavigation++" + type);
            if (type >= 0) {
                actionItemType = type;
                invalidateOptionsMenu();
            }
        }

        //5、详情页删除话题回退到列表页
        @JavascriptInterface
        public void rh_GoBack() {
            Logger.e("rh_GoBack++");
            DeviceUitl.hideSoftInput(replyEt);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onBackPressed();
                }
            });
        }

        //6、能不能分享
        @JavascriptInterface
        public void isAndroidShare(boolean flag) {
            Logger.e("isShareable++" + flag);
            isShareable = flag;
            if (isShareable) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toShare();
                    }
                });
            } else {
                ToastUtil.showToast(WebViewForIndustryCircle.this, getString(R.string.topic_is_unshareable));
            }
        }

        // ## APP中调用H5js
        //1、回复话题内容
        @JavascriptInterface
        public void addReplyTopic(String content) {
            Logger.e("addReplyTopic++" + content);
        }

        //2、能不能分享
        @JavascriptInterface
        public void isShare() {

        }

    }

    private Map<String, String> getUrlPramNameAndValue(String url) {
        String regEx = "(\\?|&+)(.+?)=([^&]*)";//匹配参数名和参数值的正则表达式
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(url);
        // LinkedHashMap是有序的Map集合，遍历时会按照加入的顺序遍历输出
        Map<String, String> paramMap = new LinkedHashMap<String, String>();
        while (m.find()) {
            String paramName = m.group(2);//获取参数名
            String paramVal = m.group(3);//获取参数值
            paramMap.put(paramName, paramVal);
        }
        return paramMap;
    }
}