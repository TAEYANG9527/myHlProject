package com.itcalf.renhe.context.pay;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.luckymoney.ForgetPayPasswordActivity;
import com.itcalf.renhe.context.luckymoney.SetPayPasswordActivity;
import com.itcalf.renhe.dto.MessageBoardOperationWithErroInfo;
import com.itcalf.renhe.utils.DeviceUitl;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.http.okhttp.OkHttpClientManager;
import com.jungly.gridpasswordview.GridPasswordView;
import com.jungly.gridpasswordview.imebugfixer.ImeDelBugFixedEditText;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.okhttp.Request;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.renhe.heliao.idl.money.pay.PayMethod;

/**
 * 选择支付方式dialog的工具类
 * Created by wangning on 2016/6/14.
 */
public class PayWaySelectDialogUtil {
    private Context context;
    private MaterialDialog materialDialog;
    private MaterialDialogsUtil materialDialogsUtil;
    private ImageLoader imageLoader;
    private SelectPayWayUtilCallBack selectPayWayUtilCallBack;

    private PayMethod luckyPayWayType = PayMethod.WEIXIN;//支付方式,默认是微信支付

    public PayWaySelectDialogUtil(Context context, MaterialDialogsUtil materialDialogsUtil,
                                  SelectPayWayUtilCallBack selectPayWayUtilCallBack) {
        this.context = context;
        this.materialDialogsUtil = materialDialogsUtil;
        this.imageLoader = ImageLoader.getInstance();
        this.selectPayWayUtilCallBack = selectPayWayUtilCallBack;
    }

