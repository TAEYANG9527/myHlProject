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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.context.pay.PayUtil;
import com.itcalf.renhe.context.pay.PayWaySelectDialogUtil;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.orhanobut.logger.Logger;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import cn.renhe.heliao.idl.enterprise.Enterprise;
import cn.renhe.heliao.idl.money.pay.ConfirmPayStatusResponse;
import cn.renhe.heliao.idl.money.pay.PayBizType;
import cn.renhe.heliao.idl.money.pay.PayMethod;

/**
 * @author chan
 * @createtime 2014-11-24
 * @功能说明 网页链接展示在webview里面，带提取标题
 */
public class WebViewCompanyAuthActivity extends BaseActivity implements PayUtil.PayCallBack, PayWaySelectDialogUtil.SelectPayWayUtilCallBack {
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
    private boolean toAuth;//是否是企业验证、默认是企业查询\

    private String payPassword;//余额支付时输入的密码
    private Enterprise.EnterpriseSearchPayOrderResponse enterpriseSearchPayOrderResponse;//服务端返回的支付数据
    private PayBizType payBizType;//发起支付的业务类型
    private String payBizSid;//业务ID
    private PayMethod luckyPayWayType = PayMethod.WEIXIN;//支付方式,默认是微信支付
    //工具
    private PayUtil payUtil;
    private PayWaySelectDialogUtil payWaySelectDialogUtil;

