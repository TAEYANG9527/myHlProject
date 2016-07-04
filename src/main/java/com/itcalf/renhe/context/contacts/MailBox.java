package com.itcalf.renhe.context.contacts;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.UserInfo;
import com.itcalf.renhe.view.ClearableEditText;

/**
 * @author       chan
 * @createtime   2015-1-6
 * @功能说明       邮箱导入联系人--登入界面
 */
public class MailBox extends BaseActivity {

	private EditText mailbox_addrEt, mailbox_pwdEt;
	private Button import_Btn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		RenheApplication.getInstance().addActivity(this);
		getTemplate().doInActivity(this, R.layout.mailbox);
	}

	@Override
	protected void findView() {
		super.findView();
		mailbox_addrEt = (ClearableEditText) findViewById(R.id.mailbox_addrEt);
		mailbox_pwdEt = (ClearableEditText) findViewById(R.id.mailbox_pwdEt);
		import_Btn = (Button) findViewById(R.id.import_Btn);
	}

	@Override
	protected void initData() {
		super.initData();
		setTextValue(R.id.title_txt, "从邮箱导入");
	}

	@Override
	protected void initListener() {
		super.initListener();
		import_Btn.setEnabled(false);
		mailbox_addrEt.addTextChangedListener(new TextWatchListener(mailbox_addrEt));
		mailbox_pwdEt.addTextChangedListener(new TextWatchListener(mailbox_pwdEt));
		import_Btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MailBox.this, MobileMailList.class);
				startActivity(intent);
				overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			}
		});

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	class TextWatchListener implements TextWatcher {
		EditText et;

		public TextWatchListener(EditText et) {
			this.et = et;
		}

		@Override
		public void afterTextChanged(Editable s) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (et.getId() == R.id.mailbox_addrEt) {
				if (!TextUtils.isEmpty(mailbox_addrEt.getText().toString().trim())) {
					mailbox_addrEt.setCompoundDrawablesWithIntrinsicBounds(null, null,
							getResources().getDrawable(R.drawable.relationship_input_del), null);
				} else {
					mailbox_addrEt.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
				}
			}
			if (et.getId() == R.id.mailbox_pwdEt) {
				if (!TextUtils.isEmpty(mailbox_pwdEt.getText().toString().trim())) {
					mailbox_pwdEt.setCompoundDrawablesWithIntrinsicBounds(null, null,
							getResources().getDrawable(R.drawable.relationship_input_del), null);
					if (!TextUtils.isEmpty(mailbox_pwdEt.getText().toString().trim())) {
						import_Btn.setEnabled(true);
					}
				} else {
					mailbox_pwdEt.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
					import_Btn.setEnabled(false);
				}
			}
		}

	}

	class ImportTask extends AsyncTask<String, Void, UserInfo> {
		private String userAccount;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected UserInfo doInBackground(String... params) {
			UserInfo userInfo = new UserInfo();
			userInfo.setLoginAccountType(params[0]);
			userInfo.setPwd(params[1]);
			this.userAccount = params[0];
			return getRenheApplication().getUserCommand().login(userInfo);
		}

		@Override
		protected void onPostExecute(UserInfo result) {
			super.onPostExecute(result);
			// removeDialog(1);
			//			if (null != result) {
			//				if (1 == result.getState()) {

			//					Intent intent = new Intent(MailBox.this, MobileMailList.class);
			//					intent.putExtra("tel", mobile);
			//					intent.putExtra("pwd", pwd);
			//					startActivityForResult(intent, COMPLETE_REGISTER_CODE);
			//					overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			//				} else if (-1 == result.getState()) {
			//					ToastUtil.showErrorToast(MailBox.this, "用户名或密码错误!");
			//				} else {
			//					ToastUtil.showErrorToast(MailBox.this, "用户名或密码为空!");
			//				}
			//			}
		}
	}

}
