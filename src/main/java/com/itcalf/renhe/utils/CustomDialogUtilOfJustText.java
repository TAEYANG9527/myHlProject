package com.itcalf.renhe.utils;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.R;

public class CustomDialogUtilOfJustText {
	private Context context;
	private String content = "";
	private CustomDialogClickListener myDialogClickListener;
	public final static int SURE_BUTTON = 1;

	/**
	 * 自定义dialog 按钮监听
	 * 
	 * @author Renhe
	 *
	 */
	public interface CustomDialogClickListener {
		public void onclick(int id);
	}

	public CustomDialogUtilOfJustText(Context context, CustomDialogClickListener myDialogClickListener) {
		this.context = context;
		this.myDialogClickListener = myDialogClickListener;
	}

	/**
	 * 创建对话框
	 */
	private AlertDialog mAlertDialog;
	private TextView contentTv;

	/**
	 * 
	 * @param context
	 * @param content
	 */
	public void createDialog(Context context, String content) {
		this.context = context;
		this.content = content;
		RelativeLayout view = (RelativeLayout) LayoutInflater.from(context)
				.inflate(com.itcalf.renhe.R.layout.custom_register_dialog_just_text, null);

		Builder mDialog = new AlertDialog.Builder(context);
		mAlertDialog = mDialog.create();
		mAlertDialog.setView(view, 0, 0, 0, 0);

		contentTv = (TextView) view.findViewById(R.id.content_tv);

		contentTv.setText(content);
		contentTv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				myDialogClickListener.onclick(SURE_BUTTON);
				mAlertDialog.dismiss();
			}
		});
		mAlertDialog.setCanceledOnTouchOutside(true);
		mAlertDialog.show();
	}

	public void DismissDialog() {
		if (mAlertDialog != null)
			mAlertDialog.dismiss();
	}

	public boolean isShowing() {
		if (mAlertDialog == null) {
			return false;
		} else {
			return mAlertDialog.isShowing();
		}

	}

}