    private int ID_TASK_PAYORDER = TaskManager.getTaskId();//余额充值
    private int ID_TASK_LUCKY_PAY = TaskManager.getTaskId();//确认支付的红包

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ActivityTemplate().doInActivity(this, R.layout.webview_company_auth_layout);
        this.payUtil = new PayUtil(this, this);
        this.payWaySelectDialogUtil = new PayWaySelectDialogUtil(this, materialDialogsUtil, this);
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
        webView.addJavascriptInterface(new JsInteration(), "android");
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
        toAuth = getIntent().getBooleanExtra("toAuth", false);
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
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!url.startsWith("heliaoapp")) {
                    if (!url.equals(WebViewCompanyAuthActivity.this.url)) {
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
                        WebViewCompanyAuthActivity.this.url = url;
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
                WebViewCompanyAuthActivity.this.url = url;
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
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
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
        });

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem helpItem = menu.findItem(R.id.item_cash_help);
        helpItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        helpItem.setVisible(true);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_cash_help:
                Intent intent = new Intent(WebViewCompanyAuthActivity.this, WebViewActWithTitle.class);
                if (toAuth)
                    intent.putExtra("url", Constants.COMPANY_VALID_CHECK__INVITE_HELP_URL);
                else
                    intent.putExtra("url", Constants.COMPANY_CHECK_INVITE_HELP_URL);
                startHlActivity(intent);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

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

    /**
     * 余额充值
     */

    private void enterpriseSearchPayOrder() {
        if (checkGrpcBeforeInvoke(ID_TASK_PAYORDER)) {
            showMaterialLoadingDialog(R.string.waitting, true);
            grpcController.enterpriseSearchPayOrder(ID_TASK_PAYORDER);
        }
    }

    /**
     * 确认支付的红包
     */

    private void confirmPayStatus() {
        if (!TextUtils.isEmpty(payBizSid) && payBizType.getNumber() >= 0) {
            if (TextUtils.isEmpty(payPassword))
                payPassword = "";
            if (checkGrpcBeforeInvoke(ID_TASK_LUCKY_PAY)) {
                showMaterialLoadingDialog();
                grpcController.confirmPayStatus(ID_TASK_LUCKY_PAY, payBizSid, payBizType);
            }
        } else {
            ToastUtil.showToast(this, R.string.pay_error_tip);
        }
    }

    @Override
    public void onSuccess(int taskId, Object result) {
        super.onSuccess(taskId, result);
        hideMaterialLoadingDialog();
        if (null != result) {
            if (result instanceof Enterprise.EnterpriseSearchPayOrderResponse) {
                enterpriseSearchPayOrderResponse = (Enterprise.EnterpriseSearchPayOrderResponse) result;
                luckyPayWayType = enterpriseSearchPayOrderResponse.getDefaultPayMethod();
                payBizType = enterpriseSearchPayOrderResponse.getBizType();
                payBizSid = enterpriseSearchPayOrderResponse.getBizSid();
                payWaySelectDialogUtil.showPayWaySelectCustomDialog(PayUtil.PAY_BIZ_TYPE_RECHARGE_BALANCE,
                        enterpriseSearchPayOrderResponse.getFee(), enterpriseSearchPayOrderResponse.getFee(), false,
                        enterpriseSearchPayOrderResponse.getPayMethodList(), luckyPayWayType);
            } else if (result instanceof ConfirmPayStatusResponse) { //确认支付成功
                payWaySelectDialogUtil.hideDialog();
                appInvokeJsMethodOfRechargeResult(true);
            }
        } else {
            appInvokeJsMethodOfRechargeResult(false);
        }
    }

    @Override
    public void onFailure(int type, String msg) {
        super.onFailure(type, msg);
        hideMaterialLoadingDialog();
        appInvokeJsMethodOfRechargeResult(false);
    }

    @Override
    public void onWeiXinPaySuccess() {
        confirmPayStatus();
    }

    @Override
    public void onAliPaySuccess() {
        confirmPayStatus();
    }

    @Override
    public void onWeiXinPayFail() {

    }

    @Override
    public void onAliPayFail() {

    }

    @Override
    public void onPayBtClick(int payType) {
        if (null == enterpriseSearchPayOrderResponse)
            return;
        Logger.e("payluckyType-->" + luckyPayWayType);
        payUtil.initPay(payType, enterpriseSearchPayOrderResponse.getFee(), enterpriseSearchPayOrderResponse.getBizType().getNumber(),
                enterpriseSearchPayOrderResponse.getBizSid(), PayUtil.PAY_BIZ_TYPE_RECHARGE_BALANCE);
    }

    @Override
    public void onPayPasswordInputed(String psw) {
        payWaySelectDialogUtil.payByBalance(payBizType.getNumber(), payBizSid, PayUtil.PAY_BIZ_TYPE_RECHARGE_BALANCE, enterpriseSearchPayOrderResponse.getFee(), psw);

    }

    @Override
    public void onPayPasswordRetry() {
        enterpriseSearchPayOrder();
    }

    @Override
    public void onPayWayTypeChanged(PayMethod payWayType) {
        luckyPayWayType = payWayType;
    }

    @Override
    public void onPayBalanceSuccess() {
        confirmPayStatus();
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
            convertView = LayoutInflater.from(WebViewCompanyAuthActivity.this).inflate(R.layout.item_popup_add_layout, parent, false);
            TextView textView = (TextView) convertView.findViewById(R.id.tv_title);
            PupBean bean = list.get(position);
            textView.setText(bean.title);
            textView.setCompoundDrawablesWithIntrinsicBounds(bean.icon, 0, 0, 0);
            return convertView;
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

    /**
     * 充值回调
     * app调用js代码
     * webView调用js的基本格式为webView.loadUrl(“javascript:methodName(parameterValues)”)
     */
    private void appInvokeJsMethodOfRechargeResult(boolean flag) {
        String call = "javascript:rechargeResult(\"" + flag + "\")";
        webView.loadUrl(call);
    }

    /**
     * Java和js交互
     */
    public class JsInteration {
        //## H5中js调用APP中的方法定义

        // 1、充值
        @JavascriptInterface
        public void recharge(boolean flag) {//true表示可以充值
            Logger.e("flag++" + flag);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    enterpriseSearchPayOrder();
                }
            });
        }

        // ## APP中调用H5js
        //1、回复话题内容
        @JavascriptInterface
        public void rechargeResult(boolean flag) {//true 回调成功 ，false 回调失败
            Logger.e("rechargeResult flag++" + flag);
        }
    }
}