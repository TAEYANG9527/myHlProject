package com.itcalf.renhe.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.itcalf.renhe.R;

public class LoadingDialog extends Dialog {

	private String mLoadingText;

	public LoadingDialog(Context context, String loadingText) {
		super(context, R.style.dialog_fullscreen);
		this.mLoadingText = loadingText;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading);
		((TextView) findViewById(R.id.loading_txv)).setText(mLoadingText);
	}

}
