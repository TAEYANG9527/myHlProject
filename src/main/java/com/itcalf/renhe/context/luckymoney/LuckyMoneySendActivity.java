package com.itcalf.renhe.context.luckymoney;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;

import com.alibaba.wukong.im.Conversation;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.context.pay.PayUtil;
import com.itcalf.renhe.context.pay.PayWaySelectDialogUtil;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.Button;
import com.itcalf.renhe.view.EditText;
import com.itcalf.renhe.view.TextView;
import com.itcalf.renhe.view.WebViewActWithTitle;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.orhanobut.logger.Logger;

import java.math.BigDecimal;
import java.text.MessageFormat;

import butterknife.BindView;
import butterknife.OnClick;
import cn.renhe.heliao.idl.money.pay.ConfirmPayStatusResponse;
import cn.renhe.heliao.idl.money.pay.PayBizType;
import cn.renhe.heliao.idl.money.pay.PayMethod;
import cn.renhe.heliao.idl.money.red.HeliaoSendRed;

/**
 * Created by wangning on 2016/5/6.
 */
public class LuckyMoneySendActivity extends BaseActivity implements PayUtil.PayCallBack, PayWaySelectDialogUtil.SelectPayWayUtilCallBack {
    @BindView(R.id.lucky_num_tv)
    TextView luckyNumTv;
    @BindView(R.id.lucky_num_et)
    EditText luckyNumEt;
    @BindView(R.id.num_tv)
    TextView numTv;
    @BindView(R.id.lucky_num_ll)
    LinearLayout luckyNumLl;
    @BindView(R.id.circle_membernum_tip_tv)
    TextView circleMembernumTipTv;
    @BindView(R.id.lucky_totalmoney_tv)
    TextView luckyTotalmoneyTv;
    @BindView(R.id.totalmoney_et)
    EditText totalmoneyEt;
    @BindView(R.id.totalmoney_tv)
    TextView totalmoneyTv;
    @BindView(R.id.lucky_totalmoney_ll)
    LinearLayout luckyTotalmoneyLl;
    @BindView(R.id.lucky_type_tip_tv)
    TextView luckyTypeTipTv;
    @BindView(R.id.lucky_type_transform_tv)
    TextView luckyTypeTransformTv;
    @BindView(R.id.lucky_totalmoney_type_ll)
    LinearLayout luckyTotalmoneyTypeLl;
    @BindView(R.id.leave_note_et)
    EditText leaveNoteEt;
    @BindView(R.id.lucky_single_leave_message_et)
    EditText luckySingleLeaveMessageEt;
    @BindView(R.id.lucky_single_leave_message_ll)
    LinearLayout luckySingleLeaveMessageLl;
    @BindView(R.id.totalmoney_to_send_tv)
    android.widget.TextView totalmoneyToSendTv;
    @BindView(R.id.send_bt)
    Button sendBt;
    @BindView(R.id.lucky_normal_totalmoney_ll)
    LinearLayout luckyNormalTotalmoneyLl;
    @BindView(R.id.totalmoney_normal_et)
    EditText totalmoneyNormalEt;
    @BindView(R.id.lucky_error_tip_tv)
    TextView luckyErrorTipTv;
    @BindView(R.id.lucky_normal_totalmoney_tv)
    TextView luckyNormalTotalmoneyTv;
    @BindView(R.id.totalmoney_normal_tv)
    TextView totalmoneyNormalTv;
    @BindView(R.id.bottom_tip_tv)
    TextView bottomTipTv;
    //常量
    private int ID_TASK_LUCKY_CONFIG = TaskManager.getTaskId();//发红包之前获取配置信息
    private int ID_TASK_LUCKY_SEND = TaskManager.getTaskId();//发红包
    private int ID_TASK_LUCKY_PAY = TaskManager.getTaskId();//确认支付的红包
    private final static int LUCKYMONEY_TYPE_SINGLE = 0;//定向单个红包
    private final static int LUCKYMONEY_TYPE_PIN = 1;//拼手气红包
    private final static int LUCKYMONEY_TYPE_NORMAL = 2;//普通红包
    private final static int LUCKYMONEY_DEFAULT_MAX_COUNT = 100;//（群聊）默认红包发送的最大个数
    private final static int LUCKYMONEY_TOTALMONEY_EDIT_MAX_COUNT = 12;//红包总金额（拼手气）、单个红包金额（普通红包）输入框可输入的最大字数
    private final static String LUCKYMONEY_SINGLE_MIN = "0.01";//单个红包金额最低金额
    private final static String LUCKYMONEY_SINGLE_MAX = "200";//单个红包金额最高金额
    public static final int PAY_TYPE_BALANCE = 1;
    public static final int PAY_TYPE_WEIXIN = 2;
    public static final int PAY_TYPE_ALI = 3;
    //数据
    private int luckyMoneyType = LUCKYMONEY_TYPE_PIN;//红包类型，0 定向单个红包 1 拼手气红包 2 普通红包 default是1
    private String conversationId;//悟空会话的conversationId
    private int conversationType;//悟空会话的类型 单聊/群聊
    private int conversationGroupNum;//悟空会话的类型 群聊总人数
    private int luckyMoneyMaxCount = LUCKYMONEY_DEFAULT_MAX_COUNT;//（群聊）红包发送的最大个数
    private BigDecimal minSingleLuckyMoney;//单个红包最小金额
    private BigDecimal maxSingleLuckyMoney;//单个红包最大金额
    private boolean luckyMoneyCountOverLimit = false;//输入的红包个数是否超过最大限制
    private boolean luckyMoneySingleOverMinLimit = false;//输入的单个红包金额低于最低金额
    private boolean luckyMoneySingleOverMaxLimit = false;//输入的单个红包金额高于最高金额
    private boolean luckyMoneyTotalOverMaxLimit = false;//输入的红包总金额高于单次支付最高金额
    private double maxTotalLuckyMoneyDouble;
    private int luckyMoneyCount;//红包个数
    private BigDecimal totalMoneyBigDecimal;//红包总金额
    private PayMethod luckyPayWayType = PayMethod.WEIXIN;//支付方式,默认是微信支付
    private String luckySid;//要发送的红包的sid
    private String payPassword;//余额支付时输入的密码
    private HeliaoSendRed.SendRedV2Response sendRedResponse;//服务端返回的支付数据
    private PayBizType payBizType;//发起支付的业务类型
    //工具
    private ImageLoader imageLoader;
    private PayUtil payUtil;
    private PayWaySelectDialogUtil payWaySelectDialogUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.luckymoney_send_layout);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem helpItem = menu.findItem(R.id.item_help);
        helpItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        helpItem.setVisible(true);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_help:
                Intent intent = new Intent(LuckyMoneySendActivity.this, WebViewActWithTitle.class);
                intent.putExtra("url", Constants.LUCKY_HELP_URL);
                startHlActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void findView() {
        super.findView();
        setTextValue("发红包");
        sendBt.setEnabled(false);
        luckyNumEt.postDelayed(new Runnable() {
            @Override
            public void run() {
                luckyNumEt.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        }, 500);
        totalmoneyNormalEt.postDelayed(new Runnable() {
            @Override
            public void run() {
                totalmoneyNormalEt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            }
        }, 500);
    }

    @Override
    protected void initListener() {
        super.initListener();
        luckyNumEt.addTextChangedListener(new editTextWatcher(luckyNumEt.getId()));
        totalmoneyEt.addTextChangedListener(new editTextWatcher(totalmoneyEt.getId()));
        totalmoneyNormalEt.addTextChangedListener(new editTextWatcher(totalmoneyNormalEt.getId()));
    }

    @Override
    protected void initData() {
        super.initData();
        this.payUtil = new PayUtil(this, this);
        this.imageLoader = ImageLoader.getInstance();
        this.payWaySelectDialogUtil = new PayWaySelectDialogUtil(this, materialDialogsUtil, this);
        conversationId = getIntent().getStringExtra("conversationId");
        conversationType = getIntent().getIntExtra("conversationType", Conversation.ConversationType.UNKNOWN);
        conversationGroupNum = getIntent().getIntExtra("conversationGroupNum", 1);
        initViewByConversationType();
        if (!TextUtils.isEmpty(conversationId)) {
            minSingleLuckyMoney = new BigDecimal(LUCKYMONEY_SINGLE_MIN);
            maxSingleLuckyMoney = new BigDecimal(LUCKYMONEY_SINGLE_MAX);
            totalMoneyBigDecimal = new BigDecimal("0.00");
            totalMoneyBigDecimal = totalMoneyBigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);//设置精度
            //设置最大字数
            InputFilter[] filters = {new InputFilter.LengthFilter(LUCKYMONEY_TOTALMONEY_EDIT_MAX_COUNT)};
            totalmoneyEt.setFilters(filters);
            totalmoneyNormalEt.setFilters(filters);
            getLuckyMoneyConfig(conversationId);
        } else {
            ToastUtil.showToast(this, getString(R.string.lucky_money_send_infoerror_tip));
            finish();
        }
    }

    private void initViewByConversationType() {
        if (conversationType == Conversation.ConversationType.CHAT) {
            luckyMoneyType = LUCKYMONEY_TYPE_SINGLE;
            luckyNumLl.setVisibility(View.GONE);
            circleMembernumTipTv.setVisibility(View.GONE);
            luckyTotalmoneyLl.setVisibility(View.GONE);
            leaveNoteEt.setVisibility(View.GONE);
            luckySingleLeaveMessageLl.setVisibility(View.VISIBLE);
            luckyTotalmoneyTypeLl.setVisibility(View.GONE);
        } else if (conversationType == Conversation.ConversationType.GROUP) {
            luckyNumLl.setVisibility(View.VISIBLE);
            circleMembernumTipTv.setVisibility(View.VISIBLE);
            circleMembernumTipTv.setText("本群共" + conversationGroupNum + "人");
            luckyTotalmoneyLl.setVisibility(View.VISIBLE);
            leaveNoteEt.setVisibility(View.VISIBLE);
            luckySingleLeaveMessageLl.setVisibility(View.GONE);
            luckyTotalmoneyTypeLl.setVisibility(View.VISIBLE);
        } else {
            ToastUtil.showToast(this, getString(R.string.lucky_money_send_infoerror_tip));
            finish();
        }
    }

    @Override
    public void onSuccess(int taskId, Object result) {
        super.onSuccess(taskId, result);
        hideMaterialLoadingDialog();
        if (null != result) {
            if (result instanceof HeliaoSendRed.RedConfigResponse) {
                HeliaoSendRed.RedConfigResponse redConfigResponse = (HeliaoSendRed.RedConfigResponse) result;
                if (redConfigResponse.getMaxRedCount() > 0) {
                    luckyMoneyMaxCount = redConfigResponse.getMaxRedCount();
                    if (luckyMoneyMaxCount >= 100 && luckyMoneyMaxCount < 1000) {
                        //设置最大字数
                        InputFilter[] filters = {new InputFilter.LengthFilter(3)};
                        luckyNumEt.setFilters(filters);
                    } else if (luckyMoneyMaxCount >= 10 && luckyMoneyMaxCount < 100) {
                        //设置最大字数
                        InputFilter[] filters = {new InputFilter.LengthFilter(2)};
                        luckyNumEt.setFilters(filters);
                    }
                }
                if (!TextUtils.isEmpty(redConfigResponse.getMinRedAmount()))
                    minSingleLuckyMoney = new BigDecimal(redConfigResponse.getMinRedAmount());
                if (!TextUtils.isEmpty(redConfigResponse.getMaxRedAmount())) {
                    maxSingleLuckyMoney = new BigDecimal(redConfigResponse.getMaxRedAmount());
                    maxTotalLuckyMoneyDouble = mul(maxSingleLuckyMoney.toString(), luckyMoneyMaxCount + "");
                }
                bottomTipTv.setVisibility(View.VISIBLE);
                bottomTipTv.setText(MessageFormat.format(getString(R.string.lucky_money_send_bottom_tip), minSingleLuckyMoney.toString(), maxSingleLuckyMoney.toString()));
                if (!TextUtils.isEmpty(redConfigResponse.getDefaultNote())) {
                    leaveNoteEt.setHint(redConfigResponse.getDefaultNote());
                    luckySingleLeaveMessageEt.setHint(redConfigResponse.getDefaultNote());
                }
                if (redConfigResponse.getNoteMaxLength() > 0) {
                    //设置最大字数
                    InputFilter[] filters = {new InputFilter.LengthFilter(redConfigResponse.getNoteMaxLength())};
                    leaveNoteEt.setFilters(filters);
                    luckySingleLeaveMessageEt.setFilters(filters);
                }
            } else if (result instanceof HeliaoSendRed.SendRedV2Response) {
                sendRedResponse = (HeliaoSendRed.SendRedV2Response) result;
                luckyPayWayType = sendRedResponse.getDefaultPayMethod();
                luckySid = sendRedResponse.getRedSid();
                payBizType = sendRedResponse.getBizType();
                payWaySelectDialogUtil.showPayWaySelectCustomDialog(PayUtil.PAY_BIZ_TYPE_LUCKYMONEY_GOODNAME,
                        sendRedResponse.getPayAmount(), sendRedResponse.getBalance(), sendRedResponse.getHasPayPassword(),
                        sendRedResponse.getPayMethodList(), luckyPayWayType);
            } else if (result instanceof ConfirmPayStatusResponse) { //确认支付成功
                payWaySelectDialogUtil.hideDialog();
                //确认支付成功
                ToastUtil.showToast(LuckyMoneySendActivity.this, "发送成功");
                finish();
            }
        }
    }

    @Override
    public void onFailure(int type, String msg) {
        super.onFailure(type, msg);
        hideMaterialLoadingDialog();
        if (type == ID_TASK_LUCKY_PAY) {
            if (luckyPayWayType == PayMethod.BALANCE) {
                payWaySelectDialogUtil.hideDialog();
                payWaySelectDialogUtil.showPayByBalanceErrorDialog();
            } else {
                ToastUtil.showToast(this, msg);
            }
        } else {
            ToastUtil.showToast(this, msg);
        }

    }

    @OnClick({R.id.lucky_type_transform_tv, R.id.send_bt})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lucky_type_transform_tv:
                transformLuckyMoneyType();
                break;
            case R.id.send_bt:
                sendLucky();
                break;
        }
    }

    /**
     * 切换红包类型，普通红包/拼手气红包
     */
    private void transformLuckyMoneyType() {
        if (luckyMoneyType == LUCKYMONEY_TYPE_PIN) {
            //切换动画
            transformAnimation(luckyTotalmoneyLl, luckyNormalTotalmoneyLl);
            luckyMoneyType = LUCKYMONEY_TYPE_NORMAL;
            luckyTypeTipTv.setText(getString(R.string.lucky_money_send_normal_tip));
            luckyTypeTransformTv.setText(getString(R.string.lucky_money_send_transform_pin_tip));
        } else if (luckyMoneyType == LUCKYMONEY_TYPE_NORMAL) {
            //切换动画
            transformAnimation(luckyNormalTotalmoneyLl, luckyTotalmoneyLl);
            luckyMoneyType = LUCKYMONEY_TYPE_PIN;
            luckyTypeTipTv.setText(getString(R.string.lucky_money_send_pin_tip));
            luckyTypeTransformTv.setText(getString(R.string.lucky_money_send_transform_normal_tip));
        }
        checkLuckyMoneyIfValid();
        initSendLuckyMoney();
    }

    /**
     * 切换动画
     *
     * @param outView 退出的viwe
     * @param inView  进入的view
     */
    private void transformAnimation(View outView, View inView) {
        ViewGroup parent = (ViewGroup) luckyTotalmoneyLl.getParent();
        int distance = parent.getWidth() - luckyTotalmoneyLl.getLeft();
        //切换动画
        ObjectAnimator animatorAlphaOut = ObjectAnimator.ofFloat(outView, "alpha", 1, 0);
        ObjectAnimator animatorTransalteOut = ObjectAnimator.ofFloat(outView, "translationX", 0, -luckyTotalmoneyLl.getRight());
        ObjectAnimator animatorAlphaIn = ObjectAnimator.ofFloat(inView, "alpha", 0, 1);
        ObjectAnimator animatorTransalteIn = ObjectAnimator.ofFloat(inView, "translationX", distance, 0);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(animatorAlphaOut).with(animatorTransalteOut).with(animatorAlphaIn).with(animatorTransalteIn);
        animSet.setDuration(300);
        animSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animSet.start();
    }

    /**
     * 获取红包配置
     */

    private void getLuckyMoneyConfig(String converstaionId) {
        if (checkGrpcBeforeInvoke(ID_TASK_LUCKY_CONFIG)) {
            showMaterialLoadingDialog();
            grpcController.getLuckyMoneyConfig(ID_TASK_LUCKY_CONFIG, converstaionId);
        }
    }

    /**
     * 发布红包(获取将要发送的红包的相关信息，用这些信息进行支付、发送)
     */

    private void sendLucky() {
        if (checkGrpcBeforeInvoke(ID_TASK_LUCKY_SEND)) {
            showMaterialLoadingDialog();
            if (null != totalMoneyBigDecimal) {
                String luckyNote = "";
                if (luckyMoneyType == LUCKYMONEY_TYPE_SINGLE) {
                    luckyMoneyCount = 1;
                    luckyNote = luckySingleLeaveMessageEt.getText().toString().trim();
                    if (TextUtils.isEmpty(luckyNote))
                        luckyNote = luckySingleLeaveMessageEt.getHint().toString().trim();
                } else {
                    luckyNote = leaveNoteEt.getText().toString().trim();
                    if (TextUtils.isEmpty(luckyNote))
                        luckyNote = leaveNoteEt.getHint().toString().trim();
                }
                grpcController.sendLucky(ID_TASK_LUCKY_SEND, luckyMoneyType, luckyMoneyCount, totalMoneyBigDecimal.toString(), luckyNote, conversationId);
            } else {
                hideMaterialLoadingDialog();
            }
        }
    }

    /**
     * 确认支付的红包
     */
