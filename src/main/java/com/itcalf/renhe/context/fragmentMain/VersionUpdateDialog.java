package com.itcalf.renhe.context.fragmentMain;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;

import com.itcalf.renhe.R;
import com.itcalf.renhe.dto.Version;
import com.itcalf.renhe.view.TextView;

/**
 * 版本更新的Dialog
 * @author YZQ
 *
 */
public class VersionUpdateDialog extends Dialog implements OnClickListener, OnDismissListener {

	public interface UpdateCallback {
		public void cancel();

		public void confirm();
	}

	private Version version;
	private TextView tvUpdateDesc, tvCancel, tvConfirm;
	private UpdateCallback updateCallback;
	private Context context;

	public VersionUpdateDialog(Context context, int theme, Version version, UpdateCallback updateCallback) {
		super(context, theme);
		this.version = version;
		this.updateCallback = updateCallback;
		this.context = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.version_update_layout);

		initView();
		initListener();

		setCancelable(version.getForceUpdate() != 1);
		setCanceledOnTouchOutside(version.getForceUpdate() != 1);

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

		tvUpdateDesc.setText(version.getUpdatelog());
		//		tvUpdateDesc.setText("1，添加支付功能，添加支付功能，添加支付功能添加支付功能添加支付功能添加支付功能添加支付功能；\n2，美化UI；\n3，修改若干bug\n1，添加支付功能，添加支付功能，添加支付功能添加支付功能添加支付功能添加支付功能添加支付功能；\n2，美化UI；\n3，修改若干bug");
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
			dismiss();
			if (updateCallback != null) {
				updateCallback.cancel();
			}
			break;
		case R.id.confirm:
			if (updateCallback != null) {
				updateCallback.confirm();
			}
			dismiss();
			break;
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		// 解除版本更新服务绑定
		//context.unbindService(mServiceConnection);
		//mServiceConnection = null;
	}

}