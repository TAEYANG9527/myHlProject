package com.itcalf.renhe.viewholder;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.MessageContent;
import com.itcalf.renhe.R;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.luckymoney.LuckyMoneyDetailActivity;
import com.itcalf.renhe.controller.GrpcController;
import com.itcalf.renhe.http.Callback;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.TextView;

import cn.renhe.heliao.idl.money.red.HeliaoRobRed;

/**
 * @author wangning  on 2015/10/12.
 */
public class ChatNormalLuckyMoneyViewHolder extends ChatViewHolder implements Callback {

    private TextView luckyNameTv;
    private TextView luckyTipTv;
    public MessageContent.TextContent messageContent;
    private String luckyName;
    private String luckySid;
    private String luckyTip;
    private MaterialDialogsUtil materialDialogsUtil;
    private MaterialDialog materialDialog;
    private ImageView openIv;
    private ImageView avatarIv;
    private TextView tipTv;
    private String luckySenderUserface;
    private static final String LUCKY_NAME = "red_name";//标题
    private static final String LUCKY_SID = "red_sid";//红包sid
    private int ID_TASK_CHECK_LUCKYMONEY_REMAIN = TaskManager.getTaskId();//检查红包是否还有剩余
    private int ID_TASK_OPEN_LUCKYMONEY = TaskManager.getTaskId();//打开红包

    public ChatNormalLuckyMoneyViewHolder(Context context, View itemView, RecyclerView chatRecyclerView,
                                          RecyclerView.Adapter adapter, Conversation conversation) {
        super(context, itemView, chatRecyclerView, adapter, conversation);
        this.materialDialogsUtil = new MaterialDialogsUtil(context);
        luckyNameTv = ((TextView) itemView.findViewById(R.id.lucky_info_title_tv));
        luckyTipTv = ((TextView) itemView.findViewById(R.id.lucky_info_tip_tv));
    }

    @Override
    public void initView(RecyclerHolder holder, Object messageObj, final int position) {
        super.initView(holder, messageObj, position);
        messageContent = (MessageContent.TextContent) message.messageContent();
        if (null == messageContent)
            return;
        luckyName = message.extension(LUCKY_NAME);
        luckySid = message.extension(LUCKY_SID);
        luckyTip = isSenderIsSelf() ? context.getString(R.string.lucky_money_see) : context.getString(R.string.lucky_money_get);
        luckyNameTv.setText(luckyName);
        luckyTipTv.setText(luckyTip);
    }

    @Override
    public void onContentRlClickListener() {
        if (!TextUtils.isEmpty(luckySid)) {
            checkLuckyMoneyRemain(luckySid);
        }
    }

    @Override
    public void onContentRlLongClickListener() {
    }

