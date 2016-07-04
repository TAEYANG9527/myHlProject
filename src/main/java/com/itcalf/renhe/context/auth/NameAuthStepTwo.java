package com.itcalf.renhe.context.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.bean.NameAuthFeeRes;
import com.itcalf.renhe.bean.NameAuthNowRes;
import com.itcalf.renhe.bean.NameAuthRes;
import com.itcalf.renhe.bean.NameAuthStatusRes;
import com.itcalf.renhe.context.auth.NameAuthFeeTask.NameAuthFeeTaskListener;
import com.itcalf.renhe.context.auth.NameAuthNowTask.NameAuthNowTaskListener;
import com.itcalf.renhe.context.auth.NameAuthStatusTask.NameAuthStatusTaskListener;
import com.itcalf.renhe.context.auth.NameAuthTask.NameAuthTaskListener;
import com.itcalf.renhe.context.pay.ChoosePayWayActivity;
import com.itcalf.renhe.context.pay.ChoosePayWayActivity.PayCallback;
import com.itcalf.renhe.utils.RequestDialog;
import com.itcalf.renhe.utils.StatisticsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.Button;
import com.itcalf.renhe.view.TextView;

import java.text.MessageFormat;
import java.util.concurrent.Executors;

public class NameAuthStepTwo extends NameAuthFragment {

