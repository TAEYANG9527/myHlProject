package com.itcalf.renhe.context.more;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.bean.AccountLimitInfo;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.utils.FadeUitl;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.LoggerFileUtil;
import com.itcalf.renhe.utils.NetworkUtil;
import com.itcalf.renhe.utils.ToastUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * @description 账户限额
 * @author Chan
 * @date 2015-6-17
 */
public class AccountLimitActivity extends BaseActivity implements OnClickListener {

	/**好友邀请上限**/
	private RelativeLayout sendInvitedLimit_Rl;
	private TextView invitedLimite_Tv;
	private TextView sendinvite_usenumb;
	private TextView sendInvitedLimit_upgrade_txt;
	/**人脉上限**/
	private RelativeLayout contactsLimit_Rl;
	private TextView contactsLimit_Tv;
	private TextView contacts_usenumb;
	private TextView contactsLimit_upgrade_txt;
	/**人脉搜索列表上限**/
	private RelativeLayout searchLimit_Rl;
	private TextView searchLimit_Tv;
	private TextView searchLimit_upgrade_txt;
	/**高级搜索**/
	private RelativeLayout seniorSearchLimit_Rl;
	private ImageView seniorSearch_img;
	private ImageView seniorSearch_Iv;
	private TextView seniorSearch_Tv;
	private TextView seniorsearch_state;
	private TextView seniorSearch_upgrade_txt;
	/**附近人脉行业过滤**/
	private RelativeLayout nearbyIndustryLimit_Rl;
	private TextView nearbyIndustry_Tv;
	private ImageView nearbyIndustry_img;
	private ImageView nearbyIndustry_Iv;
	private TextView nearbyindustry_state;
	private TextView nearbyIndustry_upgrade_txt;

	private RelativeLayout rootSv;
	private LinearLayout accountlimit_ll;
	private FadeUitl fadeUitl;

