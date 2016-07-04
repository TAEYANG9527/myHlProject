package com.itcalf.renhe.context.archives.edit;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.R;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * Title: EditEduInfoSelectSchool.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-7-23 下午3:42:31 <br>
 * @author wangning
 */
public class EditSelfInfoEditName extends EditBaseActivity {

	private EditText schoolEt;
	private boolean isModify = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setMyContentView(R.layout.edit_selfinfo_edit_name);
		setTextValue(1, "更改名字");
		schoolEt = (EditText) findViewById(R.id.schoolEt);

		String schoolString = getIntent().getStringExtra("name");
		if (!TextUtils.isEmpty(schoolString)) {
			schoolEt.setText(schoolString);
			schoolEt.setSelection(schoolString.length());
		}
		schoolEt.addTextChangedListener(tbxEdit_TextChanged);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("编辑个人信息——编辑姓名"); //统计页面
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("编辑个人信息——编辑姓名"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息 
		MobclickAgent.onPause(this);
	}

	private TextWatcher tbxEdit_TextChanged = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			isModify = true;
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
		}

	};

	@Override
	public void goBack() {
		super.goBack();
		if (isModify) {
			MaterialDialogsUtil materialDialog = new MaterialDialogsUtil(this);
			materialDialog
					.getBuilder(R.string.material_dialog_title, R.string.is_save, R.string.material_dialog_sure,
                            R.string.material_dialog_cancel, R.string.material_dialog_give_up)
					.callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onNeutral(MaterialDialog dialog) {
                            finish();
                            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                        }

                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            goSave();
                        }
                    });
			materialDialog.show();
		} else {
			finish();
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
		}
	}

	@Override
	public void goSave() {
		super.goSave();
		String name = schoolEt.getText().toString().trim();
		if (!TextUtils.isEmpty(name)) {
			Intent intent = new Intent();
			intent.putExtra("name", name);
			setResult(RESULT_OK, intent);
			finish();
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
		} else {
			Toast.makeText(EditSelfInfoEditName.this, "姓名不能为空", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}

}