    private Button btn_photo, btn_powerauth;
    private TextView tv_payword;
    private RelativeLayout rootLayout;
    private RequestDialog requestDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (fragmentView != null) {
            return fragmentView;
        }
        fragmentView = inflater.inflate(R.layout.fragment_nameauth_steptwo, null);
        initView();
        setListener();
        return fragmentView;
    }

    private void initView() {
        btn_photo = (Button) fragmentView.findViewById(R.id.btn_photo);
        btn_powerauth = (Button) fragmentView.findViewById(R.id.btn_powerauth);
        tv_payword = (TextView) fragmentView.findViewById(R.id.tv_payword);
        rootLayout = (RelativeLayout) fragmentView.findViewById(R.id.ly_root);
        requestDialog = new RequestDialog(mNameAuthAct, "提交中");
    }

    private void setListener() {
        //立即拍照
        btn_photo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(mNameAuthAct, CameraActivity.class), 100);
                StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_btn_pop_realname_photo_click), 0, "", null);
            }
        });

        //权威认证
        btn_powerauth.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                submitAuth();
                StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_btn_pop_realname_pay_click), 0, "", null);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);

        if (mNameAuthAct.authFee == -100) {//判断是否从第一步跳转过来
            initAuthFee();
        }

        if (mNameAuthAct.authFee > 0) {
            tv_payword.setText(MessageFormat.format(getString(R.string.nameauth_steptwo_payword), mNameAuthAct.authFee))
            ;
        } else {
            tv_payword.setText(R.string.nameauth_steptwo_freeauth);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100) {
            if (resultCode == Activity.RESULT_OK) {
                mNameAuthAct.photoFile = data.getStringExtra("filepath");
                mNameAuthAct.changeStep(3);
            }
        }
    }

    private void initAuthFee() {
        mNameAuthAct.authFee = mNameAuthAct.defaultFee;
        requestDialog.addFade(rootLayout, "加载中");
        String sid = RenheApplication.getInstance().getUserInfo().getSid();
        String adsid = RenheApplication.getInstance().getUserInfo().getAdSId();
        new NameAuthFeeTask(new NameAuthFeeTaskListener() {

            @Override
            public void postExecute(NameAuthFeeRes result) {
                if (null != getActivity() && isAdded()) {
                    if (result != null && result.state == 1) {
                        mNameAuthAct.authFee = result.isFree == 1 ? 0 : result.fee;
                        if (mNameAuthAct.authFee > 0) {
                            tv_payword.setText( MessageFormat.format(getString(R.string.nameauth_steptwo_payword), mNameAuthAct.authFee));
                        } else {
                            tv_payword.setText(R.string.nameauth_steptwo_freeauth);
                        }
                    }
                    requestDialog.removeFade(rootLayout);
                }
            }
        }).executeOnExecutor(Executors.newCachedThreadPool(), sid, adsid);
    }

    //权威认证
    public void submitAuth() {
        mNameAuthAct.iscanBack = false;
        requestDialog.addFade(rootLayout, "提交中");
        String sid = RenheApplication.getInstance().getUserInfo().getSid();
        String adsid = RenheApplication.getInstance().getUserInfo().getAdSId();

        if (mNameAuthAct.authFee > 0) {
            //收费认证
            new NameAuthTask(getActivity(), new NameAuthTaskListener() {

                @Override
                public void postExecute(NameAuthRes result) {
                    if (null != getActivity() && isAdded()) {
                        if (result != null) {
                            if (result.state == 1) {
                                //生成支付订单
                                String payid = result.bizSId;
                                gotoPay(payid);
                            } else {
                                mNameAuthAct.dealAuthError(result.state);
                            }
                        } else {
                            ToastUtil.showToast(mNameAuthAct, "认证失败，请检查网络稍后再试");
                        }
                        requestDialog.removeFade(rootLayout);
                        mNameAuthAct.iscanBack = true;
                    }
                }
            }).executeOnExecutor(Executors.newCachedThreadPool(), sid, adsid, "2", mNameAuthAct.name, mNameAuthAct.personalId,
                    null);
        } else {
            //免费认证
            new NameAuthNowTask(new NameAuthNowTaskListener() {

                @Override
                public void postExecute(NameAuthNowRes result) {
                    if (null != getActivity() && isAdded()) {
                        if (result != null) {
                            if (result.state == 1) {
                                if (result.status == 1) {
                                    mNameAuthAct.dealAuthResult(1);
                                } else {
                                    ToastUtil.showToast(mNameAuthAct, "认证失败");
                                    mNameAuthAct.changeStep(1);
                                }
                            } else {
                                mNameAuthAct.dealAuthError(result.state);
                            }
                        } else {
                            ToastUtil.showToast(mNameAuthAct, "认证失败，请检查网络稍后再试");
                        }
                        requestDialog.removeFade(rootLayout);
                        mNameAuthAct.iscanBack = true;
                    }
                }
            }).executeOnExecutor(Executors.newCachedThreadPool(), sid, adsid, mNameAuthAct.name, mNameAuthAct.personalId);
        }
    }

    //跳转至支付页面
    private void gotoPay(String payid) {
        ChoosePayWayActivity.launch(mNameAuthAct, Constants.BIZ_TYPE_REALNAMEAUTH, payid, "实名认证", "实名认证",
                mNameAuthAct.authFee, new PayCallback() {
                    @Override
                    public void onPayResult(int type, int flag) {
                        if (flag == 1) {//付款成功后立即获取认证结果
                            String sid = RenheApplication.getInstance().getUserInfo().getSid();
                            String adsid = RenheApplication.getInstance().getUserInfo().getAdSId();
                            new NameAuthStatusTask(new NameAuthStatusTaskListener() {
                                @Override
                                public void postExecute(NameAuthStatusRes result) {
                                    if (null != getActivity() && isAdded()) {
                                        if (result != null && result.state == 1) {
                                            if (mNameAuthAct != null && !mNameAuthAct.isFinishing()) {
                                                mNameAuthAct.authStatus = result.realNameStatus;
                                                mNameAuthAct.changeStep(1);
                                                ToastUtil.showToast(mNameAuthAct, "认证失败");
                                            }
                                        }
                                    }
                                }
                            }).executeOnExecutor(Executors.newCachedThreadPool(), sid, adsid);
                            RenheApplication.getInstance().finshPayActivity();
                        }
                    }
                });
    }
}
