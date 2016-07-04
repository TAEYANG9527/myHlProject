package com.itcalf.renhe.utils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.itcalf.renhe.R;

import java.util.HashMap;
import java.util.Map;

/**
 * 转动的进度条对话框的显示和销毁控制类
 * 
 * @author kevin
 */
@SuppressLint("NewApi")
public class RollProgressDialog {
	public static final String TAG = RollProgressDialog.class.getSimpleName();

	static Map<String, AlertDialog> mDialogMap = new HashMap<String, AlertDialog>();

	private static final int SHOW = 0;
	private static final int DISMISS = -1;

	public static AlertDialog showNetDialog(Context context, boolean cancelable, String message) {
		initHandler(context);
		AlertDialog.Builder builder = new Builder(context);
		if (Build.VERSION.SDK_INT > 11) {
			builder = new Builder(context, AlertDialog.THEME_TRADITIONAL);
		}
		View view = LayoutInflater.from(context).inflate(R.layout.ncy_dialog_loading, null);
		TextView tv = (TextView) view.findViewById(R.id.tv_message);
		tv.setText(message);
		AlertDialog dialog = builder.create();
		dialog.setCancelable(cancelable);
		dialog.dismiss();
		dialog.show();
		Window wd = dialog.getWindow();
		wd.setContentView(view);
		wd.setWindowAnimations(0);
		mDialogMap.put(context.getClass().getName(), dialog);
		return dialog;
	}

	public static AlertDialog showNetDialog(Context context, String msg) {
		return showNetDialog(context, true, msg);
	}

	/**
	 * 显示转动的进度条对话框
	 * 
	 * @param context
	 *            需要显示Dialog的Activity对象
	 * @return
	 */
	public static AlertDialog showNetDialog(Context context, boolean cancelable) {
		return showNetDialog(context, cancelable, "正在加载...");
	}

	public static AlertDialog showNetDialog(Context context) {
		return showNetDialog(context, true);
	}

	/**
	 * 销毁正在转动的进度条对话框
	 * 
	 * @param context
	 *            需要销毁Dialog的Activity对象
	 */
	public static void dismissNetDialog(Context context) {
		initHandler(context);
		if (mDialogMap.containsKey(context.getClass().getName())) {
			Dialog dialog = mDialogMap.get(context.getClass().getName());
			if (dialog != null) {
				handler.obtainMessage(DISMISS, context.getClass().getName()).sendToTarget();
			}
		}
	}

	public static void initHandler(Context context) {
		if (handler != null) {
			return;
		}
		handler = new Handler(context.getMainLooper()) {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case SHOW:
					Log.d(TAG, "显示对话框");
					break;
				case DISMISS:
					try {
						String context = (String) msg.obj;
						Dialog dialog = mDialogMap.get(context);
						dialog.dismiss();
						mDialogMap.remove(context);
					} catch (Exception e) {
					}
					break;
				default:
					break;
				}
			}
		};
	}

	static Handler handler;

	public static class NCYAlertDialog extends AlertDialog {
		protected NCYAlertDialog(Context context) {
			super(context);
		}
	}

}