//
//    private void payLuckyMoney(String luckySid, String payPassword) {
//        if (TextUtils.isEmpty(payPassword))
//            payPassword = "";
//        int luckyPayType = PAY_TYPE_WEIXIN;
//        switch (luckyPayWayType) {
//            case BALANCE:
//                luckyPayType = PAY_TYPE_BALANCE;
//                break;
//            case WEIXIN:
//                luckyPayType = PAY_TYPE_WEIXIN;
//                break;
//            case ALIPAY:
//                luckyPayType = PAY_TYPE_ALI;
//                break;
//            default:
//                break;
//        }
//        if (checkGrpcBeforeInvoke(ID_TASK_LUCKY_PAY)) {
//            showMaterialLoadingDialog();
//            grpcController.payLuckyMoney(ID_TASK_LUCKY_PAY, luckySid, luckyPayType, payPassword);
//        }
//    }

    /**
     * 确认支付的红包
     */

    private void confirmPayStatus() {
        if (!TextUtils.isEmpty(luckySid) && payBizType.getNumber() >= 0) {
            if (TextUtils.isEmpty(payPassword))
                payPassword = "";
            if (checkGrpcBeforeInvoke(ID_TASK_LUCKY_PAY)) {
                showMaterialLoadingDialog();
                grpcController.confirmPayStatus(ID_TASK_LUCKY_PAY, luckySid, payBizType);
            }
        } else {
            ToastUtil.showToast(this, R.string.pay_error_tip);
        }
    }

    /**
     * 检查输入的红包是否合法
     */
    private void checkLuckyMoneyIfValid() {
        try {
            String luckyMoneyCountString = luckyNumEt.getText().toString().trim();
            String totalMoneyString = totalmoneyEt.getText().toString().trim();
            String normalSingleMoneyString = totalmoneyNormalEt.getText().toString().trim();
            if (TextUtils.isEmpty(luckyMoneyCountString)) {
                if (luckyMoneyType == LUCKYMONEY_TYPE_NORMAL || luckyMoneyType == LUCKYMONEY_TYPE_PIN)
                    changeSendBtState(false);
                luckyMoneyCountOverLimit = false;
                luckyMoneySingleOverMaxLimit = false;
                luckyMoneySingleOverMinLimit = false;
                if (luckyMoneyType == LUCKYMONEY_TYPE_PIN) {
                    if (TextUtils.isEmpty(totalmoneyEt.getText().toString().trim())) {
                        luckyMoneyTotalOverMaxLimit = false;
                    } else {
                        if (sub(String.valueOf(maxTotalLuckyMoneyDouble), totalmoneyEt.getText().toString().trim()) < 0) {//输入的总金额大于规定的单次支付金额最高额度
                            luckyMoneyTotalOverMaxLimit = true;
                        } else {
                            luckyMoneyTotalOverMaxLimit = false;
                        }
                    }
                } else if (luckyMoneyType == LUCKYMONEY_TYPE_NORMAL || luckyMoneyType == LUCKYMONEY_TYPE_SINGLE) {
                    String moneyString = totalmoneyNormalEt.getText().toString().trim();
                    String maxSingleLuckyMoneyString = maxSingleLuckyMoney.toString();
                    String minSingleLuckyMoneyString = minSingleLuckyMoney.toString();
                    if (TextUtils.isEmpty(moneyString)) {
                        changeSendBtState(false);
                        luckyMoneySingleOverMaxLimit = false;
                        luckyMoneySingleOverMinLimit = false;
                    } else {
                        if (sub(moneyString, maxSingleLuckyMoneyString) > 0) {//输入的单个金额大于规定的单个金额额度
                            luckyMoneySingleOverMaxLimit = true;
                        } else {
                            luckyMoneySingleOverMaxLimit = false;
                            if (sub(moneyString, minSingleLuckyMoneyString) < 0) {//输入的单个金额小于规定的单个金额最低额度
                                luckyMoneySingleOverMinLimit = true;
                            } else {
                                luckyMoneySingleOverMinLimit = false;
                            }
                        }
                    }
                }
            } else {
                int count = Integer.parseInt(luckyMoneyCountString);
                if (count > luckyMoneyMaxCount) {
                    luckyMoneyCountOverLimit = true;
                } else {
                    luckyMoneyCountOverLimit = false;
                    if (luckyMoneyType == LUCKYMONEY_TYPE_PIN)
                        checkLuckySingelMoneyIfValid(totalMoneyString);
                    if (luckyMoneyType == LUCKYMONEY_TYPE_NORMAL)
                        checkLuckySingelMoneyIfValid(normalSingleMoneyString);
                }
            }
            if (TextUtils.isEmpty(luckyMoneyCountString)) {
                changeSendBtState(false);
            }
            if (luckyMoneyType == LUCKYMONEY_TYPE_PIN) {
                if (TextUtils.isEmpty(totalMoneyString)) {
                    changeSendBtState(false);
                }
            } else if (luckyMoneyType == LUCKYMONEY_TYPE_NORMAL) {
                if (TextUtils.isEmpty(normalSingleMoneyString)) {
                    changeSendBtState(false);
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            luckyMoneyCountOverLimit = true;
        } finally {
            checkIfNeedShowErrorTip();
        }
    }

    /**
     * 检查单个红包金额是否合法（default : 0.01 =< single <= 200）
     *
     * @param moneyString 总金额，单个红包金额
     */

    private void checkLuckySingelMoneyIfValid(String moneyString) {
        try {
            String maxSingleLuckyMoneyString = maxSingleLuckyMoney.toString();
            String minSingleLuckyMoneyString = minSingleLuckyMoney.toString();
            if (luckyMoneyType == LUCKYMONEY_TYPE_PIN) {
                if (TextUtils.isEmpty(moneyString)) {
                    changeSendBtState(false);
                    luckyMoneySingleOverMaxLimit = false;
                    luckyMoneySingleOverMinLimit = false;
                    luckyMoneyTotalOverMaxLimit = false;
                } else {
                    if (sub(String.valueOf(maxTotalLuckyMoneyDouble), moneyString) < 0) {//输入的总金额大于规定的单次支付金额最高额度
                        luckyMoneyTotalOverMaxLimit = true;
                    } else {
                        luckyMoneyTotalOverMaxLimit = false;
                    }
                    double singleMoney = div(moneyString, luckyNumEt.getText().toString().trim(), 5);
                    if (sub(String.valueOf(singleMoney), maxSingleLuckyMoneyString) > 0) {//输入的单个金额大于规定的单个金额最高额度
                        luckyMoneySingleOverMaxLimit = true;
                    } else {
                        if (sub(String.valueOf(singleMoney), minSingleLuckyMoneyString) < 0) {//输入的单个金额小于规定的单个金额最低额度
                            luckyMoneySingleOverMinLimit = true;
                        } else {
                            luckyMoneySingleOverMinLimit = false;
                        }
                        luckyMoneySingleOverMaxLimit = false;
                    }
                }
            } else if (luckyMoneyType == LUCKYMONEY_TYPE_NORMAL) {
                if (TextUtils.isEmpty(moneyString)) {
                    changeSendBtState(false);
                    luckyMoneySingleOverMaxLimit = false;
                    luckyMoneySingleOverMinLimit = false;
                } else {
                    if (sub(moneyString, maxSingleLuckyMoneyString) > 0) {//输入的单个金额大于规定的单个金额额度
                        luckyMoneySingleOverMaxLimit = true;
                    } else {
                        luckyMoneySingleOverMaxLimit = false;
                        if (sub(moneyString, minSingleLuckyMoneyString) < 0) {//输入的单个金额小于规定的单个金额最低额度
                            luckyMoneySingleOverMinLimit = true;
                        } else {
                            luckyMoneySingleOverMinLimit = false;
                        }
                    }
                }
            }
        } catch (NumberFormatException | IllegalAccessException e) {
            e.printStackTrace();
            luckyMoneySingleOverMaxLimit = true;
            luckyMoneySingleOverMinLimit = false;
        }
    }

    private void checkIfNeedShowErrorTip() {
        if (luckyMoneyCountOverLimit) {
            changeSendBtState(false);
            if (luckyErrorTipTv.getVisibility() != View.VISIBLE || !luckyErrorTipTv.getText().toString().trim().contains("一次最多")) {
                showLuckyMoneyErrorInfo(R.string.lucky_money_send_max_count_tip, luckyMoneyMaxCount + "");
            }
            changeViewColor(luckyNumTv, luckyNumEt, numTv, true);
        } else {
            changeViewColor(luckyNumTv, luckyNumEt, numTv, false);
            if (luckyMoneyTotalOverMaxLimit && luckyMoneyType == LUCKYMONEY_TYPE_PIN) {
                changeSendBtState(false);
                if (luckyErrorTipTv.getVisibility() != View.VISIBLE || !luckyErrorTipTv.getText().toString().trim().contains("单次支付总额不可超过"))
                    showLuckyMoneyErrorInfo(R.string.lucky_money_send_total_money_max_tip, String.valueOf(maxTotalLuckyMoneyDouble));
                changeViewColor(luckyTotalmoneyTv, totalmoneyEt, totalmoneyTv, true);
            } else {
                if (luckyErrorTipTv.getVisibility() == View.VISIBLE && luckyErrorTipTv.getText().toString().trim().contains("单次支付总额不可超过"))
                    hideLuckyMoneyErrorInfo();
                changeViewColor(luckyTotalmoneyTv, totalmoneyEt, totalmoneyTv, false);
                if (!luckyMoneySingleOverMinLimit && !luckyMoneySingleOverMaxLimit) {
                    if (luckyMoneyType == LUCKYMONEY_TYPE_PIN) {
                        if (!TextUtils.isEmpty(totalmoneyEt.getText().toString().trim()) && !TextUtils.isEmpty(luckyNumEt.getText().toString().trim())) {
                            changeSendBtState(true);
                        }
                    } else if (luckyMoneyType == LUCKYMONEY_TYPE_NORMAL) {
                        if (!TextUtils.isEmpty(totalmoneyNormalEt.getText().toString().trim()) && !TextUtils.isEmpty(luckyNumEt.getText().toString().trim())) {
                            changeSendBtState(true);
                        }
                    } else if (luckyMoneyType == LUCKYMONEY_TYPE_SINGLE) {
                        if (!TextUtils.isEmpty(totalmoneyNormalEt.getText().toString().trim())) {
                            changeSendBtState(true);
                        }
                    }
                    changeViewColor(luckyTotalmoneyTv, totalmoneyEt, totalmoneyTv, false);
                    changeViewColor(luckyNormalTotalmoneyTv, totalmoneyNormalEt, totalmoneyNormalTv, false);
                    if (luckyErrorTipTv.getVisibility() == View.VISIBLE)
                        hideLuckyMoneyErrorInfo();
                } else {
                    changeSendBtState(false);
                    if (luckyMoneySingleOverMinLimit) {
                        if (luckyErrorTipTv.getVisibility() != View.VISIBLE || !luckyErrorTipTv.getText().toString().trim().contains("不可低于"))
                            showLuckyMoneyErrorInfo(R.string.lucky_money_send_single_money_min_tip, minSingleLuckyMoney.toString());
                    } else if (luckyMoneySingleOverMaxLimit) {
                        if (luckyErrorTipTv.getVisibility() != View.VISIBLE || !luckyErrorTipTv.getText().toString().trim().contains("单个红包金额不可超过"))
                            showLuckyMoneyErrorInfo(R.string.lucky_money_send_single_money_max_tip, maxSingleLuckyMoney.toString());
                    }
                    if (luckyMoneyType == LUCKYMONEY_TYPE_PIN) {
                        changeViewColor(luckyTotalmoneyTv, totalmoneyEt, totalmoneyTv, true);
                    } else if (luckyMoneyType == LUCKYMONEY_TYPE_NORMAL || luckyMoneyType == LUCKYMONEY_TYPE_SINGLE) {
                        changeViewColor(luckyNormalTotalmoneyTv, totalmoneyNormalEt, totalmoneyNormalTv, true);
                    }
                }
            }
        }
    }

    /**
     * 动画提示错误信息
     *
     * @param errorResouceId
     * @param info
     */
    private void showLuckyMoneyErrorInfo(int errorResouceId, String info) {
//        if (luckyErrorTipTv.getVisibility() == View.VISIBLE)
//            hideLuckyMoneyErrorInfo();
        int distance = luckyErrorTipTv.getTop() + luckyErrorTipTv.getHeight();
        luckyErrorTipTv.setText(MessageFormat.format(getString(errorResouceId), info));
        //切换动画
        ObjectAnimator animatorAlphaIn = ObjectAnimator.ofFloat(luckyErrorTipTv, "alpha", 0, 1);
        ObjectAnimator animatorTransalteIn = ObjectAnimator.ofFloat(luckyErrorTipTv, "translationY", -distance, 0);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(animatorAlphaIn).with(animatorTransalteIn);
        animSet.setDuration(600);
        animSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animSet.start();
        luckyErrorTipTv.setVisibility(View.VISIBLE);
    }

    private void hideLuckyMoneyErrorInfo() {
        //切换动画
        ObjectAnimator animatorAlphaIn = ObjectAnimator.ofFloat(luckyErrorTipTv, "alpha", 1, 0);
        ObjectAnimator animatorTransalteIn = ObjectAnimator.ofFloat(luckyErrorTipTv, "translationY", 0, -luckyErrorTipTv.getBottom());
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(animatorAlphaIn).with(animatorTransalteIn);
        animSet.setDuration(600);
        animSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animSet.start();
        luckyErrorTipTv.setVisibility(View.INVISIBLE);
    }

    /**
     * 修改“塞钱进红包”按钮的状态
     *
     * @param state
     */
    private void changeSendBtState(boolean state) {
        sendBt.setEnabled(state);
    }

    /**
     * 改变文本颜色以提示用户
     *
     * @param view1
     * @param view2
     * @param view3
     * @param error
     */
    private void changeViewColor(TextView view1, EditText view2, TextView view3, boolean error) {
        if (error) {
            view1.setTextColor(getResources().getColor(R.color.BC_6));
            view2.setTextColor(getResources().getColor(R.color.BC_6));
            view3.setTextColor(getResources().getColor(R.color.BC_6));
        } else {
            view1.setTextColor(getResources().getColor(R.color.black));
            view2.setTextColor(getResources().getColor(R.color.black));
            view3.setTextColor(getResources().getColor(R.color.black));
        }
    }

    /**
     * 初始化要发送的红包的数量和金额
     */
    private void initSendLuckyMoney() {
        String luckyMoneyCountString = luckyNumEt.getText().toString().trim();
        try {
            luckyMoneyCount = Integer.parseInt(luckyMoneyCountString);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            luckyMoneyCount = 0;
        }
        if (luckyMoneyType == LUCKYMONEY_TYPE_PIN) {
            String totalMoneyString = totalmoneyEt.getText().toString().trim();
            if (!TextUtils.isEmpty(totalMoneyString)) {
                try {
                    totalMoneyBigDecimal = new BigDecimal(totalMoneyString);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                totalMoneyBigDecimal = totalMoneyBigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);//设置精度
            } else {
                totalMoneyBigDecimal = new BigDecimal("0.00");
            }
            totalmoneyToSendTv.setText(MessageFormat.format(getString(R.string.lucky_money_send_total_money), totalMoneyBigDecimal.toString()));
        } else if (luckyMoneyType == LUCKYMONEY_TYPE_NORMAL || luckyMoneyType == LUCKYMONEY_TYPE_SINGLE) {
            String normalSingleMoneyString = totalmoneyNormalEt.getText().toString().trim();
            if (luckyMoneyType == LUCKYMONEY_TYPE_NORMAL) {
                if (luckyMoneyCount <= 0 || TextUtils.isEmpty(normalSingleMoneyString)) {
                    totalMoneyBigDecimal = new BigDecimal("0.00");
                } else {
                    try {
                        totalMoneyBigDecimal = new BigDecimal(luckyMoneyCountString).multiply(new BigDecimal(normalSingleMoneyString));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                if (TextUtils.isEmpty(normalSingleMoneyString)) {
                    totalMoneyBigDecimal = new BigDecimal("0.00");
                } else {
                    try {
                        totalMoneyBigDecimal = new BigDecimal(normalSingleMoneyString);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            totalMoneyBigDecimal = totalMoneyBigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);//设置精度
            totalmoneyToSendTv.setText(MessageFormat.format(getString(R.string.lucky_money_send_total_money), totalMoneyBigDecimal.toString()));
        }
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

    /**
     * 提供精确乘法运算的mul方法
     *
     * @param value1 被乘数
     * @param value2 乘数
     * @return 两个参数的积
     */
    public static double mul(String value1, String value2) {
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
        return b1.multiply(b2).doubleValue();
    }

    /**
     * 提供精确的除法运算方法div
     *
     * @param value1 被除数
     * @param value2 除数
     * @param scale  精度，保留小数点之后多少位
     * @return 两个参数的商
     * @throws IllegalAccessException
     */
    public static double div(String value1, String value2, int scale) throws IllegalAccessException {
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
        try {
            return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
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
        if (null == sendRedResponse)
            return;
        Logger.e("payluckyType-->" + luckyPayWayType);
        payUtil.initPay(payType, sendRedResponse.getPayAmount(), PayUtil.PAY_BIZ_TYPE_LUCKYMONEY,
                sendRedResponse.getRedSid(), PayUtil.PAY_BIZ_TYPE_LUCKYMONEY_GOODNAME);
    }

    @Override
    public void onPayPasswordInputed(String psw) {
        payWaySelectDialogUtil.payByBalance(payBizType.getNumber(), luckySid, PayUtil.PAY_BIZ_TYPE_RECHARGE_BALANCE, sendRedResponse.getPayAmount(), psw);
    }

    @Override
    public void onPayPasswordRetry() {
        sendBt.performClick();
    }

    @Override
    public void onPayWayTypeChanged(PayMethod payWayType) {
        luckyPayWayType = payWayType;
    }

    @Override
    public void onPayBalanceSuccess() {
        confirmPayStatus();
    }

    class editTextWatcher implements TextWatcher {

        private int viewId;

        public editTextWatcher(int viewId) {
            this.viewId = viewId;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            switch (viewId) {
                case R.id.lucky_num_et://红包个数
                case R.id.totalmoney_et://拼手气红包，总金额
                case R.id.totalmoney_normal_et://普通红包，单个金额
                    checkLuckyMoneyIfValid();
                    initSendLuckyMoney();
                    break;
                case R.id.leave_note_et://群聊，留言
                    break;
                case R.id.lucky_single_leave_message_et://单聊，留言
                    break;
            }
        }
    }
}
