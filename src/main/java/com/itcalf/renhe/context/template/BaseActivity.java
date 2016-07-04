package com.itcalf.renhe.context.template;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.LayoutInflater.Factory;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.wukong.im.RenheIMUtil;
import com.itcalf.renhe.controller.GrpcController;
import com.itcalf.renhe.http.Callback;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.utils.FadeUitl;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.http.okhttp.OkHttpClientManager;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

/**
 * 继承Activity，并提供Activity的基本函数。
 *
 * @author piers.xie
 */
public abstract class BaseActivity<T> extends org.aisen.android.ui.activity.basic.BaseActivity implements Callback<T> {

    /**
     * 系统配置
     */
    protected SharedPreferences mSettings;
    protected TextView mTitleTxt;
    /**
     * 根据不同的会话、消息事件类型做相应的业务处理
     */
    protected List<ImageView> cacheImageViewList;
    protected List<Bitmap> cacheBitmapList;
    protected FadeUitl fadeUitl;
    protected Toolbar toolbar;
    protected TextView toolbarTitleTv;
    protected LinearLayout loadingLL;
    /**
     * 对话框工具类
     *
     * @param savedInstanceState
     */
    protected MaterialDialogsUtil materialDialogsUtil;
    public GrpcController grpcController;//grpc调用

    public ImageLoader imageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RenheApplication.getInstance().addActivity(this);
        mSettings = getPreferences(2);
        //		if (null != getSupportActionBar()) {
        //			getSupportActionBar().setHomeButtonEnabled(true);
        //			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //			getSupportActionBar().setDisplayShowHomeEnabled(true);
        //			// 获取actionbar的源码地址
        //			int titleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        //			mTitleTxt = (TextView) getWindow().findViewById(titleId);
        //			if (null != mTitleTxt) {
        //				mTitleTxt.setTypeface(Constants.APP_TYPEFACE);
        //			}
        //		}

        // registerReceiver();
        cacheImageViewList = new ArrayList<>();
        cacheBitmapList = new ArrayList<>();
        fadeUitl = new FadeUitl(this, "加载中...");
    }

    /**
     * 设置布局，获取toolbar,初始化界面
     *
     * @param layoutRes 布局文件
     */
    public void setMyContentView(int layoutRes) {
        setContentView(layoutRes);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
//            setTranslucentStatus(true);
//            SystemBarTintManager tintManager = new SystemBarTintManager(this);
//            tintManager.setStatusBarTintEnabled(true);
//            tintManager.setStatusBarTintResource(R.color.renhe_actionbar_bcg);
//        }
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbarTitleTv = (TextView) findViewById(R.id.toolbar_title_tv);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        //加载框
        loadingLL = (LinearLayout) findViewById(R.id.loadingLL);
        hideLoadingDialog();

        findView();
        initData();
        initListener();
    }

