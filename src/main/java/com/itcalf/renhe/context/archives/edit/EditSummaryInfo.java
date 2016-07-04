package com.itcalf.renhe.context.archives.edit;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.archives.edit.task.EditSummaryInfoProfessionTask;
import com.itcalf.renhe.context.archives.edit.task.EditSummaryInfoSpecialtiesTask;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.dto.Profile;
import com.itcalf.renhe.dto.Profile.UserInfo.SpecialtiesInfo;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.ClearableEditText;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Title: EditSelfInfo.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014 <br>
 * Create DateTime: 2014-7-14 下午4:38:14 <br>
 * 
 * @author wangning
 */
public class EditSummaryInfo extends EditBaseActivity {
	private EditText selfInfoEt;
	private LinearLayout professionGroup;
	private ImageButton addFessionIB;
	private RelativeLayout addProfessionRL;
	private ClearableEditText addProfessionEdt;
	private TextView professionNumb;
	private Button addProfessionBtn;
	private ScrollView personalScl;

	private final static int PROFESSION_COUNT = 12;
	private int professionCount = 0;
	private LinearLayout specialtiesLl;
	private RelativeLayout professionalLl;
	private boolean isModify = false;
	private String professions;
	private Profile pf;
	private boolean isSpecialtiesNull = true;
	private SpecialtiesInfo[] specialtiesInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new ActivityTemplate().doInActivity(this, R.layout.archieve_eidt_summaryinfo);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("编辑概要信息"); //统计页面
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("编辑概要信息"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息 
		MobclickAgent.onPause(this);
	}

	@Override
	protected void findView() {
		super.findView();

		selfInfoEt = (EditText) findViewById(R.id.contentEdt);
		professionGroup = (LinearLayout) findViewById(R.id.profession_group);
		addFessionIB = (ImageButton) findViewById(R.id.add_profession_IB);
		specialtiesLl = (LinearLayout) findViewById(R.id.self_ll);
		professionalLl = (RelativeLayout) findViewById(R.id.profession_rl);

		addProfessionRL = (RelativeLayout) findViewById(R.id.add_profession_RL);
		addProfessionEdt = (ClearableEditText) findViewById(R.id.profession_edt);
		addProfessionEdt.setFilters(new InputFilter[] { new InputFilter.LengthFilter(Constants.ARCHIVESINFOLIMITED) });
		professionNumb = (TextView) findViewById(R.id.profession_numb);
		addProfessionBtn = (Button) findViewById(R.id.add_profession_btn);
		personalScl = (ScrollView) findViewById(R.id.personal_sl);

		professionGroup.removeAllViews();

	}

	@Override
	protected void initData() {
		super.initData();
		if (getIntent().getSerializableExtra("Profile") != null) {
			pf = (Profile) getIntent().getSerializableExtra("Profile");
		} else {
			pf = (Profile) CacheManager.getInstance().populateData(this).getObject(getRenheApplication().getUserInfo().getEmail(),
					CacheManager.PROFILE);
		}
		if (getIntent().getBooleanExtra("toSpecialties", false)) {
			setTextValue(R.id.title_txt, "个人简介");
			specialtiesLl.setVisibility(View.VISIBLE);
			professionalLl.setVisibility(View.GONE);
			addProfessionRL.setVisibility(View.GONE);
			if (null != getIntent().getStringExtra("professionals")) {
				professions = getIntent().getStringExtra("professionals");
			}
			if (!TextUtils.isEmpty(professions)) {
				selfInfoEt.setText(professions);
				selfInfoEt.setSelection(selfInfoEt.getText().toString().trim().length());
			}
		} else {
			setTextValue(R.id.title_txt, "技能专长");
			personalScl.setVisibility(View.VISIBLE);
			specialtiesInfo = pf.getUserInfo().getSpecialtiesInfo();
			specialtiesLl.setVisibility(View.GONE);
			professionalLl.setVisibility(View.VISIBLE);
			addProfessionRL.setVisibility(View.VISIBLE);
			for (int i = 0; i < specialtiesInfo.length; i++) {
				if (!TextUtils.isEmpty(specialtiesInfo[i].getTitle())) {
					professionCount++;
					View canProvideInfoView = LayoutInflater.from(EditSummaryInfo.this)
							.inflate(R.layout.archieve_edit_selfinfo_canprovide_info, null);
					EditText provideInfo = (EditText) canProvideInfoView.findViewById(R.id.canprovide_item_ET);
					provideInfo.setText(specialtiesInfo[i].getTitle());
					provideInfo.setEnabled(false);
					// provideInfo.setSelection(specialtiesInfo[i].getTitle()
					// .length());
					// provideInfo.addTextChangedListener(new
					// EditTextListener());
					professionGroup.addView(canProvideInfoView);
					ImageButton removeProvideIB = (ImageButton) canProvideInfoView.findViewById(R.id.remove_provide_ib);
					removeProvideIB.setOnClickListener(new RemoveItemListener(professionGroup, canProvideInfoView));

				}
			}
			if (professionCount >= PROFESSION_COUNT) {
				addFessionIB.setVisibility(View.GONE);
				addProfessionRL.setVisibility(View.GONE);
			} else {
				addFessionIB.setVisibility(View.GONE);
				addProfessionRL.setVisibility(View.VISIBLE);
			}

		}
	}

