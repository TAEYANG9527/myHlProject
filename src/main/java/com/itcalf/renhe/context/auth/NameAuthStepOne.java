package com.itcalf.renhe.context.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.utils.LoggerFileUtil;
import com.itcalf.renhe.utils.StatisticsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.Button;
import com.itcalf.renhe.view.EditText;

import java.util.regex.Pattern;

public class NameAuthStepOne extends NameAuthFragment {

	private EditText et_realname, et_personalid;
	private Button btn_nextstep;
	private LinearLayout ly_stepone, ly_authing, ly_authsuccess;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (fragmentView != null) {
			return fragmentView;
		}
		fragmentView = inflater.inflate(R.layout.fragment_nameauth_stepone, null);
		initView();
		setListener();
		return fragmentView;
	}

	private void initView() {
		ly_stepone = (LinearLayout) fragmentView.findViewById(R.id.ly_stepone);
		et_realname = (EditText) fragmentView.findViewById(R.id.et_realname);
		et_realname.setFocusable(false);
		et_realname.setEnabled(false);
		et_personalid = (EditText) fragmentView.findViewById(R.id.et_personalid);
		btn_nextstep = (Button) fragmentView.findViewById(R.id.btn_nextstep);

		ly_authing = (LinearLayout) fragmentView.findViewById(R.id.ly_authing);
		ly_authsuccess = (LinearLayout) fragmentView.findViewById(R.id.ly_authsuccess);

	}

	private void setListener() {
		btn_nextstep.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				nextStep();
				StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_btn_pop_realname_next_click), 0, "", null);
			}
		});
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		switchAuthStatus();
	}

	private void switchAuthStatus() {
		switch (mNameAuthAct.authStatus) {
		case -1:
			String name = RenheApplication.getInstance().getUserInfo().getName();
			if (!TextUtils.isEmpty(name))
				et_realname.setText(name);
			ly_stepone.setVisibility(View.VISIBLE);
			ly_authing.setVisibility(View.GONE);
			ly_authsuccess.setVisibility(View.GONE);
			break;
		case 0:
			ly_stepone.setVisibility(View.GONE);
			ly_authing.setVisibility(View.VISIBLE);
			ly_authsuccess.setVisibility(View.GONE);
			break;

		case 1:
			ly_stepone.setVisibility(View.GONE);
			ly_authing.setVisibility(View.GONE);
			ly_authsuccess.setVisibility(View.VISIBLE);
			break;
		}
	}

	private void nextStep() {
		String name = et_realname.getText().toString().trim();
		String personalid = et_personalid.getText().toString();
		if (TextUtils.isEmpty(name) || TextUtils.isEmpty(personalid)) {
			ToastUtil.showToast(mNameAuthAct, getString(R.string.nameauth_infoempty));
			return;
		}
		Pattern idNumPattern = Pattern.compile(Constants.IDENTITY_CARD_PATTERN);
		if (!idNumPattern.matcher(personalid).matches()) {
			ToastUtil.showToast(mNameAuthAct, getString(R.string.identity_card_invalid));
			return;
		}
		mNameAuthAct.name = name;
		mNameAuthAct.personalId = personalid;
		mNameAuthAct.authFee = -100;
		mNameAuthAct.changeStep(2);
	}

}
