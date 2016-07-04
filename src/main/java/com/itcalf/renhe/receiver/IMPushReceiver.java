package com.itcalf.renhe.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.alibaba.wukong.AuthConstants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.common.KickOutActivity;
import com.itcalf.renhe.context.fragmentMain.TabMainFragmentActivity;
import com.itcalf.renhe.utils.LogoutUtil;
import com.orhanobut.logger.Logger;

/**
 * Description: <br>
 * Copyright (c) 人和网版权所有 2013<br>
 * Create DateTime: 2014-04-06 下午3:40:25<br>
 *
 * @author wangning
 */

public class IMPushReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        Logger.d("IMPushReceiver onReceive");
        if (null != RenheApplication.getInstance().getUserInfo()) {
            if (AuthConstants.Event.EVENT_AUTH_KICKOUT.equals(intent.getAction())) {
                Logger.d("IMPushReceiver onReceive>>>AuthConstants.Event.EVENT_AUTH_KICKOUT");
                if (RenheApplication.getInstance().getLogin() == 0) {
                    new LogoutUtil(context, null).closeLogin(true);
                }
                if (RenheApplication.getInstance().getLogin() != 0) {
                    Intent intent2 = new Intent(context, KickOutActivity.class);
                    intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent2.putExtra("tiker", context.getString(R.string.account_kikout));
                    context.startActivity(intent2);
                }

            } else if (AuthConstants.Event.EVENT_AUTH_LOGOUT.equals(intent.getAction())) {
                if (RenheApplication.getInstance().getLogin() == 0 && RenheApplication.getInstance().getLogOut() != 1) {
                    new LogoutUtil(context, null).closeLogin(true);
                }
                if (RenheApplication.getInstance().getLogin() != 0) {
                    Intent intent2 = new Intent(context, KickOutActivity.class);
                    intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent2.putExtra("tiker", context.getString(R.string.account_kikout));
                    context.startActivity(intent2);
                }

            } else if (TabMainFragmentActivity.KIK_OUT_ACTION.equals(intent.getAction())) {
                Intent intent2 = new Intent(context, KickOutActivity.class);
                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent2.putExtra("tiker", context.getString(R.string.account_exception));
                context.startActivity(intent2);
            }

        }
    }
}
