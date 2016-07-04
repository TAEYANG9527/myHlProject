package com.itcalf.renhe.context.pay;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.command.IPayCommand;
import com.itcalf.renhe.command.impl.PayCommandImpl;
import com.itcalf.renhe.context.pay.alipay.AlipayCommand;
import com.itcalf.renhe.context.pay.alipay.PayResult;
import com.itcalf.renhe.context.pay.wxapi.WeixinPayCommand;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.AliPayResult;
import com.itcalf.renhe.dto.UserInfo;
import com.itcalf.renhe.dto.WeixinPrepay;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.StatisticsUtil;
import com.itcalf.renhe.utils.SystemUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.WebViewActWithTitle;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 选择支付方式
 *
 * @author YZQ
 */
public class ChoosePayWayActivity extends BaseActivity implements OnClickListener {

    private Handler mHandler;

    public class PayResultBean {
        public String goodName;
        public String price;
        public long payTime;
        public int vipType;

        /**
         * @param goodName
         * @param price
         * @param payTime
         */
        public PayResultBean(String goodName, String price, long payTime, int vipType) {
            this.goodName = goodName;
            this.price = price;
            this.payTime = payTime;
            this.vipType = vipType;
        }
    }

    public interface PayCallback {
        /**
         * 支付结果
         *
         * @param type 0微信；1支付宝
         * @param flag 0失败；1成功
         */
        public void onPayResult(int type, int flag);
    }

    public static PayCallback payCallback;
    public static PayResultBean payResultBean;

    private AlipayCommand alipayCommand;
    private WeixinPayCommand weixinPayCommand;

    private int choosePay = 1;//1,支付宝,2，微信；3，其他
    private String goodName;
    private String desc;
    private String fee;
    private double totalFee, payFee, couponFee, originalFee;
    private int bizType;
    private String bizSId;
    private int vipType;

    public static void setPayCallback(PayCallback callback) {
        payCallback = callback;
    }

    /**
     * 启动本Activity
     *
     * @param context
     * @param bizType  支付类型 人和币；付费会员；人脉快递；精准推广
     * @param goodName 商品名称
     * @param desc     描述
     * @see com.itcalf.renhe.Constants#BIZ_TYPE_RENHE_MONEY    人和币
     * @see com.itcalf.renhe.Constants#BIZ_TYPE_MEMBER_PAY     付费会员
     * @see com.itcalf.renhe.Constants#BIZ_TYPE_RENMAI_EXPRESS 人脉快递
     * @see com.itcalf.renhe.Constants#BIZ_TYPE_GENERALIZATION 精准推广
     */
    public static void launch(Activity context, int bizType, String bizSId, String goodName, String desc,
                              double payFee) {
        launch(context, bizType, bizSId, goodName, desc, payFee, payFee, 0, 0, null, -1);
    }

    public static void launch(Activity context, int bizType, String bizSId, String goodName, String desc,
                              double fee,
                              PayCallback callback) {
        launch(context, bizType, bizSId, goodName, desc, fee, fee, 0, 0, callback, -1);
    }

    public static void launch(Activity context, int bizType, String bizSId, String goodName, String desc,
                              double totalFee, double payFee, double couponFee, double originalFee,
                              PayCallback callback, int vipType) {
        payCallback = callback;
        Intent intent = new Intent(context, ChoosePayWayActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("goodName", goodName);
        bundle.putString("desc", desc);
        bundle.putDouble("totalFee", totalFee);
        bundle.putDouble("payFee", payFee);
        bundle.putDouble("couponFee", couponFee);
        bundle.putDouble("originalFee", originalFee);
        bundle.putString("bizSId", bizSId);
        bundle.putInt("bizType", bizType);
        bundle.putInt("vipType", vipType);
        intent.putExtras(bundle);
        context.startActivity(intent);
        context.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    private LinearLayout payWayLogoLl;
    private ImageView payWayLogoIv;
    private TextView payWayFeeTv;
    private RelativeLayout vouchersRl;
    private TextView balanceTv;//当前会员剩余费用
    private TextView realPayFeeTv;//实际付款

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.fragment_choose_pay_way);
    }

