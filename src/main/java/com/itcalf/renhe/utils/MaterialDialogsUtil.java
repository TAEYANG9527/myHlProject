package com.itcalf.renhe.utils;

import android.content.Context;
import android.view.View;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.R;

/**
 * Created by wangning on 2015/8/18.
 */
public class MaterialDialogsUtil {
    private Context mContext;
    private String luckySid;

    public MaterialDialogsUtil(Context context) {
        this.mContext = context;
    }

    public String getLuckySid() {
        return luckySid;
    }

    public void setLuckySid(String luckySid) {
        this.luckySid = luckySid;
    }

    MaterialDialog materialDialog;
    MaterialDialog.Builder builder;

    /**
     * @param contentTextRes 正文内容
     */
    public MaterialDialog.Builder getBuilder(int contentTextRes) {
        builder = new MaterialDialog.Builder(mContext).content(contentTextRes).positiveText(R.string.material_dialog_sure)
                .negativeText(R.string.material_dialog_cancel);
        return builder;
    }

    /**
     * @param contentTextRes  正文内容
     * @param positiveTextRes 确定按钮文本
     * @param negativeTextRes 取消按钮文本
     */
    public MaterialDialog.Builder getNotitleBuilder(int contentTextRes, int positiveTextRes, int negativeTextRes) {
        builder = new MaterialDialog.Builder(mContext).content(contentTextRes).positiveText(positiveTextRes)
                .negativeText(negativeTextRes);
        return builder;
    }

    /**
     * @param titleTextRes    标题内容
     * @param contentTextRes  正文内容
     * @param positiveTextRes 确定按钮文本
     * @param negativeTextRes 取消按钮文本
     * @param neutralTextRes  放弃按钮文本
     */
    public MaterialDialog.Builder getBuilder(int titleTextRes, int contentTextRes, int positiveTextRes, int negativeTextRes,
                                             int neutralTextRes) {
        builder = new MaterialDialog.Builder(mContext).title(titleTextRes).content(contentTextRes).positiveText(positiveTextRes)
                .negativeText(negativeTextRes).neutralText(neutralTextRes);
        return builder;
    }

    /**
     * @param titleTextRes   标题内容
     * @param contentTextRes 正文内容
     */
    public MaterialDialog.Builder getBuilder(int titleTextRes, int contentTextRes) {
        builder = new MaterialDialog.Builder(mContext).title(titleTextRes).content(contentTextRes)
                .positiveText(R.string.material_dialog_sure).negativeText(R.string.material_dialog_cancel);
        return builder;
    }

    /**
     * @param content 正文内容
     */
    public MaterialDialog.Builder getBuilder(String content) {
        builder = new MaterialDialog.Builder(mContext).content(content).positiveText(R.string.material_dialog_sure)
                .negativeText(R.string.material_dialog_cancel);
        //				.typeface(Constants.APP_TYPEFACE, Constants.APP_TYPEFACE);
        return builder;
    }

    /**
     * @param titleTextRes 标题内容
     * @param content      正文内容
     */
    public MaterialDialog.Builder getBuilder(int titleTextRes, String content) {
        builder = new MaterialDialog.Builder(mContext).title(titleTextRes).content(content)
                .positiveText(R.string.material_dialog_sure).negativeText(R.string.material_dialog_cancel);
        //				.typeface(Constants.APP_TYPEFACE, Constants.APP_TYPEFACE);
        return builder;
    }

    /**
     * 加载进度弹出框
     */
    public MaterialDialog.Builder showIndeterminateProgressDialog() {
        builder = new MaterialDialog.Builder(mContext).progress(true, 0).progressIndeterminateStyle(false);
        return builder;
    }

    /**
     * 加载进度弹出框
     *
     * @param contentTextRes 正文内容
     */
    public MaterialDialog.Builder showIndeterminateProgressDialog(int contentTextRes) {
        builder = new MaterialDialog.Builder(mContext).content(contentTextRes).progress(true, 0)
                .progressIndeterminateStyle(false);
        return builder;
    }

    /**
     * 加载进度弹出框
     *
     * @param contentTextRes 正文内容
     */
    public MaterialDialog.Builder showIndeterminateProgressDialog(String contentTextRes) {
        builder = new MaterialDialog.Builder(mContext).content(contentTextRes).progress(true, 0)
                .progressIndeterminateStyle(false);
        return builder;
    }

    /**
     * 加载进度弹出框
     *
     * @param horizontal     true：横向加载进度条  false：圆圈加载进度条
     * @param contentTextRes 正文内容
     */
    public MaterialDialog.Builder showIndeterminateProgressDialog(boolean horizontal, int contentTextRes) {
        builder = new MaterialDialog.Builder(mContext).content(contentTextRes).progress(true, 0)
                .progressIndeterminateStyle(horizontal);
        return builder;
    }

    /**
     * “使用帮助类”弹出框,一般只有一个按钮
     *
     * @param titleTextRes    标题内容
     * @param contentTextRes  正文内容
     * @param positiveTextRes 确定按钮文本
     */
    public MaterialDialog.Builder getBuilder(int titleTextRes, int contentTextRes, int positiveTextRes) {
        builder = new MaterialDialog.Builder(mContext).title(titleTextRes).content(contentTextRes).positiveText(positiveTextRes);
        return builder;
    }

