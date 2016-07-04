package com.itcalf.renhe.context.wukong.im;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.wukong.Callback;
import com.alibaba.wukong.im.ConversationService;
import com.alibaba.wukong.im.IMEngine;
import com.alibaba.wukong.im.Message;
import com.alibaba.wukong.im.MessageBuilder;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.bean.CircleJoinRequestListInfo;
import com.itcalf.renhe.bean.CircleJoinUserInfo;
import com.itcalf.renhe.bean.MemberInfo;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.utils.PinyinUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;
import java.util.concurrent.Executors;

public class ActivityCircleJoinReusetDetail extends BaseActivity implements View.OnClickListener {
	private ImageView iv_avatar;
	private TextView tx_name, tx_company, tx_city, tx_title, tx_state;
	private Button bt_refuse, bt_consent;
	private MemberInfo memberInfo = new MemberInfo();
	//	private DisplayImageOptions options = new DisplayImageOptions.Builder().showStubImage(R.drawable.avatar)
	//			.showImageForEmptyUri(R.drawable.avatar).showImageOnFail(R.drawable.avatar).cacheInMemory(true).cacheOnDisc(true)
	//			.displayer(new RoundedBitmapDisplayer(10)).build();
	private int position;
	private float x_scale = 1f, y_scale = 1f;
	private String userConversationName = ""; // 圈主名
	private long imConversationId;
	private CircleJoinRequestListInfo data = new CircleJoinRequestListInfo();
	private StringBuffer approverCircleStr = new StringBuffer(",-11:该用户已是这个圈子的成员了,-10:已达圈子的人数上限,-9:圈子已满员,-8:您没有权限操作此申请记录,"
			+ "-7:审批记录已被处理,-6:审批记录不能为空,-5:审批结果必须为 1审批通过 或2代表审批不通过,-4：审批结果不能为空,-3:申请加入圈子记录id不能为空,"
			+ "-2:很抱歉，发生未知错误！,-1:很抱歉，您的权限不足！,2:请求成功，而且圈子为需要验证才能加入，已发出加入申请,1:请求成功,");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		RenheApplication.getInstance().addActivity(this);
		getTemplate().doInActivity(this, R.layout.activity_circle_join_request_detail);
		//		setTitle("好友申请");
		setTextValue(1, "好友申请");