    @Override
    protected void findView() {
        super.findView();
        findViewById(R.id.rl_zhifubao_pay).setOnClickListener(this);
        findViewById(R.id.rl_weixin_pay).setOnClickListener(this);
        findViewById(R.id.rl_other_pay).setOnClickListener(this);
        payWayLogoLl = (LinearLayout) findViewById(R.id.pay_way_logo_ll);
        payWayLogoIv = (ImageView) findViewById(R.id.pay_way_logo_iv);
        payWayFeeTv = (TextView) findViewById(R.id.pay_way_fee_tv);
        vouchersRl = (RelativeLayout) findViewById(R.id.vouchers_Rl);
        balanceTv = (TextView) findViewById(R.id.balance_Tv);
        realPayFeeTv = (TextView) findViewById(R.id.fragment_choose_pay_way_tv_fee);
        weixinPayCommand = new WeixinPayCommand(this);
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            goodName = bundle.getString("goodName");
            desc = bundle.getString("desc");
            totalFee = bundle.getDouble("totalFee", 0);
            payFee = bundle.getDouble("payFee", 0);
            couponFee = bundle.getDouble("couponFee", 0);
            originalFee = bundle.getDouble("originalFee", 0);
            bizSId = bundle.getString("bizSId");
            bizType = bundle.getInt("bizType");
            vipType = bundle.getInt("vipType");
            int color = R.color.upgrade_pt_bcg_start_color;
            switch (vipType) {
                case 1:
                    setTextValue(getString(R.string.upgrade_VIP));
                    payWayLogoLl.setBackgroundResource(R.drawable.upgrade_vip_bcg_shape);
                    payWayLogoIv.setImageResource(R.drawable.upgrade_pay_way_vip);
                    color = R.color.upgrade_vip_bcg_start_color;
                    break;
                case 2:
                    setTextValue(getString(R.string.upgrade_GOLD));
                    payWayLogoLl.setBackgroundResource(R.drawable.upgrade_gold_bcg_shape);
                    payWayLogoIv.setImageResource(R.drawable.upgrade_pay_way_gold);
                    color = R.color.upgrade_gold_bcg_start_color;
                    break;
                case 3:
                    setTextValue(getString(R.string.upgrade_PT));
                    payWayLogoLl.setBackgroundResource(R.drawable.upgrade_pt_bcg_shape);
                    payWayLogoIv.setImageResource(R.drawable.upgrade_pay_way_pt);
                    color = R.color.upgrade_pt_bcg_start_color;
                    break;
                default:
                    setTextValue(getString(R.string.upgrade_default));
                    payWayLogoLl.setBackgroundResource(R.drawable.upgrade_pt_bcg_shape);
                    payWayLogoIv.setVisibility(View.INVISIBLE);
                    color = R.color.upgrade_pt_bcg_start_color;
                    break;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                SystemUtil.changeStatusBarColor(this, color);
            }
            if (Constants.DEBUG_MODE && TextUtils.isEmpty(goodName)) {
                throw new IllegalArgumentException("商品名称不能为空");
            }
            fee = payFee + "";
            if (vipType > 0) {
                payWayFeeTv.setVisibility(View.VISIBLE);
                payWayFeeTv.setText(MessageFormat.format(getString(R.string.upgrade_fee), totalFee));
            } else {
                payWayFeeTv.setVisibility(View.INVISIBLE);
            }
            //抵扣
            if (originalFee > 0) {
                vouchersRl.setVisibility(View.VISIBLE);
                balanceTv.setText("-￥" + originalFee);
            } else {
                vouchersRl.setVisibility(View.GONE);
            }
            //实付款
            realPayFeeTv.setText("￥" + fee);
            payResultBean = new PayResultBean(goodName, fee, System.currentTimeMillis(), bundle.getInt("vipType"));
        }
    }

    @Override
    protected void initData() {
        super.initData();
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case AlipayCommand.SDK_PAY_FLAG: {
                        PayResult payResult = new PayResult((String) msg.obj);

                        // 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
                        String resultInfo = payResult.getResult();

                        String resultStatus = payResult.getResultStatus();
                        // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                        switch (resultStatus) {
                            case Constants.AlipayCode.ALIPAY_SUCCESS:// 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                                Toast.makeText(ChoosePayWayActivity.this, "支付成功", Toast.LENGTH_SHORT).show();
                                if (payCallback != null) {
                                    payCallback.onPayResult(1, 1);
                                }
                                break;
                            case Constants.AlipayCode.ALIPAY_HANDLING:// 判断resultStatus 为非“9000”则代表可能支付失败
                                // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                                Toast.makeText(ChoosePayWayActivity.this, "支付结果确认中", Toast.LENGTH_SHORT).show();
                                break;
                            case Constants.AlipayCode.ALIPAY_FAIL:
                                Toast.makeText(ChoosePayWayActivity.this, "支付失败", Toast.LENGTH_SHORT).show();
                                if (payCallback != null) {
                                    payCallback.onPayResult(1, 0);
                                }
                                break;
                            case Constants.AlipayCode.ALIPAY_USER_CANCLE:
                                Toast.makeText(ChoosePayWayActivity.this, "取消支付", Toast.LENGTH_SHORT).show();
                                if (payCallback != null) {
                                    payCallback.onPayResult(1, 0);
                                }
                                break;
                            case Constants.AlipayCode.ALIPAY_NETWORK_ERROR:
                                Toast.makeText(ChoosePayWayActivity.this, "网络连接出错，支付失败", Toast.LENGTH_SHORT).show();
                                if (payCallback != null) {
                                    payCallback.onPayResult(1, 0);
                                }
                                break;
                            default:
                                if (payCallback != null) {
                                    payCallback.onPayResult(1, 0);
                                }
                                break;
                        }
                        break;
                    }
                    case AlipayCommand.SDK_CHECK_FLAG: {
                        Toast.makeText(ChoosePayWayActivity.this, "检查结果为：" + msg.obj, Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
                return false;
            }
        });
        alipayCommand = new AlipayCommand(this, mHandler, getString(R.string.rsa_private));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_zhifubao_pay:
                choosePay = 1;
                break;
            case R.id.rl_weixin_pay:
                choosePay = 2;
                break;
            case R.id.rl_other_pay:
                choosePay = 3;
                break;
        }
        goToPay();
    }

    private void goToPay() {
        switch (choosePay) {
            case 1:
                UserInfo uInfo = RenheApplication.getInstance().getUserInfo();
                if (uInfo == null) {
                    ToastUtil.showToast(getApplicationContext(), "用户登录信息为空");
                    return;
                }
                AliPayTask aliPayTask = new AliPayTask(uInfo.getSid(), uInfo.getAdSId(), fee, goodName, desc, bizType);
                aliPayTask.execute();
                Map<String, String> alipayStatisticsMap = new HashMap<>();//阿里云统计自定义事件自定义参数
                alipayStatisticsMap.put("type", "2");
                StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_btn_menu4_vip_pay_channel_click), 0, "", alipayStatisticsMap);
                break;
            case 2:
                boolean isInstall = checkAppExist(getApplicationContext(), "com.tencent.mm");
                if (!isInstall) {
                    ToastUtil.showToast(getApplicationContext(), "请安装微信客户端");
                    return;
                }
                UserInfo userInfo = RenheApplication.getInstance().getUserInfo();
                if (userInfo == null) {
                    ToastUtil.showToast(getApplicationContext(), "用户登录信息为空");
                    return;
                }
                GetPrepayIdTask getPrepayId = new GetPrepayIdTask(userInfo.getSid(), userInfo.getAdSId(), fee, bizType, goodName);
                getPrepayId.execute();
                Map<String, String> weixinStatisticsMap = new HashMap<>();//阿里云统计自定义事件自定义参数
                weixinStatisticsMap.put("type", "1");
                StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_btn_menu4_vip_pay_channel_click), 0, "", weixinStatisticsMap);
                break;
            case 3:
                Intent i = new Intent();
                i.setClass(ChoosePayWayActivity.this, WebViewActWithTitle.class);
                i.putExtra("url", "http://m.renhe.cn/fukuan.htm");
                startHlActivity(i);
                break;
        }
    }

    public boolean checkAppExist(Context context, String packageName) {
        Intent intent = new Intent();
        intent.setPackage(packageName);
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, 0);
        if (list.size() > 0) {
            return true;
        }
        return false;
    }

    class AliPayTask extends AsyncTask<Void, Void, AliPayResult> {
        PayCommandImpl payCommand = new PayCommandImpl();
        String sid;
        String adSId;
        String fee;
        String subject;
        String body;
        int bizType;

        /**
         * @param sid
         * @param adSId
         * @param fee     金额
         * @param subject 名称
         * @param body    详情
         * @param bizType 请使用Constants里的常量
         * @see com.itcalf.renhe.Constants#BIZ_TYPE_RENHE_MONEY    人和币
         * @see com.itcalf.renhe.Constants#BIZ_TYPE_MEMBER_PAY     付费会员
         * @see com.itcalf.renhe.Constants#BIZ_TYPE_RENMAI_EXPRESS 人脉快递
         * @see com.itcalf.renhe.Constants#BIZ_TYPE_GENERALIZATION 精准推广
         */
        public AliPayTask(String sid, String adSId, String fee, String subject, String body, int bizType) {
            this.sid = sid;
            this.adSId = adSId;
            this.fee = fee;
            this.subject = subject;
            this.body = body;
            this.bizType = bizType;
        }

        @Override
        protected void onPreExecute() {
            showDialog(1);
        }

        @Override
        protected AliPayResult doInBackground(Void... params) {
            try {
                return payCommand.repayZhifubao(ChoosePayWayActivity.this, sid, adSId, bizSId, fee, subject, body, bizType);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(AliPayResult result) {
            removeDialog(1);

            if (result == null) {
                ToastUtil.showToast(getApplicationContext(), R.string.hl_no_newwork);
                return;
            }
            if (result.getState() != 1) {
                ToastUtil.showToast(getApplicationContext(), "支付宝生成订单失败,code=" + result.getState());
                return;
            }
            alipayCommand.pay(subject, body, fee, result.getOutTradeNo(), result.getNotifyUrl());
        }

    }

    class GetPrepayIdTask extends AsyncTask<Void, Void, WeixinPrepay> {

        private IPayCommand payCommand = new PayCommandImpl();
        private String sid;
        private String adSId;
        private String fee;
        private int bizType;
        private String tradeDesc;

        private StringBuffer sBuffer = new StringBuffer();

        /**
         * @param sid
         * @param adSId
         * @param fee
         * @param bizType   请使用Constants里的常量
         * @param tradeDesc 支付描述
         * @see com.itcalf.renhe.Constants#BIZ_TYPE_RENHE_MONEY    人和币
         * @see com.itcalf.renhe.Constants#BIZ_TYPE_MEMBER_PAY     付费会员
         * @see com.itcalf.renhe.Constants#BIZ_TYPE_RENMAI_EXPRESS 人脉快递
         * @see com.itcalf.renhe.Constants#BIZ_TYPE_GENERALIZATION 精准推广
         */
        public GetPrepayIdTask(String sid, String adSId, String fee, int bizType, String tradeDesc) {
            this.sid = sid;
            this.adSId = adSId;
            this.fee = fee;
            this.bizType = bizType;
            this.tradeDesc = tradeDesc;
        }

        @Override
        protected void onPreExecute() {
            showDialog(1);
        }

        @Override
        protected void onPostExecute(WeixinPrepay result) {
            removeDialog(1);

            if (result == null) {
                ToastUtil.showToast(getApplicationContext(), R.string.hl_no_newwork);
                return;
            }
            if (result.getState() != 1) {
                ToastUtil.showToast(getApplicationContext(), "微信生成订单失败,code=" + result.getState());
                return;
            }

            sBuffer.append("prepay_id\n" + result.getPrepayid() + "\n\n");
            weixinPayCommand.sendPayReq();

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected WeixinPrepay doInBackground(Void... params) {
            try {
                String ip = getIp();
                WeixinPrepay prepay = payCommand.repayWeixin(ChoosePayWayActivity.this, sid, adSId, bizSId, fee, ip, bizType,
                        tradeDesc);
                if (prepay != null) {
                    weixinPayCommand.genPayReq(prepay, sBuffer);
                }
                return prepay;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private String getIp() {
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            return intToIp(ipAddress);
        }

        private String intToIp(int i) {
            return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
        }
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 1:
                return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.loading).cancelable(true).build();
            default:
                return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        payCallback = null;
        if (Constants.DEBUG_MODE) {
            System.err.println("ChoosePayWayActivity destroy");
        }
    }

}
