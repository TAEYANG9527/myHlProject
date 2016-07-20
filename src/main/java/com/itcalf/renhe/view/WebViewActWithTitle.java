package com.itcalf.renhe.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.growingio.android.sdk.collection.GrowingIO;
import com.itcalf.renhe.R;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.context.wukong.im.ShareWebview;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.orhanobut.logger.Logger;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * @author chan
 * @createtime 2014-11-24
 * @功能说明 网页链接展示在webview里面，带提取标题
 */
public class WebViewActWithTitle extends BaseActivity {
    private RelativeLayout rootRl;
    private WebView webView;
    private ProgressBar loadingPb;
    String url = "", userString = "", isShare = "", sharePic = "", shareTitle = "", shareContent = "";
    String loginString = "";
    private PopupWindow pop;
    private View popView;
    private ListView listView;
    private List<PupBean> list;
    private PupAdapter pupAdapter;
    private PupBean shareBean;
    private PupBean openWithBrowserBean;
    private static final String FORBID_BROWSER_URL = "hecaifu.com";//该URL不支持外部浏览器打开
    private static final String FORBID_BROWSER_URL_ZANFUWU = "zanfuwu.com";//该URL不支持外部浏览器打开
    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadCallbackAboveL;
    private final static int FILE_CHOOSER_RESULT_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ActivityTemplate().doInActivity(this, R.layout.webview);
        rootRl = (RelativeLayout) findViewById(R.id.rootrl);
        webView = (WebView) findViewById(R.id.webView1);
        loadingPb = (ProgressBar) findViewById(R.id.loading_progressbar);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);//设置使用够执行JS脚本
        webSettings.setBuiltInZoomControls(true);//设置使支持缩放

        userString = webView.getSettings().getUserAgentString();
        webSettings.setUserAgentString(userString + " RenheApp/" + getResources().getString(R.string.versionname));
//        webView.setWebViewClient(new WebViewClient());
        webView.setDownloadListener(new MyWebViewDownLoadListener());
        /**
         * webview开启定位 start
         */
        //启用数据库
        webSettings.setDatabaseEnabled(true);
        //设置定位的数据库路径
        String dir = this.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        if (!TextUtils.isEmpty(dir))
            webSettings.setGeolocationDatabasePath(dir);
        //启用地理定位
        webSettings.setGeolocationEnabled(true);
        //开启DomStorage缓存
        webSettings.setDomStorageEnabled(true);
        /**
         * webview开启定位 end
         */
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
        WebChromeClient webChromeClient = new WebChromeClient() {

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
                if (newProgress == 100) {
                    loadingPb.setVisibility(View.GONE);
                } else {
                    if (loadingPb.getVisibility() == View.GONE)
                        loadingPb.setVisibility(View.VISIBLE);
                    loadingPb.setProgress(newProgress);
                }
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

            /**
             * 开启定位功能
             */
            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                super.onReceivedIcon(view, icon);
            }

            @Override
            public void onGeolocationPermissionsHidePrompt() {
                super.onGeolocationPermissionsHidePrompt();
            }

            //            @Override
//            public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
//                callback.invoke(origin, true, false);//注意个函数，第二个参数就是是否同意定位权限，第三个是是否希望内核记住
//                super.onGeolocationPermissionsShowPrompt(origin, callback);
//            }
            @Override
            public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