//    @TargetApi(19)
//    private void setTranslucentStatus(boolean on) {
//        Window win = getWindow();
//        WindowManager.LayoutParams winParams = win.getAttributes();
//        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
//        if (on) {
//            winParams.flags |= bits;
//        } else {
//            winParams.flags &= ~bits;
//        }
//        win.setAttributes(winParams);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        customTypeface();
        return super.onCreateOptionsMenu(menu);
    }

    private void customTypeface() {
        LayoutInflater layoutInflater = getLayoutInflater();
        final Factory existingFactory = layoutInflater.getFactory();
        // use introspection to allow a new Factory to be set
        try {
            Field field = LayoutInflater.class.getDeclaredField("mFactorySet");
            field.setAccessible(true);
            field.setBoolean(layoutInflater, false);
            getLayoutInflater().setFactory(new Factory() {
                @Override
                public View onCreateView(String name, final Context context, AttributeSet attrs) {
                    View view = null;
                    if (existingFactory != null) {
                        view = existingFactory.onCreateView(name, context, attrs);
                    }
                    if (name.equalsIgnoreCase("com.android.internal.view.menu.ActionMenuItemView")) {
                        try {
                            LayoutInflater li = LayoutInflater.from(context);
                            final View tv = li.createView(name, null, attrs);
                            // view.setBackgroundResource(R.drawable.myimage);
                            // ((TextView) tv).setTextSize(20);
                            ((TextView) tv).setTypeface(Constants.APP_TYPEFACE);
                            ((TextView) tv)
                                    .setTextColor(getResources().getColorStateList(R.drawable.hl_actionbar_textcolor_selected));
                            return tv;
                        } catch (InflateException e) {
                        } catch (ClassNotFoundException e) {
                        } catch (Exception e) {
                        }
                    }
                    return view;
                }
            });
        } catch (NoSuchFieldException e) {
            // ...
        } catch (IllegalArgumentException e) {
            // ...
        } catch (IllegalAccessException e) {
            // ...
        } catch (Exception e) {
        }
    }

    /**
     * 获取模板
     *
     * @return
     */
    protected ActivityTemplate getTemplate() {
        return new ActivityTemplate();
    }

    /**
     * 获取组件对象
     */
    protected void findView() {
        mTitleTxt = (TextView) findViewById(R.id.title_txt);
        ButterKnife.bind(this);
    }

    /**
     * 初始化组件数据
     */
    protected void initData() {
        materialDialogsUtil = new MaterialDialogsUtil(this);
        imageLoader = ImageLoader.getInstance();
    }

    /**
     * 初始化组件监听器
     */
    protected void initListener() {
    }

    /**
     * 获取当前Application对象
     *
     * @return MaxApplication
     */
    protected RenheApplication getRenheApplication() {
        return (RenheApplication) getApplicationContext();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // StatService.onResume(this);//百度统计
        // 友盟统计
        MobclickAgent.onPageStart("SplashScreen"); // 统计页面
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // StatService.onPause(this);
        // 友盟统计
        MobclickAgent.onPageEnd("SplashScreen"); // 保证 onPageEnd 在onPause
        // 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
    }

    /**
     * 启动Activity
     *
     * @param clazz
     */
    protected void startActivity(Class<?> clazz) {
        startActivity(new Intent(this, clazz));
        if (null != this.getParent()) {
            this.getParent().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        } else {
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        }
    }

    /**
     * 启动Activity,同时销毁自己
     *
     * @param clazz
     */
    protected void startActivityWithFinish(Class<?> clazz) {
        startActivity(clazz);
        finish();
    }

    /**
     * 重写finish方法
     */
    @Override
    public void finish() {
        super.finish();
    }

    /**
     * 启动Activity
     *
     * @param clazz
     * @param flag
     */
    protected void startActivity(Class<?> clazz, int flag) {
        Intent intent = new Intent(this, clazz);
        intent.setFlags(flag);
        startActivity(intent);
        finish();

    }

    protected void startActivity(Class<?> clazz, Bundle bundle, int flag) {
        Intent intent = new Intent(this, clazz);
        intent.setFlags(flag);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
        if (null != this.getParent()) {
            this.getParent().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        } else {
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        }

    }

    /**
     * 启动带参数传递的Activity
     *
     * @param clazz
     * @param bundle
     */
    protected void startActivity(Class<?> clazz, Bundle bundle) {
        Intent intent = new Intent(this, clazz);
        intent.putExtras(bundle);
        startActivity(intent);
        if (null != this.getParent()) {
            this.getParent().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        } else {
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        }
    }

    /**
     * 启动带参数传递的Activity
     *
     * @param clazz
     * @param intent
     */
    protected void startActivity(Class<?> clazz, Intent intent) {
        startActivity(intent);
        if (null != this.getParent()) {
            this.getParent().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        } else {
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        }
    }

    /**
     * 启动回调的Activity
     *
     * @param clazz
     */
    protected void startActivityForResult(Class<?> clazz, int requestCode) {
        Intent intent = new Intent(this, clazz);
        startActivityForResult(intent, requestCode);
        // overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_out);
        if (null != this.getParent()) {
            this.getParent().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        } else {
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        }

    }

    /**
     * 启动Activity
     *
     * @param intent 传递的intent
     */
    protected void startHlActivity(Intent intent) {
        startActivity(intent);
        if (null != this.getParent()) {
            this.getParent().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        } else {
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        }
    }

    /**
     * 启动Activity
     *
     * @param intent 传递的intent
     */
    protected void startHlActivityForResult(Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode);
        // overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_out);
        if (null != this.getParent()) {
            this.getParent().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        } else {
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        }

    }

    /**
     * 设置文本内容
     *
     * @param value
     */
    protected void setTextValue(String value) {
        if (TextUtils.isEmpty(value))
            value = "";
        setTitle("");
        if (null != toolbarTitleTv) {
            toolbarTitleTv.setText(value);
        }
        //		if (null != getSupportActionBar())
        //			getSupportActionBar().setTitle(value);
    }

    /**
     * 设置文本内容
     */
    protected void setTextValue(int titleId) {
        if (null != toolbarTitleTv) {
            toolbarTitleTv.setText(titleId);
        }
        //		if (null != getSupportActionBar())
        //			getSupportActionBar().setTitle(value);
    }

    /**
     * 设置文本内容
     *
     * @param resourceId
     * @param value
     */
    protected void setTextValue(int resourceId, String value) {
        // ((TextView) findViewById(resourceId)).setText(value);
        if (TextUtils.isEmpty(value))
            value = "";
        setTitle("");
        if (null != toolbarTitleTv) {
            toolbarTitleTv.setText(value);
        }
        //		if (null != getSupportActionBar())
        //			getSupportActionBar().setTitle(value);
    }

    /**
     * 在调用grpc方法前，检查接口是否已经在执行，grpcController是否已初始化
     *
     * @param taskId
     */
    protected boolean checkGrpcBeforeInvoke(int taskId) {
        if (TaskManager.getInstance().exist(taskId)) {
            return false;
        }
        TaskManager.getInstance().addTask(this, taskId);
        if (null == grpcController)
            grpcController = new GrpcController();
        return true;
    }

    /**
     * GRPC 调用的回调方法
     * <p/>
     * 成功进入onSuccess
     * 失败进入onFailure
     *
     * @param taskId 请求的taskId
     * @param result 实体类
     */
    @Override
    public void onSuccess(int taskId, T result) {
        if (TaskManager.getInstance().exist(taskId))
            TaskManager.getInstance().removeTask(taskId);
    }

    /**
     * GRPC 调用的回调方法
     * <p/>
     * 成功进入onSuccess
     * 失败进入onFailure
     *
     * @param type 请求的taskId
     * @param msg  错误日志
     */
    @Override
    public void onFailure(int type, String msg) {
        if (TaskManager.getInstance().exist(type))
            TaskManager.getInstance().removeTask(type);
    }

    @Override
    public void cacheData(int type, Object data) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
        if (!TextUtils.isEmpty(this.getClass().getSimpleName()))
            OkHttpClientManager.cancelTag(getClass().getSimpleName());
        RenheApplication.getInstance().removeActivity(this);
        RenheIMUtil.dismissProgressDialog(); // activity出现异常退出后如果对话框正在显示，需要关闭
        if (null != cacheImageViewList) {
            for (ImageView imageView : cacheImageViewList) {
                if (imageView != null) {
                    if (null != imageView.getDrawable() && imageView.getDrawable() instanceof BitmapDrawable) {
                        Bitmap bp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                        if (bp != null && !bp.isRecycled()) {
                            bp.recycle();
                        }
                    }
                    if (null != imageView.getBackground() && imageView.getBackground() instanceof BitmapDrawable) {
                        Bitmap bitmap1 = ((BitmapDrawable) imageView.getBackground()).getBitmap();
                        // bitmap1确认即将不再使用，强制回收，这也是我们经常忽略的地方
                        if (!bitmap1.isRecycled()) {
                            bitmap1.recycle();
                        }
                    }
                    Bitmap bp2 = imageView.getDrawingCache();
                    if (bp2 != null && !bp2.isRecycled()) {
                        bp2.recycle();
                    }
                    imageView.setDrawingCacheEnabled(false);
                    imageView.setImageBitmap(null);
                }
            }
            cacheImageViewList.clear();
            cacheImageViewList = null;
        }

        if (cacheBitmapList != null && cacheBitmapList.size() > 0) {
            for (int i = 0; i < cacheBitmapList.size(); i++) {
                if (cacheBitmapList.get(i) != null && !cacheBitmapList.get(i).isRecycled()) {
                    cacheBitmapList.get(i).recycle();
                }
            }
            cacheBitmapList.clear();
            cacheBitmapList = null;
        }
        System.gc();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    }

    //加载框
    public void showLoadingDialog() {
        if (loadingLL != null) {
            loadingLL.setVisibility(View.VISIBLE);
        }
    }

    public void hideLoadingDialog() {
        if (null != loadingLL && loadingLL.getVisibility() == View.VISIBLE) {
            loadingLL.setVisibility(View.GONE);
        }
    }

    //加载框
    public void showMaterialLoadingDialog() {
        if (materialDialogsUtil != null) {
            materialDialogsUtil.showIndeterminateProgressDialog(R.string.xlistview_header_hint_loading).canceledOnTouchOutside(false).build();
            materialDialogsUtil.show();
        }
    }

    //加载框
    public void showMaterialLoadingDialog(int loadingInfoRes, boolean canceledOnTouchOutside) {
        if (materialDialogsUtil != null) {
            materialDialogsUtil.showIndeterminateProgressDialog(loadingInfoRes).canceledOnTouchOutside(canceledOnTouchOutside).build();
            materialDialogsUtil.show();
        }
    }

    public void hideMaterialLoadingDialog() {
        if (null != materialDialogsUtil) {
            materialDialogsUtil.dismiss();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
//            finish();
//            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
        }
        return super.onKeyDown(keyCode, event);
    }
}
