package com.itcalf.renhe.utils;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;

/**
 * 消息工具类
 *
 * @author xp
 */
public class ToastUtil {
    public static void showToast(Context context, String message) {
        showToast(context, message, false);
    }

    public static void showToast(Context context, int resId) {
        showToast(context, resId, false);
    }

    public static void showErrorToast(Context context, String message) {
        showToast(context, message, true);
    }

    public static void showErrorToast(Context context, int message) {
        showToast(context, message, true);
    }

    public static void showToast(Context context, int resId, boolean errorToast) {
        showToast(context, context.getResources().getString(resId), errorToast);
    }

    public static void showToast(Context context, String message, boolean errorToast) {
        Toast toast;

        if (errorToast) {
            /*
             * We want the toast text to be red. The exact layout of the default
			 * toast is not specified, so the following may not work in future
			 * android versions. Thus, if the layout is not the way we expect,
			 * we just leave the text color as is.
			 */
            toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
            //            View view = toast.getView();
            //            if (view instanceof LinearLayout) {
            //                LinearLayout linearLayout = (LinearLayout) view;
            //                if (linearLayout.getChildCount() > 0) {
            //                    View child = linearLayout.getChildAt(0);
            //                    if (child instanceof TextView) {
            //                        ((TextView) child).setTextColor(
            //                                Color.rgb(255, 100, 100));
            //                    }
            //                }
            //            }
        } else {
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        }
        View view = toast.getView();
        if (view instanceof LinearLayout) {
            LinearLayout linearLayout = (LinearLayout) view;
            if (linearLayout.getChildCount() > 0) {
                View child = linearLayout.getChildAt(0);
                if (child instanceof TextView) {
                    ((TextView) child).setTypeface(Constants.APP_TYPEFACE);
                }
            }
        }

        toast.show();
    }

    public static void showNetworkError(Context context) {
        if (null == context)
            return;
        showToast(context, context.getResources().getString(R.string.network_error_message), false);
    }

    public static void showNetworkWIFI(Context context) {
        if (null == context)
            return;
        showToast(context, context.getResources().getString(R.string.network_wifi_message), false);
    }

    public static void showNetworkMobile(Context context) {
        if (null == context)
            return;
        showToast(context, context.getResources().getString(R.string.network_wap_message), false);
    }

    public static void showConnectError(Context context) {
        if (null == context)
            return;
        showToast(context, context.getResources().getString(R.string.connect_server_error), false);
    }

}