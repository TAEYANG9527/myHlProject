package com.itcalf.renhe.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.itcalf.renhe.service.RenheService;
import com.orhanobut.logger.Logger;

/**
 * Title: BootReceiver.java<br>
 * Description:接收开机广播，启动RHService，目的是开启人和网application，可以开机后能接收到推送通知 <br>
 * Copyright (c) 人和网版权所有 2015    <br>
 * Create DateTime: 2015-4-1 下午6:28:37 <br>
 * @author wangning
 */
public class BootReceiver extends BroadcastReceiver {
	private static final String action_boot = "android.intent.action.BOOT_COMPLETED";

	@Override
	public void onReceive(Context context, Intent intent) {
		Logger.d("BootReceiver onReceive--->"+intent.getAction());
		if (intent.getAction().equals(action_boot)) {
			context.startService(new Intent(context, RenheService.class));
		}
	}
}