		x_scale = 1f * getResources().getDisplayMetrics().widthPixels / 750;
		y_scale = 1f * getResources().getDisplayMetrics().heightPixels / 1335;
	}

	@Override
	protected void findView() {
		super.findView();
		LinearLayout.LayoutParams iv_avatarParams = new LinearLayout.LayoutParams((int) (130 * x_scale), (int) (130 * x_scale));
		iv_avatarParams.setMargins((int) (20 * x_scale), (int) (20 * x_scale), (int) (20 * x_scale), (int) (20 * x_scale));
		iv_avatar = (ImageView) findViewById(R.id.iv_avatar);
		iv_avatar.setLayoutParams(iv_avatarParams);

		//		LinearLayout.LayoutParams tx_stateParams = new LinearLayout.LayoutParams((int) (130 * x_scale), LayoutParams.MATCH_PARENT);
		//		tx_stateParams.gravity = Gravity.CENTER;
		tx_state = (TextView) findViewById(R.id.tx_state);
		//		tx_state.setLayoutParams(tx_stateParams);

		LinearLayout.LayoutParams tx_nameParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		tx_nameParams.setMargins(0, (int) (23 * x_scale), 0, (int) (15 * x_scale));
		tx_name = (TextView) findViewById(R.id.tx_name);
		tx_name.setLayoutParams(tx_nameParams);

		LinearLayout.LayoutParams tx_companyParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		tx_companyParams.setMargins(0, 0, 0, (int) (5 * x_scale));
		tx_company = (TextView) findViewById(R.id.tx_company);
		tx_company.setLayoutParams(tx_companyParams);

		tx_title = (TextView) findViewById(R.id.tx_title);
		tx_city = (TextView) findViewById(R.id.tx_city);

		bt_refuse = (Button) findViewById(R.id.bt_refuse);
		bt_consent = (Button) findViewById(R.id.bt_consent);
		iv_avatar.setOnClickListener(this);
		bt_refuse.setOnClickListener(this);
		bt_consent.setOnClickListener(this);

	}

	@Override
	protected void initData() {
		super.initData();
		position = getIntent().getIntExtra("position", -1);
		data = (CircleJoinRequestListInfo) getIntent().getSerializableExtra("list");
		userConversationName = getIntent().getStringExtra("userConversationName");
		imConversationId = Long.parseLong(getIntent().getStringExtra("imConversationId"));

		tx_name.setText(data.getMemberInfo().getName());

		String title = data.getMemberInfo().getTitle();
		title = TextUtils.isEmpty(title) ? "" : title;
		String company = data.getMemberInfo().getCompany();
		company = TextUtils.isEmpty(company) ? "" : company;
		if (TextUtils.isEmpty(title) || TextUtils.isEmpty(company)) {
			tx_company.setText(title + "" + company);
		} else {
			tx_company.setText(title + "、" + company);
		}
		String location = data.getMemberInfo().getLocation();
		location = TextUtils.isEmpty(location) ? "" : location;
		String industry = data.getMemberInfo().getIndustry();
		industry = TextUtils.isEmpty(industry) ? "" : industry;
		if (TextUtils.isEmpty(location) || TextUtils.isEmpty(industry)) {
			tx_city.setText(location + "" + industry);
		} else {
			tx_city.setText(location + "、" + industry);
		}
		tx_title.setText(data.isInvitationMemberExists()
				? (data.getInvitationMemberInfo().getName() + "邀请" + data.getMemberInfo().getName() + "加入圈子")
				: data.getPurpose());
		bt_refuse.setVisibility(data.getApproveState() != 0 ? View.GONE : View.VISIBLE);
		bt_consent.setVisibility(data.getApproveState() != 0 ? View.GONE : View.VISIBLE);
		tx_state.setVisibility(data.getApproveState() != 0 ? View.VISIBLE : View.GONE);
		ImageLoader.getInstance().displayImage(data.getMemberInfo().getUserfaceUrl(), iv_avatar);

	}

	/**
	 * 提交审核： state=1同意 2为拒绝
	 * */
	public void approveCircleJoinRequest(final int state, final int requestId) {
		RenheIMUtil.showProgressDialog(ActivityCircleJoinReusetDetail.this,R.string.loading);
		new AsyncTask<String, Void, MessageBoardOperation>() {
			@Override
			protected MessageBoardOperation doInBackground(String... params) {
				try {
					return ((RenheApplication) getApplicationContext()).getMessageBoardCommand().approveCircleJoinRequest(
							params[0], params[1], requestId, state, ActivityCircleJoinReusetDetail.this);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(MessageBoardOperation result) {
				super.onPostExecute(result);
				if (result != null) {
					if (result.getState() == 1) {
						if (state == 1) {
							addMembers();
						} else {
							RenheIMUtil.dismissProgressDialog();
							Intent intent2 = new Intent("im.ActivityCircleJoinRequest");
							intent2.putExtra("ApproveState", 2);
							intent2.putExtra("position", position);
							sendBroadcast(intent2);

							bt_refuse.setVisibility(View.GONE);
							bt_consent.setVisibility(View.GONE);
							tx_state.setVisibility(View.VISIBLE);
						}
					} else {
						RenheIMUtil.dismissProgressDialog();
						ToastUtil.showToast(ActivityCircleJoinReusetDetail.this, getResult(approverCircleStr, result.getState()));
					}
				} else {
					RenheIMUtil.dismissProgressDialog();
					ToastUtil.showToast(ActivityCircleJoinReusetDetail.this, "网络异常");
				}
			}
		}.executeOnExecutor(Executors.newCachedThreadPool(),
				((RenheApplication) getApplicationContext()).getUserInfo().getAdSId(),
				((RenheApplication) getApplicationContext()).getUserInfo().getSid());
	}

	/**
	 * IM添加成员
	 * */
	private void addMembers() {
		final CircleJoinUserInfo member = data.getMemberInfo();

		Message message = IMEngine.getIMService(MessageBuilder.class)
				.buildTextMessage(userConversationName + "邀请 " + member.getName() + " 加入了圈子");
		IMEngine.getIMService(ConversationService.class).addMembers(new Callback<List<Long>>() {
			@Override
			public void onSuccess(List<Long> arg0) {
				ToastUtil.showToast(ActivityCircleJoinReusetDetail.this, "添加成功");
				RenheIMUtil.dismissProgressDialog();

				memberInfo.setAvatar(member.getUserfaceUrl());
				memberInfo.setNickName(member.getName());
				memberInfo.setOpenId(member.getImId());
				memberInfo.setPinyin(PinyinUtil.cn2Spell(member.getName()));

				Intent intent2 = new Intent("im.ActivityCircleJoinRequest");
				intent2.putExtra("ApproveState", 1);
				intent2.putExtra("position", position);
				intent2.putExtra("MemberInfo", memberInfo);
				sendBroadcast(intent2);

				bt_refuse.setVisibility(View.GONE);
				bt_consent.setVisibility(View.GONE);
				tx_state.setVisibility(View.VISIBLE);
			}

			@Override
			public void onProgress(List<Long> arg0, int arg1) {
			}

			@Override
			public void onException(String arg0, String arg1) {
				RenheIMUtil.dismissProgressDialog();
				Toast.makeText(ActivityCircleJoinReusetDetail.this, "加入圈子失败.code:" + arg0 + " reason:" + arg0, Toast.LENGTH_SHORT)
						.show();
			}
		}, imConversationId + "", message, (long) member.getImId());
	}

	private String getResult(StringBuffer strs, Integer code) {
		int index = strs.indexOf(String.valueOf("," + code + ":"));
		if (index < 0) {
			return "state:" + code;
		}
		return strs.substring(index + 2 + String.valueOf(code).length(),
				strs.indexOf(",", index + 2 + String.valueOf(code).length()));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_avatar:
			Intent intent = new Intent(ActivityCircleJoinReusetDetail.this, MyHomeArchivesActivity.class);
			intent.putExtra("name", data.getMemberInfo().getName());
			intent.putExtra("flag", true);
			intent.putExtra("openId", Long.parseLong(String.valueOf(data.getMemberInfo().getImId())));
			startActivity(intent);
			overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			break;
		case R.id.bt_refuse:
			approveCircleJoinRequest(2, data.getId());
			break;
		case R.id.bt_consent:
			approveCircleJoinRequest(1, data.getId());
			break;
		default:
			break;
		}
	}

}