    private void showLuckyMoneyCustomDialog(final HeliaoRobRed.CheckRemainResponse remainResponse) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.luckymoney_customview, null);
        materialDialog = materialDialogsUtil.getCustomViewBuilderByView(dialoglayout,
                0, 0)//不需要确定/取消按钮
                .canceledOnTouchOutside(false)
                .build();
        openIv = (ImageView) dialoglayout.findViewById(R.id.open_iv);
        avatarIv = (ImageView) dialoglayout.findViewById(R.id.avatar_iv);
        final ImageView closeIv = (ImageView) dialoglayout.findViewById(R.id.close_iv);
        final TextView seeDetailTv = (TextView) dialoglayout.findViewById(R.id.see_others_lucky_tv);
        TextView nameTv = (TextView) dialoglayout.findViewById(R.id.name_tv);//名字
        tipTv = (TextView) dialoglayout.findViewById(R.id.tip_tv);//附加短语
        String luckyName = remainResponse.getName();
        String luckyNote = null;
        luckySenderUserface = remainResponse.getUserface();
        if (remainResponse.getHandleType() == HeliaoRobRed.CheckRemainResponse.HANDLE_TYPE.ROB) {
            openIv.setVisibility(View.VISIBLE);
            avatarIv.setVisibility(View.GONE);
            luckyNote = remainResponse.getNote();
        } else {
            //region Description
            openIv.setVisibility(View.GONE);
            //endregion
            avatarIv.setVisibility(View.VISIBLE);
            try {
                imageLoader.displayImage(luckySenderUserface, avatarIv, CacheManager.circleImageOptions);
            } catch (Exception e) {
                e.printStackTrace();
            }
            luckyNote = remainResponse.getBase().getErrorInfo();
        }
        if (!TextUtils.isEmpty(luckyName)) {
            nameTv.setText(luckyName);
        } else {
            nameTv.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(luckyNote)) {
            tipTv.setText(luckyNote);
        } else {
            tipTv.setVisibility(View.GONE);
        }
        if (null != openIv) {
            openIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(luckySid)) {
                        openIv.postDelayed(new Runnable() {//为了让用户看到铜钱转动的动画，延迟1秒后调用接口，提升视觉体验
                            @Override
                            public void run() {
                                openLuckyMoney(luckySid);
                            }
                        }, 1000);
                    }
                    openLucky(openIv);
                }
            });
        }
        if (null != closeIv) {
            closeIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    materialDialog.dismiss();
                }
            });
        }
        if (null != seeDetailTv) {
            seeDetailTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    seeLuckyMoneyDetail(luckySid);
                }
            });
        }
        //弹出红包的动画
        materialDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ObjectAnimator animatorAlpha = ObjectAnimator.ofFloat(dialoglayout, "alpha", 0, 1, 1, 1);
        ObjectAnimator animatorScalex = ObjectAnimator.ofFloat(dialoglayout, "scaleX", 0.3f, 1.05f, 0.9f, 1);
        ObjectAnimator animatorScaley = ObjectAnimator.ofFloat(dialoglayout, "scaleY", 0.3f, 1.05f, 0.9f, 1);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(animatorAlpha).with(animatorScalex).with(animatorScaley);
        animSet.setDuration(600);
        animSet.start();
        try {
            materialDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openLucky(ImageView openIv) {
        openIv.clearAnimation();
        openIv.setImageResource(R.drawable.lucky_open_animator_frame);
        if (openIv.getDrawable() instanceof AnimationDrawable) {
            ((AnimationDrawable) openIv.getDrawable()).start();
        }
    }

    private void seeLuckyMoneyDetail(String luckySid) {
        Intent intent = new Intent(context, LuckyMoneyDetailActivity.class);
        intent.putExtra("luckySid", luckySid);
        context.startActivity(intent);
        ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        if (null != materialDialog)
            materialDialog.dismiss();
    }

    /**
     * 检查红包是否还有剩余
     */
    public void checkLuckyMoneyRemain(String redSid) {
        GrpcController grpcController = new GrpcController();//grpc调用
        if (checkGrpcBeforeInvoke(ID_TASK_CHECK_LUCKYMONEY_REMAIN, grpcController)) {
            if (null != materialDialogsUtil) {
                materialDialogsUtil.showIndeterminateProgressDialog(R.string.xlistview_header_hint_loading).cancelable(false).build();
                materialDialogsUtil.show();
            }
            grpcController.checkLuckyMoneyRemain(ID_TASK_CHECK_LUCKYMONEY_REMAIN, redSid);
        }
    }

    /**
     * 打开红包
     */
    public void openLuckyMoney(String redSid) {
        GrpcController grpcController = new GrpcController();//grpc调用
        if (checkGrpcBeforeInvoke(ID_TASK_OPEN_LUCKYMONEY, grpcController)) {
            grpcController.openLuckyMoney(ID_TASK_OPEN_LUCKYMONEY, redSid);
        }
    }

    /**
     * 在调用grpc方法前，检查接口是否已经在执行，grpcController是否已初始化
     *
     * @param taskId
     */
    protected boolean checkGrpcBeforeInvoke(int taskId, GrpcController grpcController) {
        if (TaskManager.getInstance().exist(taskId)) {
            return false;
        }
        TaskManager.getInstance().addTask(this, taskId);
        if (null == grpcController)
            grpcController = new GrpcController();
        return true;
    }

    @Override
    public void onSuccess(int type, Object result) {
        if (TaskManager.getInstance().exist(type))
            TaskManager.getInstance().removeTask(type);
        if (null != materialDialogsUtil) {
            materialDialogsUtil.dismiss();
        }
        if (null != result) {
            if (result instanceof HeliaoRobRed.CheckRemainResponse) {
                HeliaoRobRed.CheckRemainResponse checkRemainResponse = (HeliaoRobRed.CheckRemainResponse) result;
                switch (checkRemainResponse.getHandleType()) {
                    case ALERT:
                    case ROB:
                        showLuckyMoneyCustomDialog(checkRemainResponse);
                        break;
                    case NEXT:
                        seeLuckyMoneyDetail(luckySid);
                        break;
                    default:
                        break;
                }
            } else if (result instanceof HeliaoRobRed.OpenRedResponse) {
                HeliaoRobRed.OpenRedResponse openRedResponse = (HeliaoRobRed.OpenRedResponse) result;
                if (openRedResponse.getRobbed()) {
                    seeLuckyMoneyDetail(luckySid);
                } else {
                    String errorInfo = openRedResponse.getBase().getErrorInfo();
                    if (null != openIv && null != avatarIv && null != tipTv) {
                        openIv.setVisibility(View.GONE);
                        avatarIv.setVisibility(View.VISIBLE);
                        try {
                            imageLoader.displayImage(luckySenderUserface, avatarIv, CacheManager.circleImageOptions);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (!TextUtils.isEmpty(errorInfo)) {
                            tipTv.setText(errorInfo);
                        } else {
                            tipTv.setVisibility(View.GONE);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onFailure(int type, String msg) {
        if (TaskManager.getInstance().exist(type))
            TaskManager.getInstance().removeTask(type);
        if (null != materialDialogsUtil) {
            materialDialogsUtil.dismiss();
        }
        if (!TextUtils.isEmpty(msg))
            ToastUtil.showToast(context, msg);
    }

    @Override
    public void cacheData(int type, Object data) {

    }
}

