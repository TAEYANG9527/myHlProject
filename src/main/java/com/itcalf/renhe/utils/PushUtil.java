package com.itcalf.renhe.utils;

import com.alibaba.sdk.android.AlibabaSDK;
import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.itcalf.renhe.dto.UserInfo;
import com.orhanobut.logger.Logger;

/**
 * Created by wangning on 2015/8/24.
 */
public class PushUtil {
    //将本设备和指定账号做绑定。account 账号名称，callback 结果回调
    public static void registerPush(final UserInfo userInfo) {
        final CloudPushService cloudPushService = AlibabaSDK.getService(CloudPushService.class);
        if (cloudPushService != null) {
            cloudPushService.bindAccount(String.valueOf(userInfo.getId()), new CommonCallback() {

                @Override
                public void onSuccess() {
                    Logger.w("阿里云推送 bindAccount success" + userInfo.getId());
                }

                @Override
                public void onFailed(String errorCode, String errorMessage) {
                    Logger.e("阿里云推送 bindAccount fail" + "err:" + errorCode + " - message:" + errorMessage);
                }
            });
        }
    }

    //阿里云推送d只绑定设备device信息,根据设备推送
    public static void registerDevicePush() {
//        final CloudPushService cloudPushService = AlibabaSDK.getService(CloudPushService.class);
//        if (cloudPushService != null) {
//            cloudPushService.bindAccount("", new CommonCallback() {
//
//                @Override
//                public void onSuccess() {
//                    Logger.w("阿里云推送（只绑定设备） bindAccount success");
//                }
//
//                @Override
//                public void onFailed(String errorCode, String errorMessage) {
//                    Logger.e("阿里云推送（只绑定设备） bindAccount fail" + "err:" + errorCode + " - message:" + errorMessage);
//                }
//            });
//        }
    }

    //解绑本设备
    public static void deletePush() {
        final CloudPushService cloudPushService = AlibabaSDK.getService(CloudPushService.class);
        if (cloudPushService != null) {
            cloudPushService.unbindAccount();
        }
    }
}
