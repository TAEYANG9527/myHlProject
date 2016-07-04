package com.itcalf.renhe.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.eventbusbean.RefreshChatUserInfoEvent;

import de.greenrobot.event.EventBus;

/**
 * Created by wangning on 2016/6/23.
 */
public class NetWorkStateReceiver extends BroadcastReceiver {
    public static final String ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION.equals(intent.getAction())) {
            //获取手机的连接服务管理器，这里是连接管理器类
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null) { // connected to the internet
                if (!RenheApplication.getInstance().getInChatOpenId().equals(Constants.CURRENT_IS_NOT_IN_CHAT)) {
                    EventBus.getDefault().post(new RefreshChatUserInfoEvent());
                }
//                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
//                    // connected to wifi
//                    Toast.makeText(context, activeNetwork.getTypeName(), Toast.LENGTH_SHORT).show();
//                } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
//                    // connected to the mobile provider's data plan
//                    Toast.makeText(context, activeNetwork.getTypeName(), Toast.LENGTH_SHORT).show();
//                }
            } else {
                // not connected to the internet
            }
        }
    }

}