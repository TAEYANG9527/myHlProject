package com.itcalf.renhe.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;

import com.itcalf.renhe.R;

/**
 * @description 限额升级Dialog
 * @author Chan
 * @date 2015-6-24
 */
public class AccountLimitUpgradeDialog extends Dialog implements OnClickListener, OnDismissListener {

	private String subjectStr = "", cancleStr = "", confirmStr = "";
	private TextView tvUpdateDesc, tvCancel, tvConfirm;
	private UpgradeCallback upgradeCallback;
	private Context context;

	public AccountLimitUpgradeDialog(Context context, int theme, String _subject, String _cancle, String _confirm,
			UpgradeCallback _upgradeCallback) {
		super(context, theme);
		this.subjectStr = _subject;
		this.cancleStr = _cancle;
		this.confirmStr = _confirm;
		this.upgradeCallback = _upgradeCallback;
		this.context = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.accountlimit_upgrade_dialog);

		initView();
		initListener();

		setCancelable(true);
		setCanceledOnTouchOutside(true);

		Window dialogWindow = getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
		lp.width = (int) (dm.widthPixels * 0.8);
		dialogWindow.setAttributes(lp);
	}

	private void initView() {
		tvUpdateDesc = (TextView) findViewById(R.id.tv_update_desc);
		tvCancel = (TextView) findViewById(R.id.cancel);
		tvConfirm = (TextView) findViewById(R.id.confirm);
		tvCancel.setText(TextUtils.isEmpty(cancleStr) ? tvCancel.getText().toString() : cancleStr);
		tvConfirm.setText(TextUtils.isEmpty(confirmStr) ? tvConfirm.getText().toString() : confirmStr);
		tvUpdateDesc.setText(subjectStr);
	}

	private void initListener() {
		tvCancel.setOnClickListener(this);
		tvConfirm.setOnClickListener(this);
		setOnDismissListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.cancel:
			if (upgradeCallback != null) {
				upgradeCallback.cancel();
			}
			break;
		case R.id.confirm:
			if (upgradeCallback != null) {
				upgradeCallback.confirm();
			}
			break;
		}
		dismiss();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
	}

	public interface UpgradeCallback {
		public void cancel();

		public void confirm();
	}
}