//                callback.invoke(origin, true, false);
//                super.onGeolocationPermissionsShowPrompt(origin, callback);
                final boolean remember = false;
                materialDialogsUtil.getBuilder("地理位置授权", "允许" + origin + "访问你当前的地理位置信息？", R.string.material_dialog_sure, R.string.material_dialog_cancel)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                callback.invoke(origin, true, remember);
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                callback.invoke(origin, false, remember);
                            }
                        }).cancelable(true);
                materialDialogsUtil.show();
            }
        };
        GrowingIO.trackWebView(webView, webChromeClient);
        if (null == loginString) {
            loginString = "";
        }
        url = url + loginString;
        Logger.d("正在打开网址：" + url);
        webView.loadUrl(url);//"http://www.renhe.cn/forgot.html"
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!url.startsWith("heliaoapp")) {
                    if (!url.equals(WebViewActWithTitle.this.url)) {
                        isShare += "false";
                    }
                    Logger.d("正在进入子网页：" + url);
                    if ((url.contains(FORBID_BROWSER_URL) || url.contains(FORBID_BROWSER_URL_ZANFUWU)) && !list.contains(shareBean)) {
                        list.add(0, shareBean);
                    }
                    if (!url.contains(FORBID_BROWSER_URL) && !url.contains(FORBID_BROWSER_URL_ZANFUWU) && !list.contains(openWithBrowserBean)) {
                        list.add(openWithBrowserBean);
                    } else if ((url.contains(FORBID_BROWSER_URL) || url.contains(FORBID_BROWSER_URL_ZANFUWU)) && list.contains(openWithBrowserBean)) {
                        list.remove(openWithBrowserBean);
                    }
                    pupAdapter.notifyDataSetChanged();
//                    view.loadUrl(url);// 使用当前WebView处理跳转
                    if (!url.endsWith(".jpg") && !url.endsWith(".png")) {
                        WebViewActWithTitle.this.url = url;
                    }
                    if (url.contains("download")) {
                        Uri uri = Uri.parse(url);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                        return true;
                    }
                }

                return false;//true表示此事件在此处被处理，不需要再广播
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Logger.d("onPageStarted");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Logger.d("onPageFinished");
                setTextValue(view.getTitle());
                WebViewActWithTitle.this.url = url;
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
        webView.setWebChromeClient(webChromeClient);

        boolean isShareable = getIntent().getBooleanExtra("shareable", true);
        pupAdapter = new PupAdapter();
        list = new ArrayList<>();
        shareBean = new PupBean("分享", R.drawable.archive_more_share_friend);
        openWithBrowserBean = new PupBean("在浏览器中打开", R.drawable.icon_browser);
        if (isShareable || url.contains(FORBID_BROWSER_URL) || url.contains(FORBID_BROWSER_URL_ZANFUWU)) {
            list.add(shareBean);
        }
        if (!url.contains(FORBID_BROWSER_URL) && !url.contains(FORBID_BROWSER_URL_ZANFUWU)) {
            list.add(openWithBrowserBean);
        }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_share:
                MobclickAgent.onEvent(WebViewActWithTitle.this, "webview_share");
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
                new ShareWebview(WebViewActWithTitle.this, webView, sharePic, shareTitle, shareContent, newUrl, noNeedShare);
                break;
            case R.id.archive_more:
                if (pop == null) {
                    createPopupwindow();
                }
                if (pop.isShowing()) {
                    pop.dismiss();
                    return true;
                }
                Rect frame = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
                int statusBarHeight = frame.top;
                pop.showAtLocation(popView, Gravity.RIGHT | Gravity.TOP, 20, getSupportActionBar().getHeight() + statusBarHeight);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem moreItem = menu.findItem(R.id.archive_more);
        moreItem.setTitle("更多");
        moreItem.setVisible(true);
        return super.onPrepareOptionsMenu(menu);
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

    @Override
    @Deprecated
    protected Dialog onCreateDialog(int id) {
        return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.waitting).cancelable(false).build();
    }

    class PupBean {
        public String title;
        public int icon;

        public PupBean(String title, int icon) {
            this.title = title;
            this.icon = icon;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getIcon() {
            return icon;
        }

        public void setIcon(int icon) {
            this.icon = icon;
        }
    }

    class PupAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(WebViewActWithTitle.this).inflate(R.layout.item_popup_add_layout, parent, false);
            TextView textView = (TextView) convertView.findViewById(R.id.tv_title);
            PupBean bean = list.get(position);
            textView.setText(bean.title);
            textView.setCompoundDrawablesWithIntrinsicBounds(bean.icon, 0, 0, 0);
            return convertView;
        }
    }

    private PopupWindow createPopupwindow() {
        LayoutInflater inflater = LayoutInflater.from(this);
        popView = inflater.inflate(R.layout.webview_popupwindow_add_layout, null);
        popView.getBackground().setAlpha(230);
        listView = (ListView) popView.findViewById(R.id.lv_popupwindow_add);
        pop = new PopupWindow(popView, WRAP_CONTENT, WRAP_CONTENT, true);
        //pop.setAnimationStyle(R.style.popwin_anim_style);
        pop.setBackgroundDrawable(new BitmapDrawable());
        pop.setOutsideTouchable(true);
        listView.setAdapter(pupAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (null != list.get(position)) {
                    if (list.get(position).getTitle().contains("分享")) {
                        shareWeb();
                    } else {
                        openWebWithBrowser();
                    }
                }
//                switch (position) {
//                    case 0:
//                        if (listView.getChildCount() == 1) {
//                            openWebWithBrowser();
//                        } else {
//                            shareWeb();
//                        }
//                        break;
//                    case 1:
//                        openWebWithBrowser();
//                        break;
//                }
                if (pop != null) {
                    pop.dismiss();
                }
            }
        });
        return pop;
    }

    private void shareWeb() {
        MobclickAgent.onEvent(WebViewActWithTitle.this, "webview_share");
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
        if (null != popView) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(popView.getWindowToken(), 0);
        }
        new ShareWebview(WebViewActWithTitle.this, webView, sharePic, shareTitle, shareContent, newUrl, noNeedShare);
    }

    private void openWebWithBrowser() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == mUploadMessage && null == mUploadCallbackAboveL) return;
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (mUploadCallbackAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (mUploadMessage != null) {
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        }
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

    @Override
    protected void onDestroy() {
        if (null != webView) {
            rootRl.removeView(webView);
            webView.destroy();
        }
//            webView.reload();
        super.onDestroy();
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
}