	//注册广播，接收数据，刷新
	private RefreshAccountLimitReceiver refreshAccountLimitReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		RenheApplication.getInstance().addActivity(this);
		new ActivityTemplate().doInActivity(this, R.layout.accountlimit);
	}

	@Override
	protected void findView() {
		super.findView();
		rootSv = (RelativeLayout) findViewById(R.id.accountlimit_Rl);
		accountlimit_ll = (LinearLayout) findViewById(R.id.accountlimit_ll);

		sendInvitedLimit_Rl = (RelativeLayout) findViewById(R.id.sendInvitedLimit_Rl);
		invitedLimite_Tv = (TextView) findViewById(R.id.invitedLimite_Tv);
		sendinvite_usenumb = (TextView) findViewById(R.id.sendinvite_usenumb);
		sendInvitedLimit_upgrade_txt = (TextView) findViewById(R.id.sendInvitedLimit_upgrade_txt);

		contactsLimit_Rl = (RelativeLayout) findViewById(R.id.contactsLimit_Rl);
		contactsLimit_Tv = (TextView) findViewById(R.id.contactsLimit_Tv);
		contacts_usenumb = (TextView) findViewById(R.id.contacts_usenumb);
		contactsLimit_upgrade_txt = (TextView) findViewById(R.id.contactsLimit_upgrade_txt);

		searchLimit_Rl = (RelativeLayout) findViewById(R.id.searchLimit_Rl);
		searchLimit_Tv = (TextView) findViewById(R.id.searchLimit_Tv);
		searchLimit_upgrade_txt = (TextView) findViewById(R.id.searchLimit_upgrade_txt);

		seniorSearchLimit_Rl = (RelativeLayout) findViewById(R.id.seniorSearchLimit_Rl);
		seniorsearch_state = (TextView) findViewById(R.id.seniorsearch_state);
		seniorSearch_img = (ImageView) findViewById(R.id.seniorSearch_img);
		seniorSearch_Iv = (ImageView) findViewById(R.id.seniorSearch_Iv);
		seniorSearch_Tv = (TextView) findViewById(R.id.seniorSearch_Tv);
		seniorSearch_upgrade_txt = (TextView) findViewById(R.id.seniorSearch_upgrade_txt);

		nearbyIndustryLimit_Rl = (RelativeLayout) findViewById(R.id.nearbyIndustryLimit_Rl);
		nearbyIndustry_Tv = (TextView) findViewById(R.id.nearbyIndustry_Tv);
		nearbyIndustry_img = (ImageView) findViewById(R.id.nearbyIndustry_img);
		nearbyIndustry_Iv = (ImageView) findViewById(R.id.nearbyIndustry_Iv);
		nearbyindustry_state = (TextView) findViewById(R.id.nearbyindustry_state);
		nearbyIndustry_upgrade_txt = (TextView) findViewById(R.id.nearbyIndustry_upgrade_txt);

	}

	@Override
	protected void initData() {
		super.initData();
		setTextValue(1, "权限");
		accountlimit_ll.setVisibility(View.GONE);
		fadeUitl = new FadeUitl(this, getResources().getString(R.string.loading));
		fadeUitl.addFade(rootSv);

		refreshAccountLimitReceiver = new RefreshAccountLimitReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constants.BroadCastAction.UPDATE_ACCOUNTLIMIT_ACTION);
		registerReceiver(refreshAccountLimitReceiver, intentFilter);

		new getAccountLimitInfoTask().executeOnExecutor(Executors.newCachedThreadPool(),
				RenheApplication.getInstance().getUserInfo().getSid(), RenheApplication.getInstance().getUserInfo().getAdSId());
	}

	@Override
	protected void initListener() {
		super.initListener();

		sendInvitedLimit_Rl.setOnClickListener(this);
		contactsLimit_Rl.setOnClickListener(this);
		searchLimit_Rl.setOnClickListener(this);
		seniorSearchLimit_Rl.setOnClickListener(this);
		nearbyIndustryLimit_Rl.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		int limitType = 0;
		Intent i = new Intent(AccountLimitActivity.this, AccountLimitUpgradeActivity.class);
		switch (v.getId()) {
		case R.id.sendInvitedLimit_Rl:
			i.putExtra("update", Constants.ACCOUNTLIMITUPGRADE[0]);
			limitType = 1;
			break;
		case R.id.contactsLimit_Rl:
			i.putExtra("update", Constants.ACCOUNTLIMITUPGRADE[1]);
			limitType = 2;
			break;
		case R.id.searchLimit_Rl:
			i.putExtra("update", Constants.ACCOUNTLIMITUPGRADE[2]);
			limitType = 3;
			break;
		case R.id.seniorSearchLimit_Rl:
			i.putExtra("update", Constants.ACCOUNTLIMITUPGRADE[3]);
			limitType = 4;
			break;
		case R.id.nearbyIndustryLimit_Rl:
			i.putExtra("update", Constants.ACCOUNTLIMITUPGRADE[4]);
			limitType = 5;
			break;
		default:
			break;
		}
		startActivity(i);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
		//和聊统计
		String content = "5.158.1" + LoggerFileUtil.getConstantInfo(AccountLimitActivity.this) + "|" + limitType;
		LoggerFileUtil.writeFile(content, true);
	}

	/**
	 * 获取账户限额信息
	 */
	class getAccountLimitInfoTask extends AsyncTask<String, Void, AccountLimitInfo> {

		protected AccountLimitInfo doInBackground(String... params) {
			Map<String, Object> reqParams = new HashMap<String, Object>();
			reqParams.put("sid", params[0]);
			reqParams.put("adSId", params[1]);
			try {
				AccountLimitInfo al = (AccountLimitInfo) HttpUtil.doHttpRequest(Constants.Http.ACCOUNTLIMIT_INFO, reqParams,
						AccountLimitInfo.class, null);
				return al;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (-1 != NetworkUtil.hasNetworkConnection(AccountLimitActivity.this)) {
			} else {
				if (fadeUitl != null)
					fadeUitl.removeFade(rootSv);
				ToastUtil.showNetworkError(AccountLimitActivity.this);
			}
		}

		@Override
		protected void onPostExecute(AccountLimitInfo result) {
			super.onPostExecute(result);
			accountlimit_ll.setVisibility(View.VISIBLE);
			if (fadeUitl != null)
				fadeUitl.removeFade(rootSv);
			if (result != null) {
				if (result.getState() == 1) {
					//每日可发送好友邀请数
					invitedLimite_Tv.setText("" + result.getAddFriendPerdayLimit());
					sendinvite_usenumb.setText(sendinvite_usenumb.getText().toString() + result.getAddFriendCountToday());
					if (result.isIncreaseAddFriendLimit()) {
						sendInvitedLimit_upgrade_txt.setText("提升");
						sendInvitedLimit_upgrade_txt.setCompoundDrawablesWithIntrinsicBounds(null, null,
								getResources().getDrawable(R.drawable.search_flag), null);
						sendInvitedLimit_upgrade_txt.setCompoundDrawablePadding(12);
						sendInvitedLimit_Rl.setEnabled(true);
					} else {
						sendInvitedLimit_upgrade_txt.setText("已提升");
						sendInvitedLimit_upgrade_txt.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
						sendInvitedLimit_Rl.setEnabled(false);
					}
					//人脉上限
					contactsLimit_Tv.setText(result.getFriendAmountLimit() < Constants.FRIENDAMOUNTLIMIT
							? ("" + result.getFriendAmountLimit()) : "无限");
					contacts_usenumb.setText(contacts_usenumb.getText().toString() + result.getFriendAmountUsed());
					if (result.isIncreaseFriendAmountLimit()) {
						contactsLimit_upgrade_txt.setText("提升");
						contactsLimit_upgrade_txt.setCompoundDrawablesWithIntrinsicBounds(null, null,
								getResources().getDrawable(R.drawable.search_flag), null);
						contactsLimit_upgrade_txt.setCompoundDrawablePadding(12);
						contactsLimit_Rl.setEnabled(true);
					} else {
						contactsLimit_upgrade_txt.setText("已提升");
						contactsLimit_upgrade_txt.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
						contactsLimit_Rl.setEnabled(false);
					}
					//人脉搜索列表上限
					searchLimit_Tv.setText("" + result.getRenmaiSearchListLimit());
					if (result.isIncreaseRenmaiSearchListLimit()) {
						searchLimit_upgrade_txt.setText("提升");
						searchLimit_upgrade_txt.setCompoundDrawablesWithIntrinsicBounds(null, null,
								getResources().getDrawable(R.drawable.search_flag), null);
						searchLimit_upgrade_txt.setCompoundDrawablePadding(12);
						searchLimit_Rl.setEnabled(true);
					} else {
						searchLimit_upgrade_txt.setText("已提升");
						searchLimit_upgrade_txt.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
						searchLimit_Rl.setEnabled(false);
					}
					//高级搜索
					if (result.isAdvacedSearchPrivilege()) {
						seniorSearch_Tv.setVisibility(View.GONE);
						seniorSearch_Iv.setVisibility(View.VISIBLE);
						seniorSearch_img.setBackgroundResource(R.drawable.btn_bg_color8ec73f_circular_empty_style);
						seniorsearch_state.setText("当前可用");
						seniorSearch_upgrade_txt.setText("已开启");
						seniorSearch_upgrade_txt.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
						seniorSearchLimit_Rl.setEnabled(false);
					} else {
						seniorSearch_Tv.setText("NO");
						seniorSearch_Tv.setVisibility(View.VISIBLE);
						seniorSearch_Iv.setVisibility(View.GONE);
						seniorSearch_img.setBackgroundResource(R.drawable.btn_bg_color_bc3_circular_solid_style);
						seniorsearch_state.setText("当前不可用");
						seniorSearch_upgrade_txt.setText("开启");
						seniorSearch_upgrade_txt.setCompoundDrawablesWithIntrinsicBounds(null, null,
								getResources().getDrawable(R.drawable.search_flag), null);
						seniorSearch_upgrade_txt.setCompoundDrawablePadding(12);
						seniorSearchLimit_Rl.setEnabled(true);
					}
					//附近人脉行业过滤
					if (result.isMemberNearbyFilter()) {
						nearbyIndustry_Tv.setVisibility(View.GONE);
						nearbyIndustry_Iv.setVisibility(View.VISIBLE);
						nearbyIndustry_img.setBackgroundResource(R.drawable.btn_bg_color8ec73f_circular_empty_style);
						nearbyindustry_state.setText("当前可用");
						nearbyIndustry_upgrade_txt.setText("已开启");
						nearbyIndustry_upgrade_txt.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
						nearbyIndustryLimit_Rl.setEnabled(false);
					} else {
						nearbyIndustry_Tv.setVisibility(View.VISIBLE);
						nearbyIndustry_Iv.setVisibility(View.GONE);
						nearbyIndustry_img.setBackgroundResource(R.drawable.btn_bg_color_bc3_circular_solid_style);
						nearbyindustry_state.setText("当前不可用");
						nearbyIndustry_upgrade_txt.setText("开启");
						nearbyIndustry_upgrade_txt.setCompoundDrawablesWithIntrinsicBounds(null, null,
								getResources().getDrawable(R.drawable.search_flag), null);
						nearbyIndustry_upgrade_txt.setCompoundDrawablePadding(12);
						nearbyIndustryLimit_Rl.setEnabled(true);
					}
				}
			} else {
				ToastUtil.showConnectError(AccountLimitActivity.this);
			}
		}
	}

	/**
	 * @description 广播接收，刷新界面处理
	 * @date 2015-6-30
	 */
	class RefreshAccountLimitReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			if (arg1.getAction() != null && arg1.getAction().equals(Constants.BroadCastAction.UPDATE_ACCOUNTLIMIT_ACTION))
				new getAccountLimitInfoTask().executeOnExecutor(Executors.newCachedThreadPool(),
						RenheApplication.getInstance().getUserInfo().getSid(),
						RenheApplication.getInstance().getUserInfo().getAdSId());
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (refreshAccountLimitReceiver != null)
			unregisterReceiver(refreshAccountLimitReceiver);
	}
}