    /**
     * 弹出支付对话框
     *
     * @param payTitle               支付弹框主题，比如“和聊红包”、“红包广告”
     * @param payAmount              需支付总金额
     * @param balance                个人账户余额
     * @param hasPayPassword         针对账户余额，是否设置了支付密码
     * @param defaultLuckyPayWayType 默认勾选支付方式
     */
    public void showPayWaySelectCustomDialog(String payTitle, final String payAmount, String balance,
                                             final boolean hasPayPassword, List<PayMethod> payTypeList,
                                             PayMethod defaultLuckyPayWayType) {
        if (null == materialDialogsUtil)
            materialDialogsUtil = new MaterialDialogsUtil(context);
        this.luckyPayWayType = defaultLuckyPayWayType;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.luckymoney_pay_dialog_customview, null);
        materialDialog = materialDialogsUtil.getCustomViewBuilderByView(dialoglayout,
                0, 0)//不需要确定/取消按钮
                .canceledOnTouchOutside(false)
                .build();
        ImageView avatarIv = (ImageView) dialoglayout.findViewById(R.id.pay_avatar_iv);
        android.widget.TextView totalMoneyTv = (android.widget.TextView) dialoglayout.findViewById(R.id.totalmoney_tv);
        android.widget.TextView payTitleTv = (android.widget.TextView) dialoglayout.findViewById(R.id.pay_title_tv);
        final FrameLayout payFl = (FrameLayout) dialoglayout.findViewById(R.id.pay_fl);
        final FrameLayout selectPayWayFl = (FrameLayout) dialoglayout.findViewById(R.id.select_payway_fl);
        final LinearLayout selectedPayWayLl = (LinearLayout) dialoglayout.findViewById(R.id.selected_payway_ll);
        final LinearLayout selectPayWayLl = (LinearLayout) dialoglayout.findViewById(R.id.select_payway_ll);
        ImageView payCloseIv = (ImageView) dialoglayout.findViewById(R.id.pay_close_iv);
        ImageView selectPayWayCloseIv = (ImageView) dialoglayout.findViewById(R.id.select_payway_close_iv);
        ImageView selectedPayWayLogoIv = (ImageView) dialoglayout.findViewById(R.id.selected_payway_logo_iv);
        android.widget.TextView selectedPayWayNameTv = (android.widget.TextView) dialoglayout.findViewById(R.id.selected_payway_name_tv);
        final GridPasswordView gridPasswordView = (GridPasswordView) dialoglayout.findViewById(R.id.password_view);
        android.widget.TextView setPswTv = (android.widget.TextView) dialoglayout.findViewById(R.id.setpsw_tv2);
        LinearLayout setpswLl = (LinearLayout) dialoglayout.findViewById(R.id.setpsw_ll);
        android.widget.Button payBt = (android.widget.Button) dialoglayout.findViewById(R.id.payBt);
        try {
            imageLoader.displayImage(RenheApplication.getInstance().getUserInfo().getUserface(), avatarIv, CacheManager.circleImageOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }
        payTitleTv.setText(payTitle);
        totalMoneyTv.setText(MessageFormat.format(context.getString(R.string.lucky_money_send_total_money), payAmount));
        boolean balancePayAvaliable = false;//余额支付是否可用
        if (sub(payAmount, balance) <= 0)
            balancePayAvaliable = true;
        initSelectedPayWayView(selectedPayWayLogoIv, selectedPayWayNameTv, hasPayPassword, setpswLl, gridPasswordView, payBt);
        initSelectPayWayDialogView(selectPayWayLl, selectPayWayCloseIv, selectedPayWayLogoIv, selectedPayWayNameTv, payTypeList, balance, hasPayPassword
                , balancePayAvaliable, setpswLl, gridPasswordView, payBt);
        payCloseIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDialog.dismiss();
            }
        });
        selectedPayWayLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transformInAnimation(payFl, selectPayWayFl, payFl);
                gridPasswordView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        DeviceUitl.hideSoftInput(gridPasswordView);
                    }
                }, 300);
            }
        });
        selectPayWayCloseIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transformOutAnimation(selectPayWayFl, payFl, selectPayWayFl);
                showKeyBoard(hasPayPassword, gridPasswordView);
            }
        });
        setPswTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDialog.dismiss();
                context.startActivity(new Intent(context, SetPayPasswordActivity.class));
                ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
        payBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int payType = PayUtil.PAY_TYPE_ALI;
                if (luckyPayWayType == PayMethod.WEIXIN) {
                    payType = PayUtil.PAY_TYPE_WEIXIN;
                } else if (luckyPayWayType == PayMethod.ALIPAY) {
                    payType = PayUtil.PAY_TYPE_ALI;
                }
                selectPayWayUtilCallBack.onPayBtClick(payType);
            }
        });
        gridPasswordView.setOnPasswordChangedListener(new GridPasswordView.OnPasswordChangedListener() {
            @Override
            public void onTextChanged(String psw) {
                if (psw.length() == 6) {
                    selectPayWayUtilCallBack.onPayPasswordInputed(psw);
                }
            }

            @Override
            public void onInputFinish(String psw) {
            }
        });
        materialDialog.show();
        showKeyBoard(hasPayPassword, gridPasswordView);
    }

    /**
     * 初始化默认选中的支付方式View
     *
     * @param selectedPayWayLogoIv
     * @param selectedPayWayNameTv
     */
    private void initSelectedPayWayView(ImageView selectedPayWayLogoIv, android.widget.TextView selectedPayWayNameTv,
                                        boolean hasPayPassword, LinearLayout setpswLl, GridPasswordView gridPasswordView
            , android.widget.Button payBt) {
        switch (luckyPayWayType) {
            case BALANCE:
                selectedPayWayLogoIv.setImageResource(R.drawable.icon_pay_money);
                selectedPayWayNameTv.setText(context.getString(R.string.lucky_money_send_payway_balance));
                if (hasPayPassword) {
                    gridPasswordView.setVisibility(View.VISIBLE);
                    setpswLl.setVisibility(View.GONE);
                } else {
                    gridPasswordView.setVisibility(View.GONE);
                    setpswLl.setVisibility(View.VISIBLE);
                }
                payBt.setVisibility(View.GONE);
                break;
            case WEIXIN:
                selectedPayWayLogoIv.setImageResource(R.drawable.icon_pay_wetchat);
                selectedPayWayNameTv.setText(context.getString(R.string.lucky_money_send_payway_weichat));
                gridPasswordView.setVisibility(View.GONE);
                setpswLl.setVisibility(View.GONE);
                payBt.setVisibility(View.VISIBLE);
                break;
            case ALIPAY:
                selectedPayWayLogoIv.setImageResource(R.drawable.icon_pay_alipay);
                selectedPayWayNameTv.setText(context.getString(R.string.lucky_money_send_payway_alipay));
                gridPasswordView.setVisibility(View.GONE);
                setpswLl.setVisibility(View.GONE);
                payBt.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    /**
     * 初始化选择支付方式界面View
     *
     * @param selectPayWayLl
     * @param selectPayWayCloseIv
     * @param selectedPayWayLogoIv
     * @param selectedPayWayNameTv
     */
    private void initSelectPayWayDialogView(final LinearLayout selectPayWayLl, final ImageView selectPayWayCloseIv,
                                            final ImageView selectedPayWayLogoIv, final android.widget.TextView selectedPayWayNameTv,
                                            final List<PayMethod> payTypeList,
                                            final String balance, final boolean hasPayPassword,
                                            final boolean balancePayAvaliable, final LinearLayout setpswLl,
                                            final GridPasswordView gridPasswordView, final android.widget.Button payBt) {
        if (null != payTypeList && !payTypeList.isEmpty()) {
            selectPayWayLl.removeAllViews();
            for (final PayMethod payType : payTypeList) {
                final View selectPayWayDialogView = LayoutInflater.from(context)
                        .inflate(R.layout.luckymoney_pay_dialog_customview_item_layout, null);
                ImageView payLogoIv = (ImageView) selectPayWayDialogView.findViewById(R.id.pay_logo_iv);
                android.widget.TextView payNameTv = (android.widget.TextView) selectPayWayDialogView.findViewById(R.id.pay_name_tv);
                CheckedTextView payCheckCtv = (CheckedTextView) selectPayWayDialogView.findViewById(R.id.pay_check_ctv);
                switch (payType) {
                    case BALANCE:
                        payLogoIv.setImageResource(R.drawable.icon_pay_money);
                        payNameTv.setText(context.getString(R.string.lucky_money_send_payway_balance) + "(" + balance + "元)");
                        if (luckyPayWayType == PayMethod.BALANCE) {
                            payCheckCtv.setChecked(true);
                        } else {
                            payCheckCtv.setChecked(false);
                        }
                        if (balancePayAvaliable) {
                            selectPayWayDialogView.setEnabled(true);
                            payNameTv.setTextColor(context.getResources().getColor(R.color.black));
                        } else {
                            selectPayWayDialogView.setEnabled(false);
                            payNameTv.setTextColor(context.getResources().getColor(R.color.tran_50_black));
                            payCheckCtv.setChecked(false);
                        }
                        selectPayWayDialogView.setTag(context.getString(R.string.lucky_money_send_payway_balance));
                        break;
                    case WEIXIN:
                        payLogoIv.setImageResource(R.drawable.icon_pay_wetchat);
                        payNameTv.setText(context.getString(R.string.lucky_money_send_payway_weichat));
                        if (luckyPayWayType == PayMethod.WEIXIN) {
                            payCheckCtv.setChecked(true);
                        } else {
                            payCheckCtv.setChecked(false);
                        }
                        selectPayWayDialogView.setTag(context.getString(R.string.lucky_money_send_payway_weichat));
                        break;
                    case ALIPAY:
                        payLogoIv.setImageResource(R.drawable.icon_pay_alipay);
                        payNameTv.setText(context.getString(R.string.lucky_money_send_payway_alipay));
                        if (luckyPayWayType == PayMethod.ALIPAY) {
                            payCheckCtv.setChecked(true);
                        } else {
                            payCheckCtv.setChecked(false);
                        }
                        selectPayWayDialogView.setTag(context.getString(R.string.lucky_money_send_payway_alipay));
                        break;
                    default:
                        break;
                }
                selectPayWayDialogView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (payType) {
                            case BALANCE:
                                selectPayWayUtilCallBack.onPayWayTypeChanged(PayMethod.BALANCE);
                                luckyPayWayType = PayMethod.BALANCE;
                                break;
                            case WEIXIN:
                                selectPayWayUtilCallBack.onPayWayTypeChanged(PayMethod.WEIXIN);
                                luckyPayWayType = PayMethod.WEIXIN;
                                break;
                            case ALIPAY:
                                selectPayWayUtilCallBack.onPayWayTypeChanged(PayMethod.ALIPAY);
                                luckyPayWayType = PayMethod.ALIPAY;
                                break;
                            default:
                                break;
                        }
                        initSelectedPayWayView(selectedPayWayLogoIv, selectedPayWayNameTv, hasPayPassword, setpswLl, gridPasswordView, payBt);
                        selectPayWayCloseIv.performClick();
                        selectPayWayLl.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                initSelectPayWayDialogView(selectPayWayLl, selectPayWayCloseIv,
                                        selectedPayWayLogoIv, selectedPayWayNameTv, payTypeList, balance, hasPayPassword, balancePayAvaliable, setpswLl, gridPasswordView, payBt);
                            }
                        }, 500);
                    }
                });
                selectPayWayLl.addView(selectPayWayDialogView);
            }
        }
    }

    public void showPayByBalanceErrorDialog() {
        final MaterialDialogsUtil materialDialog = new MaterialDialogsUtil(context);
        materialDialog.getBuilder(R.string.material_dialog_title, context.getString(R.string.pay_by_balance_error_dialog_content),
                R.string.pay_by_balance_error_dialog_forgetpsw, R.string.pay_by_balance_error_dialog_retry)
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        switch (which) {
                            case POSITIVE://忘记密码
                                Intent intent = new Intent(context, ForgetPayPasswordActivity.class);
                                context.startActivity(intent);
                                ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                                break;
                            case NEGATIVE://重试
                                selectPayWayUtilCallBack.onPayPasswordRetry();
                                break;
                            default:
                                break;
                        }
                    }
                });
        materialDialog.show();
    }

    /**
     * 切换动画
     *
     * @param outView 退出的viwe
     * @param inView  进入的view
     */

    private void transformInAnimation(View outView, View inView, View rootView) {
        ViewGroup parent = (ViewGroup) rootView.getParent();
        int distance = parent.getWidth() - rootView.getLeft();
        //切换动画
        ObjectAnimator animatorAlphaOut = ObjectAnimator.ofFloat(outView, "alpha", 1, 0);
        ObjectAnimator animatorTransalteOut = ObjectAnimator.ofFloat(outView, "translationX", 0, -rootView.getRight());
        ObjectAnimator animatorAlphaIn = ObjectAnimator.ofFloat(inView, "alpha", 0, 1);
        ObjectAnimator animatorTransalteIn = ObjectAnimator.ofFloat(inView, "translationX", distance, 0);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(animatorAlphaOut).with(animatorTransalteOut).with(animatorAlphaIn).with(animatorTransalteIn);
        animSet.setDuration(300);
        animSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animSet.start();
    }

    /**
     * 切换动画
     *
     * @param outView 退出的viwe
     * @param inView  进入的view
     */
    private void transformOutAnimation(View outView, View inView, View rootView) {
        ViewGroup parent = (ViewGroup) rootView.getParent();
        int distance = parent.getWidth() - rootView.getLeft();
        //切换动画
        ObjectAnimator animatorAlphaOut = ObjectAnimator.ofFloat(outView, "alpha", 1, 0);
        ObjectAnimator animatorTransalteOut = ObjectAnimator.ofFloat(outView, "translationX", 0, distance);
        ObjectAnimator animatorAlphaIn = ObjectAnimator.ofFloat(inView, "alpha", 0, 1);
        ObjectAnimator animatorTransalteIn = ObjectAnimator.ofFloat(inView, "translationX", -distance, 0);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(animatorAlphaOut).with(animatorTransalteOut).with(animatorAlphaIn).with(animatorTransalteIn);
        animSet.setDuration(300);
        animSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animSet.start();
    }

    /**
     * 弹出键盘
     *
     * @param view
     */
    private void showKeyBoard(boolean hasPayPassword, final GridPasswordView view) {
        if (luckyPayWayType == PayMethod.BALANCE && hasPayPassword) {
            view.postDelayed(new Runnable() {
                @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                @Override
                public void run() {
                    if (null != view.getChildAt(0)) {
                        ImeDelBugFixedEditText editText = (ImeDelBugFixedEditText) view.getChildAt(0);
                        editText.setFocusable(true);
                        editText.setFocusableInTouchMode(true);
                        editText.requestFocus();
                        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                    }
                }
            }, 300);
        }
    }

    /**
     * 隐藏dialog
     */
    public void hideDialog() {
        if (null != materialDialog)
            materialDialog.dismiss();
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

    /**
     * 余额支付
     */
    public void payByBalance(int bizType, String bizSid, String subject, String fee, String password) {
        showMaterialLoadingDialog();
        Map<String, Object> reqParams = new HashMap<>();
        reqParams.put("adSId", RenheApplication.getInstance().getUserInfo().getAdSId());
        reqParams.put("sid", RenheApplication.getInstance().getUserInfo().getSid());
        reqParams.put("bizType", bizType);
        reqParams.put("bizSId", bizSid);
        reqParams.put("deviceType", 0);
        reqParams.put("subject", subject);
        reqParams.put("fee", fee);
        reqParams.put("password", password);
        OkHttpClientManager.postAsyn(Constants.Http.PAY_BY_BALANCE, reqParams, MessageBoardOperationWithErroInfo.class, new OkHttpClientManager.ResultCallback() {
            @Override
            public void onError(Request request, Exception e) {
                hideMaterialLoadingDialog();
                hideDialog();
                showPayByBalanceErrorDialog();
            }

            @Override
            public void onResponse(Object response) {
                hideMaterialLoadingDialog();
                if (response != null) {
                    if (response instanceof MessageBoardOperationWithErroInfo) {
                        MessageBoardOperationWithErroInfo messageBoardOperationWithErroInfo = (MessageBoardOperationWithErroInfo) response;
                        switch (messageBoardOperationWithErroInfo.getState()) {
                            case 1:
                                hideDialog();
                                selectPayWayUtilCallBack.onPayBalanceSuccess();
                                break;
                            default:
                                hideDialog();
                                showPayByBalanceErrorDialog();
                                break;
                        }
                    } else {
                        hideDialog();
                        showPayByBalanceErrorDialog();
                    }
                } else {
                    hideDialog();
                    showPayByBalanceErrorDialog();
                }
            }
        });
    }

    /**
     * 提供精确减法运算的sub方法
     *
     * @param value1 被减数
     * @param value2 减数
     * @return 两个参数的差
     */
    public static double sub(String value1, String value2) {
        BigDecimal b1 = null;
        try {
            b1 = new BigDecimal(value1);
        } catch (Exception e) {
            e.printStackTrace();
            b1 = new BigDecimal("0.00");
        }
        BigDecimal b2 = null;
        try {
            b2 = new BigDecimal(value2);
        } catch (Exception e) {
            e.printStackTrace();
            b2 = new BigDecimal("0.00");
        }
        return b1.subtract(b2).doubleValue();
    }

    public interface SelectPayWayUtilCallBack {
        /**
         * 支付按钮点击
         */
        void onPayBtClick(int payType);

        /**
         * 支付密码输入完成后
         */
        void onPayPasswordInputed(String psw);

        /**
         * 支付密码输入失败后重试
         */
        void onPayPasswordRetry();

        /**
         * 当支付方式被修改后
         *
         * @param payWayType
         */
        void onPayWayTypeChanged(PayMethod payWayType);

        /**
         * 当使用余额支付成功后
         */
        void onPayBalanceSuccess();
    }
}
