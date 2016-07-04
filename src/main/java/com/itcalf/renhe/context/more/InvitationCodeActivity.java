package com.itcalf.renhe.context.more;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.bean.InvitationCode;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.utils.FadeUitl;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.NetworkUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.Button;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * @description 和聊邀请码页面
 * @author Chan
 * @date 2015-7-1
 */
public class InvitationCodeActivity extends BaseActivity {

	private RelativeLayout rootRl;
	private LinearLayout invitationcode_ll;
	private TextView myInvitationCode;
	private Button shareBtn;

	private RelativeLayout invitationCodeInputRl;
	private TextView authCode;
	private EditText inputCode;

	private LinearLayout invitation_info_ll;
	private ImageView avatar_img;
	private ImageView vipImage, realnameImage;
	private TextView username_txt, userinfo_txt;
	private Button auth_btn;

	private ImageLoader imageLoader;

	private FadeUitl fadeUitl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getTemplate().doInActivity(this, R.layout.invitation_code);
	}

	@Override
	protected void findView() {
		super.findView();
		rootRl = (RelativeLayout) findViewById(R.id.rootrl);
		invitationcode_ll = (LinearLayout) findViewById(R.id.invitationcode_ll);
		myInvitationCode = (TextView) findViewById(R.id.myInvitationCode);
		shareBtn = (Button) findViewById(R.id.invitationCode_share_btn);
		invitationCodeInputRl = (RelativeLayout) findViewById(R.id.invitationCode_input_rl);
		authCode = (TextView) findViewById(R.id.invitationCode_auth_tv);
		inputCode = (EditText) findViewById(R.id.invitationCode_input_et);

		invitation_info_ll = (LinearLayout) findViewById(R.id.invitation_info_ll);
		avatar_img = (ImageView) findViewById(R.id.avatar_img);
		vipImage = (ImageView) findViewById(R.id.vipImage);
		realnameImage = (ImageView) findViewById(R.id.realnameImage);
		username_txt = (TextView) findViewById(R.id.username_txt);
		userinfo_txt = (TextView) findViewById(R.id.userinfo_txt);
		auth_btn = (Button) findViewById(R.id.auth_btn);
	}

	@Override
	protected void initData() {
		super.initData();
		setTextValue(1, "邀请码");
		imageLoader = ImageLoader.getInstance();

		invitationcode_ll.setVisibility(View.GONE);
		fadeUitl = new FadeUitl(this, getResources().getString(R.string.loading));
		fadeUitl.addFade(rootRl);

		new getMyInvitationCodeTask().executeOnExecutor(Executors.newCachedThreadPool(),
				RenheApplication.getInstance().getUserInfo().getSid(), RenheApplication.getInstance().getUserInfo().getAdSId());

	}

	@Override
	protected void initListener() {
		super.initListener();

		shareBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent sendIntent = new Intent();
				sendIntent.setAction(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_TEXT,
						getResources().getString(R.string.invitation_code_share_context) + myInvitationCode.getText().toString());
				sendIntent.setType("text/plain");
				startActivity(Intent.createChooser(sendIntent, "和聊邀请码分享："));
			}
		});

		inputCode.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				//判读输入的长度是否为6位，是的话直接去验证
				if (s.toString().length() == 6) {
					new checkInvitationCodeTask().executeOnExecutor(Executors.newCachedThreadPool(),
							RenheApplication.getInstance().getUserInfo().getSid(),
							RenheApplication.getInstance().getUserInfo().getAdSId(), inputCode.getText().toString().trim());
				}
			}
		});

		auth_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new useInvitationCodeTask().executeOnExecutor(Executors.newCachedThreadPool(),
						RenheApplication.getInstance().getUserInfo().getSid(),
						RenheApplication.getInstance().getUserInfo().getAdSId(), inputCode.getText().toString().trim());
			}
		});
	}

	/**
	 * 获取自己的邀请码
	 */
	class getMyInvitationCodeTask extends AsyncTask<String, Void, InvitationCode> {

		protected InvitationCode doInBackground(String... params) {
			Map<String, Object> reqParams = new HashMap<String, Object>();
			reqParams.put("sid", params[0]);
			reqParams.put("adSId", params[1]);
			try {
				InvitationCode al = (InvitationCode) HttpUtil.doHttpRequest(Constants.Http.INVITATIONCODE_MYSELF, reqParams,
						InvitationCode.class, null);
				return al;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (-1 != NetworkUtil.hasNetworkConnection(InvitationCodeActivity.this)) {
			} else {
				if (fadeUitl != null)
					fadeUitl.removeFade(rootRl);
				ToastUtil.showNetworkError(InvitationCodeActivity.this);
			}
		}

		@Override
		protected void onPostExecute(InvitationCode result) {
			super.onPostExecute(result);
			if (fadeUitl != null)
				fadeUitl.removeFade(rootRl);
			if (result != null) {
				if (result.getState() == 1) {
					invitationcode_ll.setVisibility(View.VISIBLE);
					myInvitationCode.setText("" + result.getInviteCode());
					if (result.getCanInput() == 1) {
						//使用过优惠码，不出现输入框
						if (result.getUsed() == 1) {
							invitationCodeInputRl.setVisibility(View.GONE);
							invitation_info_ll.setVisibility(View.VISIBLE);
							ShowInvitationInfo(result);
							auth_btn.setText("邀请人已验证");
							auth_btn.setTextColor(getResources().getColor(R.color.C2));
							auth_btn.setEnabled(false);
						} else {
							invitationCodeInputRl.setVisibility(View.VISIBLE);
							invitation_info_ll.setVisibility(View.GONE);
						}
					} else {
						invitationCodeInputRl.setVisibility(View.GONE);
						invitation_info_ll.setVisibility(View.GONE);
					}
				}
			} else {
				ToastUtil.showConnectError(InvitationCodeActivity.this);
			}
		}
	}

	/**
	 * 使用邀请码
	 */
	class useInvitationCodeTask extends AsyncTask<String, Void, InvitationCode> {

		@Override
		protected InvitationCode doInBackground(String... params) {
			Map<String, Object> reqParams = new HashMap<String, Object>();
			reqParams.put("sid", params[0]);
			reqParams.put("adSId", params[1]);
			reqParams.put("inviteCode", params[2]);
			try {
				InvitationCode al = (InvitationCode) HttpUtil.doHttpRequest(Constants.Http.INVITATIONCODE_USE, reqParams,
						InvitationCode.class, null);
				return al;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (fadeUitl != null)
				fadeUitl.addFade(rootRl);
		}

		@Override
		protected void onPostExecute(InvitationCode result) {
			super.onPostExecute(result);
			if (fadeUitl != null)
				fadeUitl.removeFade(rootRl);
			if (result != null) {
				switch (result.getState()) {
				case 1:
					invitationCodeInputRl.setVisibility(View.GONE);
					invitation_info_ll.setVisibility(View.VISIBLE);
					auth_btn.setText("邀请人已验证");
					auth_btn.setTextColor(getResources().getColor(R.color.C2));
					auth_btn.setEnabled(false);
					break;
				case -3:
				case -4:
				case -5:
				case -6:
				default:
					auth_btn.setEnabled(true);
					ToastUtil.showToast(InvitationCodeActivity.this, "验证失败");
					break;
				}
			} else {
				ToastUtil.showConnectError(InvitationCodeActivity.this);
			}
		}
	}

	/**
	 * 检查邀请码
	 */
	class checkInvitationCodeTask extends AsyncTask<String, Void, InvitationCode> {

		@Override
		protected InvitationCode doInBackground(String... params) {
			Map<String, Object> reqParams = new HashMap<String, Object>();
			reqParams.put("sid", params[0]);
			reqParams.put("adSId", params[1]);
			reqParams.put("inviteCode", params[2]);
			try {
				InvitationCode al = (InvitationCode) HttpUtil.doHttpRequest(Constants.Http.INVITATIONCODE_CHECK, reqParams,
						InvitationCode.class, null);
				return al;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (fadeUitl != null)
				fadeUitl.addFade(rootRl);
		}

		@Override
		protected void onPostExecute(InvitationCode result) {
			super.onPostExecute(result);
			if (fadeUitl != null)
				fadeUitl.removeFade(rootRl);
			if (result != null) {
				switch (result.getState()) {
				case 1:
					invitationCodeInputRl.setVisibility(View.VISIBLE);
					invitation_info_ll.setVisibility(View.VISIBLE);
					authCode.setVisibility(View.VISIBLE);
					ShowInvitationInfo(result);
					auth_btn.setEnabled(true);
					break;
				case -3:
					invitation_info_ll.setVisibility(View.GONE);
					authCode.setVisibility(View.GONE);
					ToastUtil.showToast(InvitationCodeActivity.this, "邀请码为空");
					break;
				case -4:
					invitation_info_ll.setVisibility(View.GONE);
					authCode.setVisibility(View.GONE);
					ToastUtil.showToast(InvitationCodeActivity.this, "邀请码不存在");
					break;
				case -6:
					invitation_info_ll.setVisibility(View.GONE);
					authCode.setVisibility(View.GONE);
					ToastUtil.showToast(InvitationCodeActivity.this, "不能使用自己的邀请码");
					break;
				default:
					break;
				}
			} else {
				ToastUtil.showConnectError(InvitationCodeActivity.this);
			}
		}
	}

	/**
	 *  显示邀请者信息
	 */
	private void ShowInvitationInfo(InvitationCode result) {
		if (result == null)
			return;

		// 头像显示
		if (!TextUtils.isEmpty(result.getUserFace()) && null != avatar_img) {
			try {
				imageLoader.displayImage(result.getUserFace(), avatar_img, CacheManager.options,
						CacheManager.animateFirstDisplayListener);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		int accountType = result.getAccountType();
		boolean isRealName = result.isRealname();
		// Vip标志显示
		switch (accountType) {
		case 0:
			vipImage.setVisibility(View.GONE);
			break;
		case 1:
			vipImage.setVisibility(View.VISIBLE);
			vipImage.setImageResource(R.drawable.archive_vip_1);
			break;
		case 2:
			vipImage.setVisibility(View.VISIBLE);
			vipImage.setImageResource(R.drawable.archive_vip_2);
			break;
		case 3:
			vipImage.setVisibility(View.VISIBLE);
			vipImage.setImageResource(R.drawable.archive_vip_3);
			break;

		default:
			vipImage.setVisibility(View.GONE);
			break;
		}
		// 实名标志
		if (isRealName && accountType <= 0) {
			realnameImage.setVisibility(View.VISIBLE);
			realnameImage.setImageResource(R.drawable.archive_realname);
		} else {
			realnameImage.setVisibility(View.GONE);
		}
		username_txt.setText(result.getName());
		String title = result.getCurTitle();
		String company = result.getCurCompany();
		if (TextUtils.isEmpty(title) || TextUtils.isEmpty(company)) {
			userinfo_txt.setText(title + company);//修改成职位加公司
		} else {
			userinfo_txt.setText(title + "/" + company);//修改成职位加公司
		}
	}
}