	@Override
	protected void initListener() {
		super.initListener();

		addProfessionBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String pro = addProfessionEdt.getText().toString().trim();
				if ("".equals(pro)) {
					ToastUtil.showToast(EditSummaryInfo.this, "请输入专长信息");
					return;
				}
				// 添加列表项
				if (professionCount < PROFESSION_COUNT) {
					View canProvideInfoView = LayoutInflater.from(EditSummaryInfo.this)
							.inflate(R.layout.archieve_edit_selfinfo_canprovide_info, null);
					professionGroup.addView(canProvideInfoView);
					ImageButton removeProvideIB = (ImageButton) canProvideInfoView.findViewById(R.id.remove_provide_ib);
					removeProvideIB.setOnClickListener(new RemoveItemListener(professionGroup, canProvideInfoView));
					EditText provideInfo = (EditText) canProvideInfoView.findViewById(R.id.canprovide_item_ET);
					provideInfo.setText(pro);
					provideInfo.setEnabled(false);
					// provideInfo.addTextChangedListener(new
					// EditTextListener());
					// provideInfo.requestFocus();
					isModify = true;
					professionCount++;
					addProfessionEdt.setText("");
				}
				if (professionCount == PROFESSION_COUNT) {
					addFessionIB.setVisibility(View.GONE);
					addProfessionRL.setVisibility(View.GONE);
				}
				new Handler().post(new Runnable() {
					@Override
					public void run() {
						personalScl.fullScroll(ScrollView.FOCUS_DOWN);
					}
				});
			}
		});

		addProfessionEdt.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				String input = (String) arg0.toString();
				if (!TextUtils.isEmpty(input)) {
					professionNumb.setText("" + (Constants.ARCHIVESINFOLIMITED - input.length()));
				} else {
					professionNumb.setText("" + Constants.ARCHIVESINFOLIMITED);
				}
			}
		});

		addFessionIB.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (professionCount < PROFESSION_COUNT) {
					View canProvideInfoView = LayoutInflater.from(EditSummaryInfo.this)
							.inflate(R.layout.archieve_edit_selfinfo_canprovide_info, null);
					professionGroup.addView(canProvideInfoView);
					ImageButton removeProvideIB = (ImageButton) canProvideInfoView.findViewById(R.id.remove_provide_ib);
					removeProvideIB.setOnClickListener(new RemoveItemListener(professionGroup, canProvideInfoView));
					EditText provideInfo = (EditText) canProvideInfoView.findViewById(R.id.canprovide_item_ET);
					provideInfo.addTextChangedListener(new EditTextListener());
					// provideInfo.requestFocus();
					professionCount++;
				}
				if (professionCount == PROFESSION_COUNT) {
					addFessionIB.setVisibility(View.GONE);
				}
			}
		});

		if (getIntent().getBooleanExtra("toSpecialties", false)) {
			selfInfoEt.addTextChangedListener(new EditTextListener());
		}
	}

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
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		// imm.hideSoftInputFromWindow(arg0.getWindowToken(), 0);
		// 个人简介
		if (getIntent().getBooleanExtra("toSpecialties", false)) {

			new EditSummaryInfoProfessionTask(EditSummaryInfo.this) {
				@Override
				public void doPre() {
					super.doPre();
					showDialog(1);

				}
				@Override
				public void doPost(MessageBoardOperation result) {
					super.doPost(result);
					if (result == null) {
						removeDialog(1);
						ToastUtil.showToast(EditSummaryInfo.this, R.string.network_anomaly);
					} else if (result.getState() == 1) {
                        new ProfileTask().executeOnExecutor(Executors.newCachedThreadPool(),
                                getRenheApplication().getUserInfo().getSid(), getRenheApplication().getUserInfo().getSid(),
                                getRenheApplication().getUserInfo().getAdSId());
					} else if (result.getState() == -3) {
						removeDialog(1);
						Toast.makeText(EditSummaryInfo.this, "个人简介不能为空", Toast.LENGTH_SHORT).show();
					} else if (result.getState() == -4) {
						removeDialog(1);
						Toast.makeText(EditSummaryInfo.this, "个人简介长度过长，长度不能超过500个字", Toast.LENGTH_SHORT).show();
					} else {
						removeDialog(1);
						ToastUtil.showToast(EditSummaryInfo.this, R.string.sorry_of_unknow_exception);
					}
				}
			}.executeOnExecutor(Executors.newCachedThreadPool(), getRenheApplication().getUserInfo().getSid(),
					getRenheApplication().getUserInfo().getAdSId(), selfInfoEt.getText().toString().trim());
			//			}
		} else {
			// 个人专长
			String[] specialties = null;
			List<String> tempList = new ArrayList<String>();
			if (null != professionGroup) {
				int childCount = professionGroup.getChildCount();
				for (int i = 0; i < childCount; i++) {
					String s = ((EditText) professionGroup.getChildAt(i).findViewById(R.id.canprovide_item_ET)).getText()
							.toString().trim();
					if (!TextUtils.isEmpty(s)) {
						isSpecialtiesNull = false;
						tempList.add(s);
					}
				}
			}
			if (isSpecialtiesNull) {
				Toast.makeText(EditSummaryInfo.this, "个人专长不能为空", Toast.LENGTH_SHORT).show();
				return;
			} else {
				specialties = new String[tempList.size()];
				for (int i = 0; i < tempList.size(); i++) {
					specialties[i] = tempList.get(i);
				}
				// specialties = (String[]) tempList.toArray();
			}
			if (null == specialties || specialties.length <= 0) {
				Toast.makeText(EditSummaryInfo.this, "个人专长不能为空", Toast.LENGTH_SHORT).show();
				return;
			}
			final SpecialtiesInfo[] finalspecialtiesInfo = new SpecialtiesInfo[specialties.length];
			for (int i = 0; i < specialties.length; i++) {
				SpecialtiesInfo mInfo = new SpecialtiesInfo();
				mInfo.setTitle(specialties[i]);
				finalspecialtiesInfo[i] = mInfo;
			}

			new EditSummaryInfoSpecialtiesTask(EditSummaryInfo.this, specialties) {
				@Override
				public void doPre() {
					super.doPre();
					showDialog(1);

				}

				@Override
				public void doPost(MessageBoardOperation result) {
					super.doPost(result);
					if (result == null) {
						removeDialog(1);
						ToastUtil.showToast(EditSummaryInfo.this, R.string.network_anomaly);
					} else if (result.getState() == 1) {
                        new ProfileTask().executeOnExecutor(Executors.newCachedThreadPool(),
                                getRenheApplication().getUserInfo().getSid(), getRenheApplication().getUserInfo().getSid(),
                                getRenheApplication().getUserInfo().getAdSId());
					} else if (result.getState() == -3) {
						removeDialog(1);
						Toast.makeText(EditSummaryInfo.this, "个人专长不能为空", Toast.LENGTH_SHORT).show();
					} else if (result.getState() == -4) {
						removeDialog(1);
						Toast.makeText(EditSummaryInfo.this, "个人专长单项长度过长，长度不能超过15个字", Toast.LENGTH_SHORT).show();
					} else if (result.getState() == -5) {
						removeDialog(1);
						Toast.makeText(EditSummaryInfo.this, "个人专长数量过多，不能超过12个", Toast.LENGTH_SHORT).show();
					} else {
						removeDialog(1);
						ToastUtil.showToast(EditSummaryInfo.this, R.string.sorry_of_unknow_exception);
					}
				}

			}.executeOnExecutor(Executors.newCachedThreadPool(), getRenheApplication().getUserInfo().getSid(),
					getRenheApplication().getUserInfo().getAdSId());
		}
	}

	class RemoveItemListener implements OnClickListener {
		LinearLayout groupView;
		View itemView;

		public RemoveItemListener(LinearLayout groupView, View itemView) {
			this.groupView = groupView;
			this.itemView = itemView;
		}

		@Override
		public void onClick(View arg0) {
			isModify = true;
			if (null != groupView) {
				groupView.removeView(itemView);
			}
			professionCount--;
			if (professionCount < PROFESSION_COUNT) {
				addFessionIB.setVisibility(View.GONE);
				addProfessionRL.setVisibility(View.VISIBLE);
			}
		}
	}

	class EditTextListener implements TextWatcher {

		@Override
		public void afterTextChanged(Editable arg0) {

		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

		}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			isModify = true;
		}

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 1:
			return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.saving).cancelable(false).build();
		default:
			return null;
		}
	}

	class ProfileTask extends AsyncTask<String, Void, Profile> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Profile doInBackground(String... params) {
			try {
				return getRenheApplication().getProfileCommand().showProfile(params[0], params[1], params[2],
						EditSummaryInfo.this);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Profile result) {
			super.onPostExecute(result);
			removeDialog(1);
			if (null != result) {
				if (1 == result.getState() && null != result.getUserInfo()) {
					Intent brocastIntent = new Intent(MyHomeArchivesActivity.REFRESH_ARCHIEVE_RECEIVER_ACTION);
					brocastIntent.putExtra("Profile", result);
					sendBroadcast(brocastIntent);
					Intent intent = new Intent();
					intent.putExtra("Profile", result);
					setResult(RESULT_OK, intent);
					finish();
					overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
				}
			} else {
				ToastUtil.showNetworkError(EditSummaryInfo.this);
			}
		}
	}

}
