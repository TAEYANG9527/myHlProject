package com.itcalf.renhe.context.portal;

import android.content.Context;

import com.alibaba.sdk.android.AlibabaSDK;
import com.alibaba.sdk.android.push.CloudPushService;
import com.itcalf.renhe.BaseAsyncTask;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.utils.DeviceUitl;
import com.itcalf.renhe.utils.HttpUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Title: EditSummaryInfoProfessionTask.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-8-6 上午9:58:59 <br>
 *
 * @author wangning
 */
public class AddMemberDeviceTokenTask extends BaseAsyncTask<MessageBoardOperation> {
    private Context mContext;
    private String xinGeToken;

    public AddMemberDeviceTokenTask(Context mContext) {
        super(mContext);
        this.mContext = mContext;
        this.xinGeToken = Constants.PUSH_DFVICE_TOKEN;
    }

    @Override
    public void doPre() {

    }

    @Override
    public void doPost(MessageBoardOperation result) {

    }

    @Override
    protected MessageBoardOperation doInBackground(String... params) {
        CloudPushService cloudPushService = AlibabaSDK.getService(CloudPushService.class);
        if (cloudPushService != null) {
            xinGeToken = cloudPushService.getDeviceId();
        }
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("sid", RenheApplication.getInstance().getUserInfo().getSid());
        reqParams.put("adSId", RenheApplication.getInstance().getUserInfo().getAdSId());
        reqParams.put("token", xinGeToken);// 说明：用于推送的token，登录后用member_id加此字段保存推送用的信息
        reqParams.put("bundle", Constants.JPUSH_APP_BUNDLE);// 说明：设备bundle，用来区分是哪个应用，android客户端传递renhe_android就可以了
        reqParams.put("version", DeviceUitl.getAppVersion());// 说明：设备版本号：例如3.0.1
        reqParams.put("deviceType", 0);// 说明：设备版本号：例如3.0.1
        try {
            MessageBoardOperation mb = (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.ADD_MEMBER_DEVICE_TOKEN,
                    reqParams, MessageBoardOperation.class, null);
            return mb;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