    /**
     * “使用帮助类”弹出框,一般只有一个按钮
     *
     * @param titleTextRes    标题内容
     * @param contentTextRes  正文内容
     * @param positiveTextRes 确定按钮文本
     */
    public MaterialDialog.Builder getBuilder(String titleTextRes, String contentTextRes, String positiveTextRes) {
        builder = new MaterialDialog.Builder(mContext).title(titleTextRes).content(contentTextRes).positiveText(positiveTextRes);
        return builder;
    }

    /**
     * “使用帮助类”弹出框,一般只有一个按钮
     *
     * @param titleTextRes    标题内容
     * @param contentTextRes  正文内容
     * @param positiveTextRes 确定按钮文本
     */
    public MaterialDialog.Builder getBuilder(String titleTextRes, String contentTextRes, String positiveTextRes, String negativeTextRes) {
        builder = new MaterialDialog.Builder(mContext).title(titleTextRes).content(contentTextRes).positiveText(positiveTextRes).negativeText(negativeTextRes);
        return builder;
    }

    /**
     * @param titleTextRes    标题内容
     * @param contentTextRes  正文内容
     * @param positiveTextRes 确定按钮文本
     */
    public MaterialDialog.Builder getBuilder(int titleTextRes, String contentTextRes, int positiveTextRes,
                                             int negativeTextRes) {
        builder = new MaterialDialog.Builder(mContext).title(titleTextRes).content(contentTextRes).positiveText(positiveTextRes)
                .negativeText(negativeTextRes);
        return builder;
    }

    /**
     * @param titleTextRes    标题内容
     * @param contentTextRes  正文内容
     * @param positiveTextRes 确定按钮文本
     */
    public MaterialDialog.Builder getBuilder(String titleTextRes, String contentTextRes, int positiveTextRes,
                                             int negativeTextRes) {
        builder = new MaterialDialog.Builder(mContext).title(titleTextRes).content(contentTextRes).positiveText(positiveTextRes)
                .negativeText(negativeTextRes);
        return builder;
    }

    /**
     * 单选对话框
     *
     * @param title 标题
     * @return
     */
    public MaterialDialog.Builder showSingleChoice(int title) {
        builder = new MaterialDialog.Builder(mContext).title(title).negativeText(R.string.material_dialog_cancel)
                .alwaysCallSingleChoiceCallback();
        return builder;
    }

    /**
     * <b>带标题</b>
     * 选择对话框（eg：删除、复制等情景下的提示框）
     *
     * @param itemsRes 需要显示在对话框里边的item,需在values目录下的arrays.xml配置 string-array
     * @return
     */
    public MaterialDialog.Builder showSelectList(int title, int itemsRes) {
        builder = new MaterialDialog.Builder(mContext).title(title).items(itemsRes).cancelable(true);
        return builder;
    }

    /**
     * 不带标题
     * 选择对话框（eg：删除、复制等情景下的提示框）
     *
     * @param itemsRes 需要显示在对话框里边的item,需在values目录下的arrays.xml配置 string-array
     * @return
     */
    public MaterialDialog.Builder showSelectList(int itemsRes) {
        builder = new MaterialDialog.Builder(mContext).items(itemsRes).cancelable(true);
        return builder;
    }

    /**
     * 代标题的提示设置的对话框，（eg：检查到应用没有获取通讯录的权限，提示去设置页设置）
     *
     * @param titleRes
     * @param contentRes
     * @param positiveTextRes
     * @param negativeTextRes
     * @return
     */
    public MaterialDialog.Builder showStacked(int titleRes, int contentRes, int positiveTextRes,
                                              int negativeTextRes) {
        builder = new MaterialDialog.Builder(mContext).title(titleRes)
                .content(contentRes).positiveText(positiveTextRes)
                .negativeText(negativeTextRes).btnStackedGravity(GravityEnum.END).forceStacking(false); // this generally should not be forced, but is used for demo purposes

        return builder;
    }

    /**
     * 自定义主体内容
     */
    public MaterialDialog.Builder getCustomViewBuilder(int customViewLayoutRes, int positiveTextRes, int negativeTextRes) {
        builder = new MaterialDialog.Builder(mContext).customView(customViewLayoutRes, false);
        if (positiveTextRes > 0)
            builder.positiveText(positiveTextRes);
        if (negativeTextRes > 0)
            builder.negativeText(negativeTextRes);
        return builder;
    }

    /**
     * 自定义主体内容
     */
    public MaterialDialog.Builder getCustomViewBuilderByView(View customViewLayoutView, int positiveTextRes, int negativeTextRes) {
        builder = new MaterialDialog.Builder(mContext).customView(customViewLayoutView, false);
        if (positiveTextRes > 0)
            builder.positiveText(positiveTextRes);
        if (negativeTextRes > 0)
            builder.negativeText(negativeTextRes);
        return builder;
    }

    public void show() {
        if (null != materialDialog && materialDialog.isShowing())
            materialDialog.dismiss();
        if (null != builder) {
            materialDialog = builder.build();
            materialDialog.show();
        }
    }

    public void dismiss() {
        if (null != materialDialog)
            materialDialog.dismiss();
    }
}
