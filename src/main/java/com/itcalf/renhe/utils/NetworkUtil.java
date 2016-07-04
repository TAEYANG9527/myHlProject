package com.itcalf.renhe.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtil {

    public static int hasNetworkConnection(Context context) {
        // boolean HaveConnectedWifi = false;
        // boolean HaveConnectedMobile = false;
        if (null != context && null != context.getSystemService(Context.CONNECTIVITY_SERVICE)) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo[] netInfo = cm.getAllNetworkInfo();
            for (NetworkInfo ni : netInfo) {
                if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                    if (ni.isConnected())
                        return 1;
                if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                    if (ni.isConnected())
                        return 0;
            }
            cm = null;
            netInfo = null;
        }
        return -1;
    }

    /**
     * @return 网络类型，1:wifi,2:2g,3:3g,4:4g
     * 0,表示未知；-1表示未连接
     */
    public static int getNetworkType(Context context) {
        return 0;
//		if (null != context.getSystemService(Context.CONNECTIVITY_SERVICE)) {
//			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//			NetworkInfo netInfo = cm.getActiveNetworkInfo();
//			if (null != netInfo && netInfo.isConnected()) {
//				if (netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
//					return 1;
//				} else {
//					switch (netInfo.getType()) {
//					case TelephonyManager.NETWORK_TYPE_GPRS:
//					case TelephonyManager.NETWORK_TYPE_EDGE:
//					case TelephonyManager.NETWORK_TYPE_CDMA:
//					case TelephonyManager.NETWORK_TYPE_1xRTT:
//					case TelephonyManager.NETWORK_TYPE_IDEN:
//						return 2;
//					case TelephonyManager.NETWORK_TYPE_UMTS:
//					case TelephonyManager.NETWORK_TYPE_EVDO_0:
//					case TelephonyManager.NETWORK_TYPE_EVDO_A:
//					case TelephonyManager.NETWORK_TYPE_HSDPA:
//					case TelephonyManager.NETWORK_TYPE_HSUPA:
//					case TelephonyManager.NETWORK_TYPE_HSPA:
//					case TelephonyManager.NETWORK_TYPE_EVDO_B:
//					case TelephonyManager.NETWORK_TYPE_EHRPD:
//					case TelephonyManager.NETWORK_TYPE_HSPAP:
//						return 3;
//					case TelephonyManager.NETWORK_TYPE_LTE:
//						return 4;
//					default:
//						return 0;
//					}
//				}
//			} else {
//				return -1;
//			}
//		} else {
//			return -1;
//		}
    }
}
