package com.itcalf.renhe.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.R;
import com.itcalf.renhe.context.auth.NameAuthActivity;
import com.itcalf.renhe.context.fragmentMain.MyFragment;
import com.itcalf.renhe.context.pay.UpgradeDialog;
import com.itcalf.renhe.controller.GrpcController;
import com.itcalf.renhe.http.Callback;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.view.Button;
import com.orhanobut.logger.Logger;

import java.util.HashMap;
import java.util.Map;

import cn.renhe.heliao.idl.payupgrade.Payupgrade;

/**
 * Created by wangning on 2016/1/18.
 */
public class CheckUpgradeUtil implements Callback {
    private Context context;
    private int ID_TASK_CHECK_UPGRADE;
    private GrpcController grpcController;

    public CheckUpgradeUtil(Context context) {
        this.context = context;
        this.ID_TASK_CHECK_UPGRADE = TaskManager.getTaskId();
    }

    /**
     * 检测是否弹出升级提醒框
     */
    public void checkUpgrade() {
        if (TaskManager.getInstance().exist(ID_TASK_CHECK_UPGRADE)) {
            return;
        }
        if (grpcController == null)
            grpcController = new GrpcController();
        TaskManager.getInstance().addTask(this, ID_TASK_CHECK_UPGRADE);
        grpcController.checkIfUpgrade(ID_TASK_CHECK_UPGRADE);
    }

    /**
     * 弹出升级引导提示框
     *
     * @param context
     * @param dialogIvRes
     * @param dialogTitleTvRes
     * @param dialogSubTitleTvRes
     * @param dialogBtRes
     * @param destinationClass    要跳转到的目的页面
     * @param type                2 代表添加好友时，对方设置了只允许实名认证会员联系的引导入口
     *                            3 代表当⼈脉数量达到上限时，还要执⾏添加好友的动作时的入口；
     *                            4 代表当每日加好友的数量超过上限了，出现收费会员引导的入口；
     *                            5 代表当要查看⼀个陌⽣会员的’⼈脉’列表时，出现收费会员引导的入口
     */
    public static void showUpgradeGuideDialog(final Context context, int dialogIvRes, int dialogTitleTvRes,
                                              int dialogSubTitleTvRes, int dialogBtRes, final Class destinationClass, final int type) {
        MaterialDialogsUtil materialDialogsUtil = new MaterialDialogsUtil(context);
        final MaterialDialog materialDialog = materialDialogsUtil.getCustomViewBuilder(R.layout.upgrade_guide_customview_dialog_layout,
                0, 0)//不需要确定/取消按钮
                .build();
        ImageView dialogIv = (ImageView) materialDialog.getCustomView().findViewById(R.id.upgrade_guide_dialog_iv);
        TextView dialogTitleTv = (TextView) materialDialog.getCustomView().findViewById(R.id.upgrade_guide_dialog_title_tv);
        TextView dialogSubTitleTv = (TextView) materialDialog.getCustomView().findViewById(R.id.upgrade_guide_dialog_sub_title_tv);
        Button dialogBt = (Button) materialDialog.getCustomView().findViewById(R.id.upgrade_guide_dialog_bt);
        if (dialogIvRes > 0)
            dialogIv.setBackgroundResource(dialogIvRes);
        if (dialogTitleTvRes > 0)
            dialogTitleTv.setText(dialogTitleTvRes);
        if (dialogSubTitleTvRes > 0)
            dialogSubTitleTv.setText(dialogSubTitleTvRes);
        if (dialogBtRes > 0)
            dialogBt.setText(dialogBtRes);
        dialogBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type == 2) {
                    NameAuthActivity.launch((Activity) context, MyFragment.realNameStatus);
                } else {
                    context.startActivity(new Intent(context, destinationClass));
                    ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                }
                materialDialog.dismiss();
                Map<String, String> statisticsMap = new HashMap<>();//阿里云统计自定义事件自定义参数
                statisticsMap.put("type", type + "");
                if (type != 2) {
                    StatisticsUtil.statisticsCustomClickEvent(context.getString(R.string.android_btn_pop_upgrade_click), 0, "", statisticsMap);
                } else {
                    StatisticsUtil.statisticsCustomClickEvent(context.getString(R.string.android_btn_pop_realname_click), 0, "", statisticsMap);
                }
            }
        });
        materialDialog.show();
    }

    @Override
    public void onSuccess(int type, Object result) {
        if (TaskManager.getInstance().exist(type))
            TaskManager.getInstance().removeTask(type);
        if (null != result && result instanceof Payupgrade.PayUpgradeResponse) {
            Payupgrade.PayUpgradeResponse payUpgradeResponse = (Payupgrade.PayUpgradeResponse) result;
            Logger.w("upgrade===>" + payUpgradeResponse.getIsPayUpgrade());
            if (payUpgradeResponse.getIsPayUpgrade()) {
//                DialogUtil.showUpgradeDialog(context);
                UpgradeDialog myDialog = new UpgradeDialog(context, R.style.TranslucentUnfullwidthWinStyle);
                myDialog.show();
            }
        }
    }

    @Override
    public void onFailure(int type, String msg) {
        if (TaskManager.getInstance().exist(type))
            TaskManager.getInstance().removeTask(type);
    }

    @Override
    public void cacheData(int type, Object data) {

    }
}
