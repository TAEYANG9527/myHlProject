package com.itcalf.renhe.context.fragmentMain;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.itcalf.renhe.utils.MaterialDialogsUtil;

/**
 * Title: MyDialogFragment.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-8-27 下午12:33:13 <br>
 * @author wangning
 */
public class MyDialogFragment extends DialogFragment {
	private static String CURRENT_TIME = "CURRENT_TIME";

	public static MyDialogFragment newInstance(String currentTime) {
		// 创建一个新的带有指定参数的Fragment实例  
		MyDialogFragment fragment = new MyDialogFragment();
		Bundle args = new Bundle();
		args.putString(CURRENT_TIME, currentTime);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new MaterialDialogsUtil(getActivity()).showIndeterminateProgressDialog(getArguments().getString(CURRENT_TIME)).cancelable(false).build();
	}
}
