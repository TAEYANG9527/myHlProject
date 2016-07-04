package com.itcalf.renhe.context.template;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.controller.GrpcController;
import com.itcalf.renhe.dto.UserInfo;
import com.itcalf.renhe.http.Callback;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.orhanobut.logger.Logger;

/**
 * Created by wangning on 2015/9/7.
 */
public abstract class BaseFragment extends Fragment implements Callback {
    protected int layoutId;
    protected View rootView;
    protected LinearLayout loadingLL;
    protected UserInfo userInfo;
    public GrpcController grpcController;//grpc调用
    protected MaterialDialogsUtil materialDialogsUtil;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayoutId();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null != rootView) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (null != parent) {
                parent.removeView(rootView);
            }
        } else {
            rootView = inflater.inflate(layoutId, container, false);
        }
        setHasOptionsMenu(true); // 在动作栏中有一个菜单项．
        return rootView;
    }

    protected abstract void initLayoutId();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setMyContentView(view);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unRegisterReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        initMenuView(menu);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onMenuItemSelected(item);
        return true;
    }

    /**
     * 初始化上下文view
     */
    protected void setMyContentView(View view) {
        findView(view);
        initData();
        initListener();
        registerReceiver();
    }

    /**
     * 初始化控件
     */
    protected void findView(View view) {

    }

    protected void initMenuView(Menu menu) {

    }

    protected void onMenuItemSelected(MenuItem item) {

    }

    /**
     * 初始化数据
     */
    protected void initData() {
        if (null == getActivity())
            return;
        userInfo = RenheApplication.getInstance().getUserInfo();
        if (null == userInfo)
            return;
        materialDialogsUtil = new MaterialDialogsUtil(getActivity());
    }

    /**
     * 初始化事件监听
     */
    protected void initListener() {

    }

    /**
     * 注册广播
     */
    protected void registerReceiver() {
    }

    /**
     * 注销广播
     */
    protected void unRegisterReceiver() {
    }

    @Override
    public void onSuccess(int type, Object result) {
        TaskManager.getInstance().removeTask(type);
    }


    @Override
    public void onFailure(int type, String msg) {
        TaskManager.getInstance().removeTask(type);
        if (getActivity() == null)
            return;
//        ToastUtil.showToast(getActivity(), msg);
    }


    @Override
    public void cacheData(int type, Object data) {

    }

    /**
     * 在调用grpc方法前，检查接口是否已经在执行，grpcController是否已初始化
     *
     * @param taskId
     */
    protected boolean checkGrpcBeforeInvoke(int taskId) {
        if (TaskManager.getInstance().exist(taskId)) {
            Logger.d(taskId + "已存在，不调用");
            return false;
        }
        TaskManager.getInstance().addTask(this, taskId);
        if (null == grpcController)
            grpcController = new GrpcController();
        return true;
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
}
